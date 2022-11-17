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
    public void testStripAllHtmlTag() throws Exception {
        //check all tags are removed
        String original = "<div><a>a</a> <span>span</span> <i>i</i> <p>p</p> <div>div</div> <quote>quote</quote> <img src=\"img.jpg\"> <strong>strong</strong></div> <script>alert('hi')</script>";
        String expected = "a span i p div quote strong";
        Assert.isTrue(expected.equals(StringUtil.stripAllHtmlTag(original)), "check all tags are removed");
        
        //check < or > is escaped properly
        original = "this is just a string contain '<' synbol and '>' symbol";
        expected = "this is just a string contain '&lt;' synbol and '&gt;' symbol";
        Assert.isTrue(expected.equals(StringUtil.stripAllHtmlTag(original)), "check < or > is escaped properly");
    }
    
    @Test
    public void testStripHtmlTag() throws Exception {
        //check javascript in href is removed
        String original = "<a href=\"javascript:void(0)\">a <span>span</span></a>"; //this is allowed in 7.0.31, but should not after 7.0.32
        String expected = "<a>a <span>span</span></a>";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlTag(original, new String[]{"a", "span"})), "check javascript in href is removed");
        
        //check onclick is removed
        original = "<a onclick=\"alert('hi')\">a <span>span</span></a>";
        expected = "<a>a <span>span</span></a>";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlTag(original, new String[]{"a", "span"})), "check onclick is removed");
        
        //check href with http is allowed
        original = "<a href=\"http://www.joget.org\">a <span>span</span></a>";
        expected = "<a href=\"http://www.joget.org\">a <span>span</span></a>";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlTag(original, new String[]{"a", "span"})), "check href with http is allowed");
        
        //check href with https is allowed
        original = "<a href=\"https://www.joget.org\">a <span>span</span></a>";
        expected = "<a href=\"https://www.joget.org\">a <span>span</span></a>";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlTag(original, new String[]{"a", "span"})), "check href with https is allowed");
        
        //check href with relative path is allowed
        original = "<a href=\"menu\">a <span>span</span></a>";
        expected = "<a href=\"menu\">a <span>span</span></a>";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlTag(original, new String[]{"a", "span"})), "check href with relative path is allowed");
        
        //check img onclick is removed
        original = "<img onclick=\"alert('hi')\" src=\"img.png\">";
        expected = "<img src=\"img.png\">";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlTag(original, new String[]{"img"})), "check img onclick is removed");
        
        //check img with http is allowed
        original = "<img src=\"http://www.joget.org/img.png\">";
        expected = "<img src=\"http://www.joget.org/img.png\">";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlTag(original, new String[]{"img"})), "check img with http is allowed");
        
        //check img with https is allowed
        original = "<img src=\"https://www.joget.org/img.png\">";
        expected = "<img src=\"https://www.joget.org/img.png\">";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlTag(original, new String[]{"img"})), "check img with https is allowed");
        
        //check img with relative path is allowed
        original = "<img src=\"img.png\">";
        expected = "<img src=\"img.png\">";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlTag(original, new String[]{"img"})), "check img with relative path is allowed");
    }
    
    @Test
    public void testStripHtmlRelaxed() throws Exception {
        //check basic tags are allowed
        String original = "<div><a>a</a> <span>span</span> <i>i</i> <p>p</p> <div>div</div> <quote>quote</quote> <img src=\"img.jpg\"> <strong>strong</strong></div> <script>alert('hi')</script>";
        String expected = "<div>\n" +
                        " <a>a</a> <span>span</span> <i>i</i>\n" +
                        " <p>p</p>\n" +
                        " <div>\n" +
                        "  div\n" +
                        " </div> quote <img src=\"img.jpg\" /> <strong>strong</strong>\n" +
                        "</div>";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlRelaxed(original)), "check basic tags are allowed");
        
        //check < or > is escaped properly
        original = "this is just a string contain '<' synbol and '>' symbol";
        expected = "this is just a string contain '&lt;' synbol and '&gt;' symbol";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlRelaxed(original)), "check < or > is escaped properly");
        
        //check javascript in href is removed
        original = "<a href=\"javascript:void(0)\">a <span>span</span></a>"; //this is allowed in 7.0.31, but should not after 7.0.32
        expected = "<a>a <span>span</span></a>";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlRelaxed(original)), "check javascript in href is removed");
        
        //check onclick is removed
        original = "<a onclick=\"alert('hi')\">a <span>span</span></a>";
        expected = "<a>a <span>span</span></a>";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlRelaxed(original)), "check onclick is removed");
        
        //check href with http is allowed
        original = "<a href=\"http://www.joget.org\">a <span>span</span></a>";
        expected = "<a href=\"http://www.joget.org\">a <span>span</span></a>";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlRelaxed(original)), "check href with http is allowed");
        
        //check href with https is allowed
        original = "<a href=\"https://www.joget.org\">a <span>span</span></a>";
        expected = "<a href=\"https://www.joget.org\">a <span>span</span></a>";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlRelaxed(original)), "check href with https is allowed");
        
        //check href with relative path is allowed
        original = "<a href=\"menu\">a <span>span</span></a>";
        expected = "<a href=\"menu\">a <span>span</span></a>";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlRelaxed(original)), "check href with relative path is allowed");
        
        //check img onclick is removed
        original = "<img onclick=\"alert('hi')\" src=\"img.png\">";
        expected = "<img src=\"img.png\" />";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlRelaxed(original)), "check img onclick is removed");
        
        //check img with http is allowed
        original = "<img src=\"http://www.joget.org/img.png\">";
        expected = "<img src=\"http://www.joget.org/img.png\" />";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlRelaxed(original)), "check img with http is allowed");
        
        //check img with https is allowed
        original = "<img src=\"https://www.joget.org/img.png\">";
        expected = "<img src=\"https://www.joget.org/img.png\" />";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlRelaxed(original)), "check img with https is allowed");
        
        //check img with relative path is allowed
        original = "<img src=\"img.png\">";
        expected = "<img src=\"img.png\" />";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlRelaxed(original)), "check img with relative path is allowed");
    }
    
    @Test
    public void testEscapeImg2Base64() throws Exception {
        //todo
    }
}
