//package org.joget.commons.ignite;
//
///*
// * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// *
// * This code is free software; you can redistribute it and/or modify it
// * under the terms of the GNU General Public License version 2 only, as
// * published by the Free Software Foundation.  Oracle designates this
// * particular file as subject to the "Classpath" exception as provided
// * by Oracle in the LICENSE file that accompanied this code.
// *
// * This code is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
// * version 2 for more details (a copy is included in the LICENSE file that
// * accompanied this code).
// *
// * You should have received a copy of the GNU General Public License version
// * 2 along with this work; if not, write to the Free Software Foundation,
// * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
// *
// * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
// * or visit www.oracle.com if you need additional information or have any
// * questions.
// */
//
//import com.sun.rowset.CachedRowSetImpl;
//import java.sql.*;
//import java.math.*;
//import java.util.*;
//
//import javax.sql.rowset.*;
//
///**
// * CUSTOM: Fix for Java bug https://bugs.openjdk.org/browse/JDK-8298117
// * Original source from https://raw.githubusercontent.com/AdoptOpenJDK/openjdk-jdk11/jdk8-b120/jdk/src/share/classes/com/sun/rowset/CachedRowSetImpl.java
// */
//public class IgniteJdbcRowSetImpl extends CachedRowSetImpl {
//
//    // CUSTOM
//    private String cacheKey;
//
//    public String getCacheKey() {
//        return cacheKey;
//    }
//
//    public void setCacheKey(String cacheKey) {
//        this.cacheKey = cacheKey;
//    }
//
//    @Override
//    public String toString() {
//        return "IgniteJdbcRowSetImpl{" + "cacheKey=" + cacheKey + '}';
//    }    
//    
//    public IgniteJdbcRowSetImpl() throws SQLException {
//        super();
//    }
//    
//    public IgniteJdbcRowSetImpl(@SuppressWarnings("rawtypes") Hashtable env) throws SQLException {
//        super(env);
//    }
//    // END CUSTOM
//    
//    /**
//     * Returns the column number of the column with the given name in this
//     * <code>CachedRowSetImpl</code> object.  This method throws an
//     * <code>SQLException</code> if the given name is not the name of
//     * one of the columns in this rowset.
//     *
//     * @param name a <code>String</code> object that is the name of a column in
//     *              this <code>CachedRowSetImpl</code> object
//     * @throws SQLException if the given name does not match the name of one of
//     *         the columns in this rowset
//     */
//    private int getColIdxByName(String name) throws SQLException {
//        RowSetMetaDataImpl RowSetMD = (RowSetMetaDataImpl)this.getMetaData();
//        int cols = RowSetMD.getColumnCount();
//
//        for (int i=1; i <= cols; ++i) {
//            String colName = RowSetMD.getColumnName(i);
//            String colLabel = RowSetMD.getColumnLabel(i); // CUSTOM: Fix for Java bug https://bugs.openjdk.org/browse/JDK-8298117
//            if (colName != null)
//                if (name.equalsIgnoreCase(colName) || name.equalsIgnoreCase(colLabel)) // CUSTOM: Fix for Java bug https://bugs.openjdk.org/browse/JDK-8298117
//                    return (i);
//                else
//                    continue;
//        }
//        throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalcolnm").toString());
//
//    }
//
//    /**
//     * Returns the specified column of this <code>CachedRowSetImpl</code> object
//     * as a <code>Collection</code> object.  This method makes a copy of the
//     * column's data and utilitizes the <code>Vector</code> to establish the
//     * collection. The <code>Vector</code> class implements a growable array
//     * objects allowing the individual components to be accessed using an
//     * an integer index similar to that of an array.
//     *
//     * @return a <code>Collection</code> object that contains the value(s)
//     *         stored in the specified column of this
//     *         <code>CachedRowSetImpl</code>
//     *         object
//     * @throws SQLException if an error occurs generated the collection; or
//     *          an invalid column is provided.
//     * @see #toCollection()
//     * @see #toCollection(int)
//     * @see java.util.Vector
//     */
//    @Override
//    public Collection<?> toCollection(String column) throws SQLException {
//        return toCollection(getColIdxByName(column));
//    }
//
//    // Methods for accessing results by column name
//
//    /**
//     * Retrieves the value stored in the designated column
//     * of the current row as a <code>String</code> object.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return the column value; if the value is SQL <code>NULL</code>,
//     *         the result is <code>null</code>
//     * @throws SQLException if (1) the given column name is not the name of
//     * a column in this rowset, (2) the cursor is not on one of
//     * this rowset's rows or its insert row, or (3) the designated
//     * column does not store an SQL <code>TINYINT, SMALLINT, INTEGER
//     * BIGINT, REAL, FLOAT, DOUBLE, DECIMAL, NUMERIC, BIT, <b>CHAR</b>,
//     * <b>VARCHAR</b></code> or <code>LONGVARCHAR<</code> value. The bold SQL type
//     * designates the recommended return type.
//     */
//    @Override
//    public String getString(String columnName) throws SQLException {
//        return getString(getColIdxByName(columnName));
//    }
//
//    /**
//     * Retrieves the value stored in the designated column
//     * of the current row as a <code>boolean</code> value.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return the column value as a <code>boolean</code> in the Java programming
//     *        language; if the value is SQL <code>NULL</code>,
//     *        the result is <code>false</code>
//     * @throws SQLException if (1) the given column name is not the name of
//     *            a column in this rowset, (2) the cursor is not on one of
//     *            this rowset's rows or its insert row, or (3) the designated
//     *            column does not store an SQL <code>BOOLEAN</code> value
//     * @see #getBoolean(int)
//     */
//    @Override
//    public boolean getBoolean(String columnName) throws SQLException {
//        return getBoolean(getColIdxByName(columnName));
//    }
//
//    /**
//     * Retrieves the value stored in the designated column
//     * of the current row as a <code>byte</code> value.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return the column value as a <code>byte</code> in the Java programming
//     * language; if the value is SQL <code>NULL</code>, the result is <code>0</code>
//     * @throws SQLException if (1) the given column name is not the name of
//     * a column in this rowset, (2) the cursor is not on one of
//     * this rowset's rows or its insert row, or (3) the designated
//     * column does not store an SQL <code><B>TINYINT</B>, SMALLINT, INTEGER,
//     * BIGINT, REAL, FLOAT, DOUBLE, DECIMAL, NUMERIC, BIT, CHAR,
//     * VARCHAR</code> or <code>LONGVARCHAR</code> value. The
//     * bold type designates the recommended return type
//     */
//    @Override
//    public byte getByte(String columnName) throws SQLException {
//        return getByte(getColIdxByName(columnName));
//    }
//
//    /**
//     * Retrieves the value stored in the designated column
//     * of the current row as a <code>short</code> value.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return the column value; if the value is SQL <code>NULL</code>,
//     *         the result is <code>0</code>
//     * @throws SQLException if (1) the given column name is not the name of
//     * a column in this rowset, (2) the cursor is not on one of
//     * this rowset's rows or its insert row, or (3) the designated
//     * column does not store an SQL <code>TINYINT, <b>SMALLINT</b>, INTEGER
//     * BIGINT, REAL, FLOAT, DOUBLE, DECIMAL, NUMERIC, BIT, CHAR,
//     * VARCHAR</code> or <code>LONGVARCHAR</code> value. The bold SQL type
//     * designates the recommended return type.
//     * @see #getShort(int)
//     */
//    @Override
//    public short getShort(String columnName) throws SQLException {
//        return getShort(getColIdxByName(columnName));
//    }
//
//    /**
//     * Retrieves the value stored in the designated column
//     * of the current row as an <code>int</code> value.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return the column value; if the value is SQL <code>NULL</code>,
//     *         the result is <code>0</code>
//     * @throws SQLException if (1) the given column name is not the name
//     * of a column in this rowset,
//     * (2) the cursor is not on one of this rowset's rows or its
//     * insert row, or (3) the designated column does not store an
//     * SQL <code>TINYINT, SMALLINT, <b>INTEGER</b>, BIGINT, REAL
//     * FLOAT, DOUBLE, DECIMAL, NUMERIC, BIT, CHAR, VARCHAR</code>
//     * or <code>LONGVARCHAR</code> value. The bold SQL type designates the
//     * recommended return type.
//     */
//    @Override
//    public int getInt(String columnName) throws SQLException {
//        return getInt(getColIdxByName(columnName));
//    }
//
//    /**
//     * Retrieves the value stored in the designated column
//     * of the current row as a <code>long</code> value.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return the column value; if the value is SQL <code>NULL</code>,
//     *         the result is <code>0</code>
//     * @throws SQLException if (1) the given column name is not the name of
//     * a column in this rowset, (2) the cursor is not on one of
//     * this rowset's rows or its insert row, or (3) the designated
//     * column does not store an SQL <code>TINYINT, SMALLINT, INTEGER
//     * <b>BIGINT</b>, REAL, FLOAT, DOUBLE, DECIMAL, NUMERIC, BIT, CHAR,
//     * VARCHAR</code> or <code>LONGVARCHAR</code> value. The bold SQL type
//     * designates the recommended return type.
//     * @see #getLong(int)
//     */
//    @Override
//    public long getLong(String columnName) throws SQLException {
//        return getLong(getColIdxByName(columnName));
//    }
//
//    /**
//     * Retrieves the value stored in the designated column
//     * of the current row as a <code>float</code> value.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return the column value; if the value is SQL <code>NULL</code>,
//     *         the result is <code>0</code>
//     * @throws SQLException if (1) the given column name is not the name of
//     * a column in this rowset, (2) the cursor is not on one of
//     * this rowset's rows or its insert row, or (3) the designated
//     * column does not store an SQL <code>TINYINT, SMALLINT, INTEGER
//     * BIGINT, <b>REAL</b>, FLOAT, DOUBLE, DECIMAL, NUMERIC, BIT, CHAR,
//     * VARCHAR</code> or <code>LONGVARCHAR</code> value. The bold SQL type
//     * designates the recommended return type.
//     * @see #getFloat(String)
//     */
//    @Override
//    public float getFloat(String columnName) throws SQLException {
//        return getFloat(getColIdxByName(columnName));
//    }
//
//    /**
//     * Retrieves the value stored in the designated column
//     * of the current row of this <code>CachedRowSetImpl</code> object
//     * as a <code>double</code> value.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return the column value; if the value is SQL <code>NULL</code>,
//     *         the result is <code>0</code>
//     * @throws SQLException if (1) the given column name is not the name of
//     * a column in this rowset, (2) the cursor is not on one of
//     * this rowset's rows or its insert row, or (3) the designated
//     * column does not store an SQL <code>TINYINT, SMALLINT, INTEGER
//     * BIGINT, REAL, <b>FLOAT</b>, <b>DOUBLE</b>, DECIMAL, NUMERIC, BIT, CHAR,
//     * VARCHAR</code> or <code>LONGVARCHAR</code> value. The bold SQL type
//     * designates the recommended return types.
//     * @see #getDouble(int)
//     */
//    @Override
//    public double getDouble(String columnName) throws SQLException {
//        return getDouble(getColIdxByName(columnName));
//    }
//
//    /**
//     * Retrieves the value stored in the designated column
//     * of the current row as a <code>java.math.BigDecimal</code> object.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @param scale the number of digits to the right of the decimal point
//     * @return a java.math.BugDecimal object with <code><i>scale</i></code>
//     * number of digits to the right of the decimal point.
//     * @throws SQLException if (1) the given column name is not the name of
//     * a column in this rowset, (2) the cursor is not on one of
//     * this rowset's rows or its insert row, or (3) the designated
//     * column does not store an SQL <code>TINYINT, SMALLINT, INTEGER
//     * BIGINT, REAL, FLOAT, DOUBLE, <b>DECIMAL</b>, <b>NUMERIC</b>, BIT CHAR,
//     * VARCHAR</code> or <code>LONGVARCHAR</code> value. The bold SQL type
//     * designates the recommended return type that this method is used to
//     * retrieve.
//     * @deprecated Use the <code>getBigDecimal(String columnName)</code>
//     *             method instead
//     */
//    @Deprecated
//    @Override
//    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
//        return getBigDecimal(getColIdxByName(columnName), scale);
//    }
//
//    /**
//     * Retrieves the value stored in the designated column
//     * of the current row as a <code>byte</code> array.
//     * The bytes represent the raw values returned by the driver.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return the column value as a <code>byte</code> array in the Java programming
//     * language; if the value is SQL <code>NULL</code>, the result is <code>null</code>
//     * @throws SQLException if (1) the given column name is not the name of
//     * a column in this rowset, (2) the cursor is not on one of
//     * this rowset's rows or its insert row, or (3) the designated
//     * column does not store an SQL <code><b>BINARY</b>, <b>VARBINARY</b>
//     * </code> or <code>LONGVARBINARY</code> values
//     * The bold SQL type designates the recommended return type.
//     * @see #getBytes(int)
//     */
//    @Override
//    public byte[] getBytes(String columnName) throws SQLException {
//        return getBytes(getColIdxByName(columnName));
//    }
//
//    /**
//     * Retrieves the value stored in the designated column
//     * of the current row as a <code>java.sql.Date</code> object.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return the column value; if the value is SQL <code>NULL</code>,
//     *         the result is <code>null</code>
//     * @throws SQLException if (1) the given column name is not the name of
//     *            a column in this rowset, (2) the cursor is not on one of
//     *            this rowset's rows or its insert row, or (3) the designated
//     *            column does not store an SQL <code>DATE</code> or
//     *            <code>TIMESTAMP</code> value
//     */
//    @Override
//    public java.sql.Date getDate(String columnName) throws SQLException {
//        return getDate(getColIdxByName(columnName));
//    }
//
//    /**
//     * Retrieves the value stored in the designated column
//     * of the current row as a <code>java.sql.Time</code> object.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return the column value; if the value is SQL <code>NULL</code>,
//     *         the result is <code>null</code>
//     * @throws SQLException if the given column name does not match one of
//     *            this rowset's column names or the cursor is not on one of
//     *            this rowset's rows or its insert row
//     */
//    @Override
//    public java.sql.Time getTime(String columnName) throws SQLException {
//        return getTime(getColIdxByName(columnName));
//    }
//
//    /**
//     * Retrieves the value stored in the designated column
//     * of the current row as a <code>java.sql.Timestamp</code> object.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return the column value; if the value is SQL <code>NULL</code>,
//     *         the result is <code>null</code>
//     * @throws SQLException if the given column name does not match one of
//     *            this rowset's column names or the cursor is not on one of
//     *            this rowset's rows or its insert row
//     */
//    @Override
//    public java.sql.Timestamp getTimestamp(String columnName) throws SQLException {
//        return getTimestamp(getColIdxByName(columnName));
//    }
//
//    /**
//     * Retrieves the value of the designated column in the current row of this
//     * <code>CachedRowSetImpl</code> object as a <code>java.io.InputStream</code>
//     * object.
//     *
//     * A column value can be retrieved as a stream of ASCII characters
//     * and then read in chunks from the stream. This method is particularly
//     * suitable for retrieving large <code>LONGVARCHAR</code> values. The
//     * <code>SyncProvider</code> will rely on the JDBC driver to do any necessary
//     * conversion from the database format into ASCII format.
//     *
//     * <P><B>Note:</B> All the data in the returned stream must
//     * be read prior to getting the value of any other column. The
//     * next call to a <code>getXXX</code> method implicitly closes the stream.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return a Java input stream that delivers the database column value
//     *         as a stream of one-byte ASCII characters.  If the value is SQL
//     *         <code>NULL</code>, the result is <code>null</code>.
//     * @throws SQLException if (1) the given column name is not the name of
//     * a column in this rowset
//     * (2) the cursor is not on one of this rowset's rows or its
//     * insert row, or (3) the designated column does not store an
//     * SQL <code>CHAR, VARCHAR</code>, <code><b>LONGVARCHAR</b></code>
//     * <code>BINARY, VARBINARY</code> or <code>LONGVARBINARY</code> value. The
//     * bold SQL type designates the recommended return types that this method is
//     * used to retrieve.
//     * @see #getAsciiStream(int)
//     */
//    @Override
//    public java.io.InputStream getAsciiStream(String columnName) throws SQLException {
//        return getAsciiStream(getColIdxByName(columnName));
//
//    }
//
//    /**
//     * A column value can be retrieved as a stream of Unicode characters
//     * and then read in chunks from the stream.  This method is particularly
//     * suitable for retrieving large <code>LONGVARCHAR</code> values.
//     * The JDBC driver will do any necessary conversion from the database
//     * format into Unicode.
//     *
//     * <P><B>Note:</B> All the data in the returned stream must
//     * be read prior to getting the value of any other column. The
//     * next call to a <code>getXXX</code> method implicitly closes the stream.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return a Java input stream that delivers the database column value
//     *         as a stream of two-byte Unicode characters.  If the value is
//     *         SQL <code>NULL</code>, the result is <code>null</code>.
//     * @throws SQLException if the given column name does not match one of
//     *            this rowset's column names or the cursor is not on one of
//     *            this rowset's rows or its insert row
//     * @deprecated use the method <code>getCharacterStream</code> instead
//     */
//    @Deprecated
//    @Override
//    public java.io.InputStream getUnicodeStream(String columnName) throws SQLException {
//        return getUnicodeStream(getColIdxByName(columnName));
//    }
//
//    /**
//     * Retrieves the value of the designated column in the current row of this
//     * <code>CachedRowSetImpl</code> object as a <code>java.io.InputStream</code>
//     * object.
//     * <P>
//     * A column value can be retrieved as a stream of uninterpreted bytes
//     * and then read in chunks from the stream.  This method is particularly
//     * suitable for retrieving large <code>LONGVARBINARY</code> values.
//     *
//     * <P><B>Note:</B> All the data in the returned stream must be
//     * read prior to getting the value of any other column. The next
//     * call to a get method implicitly closes the stream. Also, a
//     * stream may return <code>0</code> for <code>CachedRowSetImpl.available()</code>
//     * whether there is data available or not.
//     *
//     * @param columnName a <code>String</code> object giving the SQL name of
//     *        a column in this <code>CachedRowSetImpl</code> object
//     * @return a Java input stream that delivers the database column value
//     *         as a stream of uninterpreted bytes.  If the value is SQL
//     *         <code>NULL</code>, the result is <code>null</code>.
//     * @throws SQLException if (1) the given column name is unknown,
//     * (2) the cursor is not on one of this rowset's rows or its
//     * insert row, or (3) the designated column does not store an
//     * SQL <code>BINARY, VARBINARY</code> or <code><b>LONGVARBINARY</b></code>
//     * The bold type indicates the SQL type that this method is recommened
//     * to retrieve.
//     * @see #getBinaryStream(int)
//     *
//     */
//    @Override
//    public java.io.InputStream getBinaryStream(String columnName) throws SQLException {
//        return getBinaryStream(getColIdxByName(columnName));
//    }
//
//
//    // Advanced features:
//
//    /**
//     * Retrieves the value of the designated column in the current row
//     * of this <code>CachedRowSetImpl</code> object as an
//     * <code>Object</code> value.
//     * <P>
//     * The type of the <code>Object</code> will be the default
//     * Java object type corresponding to the column's SQL type,
//     * following the mapping for built-in types specified in the JDBC 3.0
//     * specification.
//     * <P>
//     * This method may also be used to read datatabase-specific
//     * abstract data types.
//     * <P>
//     * This implementation of the method <code>getObject</code> extends its
//     * behavior so that it gets the attributes of an SQL structured type
//     * as an array of <code>Object</code> values.  This method also custom
//     * maps SQL user-defined types to classes
//     * in the Java programming language. When the specified column contains
//     * a structured or distinct value, the behavior of this method is as
//     * if it were a call to the method <code>getObject(columnIndex,
//     * this.getStatement().getConnection().getTypeMap())</code>.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @return a <code>java.lang.Object</code> holding the column value;
//     *         if the value is SQL <code>NULL</code>, the result is <code>null</code>
//     * @throws SQLException if (1) the given column name does not match one of
//     *            this rowset's column names, (2) the cursor is not
//     *            on a valid row, or (3) there is a problem getting
//     *            the <code>Class</code> object for a custom mapping
//     * @see #getObject(int)
//     */
//    @Override
//    public Object getObject(String columnName) throws SQLException {
//        return getObject(getColIdxByName(columnName));
//    }
//
//    //----------------------------------------------------------------
//
//    /**
//     * Sets the designated nullable column in the current row or the
//     * insert row of this <code>CachedRowSetImpl</code> object with
//     * <code>null</code> value.
//     * <P>
//     * This method updates a column value in the current row or the insert
//     * row of this rowset, but it does not update the database.
//     * If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, or (3) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateNull(String columnName) throws SQLException {
//        updateNull(getColIdxByName(columnName));
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>boolean</code> value.
//     * <P>
//     * This method updates a column value in the current row or the insert
//     * row of this rowset, but it does not update the database.
//     * If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param x the new column value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, or (3) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateBoolean(String columnName, boolean x) throws SQLException {
//        updateBoolean(getColIdxByName(columnName), x);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>byte</code> value.
//     * <P>
//     * This method updates a column value in the current row or the insert
//     * row of this rowset, but it does not update the database.
//     * If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param x the new column value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, or (3) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateByte(String columnName, byte x) throws SQLException {
//        updateByte(getColIdxByName(columnName), x);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>short</code> value.
//     * <P>
//     * This method updates a column value in the current row or the insert
//     * row of this rowset, but it does not update the database.
//     * If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param x the new column value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, or (3) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateShort(String columnName, short x) throws SQLException {
//        updateShort(getColIdxByName(columnName), x);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>int</code> value.
//     * <P>
//     * This method updates a column value in the current row or the insert
//     * row of this rowset, but it does not update the database.
//     * If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param x the new column value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, or (3) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateInt(String columnName, int x) throws SQLException {
//        updateInt(getColIdxByName(columnName), x);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>long</code> value.
//     * <P>
//     * This method updates a column value in the current row or the insert
//     * row of this rowset, but it does not update the database.
//     * If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param x the new column value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, or (3) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateLong(String columnName, long x) throws SQLException {
//        updateLong(getColIdxByName(columnName), x);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>float</code> value.
//     * <P>
//     * This method updates a column value in the current row or the insert
//     * row of this rowset, but it does not update the database.
//     * If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param x the new column value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, or (3) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateFloat(String columnName, float x) throws SQLException {
//        updateFloat(getColIdxByName(columnName), x);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>double</code> value.
//     *
//     * This method updates a column value in either the current row or
//     * the insert row of this rowset, but it does not update the
//     * database.  If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param x the new column value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, or (3) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateDouble(String columnName, double x) throws SQLException {
//        updateDouble(getColIdxByName(columnName), x);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>java.math.BigDecimal</code> object.
//     * <P>
//     * This method updates a column value in the current row or the insert
//     * row of this rowset, but it does not update the database.
//     * If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param x the new column value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, or (3) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
//        updateBigDecimal(getColIdxByName(columnName), x);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>String</code> object.
//     *
//     * This method updates a column value in either the current row or
//     * the insert row of this rowset, but it does not update the
//     * database.  If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param x the new column value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, or (3) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateString(String columnName, String x) throws SQLException {
//        updateString(getColIdxByName(columnName), x);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>byte</code> array.
//     *
//     * This method updates a column value in either the current row or
//     * the insert row of this rowset, but it does not update the
//     * database.  If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param x the new column value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, or (3) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateBytes(String columnName, byte x[]) throws SQLException {
//        updateBytes(getColIdxByName(columnName), x);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>Date</code> object.
//     *
//     * This method updates a column value in either the current row or
//     * the insert row of this rowset, but it does not update the
//     * database.  If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param x the new column value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, (3) the type
//     *            of the designated column is not an SQL <code>DATE</code> or
//     *            <code>TIMESTAMP</code>, or (4) this rowset is
//     *            <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateDate(String columnName, java.sql.Date x) throws SQLException {
//        updateDate(getColIdxByName(columnName), x);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>Time</code> object.
//     *
//     * This method updates a column value in either the current row or
//     * the insert row of this rowset, but it does not update the
//     * database.  If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param x the new column value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, (3) the type
//     *            of the designated column is not an SQL <code>TIME</code> or
//     *            <code>TIMESTAMP</code>, or (4) this rowset is
//     *            <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateTime(String columnName, java.sql.Time x) throws SQLException {
//        updateTime(getColIdxByName(columnName), x);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>Timestamp</code> object.
//     *
//     * This method updates a column value in either the current row or
//     * the insert row of this rowset, but it does not update the
//     * database.  If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param x the new column value
//     * @throws SQLException if the given column index is out of bounds or
//     *            the cursor is not on one of this rowset's rows or its
//     *            insert row
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, (3) the type
//     *            of the designated column is not an SQL <code>DATE</code>,
//     *            <code>TIME</code>, or <code>TIMESTAMP</code>, or (4) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateTimestamp(String columnName, java.sql.Timestamp x) throws SQLException {
//        updateTimestamp(getColIdxByName(columnName), x);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * ASCII stream value.
//     * <P>
//     * This method updates a column value in either the current row or
//     * the insert row of this rowset, but it does not update the
//     * database.  If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param x the new column value
//     * @param length the number of one-byte ASCII characters in the stream
//     */
//    @Override
//    public void updateAsciiStream(String columnName,
//    java.io.InputStream x,
//    int length) throws SQLException {
//        updateAsciiStream(getColIdxByName(columnName), x, length);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>double</code> value.
//     *
//     * This method updates a column value in either the current row or
//     * the insert row of this rowset, but it does not update the
//     * database.  If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param ref the new column <code>java.sql.Ref</code> value
//     * @throws SQLException if (1) the given column name does not match the
//     *        name of a column in this rowset, (2) the cursor is not on
//     *        one of this rowset's rows or its insert row, or (3) this
//     *        rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateRef(String columnName, java.sql.Ref ref) throws SQLException {
//        updateRef(getColIdxByName(columnName), ref);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>double</code> value.
//     *
//     * This method updates a column value in either the current row or
//     * the insert row of this rowset, but it does not update the
//     * database.  If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param c the new column <code>Clob</code> value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, or (3) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateClob(String columnName, Clob c) throws SQLException {
//        updateClob(getColIdxByName(columnName), c);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>java.sql.Blob </code> value.
//     *
//     * This method updates a column value in either the current row or
//     * the insert row of this rowset, but it does not update the
//     * database.  If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param b the new column <code>Blob</code> value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, or (3) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateBlob(String columnName, Blob b) throws SQLException {
//        updateBlob(getColIdxByName(columnName), b);
//    }
//
//    /**
//     * Sets the designated column in either the current row or the insert
//     * row of this <code>CachedRowSetImpl</code> object with the given
//     * <code>java.sql.Array</code> value.
//     *
//     * This method updates a column value in either the current row or
//     * the insert row of this rowset, but it does not update the
//     * database.  If the cursor is on a row in the rowset, the
//     * method {@link #updateRow} must be called to update the database.
//     * If the cursor is on the insert row, the method {@link #insertRow}
//     * must be called, which will insert the new row into both this rowset
//     * and the database. Both of these methods must be called before the
//     * cursor moves to another row.
//     *
//     * @param columnName a <code>String</code> object that must match the
//     *        SQL name of a column in this rowset, ignoring case
//     * @param a the new column <code>Array</code> value
//     * @throws SQLException if (1) the given column name does not match the
//     *            name of a column in this rowset, (2) the cursor is not on
//     *            one of this rowset's rows or its insert row, or (3) this
//     *            rowset is <code>ResultSet.CONCUR_READ_ONLY</code>
//     */
//    @Override
//    public void updateArray(String columnName, Array a) throws SQLException {
//        updateArray(getColIdxByName(columnName), a);
//    }
//
//    /**
//     * Retrieves the value of the designated column in this
//     * <code>CachedRowSetImpl</code> object as a <code>java.net.URL</code> object
//     * in the Java programming language.
//     *
//     * @return a java.net.URL object containing the resource reference described by
//     * the URL
//     * @throws SQLException if (1) the given column name not the name of a column
//     * in this rowset, or
//     * (2) the cursor is not on one of this rowset's rows or its
//     * insert row, or (3) the designated column does not store an
//     * SQL <code>DATALINK</code> value.
//     * @see #getURL(int)
//     */
//    @Override
//    public java.net.URL getURL(String columnName) throws SQLException {
//        return getURL(getColIdxByName(columnName));
//
//    }
//
//}