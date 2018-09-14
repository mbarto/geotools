package org.geotools.geojson.feature;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.geotools.geojson.GeoJSONUtil;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;

/** @source $URL$ */
public class FeatureTypeAttributeIO implements AttributeIO {

    HashMap<String, AttributeIO> ios = new HashMap();

    public FeatureTypeAttributeIO(FeatureType featureType) {
        for (PropertyDescriptor ad : featureType.getDescriptors()) {
            AttributeIO io = null;
            if (Date.class.isAssignableFrom(ad.getType().getBinding())) {
                io = new DateAttributeIO();
            } else {
                io = new DefaultAttributeIO();
            }
            ios.put(ad.getName().getLocalPart(), io);
        }
    }

    public String encode(String att, Object value) {
        return ios.get(att).encode(att, value);
    }

    public Object parse(String att, String value) {
        return ios.get(att).parse(att, value);
    }

    static class DateAttributeIO implements AttributeIO {

        public String encode(String att, Object value) {
            return GeoJSONUtil.dateFormatter.format((Date) value);
        }

        public Object parse(String att, String value) {
            try {
                final SimpleDateFormat sdf = new SimpleDateFormat(GeoJSONUtil.DATE_FORMAT);
                return sdf.parse(value);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
