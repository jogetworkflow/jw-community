package org.joget.commons.util;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility methods used by the system to manage cloud profile
 * 
 */
@Service
public final class HostManager implements ApplicationContextAware {

    public static final String SYSTEM_PROPERTY_VIRTUALHOST = "wflow.virtualhost";
    public static final String REQUEST_HOST_INITIALIZED = "REQUEST_HOST_INITIALIZED";

    protected static final ThreadLocal currentHost = new HostThreadLocal();
    protected static final ThreadLocal currentProfile = new HostInheritableThreadLocal();
    /**
     * Per-thread stack of profiles to restore, one entry per still-open {@code
     * setCurrentProfile}/{@code resetProfile()} pair. A single-slot field cannot handle nested
     * calls: if an inner pair opens while an outer one is still active (e.g. a whitelisted check
     * triggered by tenant code inside an outer worker task's tracked scope), it would overwrite
     * the outer scope's pending restore value, so the outer {@code resetProfile()} would restore
     * the wrong profile (potentially escalating a thread to the default/admin profile instead of
     * its own). A stack lets every {@code setCurrentProfile} call push its own immediately-
     * preceding value and every {@code resetProfile()} pop exactly that value back, giving correct
     * LIFO restore at any nesting depth. Not inheritable, like the field it replaces: a freshly
     * spawned worker thread must start with its own empty stack, not inherit its parent's pending
     * restore state.
     */
    protected static final ThreadLocal<Deque<Object>> previousProfileStack = new HostThreadLocal<Deque<Object>>();
    protected static String contextPath;

    /**
     * Marker pushed onto {@link #previousProfileStack} to record that the profile in effect
     * immediately before a {@code setCurrentProfile(nonNull)} call was itself {@code null} (as
     * opposed to there being no entry to restore at all). Without this, {@code resetProfile()}
     * cannot tell "nothing to restore" apart from "restore to null" and silently does nothing in
     * the latter case -- which is exactly the state every pooled/scheduled worker thread starts in
     * before its first task ever calls {@code setCurrentProfile}, so every
     * {@code finally { resetProfile(); }} guard on such a thread would otherwise leave that thread's
     * tenant profile permanently pinned to whichever tenant ran on it first.
     */
    private static final Object NULL_PROFILE = new Object();

    /**
     * Sets the Host of current HTTP request.
     * This method is security protected in Cloud installation.
     * @param hostname 
     */
    public static void setCurrentHost(String hostname) {
        currentHost.set(hostname);
    }

    /**
     * Gets the current Host of HTTP request
     * @return 
     */
    public static String getCurrentHost() {
        if (isVirtualHostEnabled()) {
            String hostname = (String)currentHost.get();
            return hostname;
        }
        else {
            return null;
        }
    }

    /**
     * Sets the profile of current HTTP request.
     * This method is security protected in Cloud installation.
     * @param profile 
     */
    public static void setCurrentProfile(String profile) {
        profile = SecurityUtil.validateStringInput(profile);
        if (profile == null) {
            // Hard clear (e.g. initHost() at the start of a new HTTP request): discard any pending
            // restore state instead of pushing "null" as a value to restore later -- this call
            // establishes a fresh baseline, it doesn't open a nested scope.
            previousProfileStack.remove();
            currentHost.remove();
            currentProfile.set(profile);
            return;
        }
        // previousProfileStack's own lazy init (a permission-checked HostThreadLocal.set(), only
        // on a thread's first tracked setCurrentProfile call) must happen HERE -- before
        // currentHost.remove()/currentProfile.set() below flip the ambient profile away from the
        // caller's own. A permission check performed after that flip sees the NEW (target)
        // profile as ambient, not the caller's, which defeats any caller-identity-based exemption
        // that depends on the ambient profile still being the caller's own at the moment of the
        // check (e.g. MultiTenantSecurityManager.isCalledDirectlyFromAdminProfileWriter(), relied
        // on by AdminProfileWriter) -- confirmed by adversarial review to actually misfire this
        // way on a fresh thread's first call, under real AspectJ around-advice weaving of
        // HostManager.set*(..) with -XnoInline (the advice/closure frames inserted between the
        // caller and this method are not, and cannot robustly be made to be, recognized by such a
        // check). Once the stack exists, every later call just pushes a plain Deque value below
        // with no permission check at all, so this only matters for the very first call.
        String current = getCurrentProfile();
        Deque<Object> stack = previousProfileStack.get();
        if (stack == null) {
            stack = new ArrayDeque<Object>();
            previousProfileStack.set(stack);
        }
        // The permission-checked currentHost.remove()/currentProfile.set() calls below are the
        // only OTHER calls here that go through a HostThreadLocal-guarded set()/remove(); an
        // unauthorized caller is denied by either of them before the push below ever runs.
        currentHost.remove();
        currentProfile.set(profile);
        // Always push -- see previousProfileStack's own javadoc for why this (not "only if the
        // slot is empty") is the actual fix: every scope must record its OWN immediately-
        // preceding value, regardless of whether an outer scope is already active.
        stack.push(current == null ? NULL_PROFILE : current);
    }

    /**
     * Sets the current profile WITHOUT recording it as a value {@link #resetProfile()} should
     * ever restore. Use this only for a permanent, thread-lifetime assignment that has no
     * matching {@code resetProfile()} call of its own -- e.g. {@code DynamicDataSourceManager}'s
     * own memoization of a profile it resolved from hostname/context-path config, once per
     * thread.
     *
     * <p>Routing that kind of call through the tracked {@link #setCurrentProfile(String)} instead
     * is unsafe: it would push an entry onto {@link #previousProfileStack} with no matching
     * {@code resetProfile()} call to ever pop it. That orphaned entry would either get popped by
     * a later, genuinely scoped pair on the same thread (restoring the untracked value instead of
     * what was truly ambient before that later scope opened) or linger until the thread's next
     * hard clear. {@code resetProfile()} itself is guarded both by {@code
     * MultiTenantSecurityManager.WHITELIST_RESTRICTED_CLASSES} (an explicit caller entry is
     * required for every legitimate call site -- see that class's own javadoc) and by the {@code
     * HostPermission} check inside {@link #currentProfile}'s {@code
     * HostInheritableThreadLocal.set()}, so an unpaired, non-whitelisted caller's {@code
     * resetProfile()} attempt is denied rather than a silent no-op either way.
     */
    public static void setCurrentProfileWithoutTracking(String profile) {
        profile = SecurityUtil.validateStringInput(profile);
        currentHost.remove();
        currentProfile.set(profile);
    }

    /**
     * Gets the current cloud profile of HTTP request
     * @return
     */
    public static String getCurrentProfile() {
        String profile = (String) currentProfile.get();
        return profile;
    }

    /**
     * Reset the profile of the current HTTP request.
     * @param profile 
     */
    public static void resetProfile() {
        Deque<Object> stack = previousProfileStack.get();
        if (stack != null && !stack.isEmpty()) {
            // Peek, don't pop, until after currentProfile.set()/currentHost.remove() succeed:
            // their permission check lives inside currentProfile.set() itself, not here, so
            // popping first would let an unauthorized direct caller (e.g. tenant BeanShell code
            // not covered by the whitelist) discard a legitimate outer scope's restore entry
            // before being denied, leaving that outer scope with nothing left to restore.
            Object previous = stack.peek();
            currentProfile.set(previous == NULL_PROFILE ? null : (String) previous);
            currentHost.remove();
            // Only reached once both calls above succeeded -- now safe to finalize the pop.
            stack.pop();
            if (stack.isEmpty()) {
                previousProfileStack.remove();
            }
        }
    }
    
    /**
     * Initials the host for the current request
     */
    public static void initHost() {
        // reset profile and set hostname
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            if (request != null && !"true".equals(request.getAttribute(REQUEST_HOST_INITIALIZED))) {
                HostManager.setCurrentProfile(null);
                String hostname = request.getServerName();
                HostManager.setCurrentHost(hostname);
                request.setAttribute(REQUEST_HOST_INITIALIZED, "true");
            }
        } catch (Exception e) {
            // ignore if servlet request is not available
        }
    }

    /**
     * Flag to indicate it is a Cloud installation
     * @return 
     */
    public static boolean isVirtualHostEnabled() {
        boolean enabled = Boolean.valueOf(System.getProperty(SYSTEM_PROPERTY_VIRTUALHOST));
        return enabled;
    }

    /**
     * Gets the context path of the HTTP request
     * @return 
     */
    public static String getContextPath() {
        return contextPath;
    }

    /**
     * Method used by system to set Application Context. 
     * This method is security protected in Cloud installation.
     * @param appContext
     * @throws BeansException 
     */
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        if (appContext instanceof WebApplicationContext) {
            String realContextPath = ((WebApplicationContext)appContext).getServletContext().getRealPath("/");
            String cPath = "/jw";
            if (realContextPath != null) {
                File contextPathFile = new File(realContextPath);
                cPath = contextPathFile.getName();
                if (!cPath.startsWith("/")) {
                    cPath = "/" + cPath;
                }
            }
            HostManager.contextPath = cPath;
        }
    }

}
