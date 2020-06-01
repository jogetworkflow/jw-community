package org.joget.commons.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:commonsApplicationContext.xml"})
public class TestStringUtil {
    
    @Test
    public void testEscapeRegex() throws Exception {
        String original = "\\*+[](){}$.?^|";
        String escaped = "\\\\\\*\\+\\[\\]\\(\\)\\{\\}\\$\\.\\?\\^\\|";
        
        Assert.isTrue(escaped.equals(StringUtil.escapeString(original, StringUtil.TYPE_REGEX, null)));
        Assert.isTrue(original.equals(StringUtil.unescapeString(escaped, StringUtil.TYPE_REGEX, null)));
    }
    
    @Test
    public void testEscapeJSON() throws Exception {
        String original = "this is a string with ' & \"";
        String escaped = "this is a string with ' & \\\"";
        
        Assert.isTrue(escaped.equals(StringUtil.escapeString(original, StringUtil.TYPE_JSON, null)));
        Assert.isTrue(original.equals(StringUtil.unescapeString(escaped, StringUtil.TYPE_JSON, null)));
    }
    
    @Test
    public void testEscapeJS() throws Exception {
        String original = "";
        String escaped = "";
        
        Assert.isTrue(escaped.equals(StringUtil.escapeString(original, StringUtil.TYPE_JAVASCIPT, null)));
        Assert.isTrue(original.equals(StringUtil.unescapeString(escaped, StringUtil.TYPE_JAVASCIPT, null)));
    }
    
    @Test
    public void testEscapeHTML() throws Exception {
        String original = "<i class=\"far fa-dot-circle\"></i> test";
        String escaped = "&lt;i class=&quot;far fa-dot-circle&quot;&gt;&lt;/i&gt; test";
        
        Assert.isTrue(escaped.equals(StringUtil.escapeString(original, StringUtil.TYPE_HTML, null)));
        Assert.isTrue(original.equals(StringUtil.unescapeString(escaped, StringUtil.TYPE_HTML, null)));
    }
    
    @Test
    public void testEscapeXML() throws Exception {
        String original = "<name attr=\"attrvalue\">test</name>";
        String escaped = "&lt;name attr=&quot;attrvalue&quot;&gt;test&lt;/name&gt;";
        
        Assert.isTrue(escaped.equals(StringUtil.escapeString(original, StringUtil.TYPE_XML, null)));
        Assert.isTrue(original.equals(StringUtil.unescapeString(escaped, StringUtil.TYPE_XML, null)));
    }
    
    @Test
    public void testEscapeJava() throws Exception {
        String original = "A java value with ', \", \t & \n";
        String escaped = "A java value with ', \\\", \\t & \\n";
        
        Assert.isTrue(escaped.equals(StringUtil.escapeString(original, StringUtil.TYPE_JAVA, null)));
        Assert.isTrue(original.equals(StringUtil.unescapeString(escaped, StringUtil.TYPE_JAVA, null)));
    }
    
    @Test
    public void testEscapeSQL() throws Exception {
        String original = "A sql value with ' in the string";
        String escaped = "A sql value with '' in the string";
        
        Assert.isTrue(escaped.equals(StringUtil.escapeString(original, StringUtil.TYPE_SQL, null)));
        Assert.isTrue(original.equals(StringUtil.unescapeString(escaped, StringUtil.TYPE_SQL, null)));
    }
    
    @Test
    public void testEscapeURL() throws Exception {
        String original = "a url param with & = ?";
        String escaped = "a+url+param+with+%26+%3D+%3F";
        
        Assert.isTrue(escaped.equals(StringUtil.escapeString(original, StringUtil.TYPE_URL, null)));
        Assert.isTrue(original.equals(StringUtil.unescapeString(escaped, StringUtil.TYPE_URL, null)));
    }
    
    @Test
    public void testEscapeNl2Br() throws Exception {
        String original = "test\r\ntest";
        String escaped = "test<br class=\"nl2br\" />test";
        
        Assert.isTrue(escaped.equals(StringUtil.escapeString(original, StringUtil.TYPE_NL2BR, null)));
        Assert.isTrue(original.equals(StringUtil.unescapeString(escaped, StringUtil.TYPE_NL2BR, null)));
    }
    
    @Test
    public void testEscapeSeparator() throws Exception {
        String original = "value1;value2;valu3;value4";
        String escaped = "value1, value2, valu3, value4";
        
        Assert.isTrue(escaped.equals(StringUtil.escapeString(original, StringUtil.TYPE_SEPARATOR + "(, )", null)));
        Assert.isTrue(original.equals(StringUtil.unescapeString(escaped, StringUtil.TYPE_SEPARATOR + "(, )", null)));
    }
    
    @Test
    public void testEscapeExpression() throws Exception {
        String original = "A value with ' in the string";
        String escaped = "A value with '' in the string";
        
        Assert.isTrue(escaped.equals(StringUtil.escapeString(original, StringUtil.TYPE_EXP, null)));
        Assert.isTrue(original.equals(StringUtil.unescapeString(escaped, StringUtil.TYPE_EXP, null)));
    }
    
    @Test
    public void testEscapeImg2Base64() throws Exception {
        //todo
    }
}
