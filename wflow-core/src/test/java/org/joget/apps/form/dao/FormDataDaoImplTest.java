package org.joget.apps.form.dao;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import javax.cache.Cache;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.*;


public class FormDataDaoImplTest {

    private FormDataDaoImpl dao;
    @Mock
    private Cache formPersistentClassCache;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        dao = spy(new FormDataDaoImpl());
    }

    @Test
    public void testProcessQuery_ReservedKeywordHandlingSmallCaps() {

        String input = "SELECT e.customProperties.class, e.customProperties.null, e.customProperties.true, e.customProperties.false FROM app_fd_test e";
        String result = dao.processQuery(input);

        assertTrue("Should add t__ prefix to reserved keyword 'class'", result.contains("e.customProperties.t__class"));
        assertTrue("Should add t__ prefix to reserved keyword 'null'", result.contains("e.customProperties.t__null"));
        assertTrue("Should add t__ prefix to reserved keyword 'true'", result.contains("e.customProperties.t__true"));
        assertTrue("Should add t__ prefix to reserved keyword 'false'", result.contains("e.customProperties.t__false "));
    }

    @Test
    public void testProcessQuery_ReservedKeywordHandlingBigCaps() {

        String input = "SELECT e.customProperties.CLASS, e.customProperties.NULL, e.customProperties.TRUE, e.customProperties.FALSE FROM app_fd_test e";
        String result = dao.processQuery(input);

        assertTrue("Should add t__ prefix to reserved keyword 'CLASS'", result.contains("e.customProperties.t__CLASS"));
        assertTrue("Should add t__ prefix to reserved keyword 'NULL'", result.contains("e.customProperties.t__NULL"));
        assertTrue("Should add t__ prefix to reserved keyword 'TRUE'", result.contains("e.customProperties.t__TRUE"));
        assertTrue("Should add t__ prefix to reserved keyword 'FALSE'", result.contains("e.customProperties.t__FALSE"));
    }

    @Test
    public void testProcessQuery_DigitPrefixedFields() {

        String input = "SELECT e.customProperties.123field FROM app_fd_test e";
        String result = dao.processQuery(input);

        assertEquals("Should add t__ prefix to digit-prefixed field",
                "SELECT e.customProperties.t__123field FROM app_fd_test e", result);
    }

    @Test
    public void testExtractAliasToTableMapping_SingleTables() {
        String query = "FROM app_fd_TEST_TABLE e";
        FormDataDaoImpl dao = new FormDataDaoImpl();
        Map<String, String> result = dao.extractAliasToTableMapping(query);

        assertTrue(result.containsKey("e"));
        assertEquals("app_fd_TEST_TABLE", result.get("e"));
        assertEquals(1, result.size());
    }

    @Test
    public void testExtractAliasToTableMapping_MultipleTables() {
        String query = "FROM app_fd_TABLE1 e, app_fd_TABLE2 AS TABLE2, app_fd_TABLE3 AS TABLE3";
        FormDataDaoImpl dao = new FormDataDaoImpl();
        Map<String, String> result = dao.extractAliasToTableMapping(query);

        assertEquals(3, result.size());
        assertEquals("app_fd_TABLE1", result.get("e"));
        assertEquals("app_fd_TABLE2", result.get("TABLE2"));
        assertEquals("app_fd_TABLE3", result.get("TABLE3"));
    }

    @Test
    public void testNormalizeCustomPropertiesForAlias_BasicCaseNormalization() {
        // Create mock PersistentClass with customProperties
        PersistentClass mockPC = createMockPersistentClass("FIELD2", "field1");

        // Mock cache to return our mock PersistentClass
        when(formPersistentClassCache.get(anyString())).thenReturn(mockPC);

        FormDataDaoImpl dao = new FormDataDaoImpl() {
            @Override
            protected String getFormMappingPath() {
                return "src/test/resources/app_forms/";
            }

            @Override
            protected String getPersistentClassCacheKey(String tableName) {
                return "test_key_" + tableName;
            }
        };

        // Inject mocked cache via reflection or create a setter method
        injectCache(dao, formPersistentClassCache);

        String query = "SELECT e.customProperties.field2 FROM app_fd_TEST_TABLE e";
        String result = dao.normalizeCustomPropertiesForAlias(query, "e", "app_fd_TEST_TABLE");

        // Verify - field2 should be corrected to FIELD2 (exact case from hbm.xml)
        assertTrue(result.contains("e.customProperties.FIELD2"));
        assertFalse(result.contains("e.customProperties.field2"));
    }

    @Test
    public void testNormalizeCustomPropertiesForAlias_MultipleFieldNormalization() {
        // Create mock PersistentClass with customProperties
        PersistentClass mockPC = createMockPersistentClass("FIELD2", "field1");

        // Mock cache to return our mock PersistentClass
        when(formPersistentClassCache.get(anyString())).thenReturn(mockPC);

        FormDataDaoImpl dao = new FormDataDaoImpl() {
            @Override
            protected String getFormMappingPath() {
                return "src/test/resources/app_forms/";
            }

            @Override
            protected String getPersistentClassCacheKey(String tableName) {
                return "test_key_" + tableName;
            }
        };

        // Inject mocked cache
        injectCache(dao, formPersistentClassCache);

        String query = "SELECT e.customProperties.field2, e.customProperties.FIELD1, e.id FROM app_fd_TEST_TABLE e WHERE e.customProperties.field1 = 'test'";
        String result = dao.normalizeCustomPropertiesForAlias(query, "e", "app_fd_TEST_TABLE");

        // Verify - field2 should be corrected to FIELD2, FIELD1 should remain FIELD1, field1 should be corrected to field1
        assertTrue(result.contains("e.customProperties.FIELD2"));  // corrected
        assertTrue(result.contains("e.customProperties.field1"));  // corrected in SELECT clause but already correct in WHERE clause

        // Ensure incorrect cases are no longer present
        assertFalse(result.contains("e.customProperties.field2"));
        assertFalse(result.contains("e.customProperties.FIELD1"));
    }

    private PersistentClass createMockPersistentClass(String... fieldNames) {
        PersistentClass mockPC = mock(PersistentClass.class);
        Property mockCustomProp = mock(Property.class);
        Component mockComponent = mock(Component.class);

        when(mockPC.getProperty("customProperties")).thenReturn(mockCustomProp);
        when(mockCustomProp.getValue()).thenReturn(mockComponent);

        // Create mock properties for each field
        Map<String, Property> mockProperties = new HashMap<>();
        for (String fieldName : fieldNames) {
            Property mockProp = mock(Property.class);
            when(mockProp.getName()).thenReturn(fieldName);
            mockProperties.put(fieldName, mockProp);
        }

        // Mock iterator for properties
        when(mockComponent.getProperties()).thenReturn(new ArrayList<>(mockProperties.values()));

        return mockPC;
    }

    private void injectCache(FormDataDaoImpl dao, Cache cache) {
        try {
            java.lang.reflect.Field field = FormDataDaoImpl.class.getDeclaredField("formPersistentClassCache");
            field.setAccessible(true);
            field.set(dao, cache);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject cache", e);
        }
    }

    @Test
    public void testReplaceColumnNameWithPrefix_SpacePrefixedWithMixedCase() {
        FormDataDaoImpl dao = new FormDataDaoImpl();
        String input = "WHERE c_fieldName = 'value' AND C_otherField > 10";
        String expected = "WHERE e.customProperties.fieldName = 'value' AND e.customProperties.otherField > 10";
        String result = dao.replaceColumnNameWithPrefix("app_fd_test", input);
        assertEquals(expected, result);
    }

    @Test
    public void testReplaceColumnNameWithPrefix_NestedParenthesesWithMixedCase() {
        FormDataDaoImpl dao = new FormDataDaoImpl();
        String input = "WHERE ((c_field1 = 'value') AND (C_field2 > 5))";
        String expected = "WHERE ((e.customProperties.field1 = 'value') AND (e.customProperties.field2 > 5))";
        String result = dao.replaceColumnNameWithPrefix("app_fd_test", input);
        assertEquals(expected, result);
    }
    
}
