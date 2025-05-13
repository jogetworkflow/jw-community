package org.joget.governance.lib;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.joget.apps.app.service.RegexMatchesFunctionResolver;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class TestOrphanedFormDataCheck {
    
    public final static String quotedKeyword = "quotedTN";
    public final static String hashKeywordNested = "hashNestedTN";
    public final static String hashKeywordEscape = "hashEscapeTN";
    public final static String hashKeywordDot = "hashDotTN";
    public final static String hashKeywordParam = "hashParamTN";
    public final static String hashKeyword = "hashTN";
    public final static String beanShellKeyword = "beanshellTN";
    public final static String tableKeyword = "tableTN";
    public final static String formHashKeyword = "formHashTN";
    public final static String notExistKeyword = "notExistTN";
    
    public XPath xpath;
    byte[] defXml;
    
    @Before
    public void setup() throws IOException {
        xpath = XPathFactory.newInstance().newXPath();
        xpath.setXPathFunctionResolver(new RegexMatchesFunctionResolver());
        
        defXml = IOUtils.toByteArray(getClass().getResourceAsStream("/usage.xml"));
    }
    
    // "keyword"
    @Test
    public void testQuotedKeyword() {
        assertTrue(checkKeywordExist(quotedKeyword));
    }
    
    // .keyword}
    @Test
    public void testHashKeywordNested() {
        assertTrue(checkKeywordExist(hashKeywordNested));
    }
    
    // .keyword?
    @Test
    public void testHashKeywordEscape() {
        assertTrue(checkKeywordExist(hashKeywordEscape));
    }
    
    // .keyword.
    @Test
    public void testHashKeywordDot() {
        assertTrue(checkKeywordExist(hashKeywordDot));
    }
    
    // .keyword[
    @Test
    public void testHashKeywordParam() {
        assertTrue(checkKeywordExist(hashKeywordParam));
    }
    
    // .keyword#
    @Test
    public void testHashKeyword() {
        assertTrue(checkKeywordExist(hashKeyword));
    }
    
    // "keyword\"
    @Test
    public void testBeanShellKeyword() {
        assertTrue(checkKeywordExist(beanShellKeyword));
    }
    
    // app_fd_keyword
    @Test
    public void testTableKeyword() {
        assertTrue(checkKeywordExist(tableKeyword));
    }
    
    // form.keyword.
    @Test
    public void testFormHashKeyword() {
        assertTrue(checkKeywordExist(formHashKeyword));
    }
    
    // not exist keyword
    @Test
    public void testNotExistKeyword() {
        assertFalse(checkKeywordExist(notExistKeyword));
    }
    
    public boolean checkKeywordExist(String keyword) {
        Set<String> tables = new HashSet();
        tables.add(keyword);
        
        OrphanedFormDataCheck.checkUsages(defXml, tables, xpath);
        
        return !tables.contains(keyword); //tables not contains the keyword meaning it found in the definition
    }
}
