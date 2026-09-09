package org.joget.commons.util;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility methods used by the system to manage cloud profile
 * 
 */
@Service
@Lazy
public final class HostManager implements ApplicationContextAware {

    public static final String SYSTEM_PROPERTY_VIRTUALHOST = "wflow.virtualhost";
    public static final String REQUEST_HOST_INITIALIZED = "REQUEST_HOST_INITIALIZED";

    protected static final ThreadLocal currentHost = new HostThreadLocal();
    protected static final ThreadLocal currentProfile = new HostInheritableThreadLocal();
    /**
     * A per-thread stack of pending restore values, one push per {@code setCurrentProfile} call
     * and one pop per matching {@code resetProfile()}. A single-slot field cannot support nested
     * scopes: if a second {@code setCurrentProfile}/{@code resetProfile()} pair opens while an
     * outer one is still active (e.g. a license check triggered by tenant code inside an outer
     * worker task's tracked scope), the slot is already occupied, so the inner call's
     * {@code resetProfile()} would restore the OUTER scope's value instead of its own -- corrupting
     * the outer scope's remaining work (worst case, restoring to the platform's default/admin
     * profile on a pooled worker thread, a non-admin-to-admin escalation, not just a data-isolation
     * leak). A stack lets every scope push and pop its own value regardless of nesting depth. Not
     * inheritable (matches the old field's non-inheritable {@code HostThreadLocal} type): a freshly
     * spawned worker thread must start with an empty stack, not inherit its parent's still-pending
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
            // restore state entirely rather than pushing "null" as a value to restore later -- this
            // call is establishing a fresh baseline, not opening a nested scope of its own.
            // previousProfileStack.remove() is itself a permission-checked HostThreadLocal
            // operation, so an unauthorized caller is denied here before anything else runs.
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
     * {@code resetProfile()} call to ever pop it -- an orphaned entry that a LATER, genuinely
     * scoped {@code setCurrentProfile}/{@code resetProfile()} pair on the same thread would
     * eventually pop (restoring the untracked memoization's value instead of what was truly
     * ambient before that later scope opened), or, if never popped, would linger until the
     * thread's next hard clear (see {@link #setCurrentProfile(String)}'s {@code profile == null}
     * branch). {@code resetProfile()} itself is guarded via {@code
     * MultiTenantSecurityManager.WHITELIST_RESTRICTED_CLASSES} (an explicit caller-based entry is
     * required for every legitimate call site -- see that class's own javadoc) plus the {@code
     * HostPermission} check embedded in {@link #currentProfile}'s {@code
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
            // Peek, don't pop, until after the permission-checked currentProfile.set()/
            // currentHost.remove() calls below succeed (the check lives inside currentProfile
            // .set()'s HostInheritableThreadLocal override, not in resetProfile() itself).
            // Popping first would let an unauthorized direct caller (e.g. tenant BeanShell code not
            // covered by the whitelist) discard a legitimate outer scope's restore entry and then
            // get denied, leaving that outer scope's own later resetProfile() with nothing to
            // restore.
            Object previous = stack.peek();
            currentProfile.set(previous == NULL_PROFILE ? null : (String) previous);
            currentHost.remove();
            // Only reached if both calls above succeeded (authorized) -- now safe to finalize the
            // mutation.
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
