package org.joget.governance.model;

/**
 * Represents the result of performing an {@link GovHealthCheckAction}.
 * <p>
 * The {@code GovHealthCheckActionResult} encapsulates success or failure information,
 * an optional message, redirect URL, and remove the scan result detail.
 * </p>
 */
public class GovHealthCheckActionResult {
    private boolean success;
    private boolean removeFromResult;
    private String message;
    private String redirectUrl;

    /**
     * Creates a new {@code ActionResult}.
     *
     * @param success whether the action succeeded
     * @param removeFromResult whether to remove the scanned detail from the result
     * @param message an optional message describing the result
     */
    public GovHealthCheckActionResult(boolean success, boolean removeFromResult, String message) {
        this.success = success;
        this.removeFromResult = removeFromResult;
        this.message = message;
    }

    /**
     * Creates a successful {@code ActionResult}.
     *
     * @param removeFromResult whether to remove the scanned detail from the result
     * @param message the success message
     * @return a new {@code ActionResult} instance
     */
    public static GovHealthCheckActionResult success(boolean removeFromResult, String message) {
        return new GovHealthCheckActionResult(true, removeFromResult, message);
    }

    /**
     * Creates a failed {@code ActionResult}.
     *
     * @param removeFromResult whether to remove the scanned detail from the result
     * @param message the error message
     * @return a new {@code ActionResult} instance
     */
    public static GovHealthCheckActionResult fail(boolean removeFromResult, String message) {
        return new GovHealthCheckActionResult(false, removeFromResult, message);
    }

    /**
     * Indicates whether the action succeeded.
     *
     * @return {@code true} if successful; {@code false} otherwise
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Indicates whether to remove the scanned detail from the result
     *
     * @return {@code true} to remove; {@code false} otherwise
     */
    public boolean isRemoveFromResult() {
        return removeFromResult;
    }

    /**
     * Returns the result message.
     *
     * @return the message text
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the redirect URL (if any).
     *
     * @return the redirect URL, or {@code null} if not defined
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * Sets the redirect URL for this result.
     *
     * @param redirectUrl the redirect URL
     */
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
