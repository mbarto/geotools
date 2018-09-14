package org.geotools.geojson.feature;

import java.io.IOException;
import org.geotools.geojson.DelegatingHandler;
import org.geotools.geojson.IContentHandler;
import org.geotools.geojson.geom.GeometryHandler;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

import com.vividsolutions.jts.geom.GeometryFactory;

public class ObjectHandler extends DelegatingHandler<Object> {
    GeometryHandler geometryHandler;

    public ObjectHandler(GeometryFactory factory) throws ParseException, IOException {
        delegate = new FeatureHandler();
        delegate.startObjectEntry("properties");
        geometryHandler = new GeometryHandler(factory);
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        boolean res1 = geometryHandler.startObjectEntry(key);
        boolean res2 = delegate.startObjectEntry(key);
        return res1 || res2;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        boolean res1 = geometryHandler.primitive(value);
        boolean res2 = delegate.primitive(value);
        return res1 || res2;
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        boolean res1 = geometryHandler.endObject();
        if (super.endObject()) {
            return delegate.endObject() || res1;
        }
        return res1;
    }

    @Override
    public Object getValue() {
        Object res = geometryHandler.getValue();
        if (res == null) {
            res = super.getValue();
        }
        return res;
    }
}
