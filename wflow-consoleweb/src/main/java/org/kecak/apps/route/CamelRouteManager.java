package org.kecak.apps.route;

import com.kinnarastudio.commons.Try;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.joget.commons.util.LogUtil;

public class CamelRouteManager implements CamelContextAware {

    private CamelContext camelContext;

	public CamelContext getCamelContext() {
		return camelContext;
	}

	public void setCamelContext(CamelContext camelContext) {
		this.camelContext = camelContext;
	}
	
	public boolean stopContext(String id) {
		boolean result = false;
		try {
			camelContext.getRoutes().forEach(Try.onConsumer(r -> {
				// remove all existing routes
				camelContext.removeEndpoint(r.getEndpoint());
				camelContext.stopRoute(r.getId());
				camelContext.removeRoute(r.getId());
			}));

			result = true;
			LogUtil.info(getClass().getName(), camelContext.getName()+" is stopped");
		} catch (Exception e) {
			LogUtil.error(getClass().getName(), e, e.getMessage());
		}
		
		return result;
	}
	
	public boolean startContext(String id) {
		boolean result = false;
		try {
			// add again deleted routes
			getCamelContext().addRoutes(new EmailProcessorRouteBuilder());
			getCamelContext().start();
			result = true;
			LogUtil.info(getClass().getName(), getCamelContext().getName()+" is started");
		} catch (Exception e) {
			LogUtil.error(getClass().getName(), e, e.getMessage());
		}
		
		return result;
	}
}
