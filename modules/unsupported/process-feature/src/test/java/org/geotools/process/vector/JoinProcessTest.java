package org.geotools.process.vector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.TestData;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.Join;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.property.PropertyDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;

public class JoinProcessTest {

    private static final Logger logger = Logger.getLogger("org.geotools.process.feature.gs.JoinProcessTest");
    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
    
    private DataStore data;
    
    @Before
    public void setup() throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("dbtype", "postgis");
        params.put("host", "localhost");
        params.put("port", "5432");
        params.put("database", "geodb");
        params.put("user", "postgres");
        params.put("passwd", "postgres");
        params.put("Expose primary keys", "true");
        data = DataStoreFinder.getDataStore(params);
    }
    
    @Test
    public void testJoin() throws IOException {
        Transaction transaction = new DefaultTransaction();
        Query query = new Query("earthsearch_regions");
        query.getJoins().add(new Join("basin_reviews" ,ff.intersects(ff.property("a.geom"), ff.property("b.geom"))));
        FeatureReader<SimpleFeatureType, SimpleFeature> reader = data.getFeatureReader(query, transaction);
        while(reader.hasNext()) {
            SimpleFeature f = reader.next();
            System.out.println(f.getAttribute("region"));
        }
    }
}
