package org.kecak.apps.route;

import org.apache.camel.builder.RouteBuilder;
import org.joget.commons.util.SetupManager;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * The builder will be executed in 2 points, during inital loading and
 * saving Settings
 */
@Deprecated
public class EmailProcessorRouteBuilder extends RouteBuilder {

	private SetupManager setupManager;

    @Override
	public void configure() {
		@Nonnull String emailAccount = Optional.of("emailAccount").map(setupManager::getSettingValue).orElse("");
		@Nonnull String emailPassword = Optional.of("emailPassword").map(setupManager::getSettingValue).orElse("");
		@Nonnull String emailProtocol = Optional.of("emailProtocol").map(setupManager::getSettingValue).orElse("");
		@Nonnull String emailHost = Optional.of("emailHost").map(setupManager::getSettingValue).orElse("");
		@Nonnull String emailPort = Optional.of("emailPort").map(setupManager::getSettingValue).orElse("");
		@Nonnull String emailFolder = Optional.of("emailFolder").map(setupManager::getSettingValue).orElse("");

		// set default port
		if(emailPort.isEmpty()) {
			if("imap".equalsIgnoreCase(emailProtocol))
				emailPort = "143"; // default IMAP
			else if("imaps".equalsIgnoreCase(emailProtocol))
				emailPort = "993"; // default IMAPS
		}

		// set default folder
		if(emailFolder.isEmpty())
			emailFolder = "INBOX";

		if (!emailAccount.isEmpty() && !emailPassword.isEmpty() && !emailProtocol.isEmpty() && !emailHost.isEmpty() && !emailPort.isEmpty()) {

			StringBuilder fromUriBuilder = new StringBuilder();
			fromUriBuilder.append(emailProtocol).append("://").append(emailHost).append(":").append(emailPort);
			fromUriBuilder.append("?username=").append(emailAccount);
			fromUriBuilder.append("&password=").append(emailPassword);
			fromUriBuilder.append("&folderName=").append(emailFolder);
			fromUriBuilder.append("&delete=false&unseen=true");

			String fromUri = fromUriBuilder.toString();
			from(fromUri).beanRef("emailProcessor", "parseEmail");
		}
	}

	public void setSetupManager(SetupManager setupManager) {
		this.setupManager = setupManager;
	}
}