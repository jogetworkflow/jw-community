package org.kecak.apps.form.model;

import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;

/**
 * @author aristo
 *
 * Indicator for element that has multi values
 */
@Deprecated
public interface MultiValueElement {
    /**
     * Method to retrieve element value from form data ready to be shown to UI.
     * You can override this to use your own value formatting.
     *
     * @param formData
     * @return
     */
    default String[] getElementValues(FormData formData) {
        assert this instanceof Element;
        return FormUtil.getElementPropertyValues((Element) this, formData);
    }
}
