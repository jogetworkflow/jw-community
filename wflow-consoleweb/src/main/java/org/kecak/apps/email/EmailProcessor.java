package org.kecak.apps.email;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.kecak.apps.app.model.EmailProcessorPlugin;

import javax.annotation.Nonnull;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class for triggering email processor plugins
 */
public class EmailProcessor {
    public static final String FROM = "from";
    public static final String SUBJECT = "subject";

//    private EmailApprovalContentDao emailApprovalContentDao;
    private WorkflowManager workflowManager;
    private AppService appService;
    private WorkflowUserManager workflowUserManager;
    private DirectoryManager directoryManager;
    private PluginManager pluginManager;
    private AppDefinitionDao appDefinitionDao;
    private WorkflowHelper workflowHelper;

    /**
     *
     * @param body
     * @param exchange
     */
    public void parseEmail(@Body final String body, final Exchange exchange) {
        // get sender email address
        final String from = exchange.getIn().getHeader(FROM).toString();
        final String fromEmail = from.replaceAll("^.*<|>.*$", "");
        final Set<String> usernames = getUsername(fromEmail);

        final String subject = exchange.getIn().getHeader(SUBJECT).toString().replace("\t", "__").replace("\n", "__").replace(" ", "__");

        Optional.ofNullable(appDefinitionDao.findPublishedApps(null, null, null, null))
                .map(Collection::stream)
                .orElse(Stream.empty())

                // set current app definition
                .peek(AppUtil::setCurrentAppDefinition)

                .forEach(appDefinition -> Optional.ofNullable(appDefinition.getPluginDefaultPropertiesList())
                        .map(Collection::stream)
                        .orElse(Stream.empty())

                        // process every plugin default property
                        .forEach(pluginDefaultProperty -> Stream.of(pluginDefaultProperty)
                                .map(p -> pluginManager.getPlugin(p.getId()))
                                .filter(p -> p instanceof EmailProcessorPlugin && p instanceof ExtDefaultPlugin)
                                .map(p -> (ExtDefaultPlugin) p)
                                .forEach(p -> {
                                    Map<String, Object> pluginProperties = PropertyUtil.getPropertiesValueFromJson(pluginDefaultProperty.getPluginProperties());
                                    p.setProperties(pluginProperties);

                                    Map<String, Object> parameterProperties = new HashMap<>(pluginProperties);
                                    parameterProperties.put(EmailProcessorPlugin.PROPERTY_APP_DEFINITION, appDefinition);
                                    parameterProperties.put(EmailProcessorPlugin.PROPERTY_FROM, fromEmail);
                                    parameterProperties.put(EmailProcessorPlugin.PROPERTY_SUBJECT, subject);
                                    parameterProperties.put(EmailProcessorPlugin.PROPERTY_BODY, body);
                                    parameterProperties.put(EmailProcessorPlugin.PROPERTY_EXCHANGE, exchange);

                                    usernames.forEach(username -> {
                                        workflowUserManager.setCurrentThreadUser(username);

                                        try {
                                            if (((EmailProcessorPlugin) p).filter(parameterProperties)) {
                                                LogUtil.info(getClass().getName(), "Processing Email Plugin [" + p.getName() + "] for application [" + appDefinition.getAppId() + "] as [" + username + "]");
                                                workflowHelper.addAuditTrail(this.getClass().getName(), "parseEmail", subject, new Class[]{String.class}, new Object[]{subject}, false);

                                                ((EmailProcessorPlugin) p).parse(parameterProperties);
                                            } else {
                                                LogUtil.debug(getClass().getName(), "Skipping Email Plugin [" + p.getName() + "] : Not meeting filter condition");
                                            }
                                        } catch (Exception e) {
                                            ((EmailProcessorPlugin) p).onError(parameterProperties, e);
                                        }
                                    });
                                })));
    }

    /**
     *
     * @param sender
     * @return
     */
    @Nonnull
    private Set<String> getUsername(String sender) {
        // get sender
        InternetAddress ia = null;
        try {
            ia = new InternetAddress(sender);
        } catch (AddressException e) {
            LogUtil.error(this.getClass().getName(), e, e.getMessage());
        }

        if (ia == null) {
            LogUtil.warn(getClass().getName(), "Address not found for sender [" + sender + "]");
            return Collections.EMPTY_SET;
        }

        String email = ia.getAddress();

        Set<String> usernames = Optional.of(email)
                .map(s -> directoryManager.getUserList(s, null, null, 0, null))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(User::getUsername)
                .collect(Collectors.toSet());

        if(usernames.isEmpty()) {
            usernames.add(getDefaultUser(email));
        }

        LogUtil.info(getClass().getName(), "Email from [" + email + "] is recognized as [" + String.join(";", usernames) + "]");

        return usernames;
    }


    private String getDefaultUser(String email) {
        return directoryManager.getUserList(email.replaceAll("@.+", ""), null, null, null, null)
                .stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst()
                .map(User::getUsername)
                .orElse(DirectoryUtil.ROLE_ANONYMOUS);
    }

    public void setWorkflowManager(WorkflowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    public void setWorkflowUserManager(WorkflowUserManager workflowUserManager) {
        this.workflowUserManager = workflowUserManager;
    }

    public void setDirectoryManager(DirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }

    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public void setAppDefinitionDao(AppDefinitionDao appDefinitionDao) {
        this.appDefinitionDao = appDefinitionDao;
    }

    public void setWorkflowHelper(WorkflowHelper workflowHelper) {
        this.workflowHelper = workflowHelper;
    }
}
