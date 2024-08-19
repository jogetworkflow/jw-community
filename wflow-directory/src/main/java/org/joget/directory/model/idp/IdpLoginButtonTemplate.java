package org.joget.directory.model.idp;

/**
 * Class to represent an Identity Provider login button template
 */
public class IdpLoginButtonTemplate {
    private final boolean useDefault;
    private final String template;

    /**
     * Create a new login button template.
     * <p>
     * When {@code useDefault = true}, the template renderer will wrap the {@code template} in a {@code <button>} tag
     * with the default button styling.
     * </p>
     * <p>
     * When {@code useDefault = false}, the template renderer will wrap the {@code template} in a {@code <div>} tag,
     * therefore the styling of the button should be considered in the {@code template} itself.
     * </p>
     *
     * @implNote The template renderer will take the {@code template} string as-is and will not escape any HTML content.
     * Extra care and consideration should be taken to ensure that text is properly escaped before passing the template.
     * @param useDefault whether to use a default button
     * @param template   the content of the button (HTML/plain text)
     */
    public IdpLoginButtonTemplate(boolean useDefault, String template) {
        this.useDefault = useDefault;
        this.template = template;
    }

    public boolean isUseDefault() {
        return useDefault;
    }

    public String getTemplate() {
        return template;
    }
}
