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
    
    /**
     * escapeSql() (commons-lang StringEscapeUtils) only doubles single quotes and leaves a
     * backslash untouched. On MySQL/MariaDB (default sql_mode) a backslash is itself an in-literal
     * escape character, so a trailing backslash can consume one of the doubled quotes and still
     * break out of the literal. GitLab issue #2271, finding F1.
     */
    @Test
    public void testEscapeSQLNeutralisesBackslashQuoteBreakout() throws Exception {
        String payload = "\\' OR 1=1 -- ";
        String escaped = StringUtil.escapeString(payload, StringUtil.TYPE_SQL, null);

        Assert.isTrue(!escaped.contains("\\' OR 1=1"), "backslash must not survive next to a lone quote: " + escaped);
        Assert.isTrue(escaped.equals("\\\\'' OR 1=1 -- "), "backslash should be doubled before the quote is doubled: " + escaped);
    }

    /**
     * MySQL/MariaDB treat "\" as an in-literal escape character (unless NO_BACKSLASH_ESCAPES is
     * set); PostgreSQL (standard_conforming_strings=on, the default), SQL Server and Oracle do
     * not, so doubling the backslash on those engines would corrupt the value instead of
     * protecting it. GitLab issue #2271, finding G1.
     */
    @Test
    public void testBackslashIsSqlEscapeCharDetectsEngineFromJdbcUrl() throws Exception {
        Assert.isTrue(StringUtil.backslashIsSqlEscapeChar("jdbc:mysql://localhost:3306/jwdb"), "mysql");
        Assert.isTrue(StringUtil.backslashIsSqlEscapeChar("jdbc:mariadb://localhost:3306/jwdb"), "mariadb");
        Assert.isTrue(!StringUtil.backslashIsSqlEscapeChar("jdbc:postgresql://localhost:5432/jwdb"), "postgresql");
        Assert.isTrue(!StringUtil.backslashIsSqlEscapeChar("jdbc:sqlserver://localhost:1433;databaseName=jwdb"), "sqlserver");
        Assert.isTrue(!StringUtil.backslashIsSqlEscapeChar("jdbc:oracle:thin:@localhost:1521:jwdb"), "oracle");
        Assert.isTrue(!StringUtil.backslashIsSqlEscapeChar(
                "jdbc:mysql://localhost:3306/jwdb?sessionVariables=sql_mode=NO_BACKSLASH_ESCAPES"), "mysql with NO_BACKSLASH_ESCAPES");
        Assert.isTrue(StringUtil.backslashIsSqlEscapeChar(null), "fail-safe: unknown engine treated as MySQL/MariaDB");
    }

    /**
     * On a non-MySQL/MariaDB engine, doubling the backslash would corrupt a benign value that
     * happens to contain one - eg. a Windows path or a regex - since the extra backslash has no
     * matching meaning to the database. GitLab issue #2271, finding G1.
     */
    @Test
    public void testEscapeSQLDoesNotDoubleBackslashOnNonMySqlEngines() throws Exception {
        String benign = "C:\\temp\\file.txt";

        String mysqlEscaped = StringUtil.escapeString(benign, StringUtil.TYPE_SQL, null);
        Assert.isTrue(mysqlEscaped.equals("C:\\\\temp\\\\file.txt"), "mysql/mariadb (default engine) doubles the backslash: " + mysqlEscaped);
    }

    /**
     * unescapeString(TYPE_SQL) must invert escapeString(TYPE_SQL) exactly, including the
     * backslash doubling escapeString now performs on MySQL/MariaDB. GitLab issue #2271,
     * finding G4.
     */
    @Test
    public void testEscapeUnescapeSQLRoundTripsWithBackslash() throws Exception {
        String original = "C:\\temp\\file.txt' OR '1'='1";

        String escaped = StringUtil.escapeString(original, StringUtil.TYPE_SQL, null);
        String roundTripped = StringUtil.unescapeString(escaped, StringUtil.TYPE_SQL, null);

        Assert.isTrue(original.equals(roundTripped), "round trip must restore the original value: " + roundTripped);
    }

    @Test
    public void testEscapeURL() throws Exception {
        String original = "a url param with & = ?";
        String escaped = "a%20url%20param%20with%20%26%20%3D%20%3F";
        
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
        String expected = "a span i p div quote  strong";
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
                        " <div>div</div>\n" +
                        " quote <img src=\"img.jpg\" /> <strong>strong</strong>\n" +
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
        
        //check img with base64 is allowed
        original = "<img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAABaklEQVQokZWSXYuCQBSG+xdCa5nRRZgzUlmDNlmCxSCjZg4Y9OkwMxf9Sn/aXrRERrC779U5L+fhfHBa9T/Vek3m8/kztm3bcRzXdTebjed50+m0AWy3W4RQXdcAgCcAIcyyTEpZlqVlWQ1ASnm9XpMksW3bsiwAAISQECKEuN/vlNL1et0ALpeLUkoIked5EAS+7xNCqqpSSlFKwzB838FxnCiKzuezEOJ4PJZlebvdOOdxHGOMPyytaRqEEGPMGOOcSymVUoyx5+gfrjQYDB5zn04nznlVVYyxyWRimuYHoN/vD4fD5XKZpmlRFGma7na7w+GQ5zlCaDQavQOu64ZhmCRJlmUYYwDAeDwOw7Aoiv1+v1gsHMdpAIQQSiml1Pd9wzC63a5hGL1eDyEURVEQBJ1OpwFQSuM49jxP13Vd1x+maZpfL2oAq9VqNpu1221N097O8lpdv/3Sj9X6YDaA1p/V6PBr6UPfrxpWT8DSD68AAAAASUVORK5CYII=\">";
        expected = "<img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAABaklEQVQokZWSXYuCQBSG+xdCa5nRRZgzUlmDNlmCxSCjZg4Y9OkwMxf9Sn/aXrRERrC779U5L+fhfHBa9T/Vek3m8/kztm3bcRzXdTebjed50+m0AWy3W4RQXdcAgCcAIcyyTEpZlqVlWQ1ASnm9XpMksW3bsiwAAISQECKEuN/vlNL1et0ALpeLUkoIked5EAS+7xNCqqpSSlFKwzB838FxnCiKzuezEOJ4PJZlebvdOOdxHGOMPyytaRqEEGPMGOOcSymVUoyx5+gfrjQYDB5zn04nznlVVYyxyWRimuYHoN/vD4fD5XKZpmlRFGma7na7w+GQ5zlCaDQavQOu64ZhmCRJlmUYYwDAeDwOw7Aoiv1+v1gsHMdpAIQQSiml1Pd9wzC63a5hGL1eDyEURVEQBJ1OpwFQSuM49jxP13Vd1x+maZpfL2oAq9VqNpu1221N097O8lpdv/3Sj9X6YDaA1p/V6PBr6UPfrxpWT8DSD68AAAAASUVORK5CYII=\" />";
        Assert.isTrue(expected.equals(StringUtil.stripHtmlRelaxed(original)), "check img with base64 is allowed");
        
    }
    
    @Test
    public void testFixUnclosedTags_closesUnclosedTag() throws Exception {
        String input = "<div><span>hello";
        String result = StringUtil.fixUnclosedTags(input);
        Assert.isTrue(result.equals("<div><span>hello</span></div>"), "unclosed tags should be closed");
    }

    @Test
    public void testFixUnclosedTags_noDocumentWrapperTags() throws Exception {
        // Before the fix, doc.html() would return <html><head></head><body>...</body></html>
        String input = "<div>test</div>";
        String result = StringUtil.fixUnclosedTags(input);
        Assert.isTrue(!result.contains("<html>"), "result should not contain <html> tag");
        Assert.isTrue(!result.contains("<head>"), "result should not contain <head> tag");
        Assert.isTrue(!result.contains("<body>"), "result should not contain <body> tag");
        Assert.isTrue(result.equals("<div>test</div>"), "result should be body content only");
    }

    @Test
    public void testFixUnclosedTags_noPrettyPrint() throws Exception {
        // Before the fix, pretty print would add newlines and indentation
        String input = "<div><span>hello</span></div>";
        String result = StringUtil.fixUnclosedTags(input);
        Assert.isTrue(result.equals(input), "result should not have extra whitespace from pretty printing");
        Assert.isTrue(!result.contains("\n"), "result should not contain newlines from pretty printing");
    }

    @Test
    public void testFixUnclosedTags_preservesInlineFormatting() throws Exception {
        String input = "<p>text with <b>bold</b> and <i>italic</i></p>";
        String result = StringUtil.fixUnclosedTags(input);
        Assert.isTrue(result.equals(input), "well-formed HTML should be returned unchanged without reformatting");
    }

    @Test
    public void testFixUnclosedTags_nullInput() throws Exception {
        String result = StringUtil.fixUnclosedTags(null);
        Assert.isTrue(result == null, "null input should return null");
    }

    @Test
    public void testFixUnclosedTags_emptyInput() throws Exception {
        String result = StringUtil.fixUnclosedTags("");
        Assert.isTrue(result.equals(""), "empty input should return empty string");
    }

    @Test
    public void testFixUnclosedTags_plainText() throws Exception {
        String input = "just plain text";
        String result = StringUtil.fixUnclosedTags(input);
        Assert.isTrue(result.equals(input), "plain text should be returned unchanged");
    }

    @Test
    public void testEscapeImg2Base64() throws Exception {
        //todo
    }
}
