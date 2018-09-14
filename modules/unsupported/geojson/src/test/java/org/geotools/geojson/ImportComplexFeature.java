package org.geotools.geojson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class ImportComplexFeature {

    public static void main(String[] args) {

        try (BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                ImportComplexFeature.class.getResourceAsStream("complex.json"),
                                Charset.forName("UTF-8")))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line + "\n");
            }
            FeatureJSON fjson = new FeatureJSON("feature");
            Reader jsonReader = new StringReader(builder.toString());
            try {
                FeatureCollection<SimpleFeatureType, SimpleFeature> fcoll =
                        fjson.readFeatureCollection(jsonReader);
                DefaultFeatureCollection dfc = new DefaultFeatureCollection(fcoll);
            } catch (IOException e) {
                throw new RuntimeException("cannot read json", e);
            }
            // System.out.print(builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
