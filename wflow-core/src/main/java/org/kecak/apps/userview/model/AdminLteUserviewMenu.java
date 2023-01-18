package org.kecak.apps.userview.model;

import org.joget.apps.userview.model.UserviewMenu;

/**
 * @author aristo
 *
 * Bootstrap AdminLTE Theme for {@link UserviewMenu}
 */
public interface AdminLteUserviewMenu extends BootstrapUserviewMenu {
    String getAdminLteJspPage(BootstrapUserviewTheme theme);

    String getAdminLteRenderPage();

    String getAdminLteDecoratedMenu();
}
