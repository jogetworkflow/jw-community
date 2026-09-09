package org.joget.apps.workflow.security;

import org.springframework.security.core.Authentication;

/**
 * Stands in for enterprise's {@code LicenseAutheticationTokenWrapper}: a public subclass of
 * {@link AuthenticationTokenWrapper} with a public constructor whose only job is
 * {@code super(auth)}.
 *
 * <p>Its reason to exist is the frame it contributes. A stack check that looks for the first frame
 * whose declaring class is not exactly {@code AuthenticationTokenWrapper} would mistake this
 * subclass' own {@code <init>} frame for "the caller", find it trusted (webapp classloader), and
 * never inspect the real originator one frame further up. That would let any BeanShell or OSGI
 * caller mint a validly signed wrapper simply by going through the subclass.
 *
 * <p>Top-level and public on purpose so a BeanShell script can name it. Deliberately NOT named
 * {@code Test*} so surefire does not try to run it.
 */
public class SigningShieldSubclassWrapper extends AuthenticationTokenWrapper {

    public SigningShieldSubclassWrapper(Authentication auth) {
        super(auth);
    }
}
