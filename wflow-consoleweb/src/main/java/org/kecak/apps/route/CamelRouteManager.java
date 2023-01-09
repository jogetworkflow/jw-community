package org.kecak.apps.route;

import com.kinnarastudio.commons.Try;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.kecak.apps.incomingEmail.dao.IncomingEmailDao;
import org.kecak.apps.incomingEmail.model.IncomingEmail;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class CamelRouteManager implements CamelContextAware {

    private CamelContext camelContext;

	private IncomingEmailDao incomingEmailDao;

	private SecurityUtil securityUtil;

	public void setCamelContext(CamelContext camelContext) {
		this.camelContext = camelContext;
	}
	
	public boolean stopContext() {
		boolean result = false;
		try {
			getCamelContext().getRoutes().forEach(Try.onConsumer(r -> {
				// remove all existing routes
				getCamelContext().removeEndpoint(r.getEndpoint());
				getCamelContext().stopRoute(r.getId());
				getCamelContext().removeRoute(r.getId());
			}));

			result = true;

			LogUtil.info(getClass().getName(), camelContext.getName() + " is stopped");
		} catch (Exception e) {
			LogUtil.error(getClass().getName(), e, e.getMessage());
		}
		
		return result;
	}

	public boolean startContext() {
		final Collection<IncomingEmail> incomingEmails = incomingEmailDao.find(null, null, null, null, null, null);

		return Optional.ofNullable(incomingEmails)
				.map(Collection::stream)
				.orElseGet(Stream::empty)
				.filter(IncomingEmail::getActive)
				.map(IncomingEmail::getId)
				.allMatch(this::startContext);
	}

	public boolean startContext(String id) {
		boolean result = false;
		try {
			// add again deleted routes
			final IncomingEmailRouteBuilder routesBuilder = new IncomingEmailRouteBuilder();
			routesBuilder.setIncomingEmailId(id);
			routesBuilder.setIncomingEmailDao(incomingEmailDao);

			getCamelContext().addRoutes(routesBuilder);
			getCamelContext().start();
			result = true;
			LogUtil.info(getClass().getName(), getCamelContext().getName()+" is started");
		} catch (Exception e) {
			LogUtil.error(getClass().getName(), e, e.getMessage());
		}
		
		return result;
	}


	public CamelContext getCamelContext() {
		return camelContext;
	}
	public void setIncomingEmailDao(IncomingEmailDao incomingEmailDao) {
		this.incomingEmailDao = incomingEmailDao;
	}

	public void setSecurityUtil(SecurityUtil securityUtil) {
		this.securityUtil = securityUtil;
	}
}
