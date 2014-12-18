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
package org.geotools.data.sqlserver;

import org.geotools.jdbc.JDBCApplyLimitOffset;
import org.geotools.jdbc.JDBCTestSetup;

public class SQLServerApplyLimitOffset extends JDBCApplyLimitOffset {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new SQLServerTestSetup();
    }

    
    protected boolean checkOffset(String sql, int offset) {
        return sql.toUpperCase().replace(" ", "").contains("_GT_ROW_NUMBER>"+offset);
    }
    
    protected boolean checkLimit(String sql, int offset, int limit) {
        if(offset == 0) {
            return sql.toUpperCase().replace(" ", "").contains("TOP"+limit);
        }
        return sql.toUpperCase().replace(" ", "").contains("_GT_ROW_NUMBER<="+(offset+limit));
    }
}
