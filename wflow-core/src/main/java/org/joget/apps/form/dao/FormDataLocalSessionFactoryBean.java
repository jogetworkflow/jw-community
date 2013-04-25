package org.joget.apps.form.dao;

import org.hibernate.cfg.Configuration;
import org.joget.commons.util.LogUtil;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

public class FormDataLocalSessionFactoryBean extends LocalSessionFactoryBean {

    private FormDataDao dao;

    @Override
    protected void postProcessMappings(Configuration config) {
        try {
            dao.customizeConfiguration(config);
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
    }

    public FormDataDao getDao() {
        return dao;
    }

    public void setDao(FormDataDao dao) {
        this.dao = dao;
    }
}
