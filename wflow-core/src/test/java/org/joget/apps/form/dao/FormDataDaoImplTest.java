package org.joget.apps.form.dao;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class FormDataDaoImplTest {

    private static Method getIndexNameMethod;
    private static int maxIndexNameLength;

    @BeforeClass
    public static void setUpClass() throws Exception {
        getIndexNameMethod = FormDataDaoImpl.class.getDeclaredMethod("getIndexName", String.class, String.class);
        getIndexNameMethod.setAccessible(true);

        Field field = FormDataDaoImpl.class.getDeclaredField("MAX_INDEX_NAME_LENGTH");
        field.setAccessible(true);
        maxIndexNameLength = field.getInt(null);
    }

    @Test
    public void returnsCandidateUnchangedWhenWellWithinMaxLength() throws Exception {
        String result = invokeGetIndexName("users", "email");

        assertEquals("idx_users_email", result);
    }

    @Test
    public void returnsCandidateUnchangedWhenExactlyAtMaxLength() throws Exception {
        String tableName = repeat('t', 30);
        String columnName = repeat('c', maxIndexNameLength - 5 - 30);
        String candidate = "idx_" + tableName + "_" + columnName;
        assertEquals(maxIndexNameLength, candidate.length());

        String result = invokeGetIndexName(tableName, columnName);

        assertEquals(candidate, result);
    }

    @Test
    public void hashesAndTruncatesWhenOneCharacterOverMaxLength() throws Exception {
        String tableName = repeat('t', 30);
        String columnName = repeat('c', maxIndexNameLength - 5 - 30 + 1);
        String candidate = "idx_" + tableName + "_" + columnName;
        assertEquals(maxIndexNameLength + 1, candidate.length());

        String result = invokeGetIndexName(tableName, columnName);

        assertEquals(maxIndexNameLength, result.length());
        assertEquals(expectedHashedName(candidate), result);
    }

    @Test
    public void hashesAndTruncatesWhenCandidateFarExceedsMaxLength() throws Exception {
        String tableName = repeat('t', 40);
        String columnName = repeat('c', 40);
        String candidate = "idx_" + tableName + "_" + columnName;
        assertTrue(candidate.length() > maxIndexNameLength);

        String result = invokeGetIndexName(tableName, columnName);

        assertEquals(maxIndexNameLength, result.length());
        assertEquals(expectedHashedName(candidate), result);
    }

    @Test
    public void isDeterministicForTheSameInputs() throws Exception {
        String tableName = repeat('a', 50);
        String columnName = repeat('b', 50);

        String first = invokeGetIndexName(tableName, columnName);
        String second = invokeGetIndexName(tableName, columnName);

        assertEquals(first, second);
    }

    @Test
    public void differentInputsProduceDifferentHashedNames() throws Exception {
        String tableName = repeat('a', 50);

        String result1 = invokeGetIndexName(tableName, repeat('b', 50));
        String result2 = invokeGetIndexName(tableName, repeat('c', 50));

        assertNotEquals(result1, result2);
    }

    private String expectedHashedName(String candidate) {
        String hash = UUID.nameUUIDFromBytes(candidate.getBytes(StandardCharsets.UTF_8))
                .toString().replace("-", "").substring(0, 12);
        int prefixLength = maxIndexNameLength - hash.length() - 1;
        return candidate.substring(0, prefixLength) + "_" + hash;
    }

    private String invokeGetIndexName(String tableName, String columnName) throws Exception {
        try {
            return (String) getIndexNameMethod.invoke(null, tableName, columnName);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    private static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
