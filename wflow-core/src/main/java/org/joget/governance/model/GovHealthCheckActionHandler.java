package org.joget.governance.model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Functional interface representing the logic to be executed
 * when a {@link GovHealthCheckAction} is performed.
 * <p>
 * Implementations can be provided as lambda expressions or anonymous classes.
 * </p>
 *
 * <p>
 * Example:
 * <pre>{@code
 * GovHealthCheckActionHandler handler = (action, request, response) -> {
 *     // custom logic here
 *     return GovHealthCheckActionResult.success("Action executed successfully");
 * };
 * }</pre>
 * </p>
 */
@FunctionalInterface
public interface GovHealthCheckActionHandler {
    
    /**
     * Handles the logic when a {@link GovHealthCheckAction} is performed.
     *
     * @param action   the {@link GovHealthCheckAction} that was triggered
     * @param pluginClass the {@link GovHealthCheck} plugin class that having this action
     * @param detail the scanned detail to perform action
     * @param request  the current HTTP request
     * @param response the current HTTP response
     * @return an {@link GovHealthCheckActionResult} representing the result of the action, 
     * return null mean the HTTP response is handled by the function and no further action needed.
     */
    GovHealthCheckActionResult handle(GovHealthCheckAction action, String pluginClass, String detail, HttpServletRequest request, HttpServletResponse response);
}
