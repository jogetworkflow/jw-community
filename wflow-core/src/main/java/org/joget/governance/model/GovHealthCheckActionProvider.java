package org.joget.governance.model;

import java.util.List;

/**
 * Defines a GovHealthCheck that can provide one or more {@link GovHealthCheckAction}s.
 * <p>
 * Implementations typically return a list of actions that can be rendered
 * as clickable buttons in the Gov Health Check details.
 * Each {@link GovHealthCheckAction} defines its own behavior when clicked.
 * </p>
 *
 * <p>
 * Example:
 * <pre>{@code
 * public class MyActionPlugin extends GovHealthCheckAbstract implements GovHealthCheckActionProvider {
 *     @Override
 *     public List<GovHealthCheckAction> getActions() {
 *         return List.of(
 *             new GovHealthCheckAction("remove", "Remove", "fas fa-trash-alt", "Remove this risk",
 *                  (action, request, response) -> ActionResult.success(true, "Removed successfully!")
 *             )
 *         );
 *     }
 * }
 * }</pre>
 * </p>
 */
public interface GovHealthCheckActionProvider {
    
    /**
     * Returns a list of available {@link GovHealthCheckAction}s.
     *
     * @return a list of actions that can be rendered for each scanned result details
     */
    List<GovHealthCheckAction> getActions();
}
