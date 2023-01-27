package org.kecak.apps.userview.model;

import org.joget.apps.userview.model.UserviewTheme;

/**
 * @author aristo
 *
 * Theming for mobile application. This interface should be implemented in {@link UserviewTheme}
 * or its children
 */
public interface MobileApplicationTheme {
    /**
     * Color for:
     *  <li>Application Bar</li>
     *  <li>Button</li>
     */
    String getMobilePrimaryColor();

    /**
     * Color for:
     *  <li></li>
     *
     * @return
     */
    String getMobileAccentColor();

    /**
     * Color for app's background
     *
     * @return
     */
    String getMobileBackgroundColor();

    /**
     * Color for text in:
     *  <li>Navigation drawer</li>
     *  <li>Element text</li>
     *
     * @return
     */
    String getMobilePrimaryFontColor();

    /**
     * Color for:
     *  <li>Application Bar's title</li>
     *
     * @return
     */
    String getMobileSecondaryFontColor();
}