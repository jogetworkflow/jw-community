package org.kecak.apps.userview.model;

import org.joget.apps.userview.model.UserviewMenu;

/**
 * @author Yonathan
 *
 * Bootstrap AdminLTE Theme for {@link UserviewMenu}
 */
public interface AdminKitUserviewMenu extends BootstrapUserviewMenu {
    String getAdminKitJspPage(BootstrapUserviewTheme theme);

    String getAdminKitRenderPage();

    String getAdminKitDecoratedMenu();
}
