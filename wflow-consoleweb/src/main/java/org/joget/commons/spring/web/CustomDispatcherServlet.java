package org.joget.commons.spring.web;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Overrides Spring's DispatcherServlet to support re-initialization of the 
 * ApplicationContext if previous attempts fail.
 */
public class CustomDispatcherServlet extends DispatcherServlet {

    private static volatile CustomDispatcherServlet customDispatcherServlet;

    public static CustomDispatcherServlet getCustomDispatcherServlet() {
        return customDispatcherServlet;
    }

    // First (platform) writer wins. web.xml declares the real dispatcher load-on-startup=2, so the
    // platform instance is constructed at webapp startup, before any tenant request (and therefore any
    // tenant BeanShell) can run -- so the field is already non-null by then. This static anchor is the
    // identity the multi-tenant guards (SharedSpringInfrastructureGuardAspect) use to recognize the
    // shared dispatcher servlet/context; a public constructor with a last-writer-wins assignment let
    // untrusted BeanShell `new CustomDispatcherServlet()` repoint it at a throwaway and thereby
    // neutralize those guards (cross-tenant ServletRequestHandledEvent capture). A once-only assignment
    // makes a tenant-constructed instance a no-op for the anchor.
    private static synchronized void assignSharedInstance(CustomDispatcherServlet instance) {
        if (customDispatcherServlet == null) {
            customDispatcherServlet = instance;
        }
    }


    // #2261: declared here (webapp class, woven) so the shared-dispatcher guard sees init(ServletConfig) BEFORE
    // GenericServlet stores the config; the inherited GenericServlet method is not weavable.
    @Override
    public void init(jakarta.servlet.ServletConfig config) throws jakarta.servlet.ServletException {
        super.init(config);
    }
    public CustomDispatcherServlet() {
        // Do not publish per-request ServletRequestHandledEvent (request URI, remote user, HTTP
        // session id) into the shared application context: under multi-tenancy a listener registered
        // by one tenant must not observe other tenants' request metadata. Joget has no consumer of
        // these events.
        setPublishEvents(false);
        assignSharedInstance(this);
    }

    public CustomDispatcherServlet(WebApplicationContext webApplicationContext) {
        super(webApplicationContext);
        setPublishEvents(false);
        assignSharedInstance(this);
    }

}
