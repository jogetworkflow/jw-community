package org.kecak.apps.userview.model;

import org.joget.apps.userview.model.UserviewMenu;

/**
 * @author aristo
 *
 * Bootstrap Ace Theme for {@link UserviewMenu}
 */
public interface AceUserviewMenu extends BootstrapUserviewMenu {
    String getAceJspPage(BootstrapUserviewTheme theme);

    String getAceRenderPage();

    String getAceDecoratedMenu();
}