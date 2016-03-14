package org.joget.commons.spring.web;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Overrides Spring's DelegatingFilterProxy to support re-initialization of the 
 * ApplicationContext if previous attempts fail.
 */
public class CustomDelegatingFilterProxy extends DelegatingFilterProxy {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest req = ((HttpServletRequest)request);
        String url = req.getContextPath() + "/setup";
        try {
            if (!req.getRequestURI().startsWith(url)) {
                super.doFilter(request, response, filterChain);
            } else {
                filterChain.doFilter(request, response);
            }
        } catch(IllegalStateException ise) {
            if (ise.getMessage().startsWith("No WebApplicationContext found")) {
                LogUtil.info(getClass().getName(), "No WebApplicationContext found, redirecting to error page");
                ((HttpServletResponse)response).sendRedirect(url);
            } else {
                throw ise;
            }
        } catch(ServletException e) {
            if (e.getMessage().startsWith("java.lang.IllegalStateException: getOutputStream() has already been called for this response")) {
                //ignore to override the response
            } else {
                LogUtil.error(getClass().getName(), e, e.getMessage());
                throw e;
            }
        } catch (IOException e) {
            LogUtil.error(getClass().getName(), e, e.getMessage());
            throw e;
        }
    }

    @Override
    protected WebApplicationContext findWebApplicationContext() {
        WebApplicationContext wc = super.findWebApplicationContext();
        if (wc == null) {
            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
            if (request != null) {
                ServletContext sc = request.getServletContext();
                ServletContextEvent sce = new ServletContextEvent(sc);
                CustomContextLoaderListener cll = new CustomContextLoaderListener();
                cll.contextInitialized(sce);
                wc = super.findWebApplicationContext();
                if (wc != null) {
                    DispatcherServlet servlet = CustomDispatcherServlet.getCustomDispatcherServlet();
                    try {
                        servlet.init();
                    } catch (ServletException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        return wc;
    }

}
