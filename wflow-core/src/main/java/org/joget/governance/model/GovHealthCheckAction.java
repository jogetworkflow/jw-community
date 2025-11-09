package org.joget.governance.model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;

/**
 * Represents an actionable button rendered by a Gov Health Check to each scanned result.
 * <p>
 * A {@code GovHealthCheckAction} defines both the metadata (such as label, icon)
 * and the logic that should be executed when the button is clicked.
 * Each action holds its own {@link GovHealthCheckActionHandler}, making it fully
 * self-contained and easy to register dynamically.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * new GovHealthCheckAction("remove", "Remove", "fas fa-trash-alt", "Remove this risk",
 *     (action, request, response) -> ActionResult.success(true, "Removed successfully!")
 * );
 * }</pre>
 * </p>
 */
public class GovHealthCheckAction {
    
    private String id;
    private String label;
    private String icon;
    private String confirmMessage;
    private Boolean popup;
    
    // The logic for what happens when this action is performed
    private GovHealthCheckActionHandler handler;

    public GovHealthCheckAction(String id, String label, String icon, GovHealthCheckActionHandler handler) {
        this(id, label, icon, null, false, handler);
    }
    
    public GovHealthCheckAction(String id, String label, String icon, String confirmMessage, Boolean popup, GovHealthCheckActionHandler handler) {
        this.id = id;
        this.label = label;
        this.icon = icon;
        this.confirmMessage = confirmMessage;
        this.popup = popup;
        this.handler = handler;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public String getLabel() { return label; }
    public String getIcon() { return icon; }
    public String getConfirmMessage() { return confirmMessage; }
    public Boolean getPopup() { return popup; }
    
    /**
     * Executes this action by invoking its {@link GovHealthCheckActionHandler}.
     *
     * @param pluginClass the {@link GovHealthCheck} plugin class that having this action
     * @param detail the scanned detail to perform action
     * @param request  the current HTTP request
     * @param response the current HTTP response
     * @return the {@link GovHealthCheckActionResult} returned by the handler
     */
    public GovHealthCheckActionResult perform(String pluginClass, String detail, HttpServletRequest request, HttpServletResponse response) {
        if (handler != null) {
            return handler.handle(this, pluginClass, detail, request, response);
        }
        LogUtil.debug(GovHealthCheckAction.class.getName(), "No handler defined for action: " + id);
        return GovHealthCheckActionResult.fail(false, ResourceBundleUtil.getMessage("console.governance.actionNotFound"));
    }
}
