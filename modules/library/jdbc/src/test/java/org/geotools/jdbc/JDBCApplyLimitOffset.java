/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2014, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.jdbc;

public abstract class JDBCApplyLimitOffset extends JDBCTestSupport {

    StringBuffer sql = new StringBuffer("SELECT * FROM TABLE");
    
    public void testNegativeLimitIsNotApplied() {
        dataStore.getSQLDialect().applyLimitOffset(sql, -1, 0);
        assertTrue(sql.toString().equalsIgnoreCase("SELECT * FROM TABLE"));
    }
    
    public void testNegativeLimitAndPositiveOffset() {
        dataStore.getSQLDialect().applyLimitOffset(sql, -1, 1);
        assertTrue(checkOffset(sql.toString(), 1));
        assertFalse(checkLimit(sql.toString(), 1, -1));
    }
    
    public void testLimitAndOffset() {
        dataStore.getSQLDialect().applyLimitOffset(sql, 10, 1);
        assertTrue(checkOffset(sql.toString(), 1));
        assertTrue(checkLimit(sql.toString(), 1,  10));
    }
    
    public void testLimitAndNoOffset() {
        dataStore.getSQLDialect().applyLimitOffset(sql, 10, 0);
        assertFalse(checkOffset(sql.toString(), 0));
        assertTrue(checkLimit(sql.toString(), 0,  10));
    }

    protected abstract boolean checkOffset(String sql, int offset);
    protected abstract boolean checkLimit(String sql, int offset, int limit);

}
