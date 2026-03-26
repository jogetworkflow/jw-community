package org.joget.commons.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestSecurityUtil {

    @Test
    public void testNormalizedFileNameWithValidInput() {
        // Test normal filename
        assertEquals("test.txt", SecurityUtil.normalizedFileName("test.txt"));

        // Test filename with spaces
        assertEquals("test file.txt", SecurityUtil.normalizedFileName("test file.txt"));

        // Test filename with Unicode characters
        assertEquals("café.txt", SecurityUtil.normalizedFileName("café.txt"));

        // Test filename with special characters
        assertEquals("file@#$%^&()[]{}.txt", SecurityUtil.normalizedFileName("file@#$%^&()[]{}.txt"));

        // Test filename with Unicode characters
        assertEquals("测试文件.txt", SecurityUtil.normalizedFileName("测试文件.txt"));
        assertEquals("한글.txt", SecurityUtil.normalizedFileName("한글.txt"));
        assertEquals("файл.txt", SecurityUtil.normalizedFileName("файл.txt"));
        assertEquals("ファイル.txt", SecurityUtil.normalizedFileName("ファイル.txt"));

        // Test filename with accented characters
        assertEquals("café.txt", SecurityUtil.normalizedFileName("café.txt"));

        // Test filename with combined Unicode characters
        assertEquals("café.txt", SecurityUtil.normalizedFileName("cafe\u0301.txt"));

        // Test empty filename
        assertEquals("", SecurityUtil.normalizedFileName(""));

        // Test filename with dots
        assertEquals("test.file.txt", SecurityUtil.normalizedFileName("test.file.txt"));

        // Test filename with underscores and hyphens
        assertEquals("test_file-1.txt", SecurityUtil.normalizedFileName("test_file-1.txt"));
    }

    @Test
    public void testNormalizedFileNameWithUrlEncodedInput() {
        // Test URL encoded spaces
        assertEquals("test file.txt", SecurityUtil.normalizedFileName("test%20file.txt"));

        // Test URL encoded special characters
        assertEquals("file&test.txt", SecurityUtil.normalizedFileName("file%26test.txt"));

        // Test multiple URL encoded characters
        assertEquals("file@#$%.txt", SecurityUtil.normalizedFileName("file%40%23%24%25.txt"));

        // Test URL encoded Unicode
        assertEquals("café.txt", SecurityUtil.normalizedFileName("caf%C3%A9.txt"));
    }

    @Test
    public void testNormalizedFileNameWithMixedCaseEncoding() {
        // Test URL encoding with mixed case hex
        assertEquals("test/file.txt", SecurityUtil.normalizedFileName("test%2Ffile.txt")); // lowercase
        assertEquals("test/file.txt", SecurityUtil.normalizedFileName("test%2ffile.txt")); // lowercase
    }

    @Test
    public void testNormalizedFileNameWithValidUrlEncoding() {
        // Test various valid URL encodings
        assertEquals("test file.txt", SecurityUtil.normalizedFileName("test%20file.txt"));
        assertEquals("test@file.txt", SecurityUtil.normalizedFileName("test%40file.txt"));
        assertEquals("test#file.txt", SecurityUtil.normalizedFileName("test%23file.txt"));
        assertEquals("test%file.txt", SecurityUtil.normalizedFileName("test%25file.txt"));
        assertEquals("test&file.txt", SecurityUtil.normalizedFileName("test%26file.txt"));
        assertEquals("test+file.txt", SecurityUtil.normalizedFileName("test%2Bfile.txt"));
        assertEquals("test=file.txt", SecurityUtil.normalizedFileName("test%3Dfile.txt"));
        assertEquals("test?file.txt", SecurityUtil.normalizedFileName("test%3Ffile.txt"));
    }

    @Test
    public void testNormalizedFileNameWithUnicodeNormalization() {
        // Test composed vs decomposed Unicode characters
        assertEquals("\u00E9", SecurityUtil.normalizedFileName("e\u0301")); // é vs e + combining acute accent

        // Test other Unicode normalization cases
        assertEquals("\u00C5", SecurityUtil.normalizedFileName("A\u030A")); // Å vs A + combining ring above
    }

    @Test(expected = SecurityException.class)
    public void testNormalizedFileNameWithPathTraversalForwardSlash() {
        SecurityUtil.normalizedFileName("../../../etc/passwd");
    }

    @Test(expected = SecurityException.class)
    public void testNormalizedFileNameWithPathTraversalBackslash() {
        SecurityUtil.normalizedFileName("..\\..\\..\\windows\\system32\\config\\sam");
    }

    @Test(expected = SecurityException.class)
    public void testNormalizedFileNameWithMixedPathTraversal() {
        SecurityUtil.normalizedFileName("../..\\../test.txt");
    }

    @Test
    public void testNormalizedFileNameWithEncodedPathTraversalAttempt() {
        // Test that URL-encoded path traversal is caught after decoding
        assertThrows(
                "Expected SecurityException for path traversal",
                SecurityException.class,
                () -> SecurityUtil.normalizedFileName("..%2F..%2Fetc%2Fpasswd")
        );
        assertThrows(
                "Expected SecurityException for path traversal",
                SecurityException.class,
                () -> SecurityUtil.normalizedFileName("..%5C..%5Cwindows%5Csystem32")
        );
    }

    @Test
    public void testNormalizedFileNameWithNonPathTraversalDots() {
        // Test filename with dots (but not path traversal)
        assertEquals("test.file.name.txt", SecurityUtil.normalizedFileName("test.file.name.txt"));

        // Test filename starting with dot (hidden files)
        assertEquals(".hidden.txt", SecurityUtil.normalizedFileName(".hidden.txt"));

        // Test filename with consecutive dots
        assertEquals("test..txt", SecurityUtil.normalizedFileName("test..txt"));
        assertEquals("test..file.txt", SecurityUtil.normalizedFileName("test..file.txt"));

        // Test that safe encoded characters don't trigger path traversal
        assertEquals("test..file.txt", SecurityUtil.normalizedFileName("test%2E%2Efile.txt")); // encoded dots
    }

    @Test
    public void testNormalizedFileNameWithComplexScenarios() {
        // Test URL encoded with Unicode normalization
        assertEquals("café test.txt", SecurityUtil.normalizedFileName("caf%C3%A9%20test.txt"));

        // Test mixed special characters and URL encoding
        assertEquals("test@file#123.txt", SecurityUtil.normalizedFileName("test%40file%23123.txt"));

        // Test long filename with various characters
        String longFilename = "Very-Long_Filename@With#Many$Special%Characters&And*Unicode+Characters=Like café and 日本語.txt";
        assertEquals(longFilename, SecurityUtil.normalizedFileName(longFilename));

        // test with full width colon
        assertEquals("test：file.txt", SecurityUtil.normalizedFileName("test：file.txt"));

        // test with mixed width colons
        assertEquals("Full：Width Half:Width.txt", SecurityUtil.normalizedFileName("Full：Width Half:Width.txt"));
    }

    @Test
    public void testNormalizedFileNameWithNumbers() {
        // Test filenames with numbers
        assertEquals("test123.txt", SecurityUtil.normalizedFileName("test123.txt"));

        // Test URL encoded numbers
        assertEquals("test123.txt", SecurityUtil.normalizedFileName("test%31%32%33.txt"));
    }

    @Test
    public void testIssue1729_T7326() {
        assertEquals("test+file.txt", SecurityUtil.normalizedFileName("test+file.txt"));
        assertEquals("test＋file.txt", SecurityUtil.normalizedFileName("test＋file.txt"));
    }
}
