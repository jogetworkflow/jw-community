package org.joget.apps.app.model;

import junit.framework.Assert;
import org.joget.apps.app.service.MarketplaceUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class TestMarketplace {
    
    
    @Test
    public void testCompareVersion() {
        //test equals
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0.0", "9.0.0"), 0);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0.1", "9.0.1"), 0);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0-SNAPSHOT", "9.0-SNAPSHOT"), 0);
        
        //test having update
        Assert.assertEquals(MarketplaceUtil.compareVersion("8.0.0", "9.0.1"), -1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0.0", "9.0.1"), -1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0.1", "9.1.1"), -1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0.1.1", "9.0.2"), -1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0-SNAPSHOT", "9.0.0"), -1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0-BETA", "9.0.0"), -1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0-SNAPSHOT", "9.0-BETA"), -1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0-BETA", "9.0-BETA1"), -1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0-BETA1", "9.0-BETA2"), -1);
        
        //test latest
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0.0", "8.0.1"), 1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0.1", "9.0.0"), 1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.1.1", "9.1.0"), 1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0.2", "9.0.1.1"), 1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0.0", "9.0-SNAPSHOT"), 1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0.0", "9.0-BETA"), 1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0-BETA", "9.0-SNAPSHOT"), 1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0-BETA1", "9.0-BETA"), 1);
        Assert.assertEquals(MarketplaceUtil.compareVersion("9.0-BETA2", "9.0-BETA1"), 1);
    }
    
}
