package org.joget.apps.app.service;

import org.joget.commons.util.SetupManager;

/**
 * {@link Deprecated}
 *
 * Use {@link org.kecak.apps.app.service.AuthTokenService}
 */
@Deprecated
public class AuthTokenService extends org.kecak.apps.app.service.AuthTokenService {
    public AuthTokenService(SetupManager setupManager) {
        super(setupManager);
    }
}