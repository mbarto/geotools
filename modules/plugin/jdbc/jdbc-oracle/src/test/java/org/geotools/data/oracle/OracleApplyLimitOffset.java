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
package org.geotools.data.oracle;

import org.geotools.jdbc.JDBCApplyLimitOffset;
import org.geotools.jdbc.JDBCTestSetup;

public class OracleApplyLimitOffset extends JDBCApplyLimitOffset {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new OracleTestSetup();
    }

    
    protected boolean checkOffset(String sql, int offset) {
        return sql.toUpperCase().replace(" ", "").contains(">"+offset);
    }
    
    protected boolean checkLimit(String sql, int offset, int limit) {
        return sql.toUpperCase().replace(" ", "").contains("<="+(limit+offset));
    }
}
