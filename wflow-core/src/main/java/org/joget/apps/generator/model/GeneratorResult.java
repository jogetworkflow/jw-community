package org.joget.apps.generator.model;

/**
 * Result of a generator
 * 
 */
public class GeneratorResult {
    private boolean error = false;
    private String message;
    private String itemId;

    /**
     * Flag to indicate an error
     * @return 
     */
    public boolean isError() {
        return error;
    }

    /**
     * Sets a flag to indicate an error
     * @param error 
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * Gets the message of the generation outcome
     * @return 
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message of the generation outcome
     * @param message 
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the id of the generated item for other generator to reuse
     * @return 
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * Stes the id of the generated item for other generator to reuse
     * @param itemId 
     */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}
