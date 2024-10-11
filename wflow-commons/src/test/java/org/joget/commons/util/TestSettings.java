package org.joget.commons.util;

import java.util.Collection;
import org.joget.commons.spring.model.Setting;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test Setting CRUD and setup manager helper
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testCommonsApplicationContext.xml"})
public class TestSettings {
    
    @Autowired
    SetupDao setupDao;
    
    @Autowired
    SetupManager setupManager;
    
    @Autowired
    SetupManagerHelper setupManagerHelper;
    
    @Test
    @Transactional
    public void testSetupManagerHelper() throws Exception {
        //make sure it start clean
        cleanProperty("testingSetting");
        
        Assert.assertTrue(((TestSetupManagerHelperImpl) setupManagerHelper).audits.isEmpty());
        Assert.assertTrue(((TestSetupManagerHelperImpl) setupManagerHelper).cacheSettingMaps.isEmpty());
        
        //retrive dummy setting
        String testingSetting = setupManager.getSettingValue("testingSetting");
        Assert.assertTrue(!((TestSetupManagerHelperImpl) setupManagerHelper).cacheSettingMaps.isEmpty());
        Assert.assertNull(testingSetting);
        
        //add setting
        Setting setting = new Setting();
        setting.setProperty("testingSetting");
        setting.setValue("1");
        setupManager.saveSetting(setting);
        
        //check audit trail
        Assert.assertTrue(((TestSetupManagerHelperImpl) setupManagerHelper).audits.containsKey("testingSetting"));
        
        //retrieve again
        testingSetting = setupManager.getSettingValue("testingSetting");
        Assert.assertTrue(testingSetting != null && testingSetting.equals("1"));
        
        //check SetupManagerHelper.checkSettingChanges is triggerred with new value
        Assert.assertTrue(((TestSetupManagerHelperImpl) setupManagerHelper).cacheSettingMaps.containsKey("testingSetting"));
        Assert.assertTrue(((TestSetupManagerHelperImpl) setupManagerHelper).cacheSettingMaps.get("testingSetting").getValue().equals("1"));
        
        //delete the setting
        setupManager.deleteSetting("testingSetting");
        testingSetting = setupManager.getSettingValue("testingSetting");
        Assert.assertNull(testingSetting);
        
        //check SetupManagerHelper.auditSettingChange is with null valye
        Assert.assertTrue(((TestSetupManagerHelperImpl) setupManagerHelper).audits.get("testingSetting").getValue() == null);
    }
    
    
    @Test
    @Transactional
    public void testSetupDao() throws Exception {
        //make sure it start clean
        cleanProperty("testSetupDao");
        
        //create
        Setting setting = new Setting();
        setting.setProperty("testSetupDao");
        setting.setValue("1");
        setting = (Setting) setupDao.save(setting);
        
        //Retrieve, check value is saved
        setting = (Setting) setupDao.find(setting.getId());
        Assert.assertTrue(setting != null && setting.getValue().equals("1"));
        
        //update
        setting.setValue("2");
        setupDao.saveOrUpdate(setting);
        
        //Retrieve, check value is saved
        setting = (Setting) setupDao.find(setting.getId());
        Assert.assertTrue(setting != null && setting.getValue().equals("2"));
        
        //Retrieve with conditions
        Collection settings = setupDao.find("where property=?", new Object[]{"testSetupDao"}, null, null, null, null);
        Assert.assertTrue(!settings.isEmpty());
        
        //Delete
        setupDao.delete(setting);
        setting = (Setting) setupDao.find(setting.getId());
        Assert.assertTrue(setting == null);
    }
    
    private void cleanProperty(String property) {
        Collection<Setting> found = setupDao.find("where property=?", new Object[]{property}, null, null, null, null);
        if (found != null && !found.isEmpty()) {
            for (Setting f : found) {
                setupDao.delete(f);
            }
        }
    }
}
