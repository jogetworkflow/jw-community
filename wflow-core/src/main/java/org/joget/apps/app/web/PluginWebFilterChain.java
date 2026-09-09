package org.joget.apps.app.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebFilter;
import org.joget.plugin.base.SystemConfigurablePlugin;
import org.springframework.security.web.firewall.DefaultRequestRejectedHandler;
import org.springframework.security.web.firewall.FirewalledRequest;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.web.filter.GenericFilterBean;

/**
 * This class is created based on FilterChainProxy to hold filters data 
 * and process the filter chain
 */
public class PluginWebFilterChain extends GenericFilterBean {

    protected Map<String, UrlPatternFiltersHolder> urlPatternFiltersHolder = new HashMap<>();

    private static final String FILTER_APPLIED = PluginWebFilterChain.class.getName().concat(".APPLIED");

    private HttpFirewall firewall = new StrictHttpFirewall();

    private RequestRejectedHandler requestRejectedHandler = new DefaultRequestRejectedHandler();

    private ThrowableAnalyzer throwableAnalyzer = new ThrowableAnalyzer();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        boolean clearContext = request.getAttribute(FILTER_APPLIED) == null;
        if (!clearContext) {
            doFilterInternal(request, response, chain);
            return;
        }
        try {
            request.setAttribute(FILTER_APPLIED, Boolean.TRUE);
            doFilterInternal(request, response, chain);
        } catch (Exception ex) {
            Throwable[] causeChain = this.throwableAnalyzer.determineCauseChain(ex);
            Throwable requestRejectedException = this.throwableAnalyzer
                    .getFirstThrowableOfType(RequestRejectedException.class, causeChain);
            if (!(requestRejectedException instanceof RequestRejectedException)) {
                throw ex;
            }
            this.requestRejectedHandler.handle((HttpServletRequest) request, (HttpServletResponse) response,
                    (RequestRejectedException) requestRejectedException);
        } finally {
            request.removeAttribute(FILTER_APPLIED);
        }
    }

    private void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        FirewalledRequest firewallRequest = this.firewall.getFirewalledRequest((HttpServletRequest) request);
        HttpServletResponse firewallResponse = this.firewall.getFirewalledResponse((HttpServletResponse) response);
        List<Filter> filters = getFilters(firewallRequest);
        if (filters == null || filters.size() == 0) {
            continueToOriginalChain(firewallRequest, chain, firewallRequest, firewallResponse);
            return;
        }
        VirtualFilterChain virtualFilterChain = new VirtualFilterChain(firewallRequest, chain, filters);
        // #2261 (08 Sep 2026, Sol review, round 3): a registered filter could RETAIN this
        // VirtualFilterChain instead of calling it synchronously, return, and have separate, later
        // code -- potentially during a completely different request's own filter-chain processing --
        // invoke the retained chain. MultiTenantSecurityManager.isUntrustedCodeOnStack()'s
        // stack-scan-based trust boundary cannot reliably distinguish that from a legitimate call,
        // since an unrelated PluginWebFilterChain frame belonging to the OTHER, coincidentally-live
        // invocation can satisfy its re-entry check. Invalidating the chain here, once this
        // synchronous call returns, closes the exploit at its source instead: any later attempt to
        // reuse it fails immediately, before ever reaching the stack-scan logic at all.
        try {
            virtualFilterChain.doFilter(firewallRequest, firewallResponse);
        } finally {
            virtualFilterChain.invalidate();
        }
    }

    // Single, stable call site for handing off from plugin-filter-chain processing to the real
    // chain, used by both exit paths below. MultiTenantSecurityManager.isUntrustedCodeOnStack()
    // treats this exact frame as a trust boundary, since a plugin filter's own stack frame otherwise
    // stays live for the whole downstream continuation.
    private static void continueToOriginalChain(FirewalledRequest firewalledRequest, FilterChain originalChain,
            ServletRequest request, ServletResponse response) throws IOException, ServletException {
        // Deactivate path stripping as we exit the filter chain
        firewalledRequest.reset();
        originalChain.doFilter(request, response);
    }

    /**
     * Returns the merged, order-sorted filter chain of every url pattern
     * bucket matching the supplied URL. Filters that were registered under
     * more than one matching pattern are only included once.
     *
     * @param request the request to match
     * @return an ordered array of Filters defining the filter chain
     */
    private List<Filter> getFilters(HttpServletRequest request) {
        Map<String, PluginWebFilter> matchedFilters = new LinkedHashMap<>();
        for (UrlPatternFiltersHolder holder : this.urlPatternFiltersHolder.values()) {
            if (holder.isMatch(request)) {
                for (PluginWebFilter filter : holder.getFilterMap().values()) {
                    matchedFilters.put(filter.getName(), filter);
                }
            }
        }
        if (matchedFilters.isEmpty()) {
            return null;
        }
        return matchedFilters.values().stream()
                .sorted(Comparator.comparingInt(PluginWebFilter::getOrder))
                .collect(Collectors.toList());
    }
    
    protected UrlPatternFiltersHolder getUrlPatternFiltersHolder(String urlPattern) {
        if (!urlPatternFiltersHolder.containsKey(urlPattern)) {
            urlPatternFiltersHolder.put(urlPattern, new UrlPatternFiltersHolder(urlPattern));
        }
        return urlPatternFiltersHolder.get(urlPattern);
    }
    
    /**
     * add filter to the url patterns holder
     * 
     * @param filter 
     */
    public void addFilter(PluginWebFilter filter) {
        String[] urlPatterns = filter.getUrlPatterns();
        if (urlPatterns != null) {
            for (String p : urlPatterns) {
                UrlPatternFiltersHolder holder = getUrlPatternFiltersHolder(p);
                holder.add(filter);
            }
        }
    }
    
    /**
     * Remove filter from the url patterns
     * 
     * @param filter 
     */
    public void removeFilter(PluginWebFilter filter) {
        String[] urlPatterns = filter.getUrlPatterns();
        if (urlPatterns != null) {
            for (String p : urlPatterns) {
                UrlPatternFiltersHolder holder = getUrlPatternFiltersHolder(p);
                holder.remove(filter);

                //if this url pattern no have any filter already, remove it
                if (holder.isEmpty()) {
                    urlPatternFiltersHolder.remove(p);
                }
            }
        }
    }
    
    /**
     * Check there is no filter chain
     * 
     * @return 
     */
    public boolean isEmpty() {
        return urlPatternFiltersHolder.isEmpty();
    }
    
    /**
     * Internal {@code FilterChain} implementation that is used to hold the filters
     * and the url pattern matcher
     */
    private static final class UrlPatternFiltersHolder {
        private Map<String, PluginWebFilter> filters = new LinkedHashMap<>();
        private AntPathRequestMatcher matcher;
                
        public UrlPatternFiltersHolder(String urlPattern) {
            this.matcher = new AntPathRequestMatcher(urlPattern);
        }
        
        public boolean isMatch(HttpServletRequest request) {
            return this.matcher.matches(request);
        }
        
        public void add(PluginWebFilter filter) {
            filters.put(filter.getName(), filter);
            
            if (filters.size() > 1) {
                //sort filters following order
                filters = filters.entrySet()
                    .stream()
                    .sorted(Comparator.comparingInt(entry -> entry.getValue().getOrder()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, 
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, // Merge function
                            LinkedHashMap::new // Preserve insertion order
                    ));
            }
        }
        
        public void remove(PluginWebFilter filter) {
            filters.remove(filter.getName());
        }
        
        public boolean isEmpty() {
            return filters.isEmpty();
        }
        
        public List<Filter> getFilters() {
            return new ArrayList<Filter>(filters.values());
        }

        public Map<String, PluginWebFilter> getFilterMap() {
            return filters;
        }
    }

    /**
     * Internal {@code FilterChain} implementation that is used to pass a
     * request through the additional internal list of filters which match the
     * request.
     */
    private static final class VirtualFilterChain implements FilterChain {

        private final FilterChain originalChain;

        private final List<Filter> additionalFilters;

        private final FirewalledRequest firewalledRequest;

        private final int size;

        private int currentPosition = 0;

        // #2261 (08 Sep 2026, Sol review, round 3): set once the top-level doFilterInternal(..)
        // call that constructed this instance returns, so a filter that RETAINS this chain instead
        // of calling it synchronously -- then invokes it later, possibly during a completely
        // different, unrelated request's own filter-chain processing -- gets rejected outright,
        // rather than relying on isUntrustedCodeOnStack()'s stack-scan to notice, which cannot
        // reliably distinguish a genuinely unrelated PluginWebFilterChain frame (from that OTHER,
        // coincidentally-live invocation) from this chain's own owning invocation.
        private volatile boolean invalidated = false;

        // #2261 (08 Sep 2026, Sol review, round 4): completion-only invalidation still permits a
        // CONCURRENT replay while the owning invocation is still live (`invalidated` stays false
        // for as long as the owning call has not yet returned) -- e.g. a filter publishes this
        // chain through a static/shared field and blocks, while a second, unrelated request's own
        // thread reads that field and invokes the chain concurrently, reaching
        // continueToOriginalChain() legitimately from an entirely different call stack. This
        // instance is only ever meant to be driven synchronously, entirely within the ONE thread
        // that constructed it (matching Spring's own FilterChainProxy.VirtualFilterChain, which
        // this class is explicitly modeled on) -- capturing that thread and rejecting any call from
        // a different one closes this regardless of whether the owning invocation has completed
        // yet, independent of the `invalidated` flag above.
        private final Thread owningThread = Thread.currentThread();

        // #2261 (08 Sep 2026, Sol review, round 5): thread confinement and the invalidated flag
        // together stop a RETAINED/detached/concurrent replay, but do not stop a plugin filter
        // calling THIS chain's own doFilter(..) TWICE, synchronously, on the SAME thread, before its
        // own call returns -- currentPosition stays at size after the first such call reaches the
        // terminal branch below, so a second, same-thread call before returning would reach
        // continueToOriginalChain(..) (and the real downstream chain) again. Consuming the terminal
        // transition here, once, closes that: this field is only ever read/written from
        // owningThread (guaranteed by the check above), so no synchronization is needed, matching
        // currentPosition's own existing non-volatile, single-thread-only assumption.
        private boolean terminalReached = false;

        private VirtualFilterChain(FirewalledRequest firewalledRequest, FilterChain chain,
                List<Filter> additionalFilters) {
            this.originalChain = chain;
            this.additionalFilters = additionalFilters;
            this.size = additionalFilters.size();
            this.firewalledRequest = firewalledRequest;
        }

        private void invalidate() {
            this.invalidated = true;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            if (Thread.currentThread() != this.owningThread) {
                throw new ServletException("This filter chain may only be invoked on the thread "
                        + "that originally constructed it");
            }
            if (this.invalidated) {
                throw new ServletException("This filter chain has already completed for its "
                        + "original request and cannot be invoked again");
            }
            if (this.currentPosition == this.size) {
                // #2261 (08 Sep 2026, colleague-reported regression against dx82-samples):
                // THIS branch, not the empty-filters one in doFilterInternal, is the exit point an
                // installed plugin filter (e.g. SampleHeaderFilter, matching Joget's own documented
                // PluginWebFilterAbstract pattern) actually reaches -- any registered filter's own
                // doFilter(..) calls filterChain.doFilter(request, response) on THIS VirtualFilterChain
                // instance, and once every registered filter has run once, that recursive call lands
                // here. An externally-supplied patch fixed the OTHER (empty-filters) exit point and
                // added continueToOriginalChain(..) as a MultiTenantSecurityManager.isUntrustedCodeOnStack()
                // trust-boundary marker, but never routed THIS actually-hit exit point through it --
                // independently confirmed by tracing the call chain (not by trusting the patch's own
                // "used by both exit paths" comment) that leaving this branch unpatched reproduces the
                // reported regression exactly: MultiTenantPluginAspect.securityCheck()'s advice around
                // the plugin filter's own doFilter(..) keeps `active` true for the ENTIRE nested
                // downstream chain (JSP compilation, static resources) until pjp.proceed() returns --
                // i.e. until this recursive call, and everything it does, completes -- so without the
                // marker HERE too, isUntrustedCodeOnStack()'s walk still reaches the plugin's own
                // OSGI-bundle-loaded doFilter frame further out on the SAME stack and wrongly flags the
                // whole downstream continuation as untrusted, exactly as reported.
                if (this.terminalReached) {
                    throw new ServletException("This filter chain has already reached the end of "
                            + "its chain and cannot be invoked again");
                }
                this.terminalReached = true;
                continueToOriginalChain(this.firewalledRequest, this.originalChain, request, response);
                return;
            }
            this.currentPosition++;
            Filter nextFilter = this.additionalFilters.get(this.currentPosition - 1);
            
            //get plugin again to set the system properties
            if (nextFilter instanceof SystemConfigurablePlugin) {
                PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
                nextFilter = (Filter) pluginManager.getPlugin(ClassUtils.getUserClass(nextFilter).getName());
            }
            
            nextFilter.doFilter(request, response, this);
        }

    }
}
