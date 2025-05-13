package org.joget.apps.app.model;

import java.io.IOException;
import junit.framework.Assert;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.StringUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class TestHashVariable {
    
    public static String html;
    public static String sql;
    public static String js;
    public static String css;
    public static String java;
    public static String doubleQuate;
    public static String specialChars = "!@#$%^&*()_+-={}|[]\\:\";',./<>?\t\r\n`~";
    public static String separated = "Joget Inc.;<ABC> Inc.;Jack & Co.";
    public static String nl2br = "Joget Inc.\n<ABC> Inc.\nJack & Co.";
    public static String jsEscapeString = "This is a simple string with '\\' and '\\/' char.";
    public static String escaped_html;
    public static String escaped_sql;
    public static String escaped_js;
    public static String escaped_java;
    public static String escaped_specialChars = "!@#$%^&amp;*()_+-={}|[]\\:\";',./&lt;&gt;? `~";
    public static String escaped_doubleQuate;
    
    @Before
    public void setup() throws IOException {
        AppDefinition appDef = TestUtil.createAppDefinition("testHashVariable", 1l);
        AppUtil.setCurrentAppDefinition(appDef);
        
        //html
        html = TestUtil.readFile("/envVariable/html.txt").trim();
        escaped_html = TestUtil.readFile("/envVariable/escaped_html.txt").trim();
        TestUtil.createEnvVariable(appDef, "html", html);
        
        //sql
        sql = TestUtil.readFile("/envVariable/sql.txt").trim();
        escaped_sql = TestUtil.readFile("/envVariable/escaped_sql.txt").trim();
        TestUtil.createEnvVariable(appDef, "sql", sql);
        
        //js
        js = TestUtil.readFile("/envVariable/js.txt").trim();
        escaped_js = TestUtil.readFile("/envVariable/escaped_js.txt").trim();
        TestUtil.createEnvVariable(appDef, "js", js);
        
        //css
        css = TestUtil.readFile("/envVariable/css.txt").trim();
        TestUtil.createEnvVariable(appDef, "css", css);
        
        //java
        java = TestUtil.readFile("/envVariable/java.txt").trim();
        escaped_java = TestUtil.readFile("/envVariable/escaped_java.txt").trim();
        TestUtil.createEnvVariable(appDef, "java", java);
        
        //double quote
        doubleQuate = TestUtil.readFile("/envVariable/doubleQuote.txt").trim();
        escaped_doubleQuate = TestUtil.readFile("/envVariable/escaped_doubleQuote.txt").trim();
        TestUtil.createEnvVariable(appDef, "doubleQuote", escaped_doubleQuate);
        
        //special chars
        TestUtil.createEnvVariable(appDef, "specialChars", specialChars);
        
        //separated
        TestUtil.createEnvVariable(appDef, "separated", separated);
        
        //nl2br
        TestUtil.createEnvVariable(appDef, "nl2br", nl2br);
        
        //JS escape char string
        TestUtil.createEnvVariable(appDef, "jsEscapeString", jsEscapeString);
        
        TestUtil.createEnvVariable(appDef, "type", "html");
    }
    
    @Test
    public void testHashVariableWithJavascriptEscapeChars() {
        Assert.assertEquals("test\'test", AppUtil.processHashVariable("#exp.'test\''' + 'test'#", null, null, null));
        Assert.assertEquals("\u663e\u793a\u8865\u5145\u4fe1\u606f", AppUtil.processHashVariable("#exp.'\u663e\u793a\u8865\u5145\u4fe1\u606f'#", null, null, null));
        Assert.assertEquals(jsEscapeString + "\\\r\n" + jsEscapeString, AppUtil.processHashVariable("#exp.'{envVariable.jsEscapeString?expression}' + '\\\r\n' + '{envVariable.jsEscapeString?expression}'#", null, null, null));
        Assert.assertEquals("line 1\\\r\nline 2", AppUtil.processHashVariable("#exp.'line 1' + '\\\r\n' + 'line 2'#", null, null, null));
        Assert.assertEquals(escaped_specialChars + "\\ " + escaped_specialChars, AppUtil.processHashVariable("#exp.'{envVariable.specialChars?expression}' + '\\\r\n' + '{envVariable.specialChars?expression}'#", null, null, null));
        
        Assert.assertEquals("{\"value\" : \"" + StringUtil.escapeString("test\'test", StringUtil.TYPE_JSON, null) + "\"}", AppUtil.processHashVariable("{\"value\" : \"" + StringUtil.escapeString("#exp.'test\''' + 'test'#", StringUtil.TYPE_JSON, null) + "\"}", null, StringUtil.TYPE_JSON, null));
        Assert.assertEquals("{\"value\" : \"" + StringUtil.escapeString("\u663e\u793a\u8865\u5145\u4fe1\u606f", StringUtil.TYPE_JSON, null) + "\"}", AppUtil.processHashVariable("{\"value\" : \"" + StringUtil.escapeString("#exp.'\u663e\u793a\u8865\u5145\u4fe1\u606f'#", StringUtil.TYPE_JSON, null) + "\"}", null, StringUtil.TYPE_JSON, null));
        Assert.assertEquals("{\"value\" : \"" + StringUtil.escapeString(jsEscapeString + "\\\r\n" + jsEscapeString, StringUtil.TYPE_JSON, null) + "\"}", AppUtil.processHashVariable("{\"value\" : \"" + StringUtil.escapeString("#exp.'{envVariable.jsEscapeString?expression}' + '\\\r\n' + '{envVariable.jsEscapeString?expression}'#", StringUtil.TYPE_JSON, null) + "\"}", null, StringUtil.TYPE_JSON, null));
        Assert.assertEquals("{\"value\" : \"" + StringUtil.escapeString("line 1\\\r\nline 2", StringUtil.TYPE_JSON, null) + "\"}", AppUtil.processHashVariable("{\"value\" : \"" + StringUtil.escapeString("#exp.'line 1' + '\\\r\n' + 'line 2'#", StringUtil.TYPE_JSON, null) + "\"}", null, StringUtil.TYPE_JSON, null));
        Assert.assertEquals("{\"value\" : \"" + StringUtil.escapeString(escaped_specialChars + "\\ " + escaped_specialChars, StringUtil.TYPE_JSON, null) + "\"}", AppUtil.processHashVariable("{\"value\" : \"" + StringUtil.escapeString("#exp.'{envVariable.specialChars?expression}' + '\\\r\n' + '{envVariable.specialChars?expression}'#", StringUtil.TYPE_JSON, null) + "\"}", null, StringUtil.TYPE_JSON, null));
    }
    
    @Test
    public void testEscape() {
        Assert.assertEquals(escaped_html, AppUtil.processHashVariable("#envVariable.html#", null, null, null));
        Assert.assertEquals(escaped_sql, AppUtil.processHashVariable("#envVariable.sql#", null, null, null));
        Assert.assertEquals(escaped_js, AppUtil.processHashVariable("#envVariable.js#", null, null, null));
        Assert.assertEquals(css, AppUtil.processHashVariable("#envVariable.css#", null, null, null));
        Assert.assertEquals(escaped_java, AppUtil.processHashVariable("#envVariable.java#", null, null, null));
        Assert.assertEquals(escaped_specialChars, AppUtil.processHashVariable("#envVariable.specialChars#", null, null, null));
        Assert.assertEquals("Joget Inc.; Inc.;Jack &amp; Co.", AppUtil.processHashVariable("#envVariable.separated#", null, null, null));
        Assert.assertEquals("Joget Inc.  Inc. Jack &amp; Co.", AppUtil.processHashVariable("#envVariable.nl2br#", null, null, null));
    }
    
    @Test
    public void testNoEscape() {
        Assert.assertEquals(html, AppUtil.processHashVariable("#envVariable.html?noescape#", null, null, null));
        Assert.assertEquals(sql, AppUtil.processHashVariable("#envVariable.sql?noescape#", null, null, null));
        Assert.assertEquals(js, AppUtil.processHashVariable("#envVariable.js?noescape#", null, null, null));
        Assert.assertEquals(css, AppUtil.processHashVariable("#envVariable.css?noescape#", null, null, null));
        Assert.assertEquals(java, AppUtil.processHashVariable("#envVariable.java?noescape#", null, null, null));
        Assert.assertEquals(specialChars, AppUtil.processHashVariable("#envVariable.specialChars?noescape#", null, null, null));
        Assert.assertEquals(separated, AppUtil.processHashVariable("#envVariable.separated?noescape#", null, null, null));
        Assert.assertEquals(nl2br, AppUtil.processHashVariable("#envVariable.nl2br?noescape#", null, null, null));
    }
    
    @Test
    public void testEscapeWithFormat() {
        Assert.assertEquals("Joget Inc.,  Inc., Jack &amp; Co.", AppUtil.processHashVariable("#envVariable.separated?separator(, )#", null, null, null));
        Assert.assertEquals("Joget Inc.<br class=\"nl2br\" /> Inc.<br class=\"nl2br\" />Jack &amp; Co.", AppUtil.processHashVariable("#envVariable.nl2br?nl2br#", null, null, null));
    }
    
    @Test
    public void testNoEscapeWithFormat() {
        Assert.assertEquals("Joget Inc., <ABC> Inc., Jack & Co.", AppUtil.processHashVariable("#envVariable.separated?separator(, );java#", null, null, null));
        Assert.assertEquals("Joget Inc., <ABC> Inc., Jack & Co.", AppUtil.processHashVariable("#envVariable.separated?separator(, );noescape#", null, null, null));
        Assert.assertEquals("Joget Inc., &lt;ABC&gt; Inc., Jack &amp; Co.", AppUtil.processHashVariable("#envVariable.separated?separator(, );html#", null, null, null));
        Assert.assertEquals("Joget Inc.<br class=\\\"nl2br\\\" /><ABC> Inc.<br class=\\\"nl2br\\\" />Jack & Co.", AppUtil.processHashVariable("#envVariable.nl2br?nl2br;java#", null, null, null));
        Assert.assertEquals("Joget Inc.<br class=\"nl2br\" /><ABC> Inc.<br class=\"nl2br\" />Jack & Co.", AppUtil.processHashVariable("#envVariable.nl2br?nl2br;noescape#", null, null, null));
        Assert.assertEquals("Joget Inc.<br class=\"nl2br\" />&lt;ABC&gt; Inc.<br class=\"nl2br\" />Jack &amp; Co.", AppUtil.processHashVariable("#envVariable.nl2br?html;nl2br#", null, null, null));
    }
    
    @Test
    public void testNestedEscape() {
        Assert.assertEquals(escaped_html, AppUtil.processHashVariable("#exp.'{envVariable.type}'.equals('html')?'{envVariable.html?expression}':'{envVariable.java?expression}'#", null, null, null));
        Assert.assertEquals(escaped_java, AppUtil.processHashVariable("#exp.'{envVariable.type}'.equals('java')?'{envVariable.html?expression}':'{envVariable.java?expression}'#", null, null, null));
    }
    
    @Test
    public void testNestedWithFormat() {
        Assert.assertEquals(html, AppUtil.processHashVariable("#exp.'{envVariable.type}'.equals('html')?'{envVariable.html?expression}':'{envVariable.java?expression}'?noescape#", null, null, null));
        Assert.assertEquals(java, AppUtil.processHashVariable("#exp.'{envVariable.type}'.equals('java')?'{envVariable.html?expression}':'{envVariable.java?expression}'?noescape#", null, null, null));
    }
    
    @Test
    public void testDoubleQuote() {
        Assert.assertEquals(escaped_doubleQuate, AppUtil.processHashVariable("#envVariable.doubleQuote#", null, null, null));
    }
      
    @After
    public void clean() {
        TestUtil.deleteAllVersions("testHashVariable");
    }
}
