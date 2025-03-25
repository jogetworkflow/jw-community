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
            firewallRequest.reset();
            chain.doFilter(firewallRequest, firewallResponse);
            return;
        }
        VirtualFilterChain virtualFilterChain = new VirtualFilterChain(firewallRequest, chain, filters);
        virtualFilterChain.doFilter(firewallRequest, firewallResponse);
    }

    /**
     * Returns the first filter chain matching the supplied URL.
     *
     * @param request the request to match
     * @return an ordered array of Filters defining the filter chain
     */
    private List<Filter> getFilters(HttpServletRequest request) {
        for (UrlPatternFiltersHolder holder : this.urlPatternFiltersHolder.values()) {
            if (holder.isMatch(request)) {
                return holder.getFilters();
            }
        }
        return null;
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

        private VirtualFilterChain(FirewalledRequest firewalledRequest, FilterChain chain,
                List<Filter> additionalFilters) {
            this.originalChain = chain;
            this.additionalFilters = additionalFilters;
            this.size = additionalFilters.size();
            this.firewalledRequest = firewalledRequest;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            if (this.currentPosition == this.size) {
                // Deactivate path stripping as we exit the filter chain
                this.firewalledRequest.reset();
                this.originalChain.doFilter(request, response);
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
