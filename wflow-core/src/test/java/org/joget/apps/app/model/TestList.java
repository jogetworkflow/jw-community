package org.joget.apps.app.model;

import static java.lang.System.out;
import java.util.HashMap;
import java.util.Map;
import org.joget.apps.datalist.service.DataListDecorator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class TestList {
    
    @Test
    public void testListDecoratorGenerateLink() {
        //prepare data
        Map<String, String> rowObj = new HashMap<String, String>();
        rowObj.put("id", "123456");
        rowObj.put("firstName", "Jessey");
        rowObj.put("lastName", "Ho");
        
        //when without label
        String href= "?";
        String link = DataListDecorator.generateLink(rowObj, href, "_self", "d-2234-checkbox_id", "id", "", "", "");
        Assert.assertEquals(link, "");
        
        //when without href
        link = DataListDecorator.generateLink(rowObj, null, "_self", "d-2234-checkbox_id", "id", "Label", "", "");
        Assert.assertEquals(link, "Label");
        
        //normal row action link with label
        link = DataListDecorator.generateLink(rowObj, href, "_self", "d-2234-checkbox_id", "id", "Label", "", "");
        Assert.assertEquals(link, "<a href=\"?d-2234-checkbox_id=123456\" target=\"_self\" class=\"\">Label</a>");
        
        //normal row action link with label & confirmation
        link = DataListDecorator.generateLink(rowObj, href, "_self", "d-2234-checkbox_id", "id", "Label", "Are you sure?", "");
        Assert.assertEquals(link, "<a href=\"?d-2234-checkbox_id=123456\" target=\"_self\" onclick=\"return confirm('Are you sure?')\" class=\"\">Label</a>");
        
        //normal row action link with label & css class
        link = DataListDecorator.generateLink(rowObj, href, "_self", "d-2234-checkbox_id", "id", "Label", "", "btn-primary btn-sm");
        Assert.assertEquals(link, "<a href=\"?d-2234-checkbox_id=123456\" target=\"_self\" class=\"btn-primary btn-sm\">Label</a>");
        
        //normal row action link with label, confirmation & css class
        link = DataListDecorator.generateLink(rowObj, href, "_self", "d-2234-checkbox_id", "id", "Label", "Are you sure?", "btn-primary btn-sm");
        Assert.assertEquals(link, "<a href=\"?d-2234-checkbox_id=123456\" target=\"_self\" onclick=\"return confirm('Are you sure?')\" class=\"btn-primary btn-sm\">Label</a>");
        
        //row action link with query string with label, confirmation & css class
        href= "?_listId=list_sublist&appVersion=1&_lid=f1&d-2234-ac=rowAction_1&d-2234-checkbox_id=5566";
        link = DataListDecorator.generateLink(rowObj, href, "_self", "d-2234-checkbox_id", "id", "Label", "Are you sure?", "btn-primary btn-sm");
        Assert.assertEquals(link, "<a href=\"?_listId=list_sublist&amp;appVersion=1&amp;_lid=f1&amp;d-2234-ac=rowAction_1&amp;d-2234-checkbox_id=123456\" target=\"_self\" onclick=\"return confirm('Are you sure?')\" class=\"btn-primary btn-sm\">Label</a>");
        
        //row action link with url & query string with label, confirmation & css class
        href= "testinglink?_listId=list_sublist&appVersion=1&_lid=f1&d-2234-ac=rowAction_1&d-2234-checkbox_id=5566";
        link = DataListDecorator.generateLink(rowObj, href, "_self", "d-2234-checkbox_id", "id", "Label", "Are you sure?", "btn-primary btn-sm");
        Assert.assertEquals(link, "<a href=\"testinglink?_listId=list_sublist&amp;appVersion=1&amp;_lid=f1&amp;d-2234-ac=rowAction_1&amp;d-2234-checkbox_id=123456\" target=\"_self\" onclick=\"return confirm('Are you sure?')\" class=\"btn-primary btn-sm\">Label</a>");
        
        //multiple hrefParam
        link = DataListDecorator.generateLink(rowObj, href, "_self", "d-2234-checkbox_id;firstName;lastName", "id;firstName;lastName", "Label", "Are you sure?", "btn-primary btn-sm");
        Assert.assertEquals(link, "<a href=\"testinglink?_listId=list_sublist&amp;firstName=Jessey&amp;lastName=Ho&amp;appVersion=1&amp;_lid=f1&amp;d-2234-ac=rowAction_1&amp;d-2234-checkbox_id=123456\" target=\"_self\" onclick=\"return confirm('Are you sure?')\" class=\"btn-primary btn-sm\">Label</a>");
        
        //variable support
        link = DataListDecorator.generateLink(rowObj, href, "_self", "d-2234-checkbox_id;custom", "id;{firstName} {lastName}", "Label", "Are you sure?", "btn-primary btn-sm");
        Assert.assertEquals(link, "<a href=\"testinglink?_listId=list_sublist&amp;appVersion=1&amp;_lid=f1&amp;d-2234-ac=rowAction_1&amp;custom=Jessey+Ho&amp;d-2234-checkbox_id=123456\" target=\"_self\" onclick=\"return confirm('Are you sure?')\" class=\"btn-primary btn-sm\">Label</a>");
        
        //target is popup
        href= "testinglink?d-2234-ac=rowAction_1";
        link = DataListDecorator.generateLink(rowObj, href, "popup", "d-2234-checkbox_id", "id", "Label", "Are you sure?", "btn-primary btn-sm");
        Assert.assertEquals(link, "<a href=\"testinglink?d-2234-ac=rowAction_1&amp;d-2234-checkbox_id=123456\"onclick=\"return dlPopupAction(this, 'Are you sure?')\" class=\"btn-primary btn-sm\">Label</a>");
        
        //target is popup without confirmation
        link = DataListDecorator.generateLink(rowObj, href, "popup", "d-2234-checkbox_id", "id", "Label", "", "btn-primary btn-sm");
        Assert.assertEquals(link, "<a href=\"testinglink?d-2234-ac=rowAction_1&amp;d-2234-checkbox_id=123456\"onclick=\"return dlPopupAction(this, '')\" class=\"btn-primary btn-sm\">Label</a>");
        
        //target is post
        link = DataListDecorator.generateLink(rowObj, href, "post", "d-2234-checkbox_id", "id", "Label", "Are you sure?", "btn-primary btn-sm");
        Assert.assertEquals(link, "<a href=\"testinglink?d-2234-ac=rowAction_1&amp;d-2234-checkbox_id=123456\"onclick=\"return dlPostAction(this, 'Are you sure?')\" class=\"btn-primary btn-sm\">Label</a>");
       
        //target is post without confirmation
        link = DataListDecorator.generateLink(rowObj, href, "post", "d-2234-checkbox_id", "id", "Label", "", "btn-primary btn-sm");
        Assert.assertEquals(link, "<a href=\"testinglink?d-2234-ac=rowAction_1&amp;d-2234-checkbox_id=123456\"onclick=\"return dlPostAction(this, '')\" class=\"btn-primary btn-sm\">Label</a>");
    }
}
