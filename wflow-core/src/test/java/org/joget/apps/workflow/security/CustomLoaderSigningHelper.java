package org.joget.apps.workflow.security;

import org.springframework.security.core.Authentication;

/**
 * A plain helper that reaches the {@link AuthenticationTokenWrapper} signing constructor.
 *
 * <p>Its point is not what it does but where it can be DEFINED. Redefined through a classloader of
 * an attacker's own making -- {@code new ClassLoader(null)} that hand-delegates the shared types --
 * it keeps this name and still calls the same constructor, while its defining loader matches no
 * known Felix loader-class-name pattern and has no parent to walk. A "does the loader look like a
 * bundle loader" test judges it trusted; a "is the loader in our own delegation chain" test does
 * not.
 *
 * <p>Top-level and public so it can be redefined and invoked reflectively; not {@code Test*} so
 * surefire ignores it.
 */
public class CustomLoaderSigningHelper {

    public Object sign(Authentication auth) {
        return new AuthenticationTokenWrapper(auth);
    }
}
