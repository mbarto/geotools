/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2010, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.geojson.feature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.ComplexFeatureBuilder;
import org.geotools.feature.FeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.DelegatingHandler;
import org.json.simple.parser.ParseException;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/** @source $URL$ */
public class FeatureCollectionHandler extends DelegatingHandler<Feature>
        implements IFeatureCollectionHandler {

    FeatureBuilder builder;
    AttributeIO attio;
    String featureName;

    Feature feature;
    CoordinateReferenceSystem crs;
    List stack;

    public FeatureCollectionHandler() {
        this(null, "feature", null);
    }

    public FeatureCollectionHandler(FeatureType featureType, String featureName, AttributeIO attio) {
        if (featureType != null) {
            builder = new ComplexFeatureBuilder(featureType);
        }
        this.featureName = featureName;
        if (attio == null) {
            if (featureType != null) {
                attio = new FeatureTypeAttributeIO(featureType);
            } else {
                attio = new DefaultAttributeIO();
            }
        }

        this.attio = attio;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if ("features".equals(key)) {
            delegate = UNINITIALIZED;

            return true;
        } else if ("crs".equals(key)) {
            delegate = new CRSHandler();
            return true;
        }

        return super.startObjectEntry(key);
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        if (delegate == UNINITIALIZED) {
            delegate = new FeatureHandler(builder, featureName, attio);
            if (crs != null) {
                // build might not be initialized yet, since its build for the first feature, if
                // we have already seen a crs, ensure we set it
                ((FeatureHandler) delegate).setCRS(crs);
            }
            // maintain a stack to track when the "features" array ends
            stack = new ArrayList();

            return true;
        }

        // are we handling a feature collection? stack is null otherwise
        if (stack != null) {
            stack.add(null);
        }
        return super.startArray();
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        // are we handling a feature collection? stack is null otherwise
        if (stack != null) {
            if (stack.isEmpty()) {
                // end of features array, clear the delegate
                delegate = NULL;
                return true;
            }

            stack.remove(0);
        }
        return super.endArray();
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        super.endObject();

        if (delegate instanceof FeatureHandler) {
            feature = ((FeatureHandler) delegate).getValue();
            if (feature != null) {
                // check for a null builder, if it is null set it with the feature type
                // from this feature
                if (builder == null) {
                    FeatureType featureType = feature.getType();
                    /*if (featureType.getCoordinateReferenceSystem() == null && crs != null) {
                        // retype with a crs
                        featureType = SimpleFeatureTypeBuilder.retype(featureType, crs);
                    }*/
                    builder = new ComplexFeatureBuilder(featureType);
                }

                ((FeatureHandler) delegate).init();
                // we want to pause at this point
                return false;
            }
        } else if (delegate instanceof CRSHandler) {
            crs = ((CRSHandler) delegate).getValue();
            if (crs != null) {
                delegate = NULL;
            }
        }

        return true;
    }

    @Override
    public void endJSON() throws ParseException, IOException {
        delegate = null;
        feature = null;
        // crs = null; //JD: keep crs around because we need it post parsing json
    }

    //    public boolean hasMoreFeatures() {
    //        return delegate != null;
    //    }

    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    @Override
    public Feature getValue() {
        return feature;
    }
}
