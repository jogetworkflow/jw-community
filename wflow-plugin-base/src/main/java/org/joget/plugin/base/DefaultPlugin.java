package org.joget.plugin.base;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public abstract class DefaultPlugin implements Plugin, BundleActivator {

    protected ServiceRegistration registration;

    public void start(BundleContext context) {
        registration = context.registerService(getClass().getName(), this, null);
    }

    public void stop(BundleContext context) {
        registration.unregister();
    }
}
