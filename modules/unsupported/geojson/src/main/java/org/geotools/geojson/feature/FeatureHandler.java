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
import java.util.Collections;
import java.util.List;

import org.geotools.feature.AttributeImpl;
import org.geotools.feature.ComplexFeatureBuilder;
import org.geotools.feature.FeatureBuilder;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.geotools.feature.type.FeatureTypeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.geojson.DelegatingHandler;
import org.geotools.geojson.IContentHandler;
import org.geotools.geojson.geom.GeometryCollectionHandler;
import org.geotools.geojson.geom.GeometryHandler;
import org.json.simple.parser.ParseException;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/** @source $URL$ */
public class FeatureHandler extends DelegatingHandler<Feature> {

    private int fid = 0;

    private String separator = "-";

    String id;

    Geometry geometry;

    List<Object> values;

    List<String> properties;

    CoordinateReferenceSystem crs;

    FeatureBuilder builder;

    AttributeIO attio;

    Feature feature;

    private String baseId = "feature";

    /** should we attempt to automatically build fids */
    private boolean autoFID = false;

    public static final AttributeType ANYTYPE_TYPE =
            new AttributeTypeImpl(
                    /* name: */ new NameImpl("http://www.w3.org/2001/XMLSchema", "anyType"),
                    /* binding: */ java.lang.Object.class,
                    /* identified: */ false,
                    /* abstract: */ false,
                    /* restrictions: */ Collections.<Filter>emptyList(),
                    /* superType: */ null,
                    /* description: */ null);

    public static final AttributeType ANYSIMPLETYPE_TYPE =
            new AttributeTypeImpl(
                    /* name: */ new NameImpl("http://www.w3.org/2001/XMLSchema", "anySimpleType"),
                    /* binding: */ java.lang.Object.class,
                    /* identified: */ false,
                    /* abstract: */ false,
                    /* restrictions: */ Collections.<Filter>emptyList(),
                    /* superType: */ ANYTYPE_TYPE,
                    /* description: */ null);

    public static final AttributeType STRING_TYPE =
            new AttributeTypeImpl(
                    /* name: */ new NameImpl("http://www.w3.org/2001/XMLSchema", "string"),
                    /* binding: */ String.class,
                    /* identified: */ false,
                    /* abstract: */ false,
                    /* restrictions: */ Collections.<Filter>emptyList(),
                    /* superType: */ ANYSIMPLETYPE_TYPE,
                    /* description: */ null);

    public static final AttributeType BOOLEAN_TYPE =
            new AttributeTypeImpl(
                    /* name: */ new NameImpl("http://www.w3.org/2001/XMLSchema", "boolean"),
                    /* binding: */ Boolean.class,
                    /* identified: */ false,
                    /* abstract: */ false,
                    /* restrictions: */ Collections.<Filter>emptyList(),
                    /* superType: */ ANYSIMPLETYPE_TYPE,
                    /* description: */ null);
    
    public static final AttributeType INT_TYPE =
            new AttributeTypeImpl(
                    /* name: */ new NameImpl("http://www.w3.org/2001/XMLSchema", "int"),
                    /* binding: */ Integer.class,
                    /* identified: */ false,
                    /* abstract: */ false,
                    /* restrictions: */ Collections.<Filter>emptyList(),
                    /* superType: */ ANYSIMPLETYPE_TYPE,
                    /* description: */ null);
    
    public static final AttributeType DOUBLE_TYPE =
            new AttributeTypeImpl(
                    /* name: */ new NameImpl("http://www.w3.org/2001/XMLSchema", "double"),
                    /* binding: */ Double.class,
                    /* identified: */ false,
                    /* abstract: */ false,
                    /* restrictions: */ Collections.<Filter>emptyList(),
                    /* superType: */ ANYSIMPLETYPE_TYPE,
                    /* description: */ null);

    // ***************************

    // *** Taken from GMLSchema ***
    public static final AttributeType GEOMETRYPROPERTYTYPE_TYPE = build_GEOMETRYPROPERTYTYPE_TYPE();
    
    private static AttributeType build_GEOMETRYPROPERTYTYPE_TYPE() {
        AttributeType builtType;
        builtType =
                new AttributeTypeImpl(
                        new NameImpl("http://www.opengis.net/gml", "GeometryPropertyType"),
                        com.vividsolutions.jts.geom.Geometry.class,
                        false,
                        false,
                        Collections.<Filter>emptyList(),
                        ANYTYPE_TYPE,
                        null);

        return builtType;
    }

    public static final AttributeType NULLTYPE_TYPE = build_NULLTYPE_TYPE();

    private static AttributeType build_NULLTYPE_TYPE() {
        AttributeType builtType;
        builtType =
                new AttributeTypeImpl(
                        new NameImpl("http://www.opengis.net/gml", "NullType"),
                        java.lang.Object.class,
                        false,
                        false,
                        Collections.<Filter>emptyList(),
                        ANYSIMPLETYPE_TYPE,
                        null);

        return builtType;
    }
    
    public FeatureHandler() {
        this(null, "feature", new DefaultAttributeIO());
    }

    public FeatureHandler(FeatureBuilder builder, String featureName, AttributeIO attio) {
        this.builder = builder;
        this.baseId = featureName;
        this.attio = attio;
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        if (properties == NULL_LIST) {
            properties = new ArrayList();
        } else if (properties != null && !(delegate instanceof ArrayHandler)) {
            // start of a new object in properties means a geometry
            delegate = new ObjectHandler(new GeometryFactory());
        }

        return super.startObject();
    }

    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if ("id".equals(key) && properties == null) {
            id = "";
            return true;
        } else if ("crs".equals(key) && properties == null /* it's top level, not a property */) {
            delegate = new CRSHandler();
            return true;
        } else if ("geometry".equals(key)
                && properties == null /* it's top level, not a property */) {
            delegate = new GeometryHandler(new GeometryFactory());
            return true;
        } else if ("properties".equals(key) && delegate == NULL) {
            properties = NULL_LIST;
            values = new ArrayList();
        } else if (properties != null && delegate == NULL) {
            properties.add(key);
            return true;
        }

        return super.startObjectEntry(key);
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        if (properties != null && delegate == NULL) {
            // array inside of properties
            delegate = new ArrayHandler();
        }

        return super.startArray();
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        if (delegate instanceof ArrayHandler) {
            super.endArray();
            values.add(((ArrayHandler) delegate).getValue());
            delegate = NULL;
        }
        return super.endArray();
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        if (delegate instanceof IContentHandler) {
            ((IContentHandler) delegate).endObject();

            if (delegate instanceof GeometryHandler) {
                Geometry g = ((IContentHandler<Geometry>) delegate).getValue();
                if (g == null
                        && ((GeometryHandler) delegate).getDelegate()
                                instanceof GeometryCollectionHandler) {
                    // this means that the collecetion handler is still parsing objects, continue
                    // to delegate to it
                } else {
                    if (properties != null) {
                        // this is a regular property
                        values.add(g);
                    } else {
                        // its the default geometry
                        geometry = g;
                    }
                    delegate = NULL;
                }
            } else if (delegate instanceof CRSHandler) {
                crs = ((CRSHandler) delegate).getValue();
                delegate = UNINITIALIZED;
            } else if (delegate instanceof ObjectHandler) {
                values.add(((ObjectHandler) delegate).getValue());
                delegate = NULL;
            } else if (delegate instanceof ArrayHandler) {
                values.add(((ArrayHandler) delegate).getValue());
                delegate = NULL;
            }

            return true;
        } else if (delegate == UNINITIALIZED) {
            delegate = NULL;
            return true;
        } else if (properties != null) {
            if (builder == null) {
                // no builder specified, build on the fly
                builder = createBuilder();
            }
            for (int i = 0; i < properties.size(); i++) {
                String att = properties.get(i);
                Object val = values.get(i);

                if (val instanceof String) {
                    val = attio.parse(att, (String) val);
                }
                appendAttribute(att, val);
            }

            properties = null;
            values = null;
            return true;
        } else {
            feature = buildFeature();
            id = null;
            geometry = null;
            properties = null;
            values = null;

            return true;
        }
    }

    private void appendAttribute(String att, Object val) {
        
        AttributeDescriptor attDescriptor =
                new AttributeDescriptorImpl(getAttributeType(val == null ? Object.class : val.getClass()), new NameImpl("EMSA", att), 0, -1, false, null);
        ((ComplexFeatureBuilder)builder).append(new NameImpl("EMSA", att), new AttributeImpl(val, attDescriptor,  null));
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        if (delegate instanceof GeometryHandler && value == null) {
            delegate = NULL;
            return true;
        } else if ("".equals(id)) {
            id = value.toString();
            setFID(id);
            return true;
        } else if (values != null && delegate == NULL) {
            // use the attribute parser
            values.add(value);
            return true;
        }

        return super.primitive(value);
    }

    @Override
    public Feature getValue() {
        return feature;
    }

    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    public void init() {
        feature = null;
    }

    ComplexFeatureBuilder createBuilder() {
        ArrayList<PropertyDescriptor> childSchema = new ArrayList<PropertyDescriptor>();
        /*Name attOne = new NameImpl("ns", "att1");
        AttributeDescriptor attOneDescriptor =
                new AttributeDescriptorImpl(FakeTypes.STRING_TYPE, attOne, 0, -1, false, null);
        childSchema.add(attOneDescriptor);*/
        GeometryDescriptor ptor = null;
        if (properties != null) {
            for (int i = 0; i < properties.size(); i++) {
                String prop = properties.get(i);
                Object valu = values.get(i);
                Name att = new NameImpl("EMSA", prop);
                AttributeDescriptor attDescriptor =
                        new AttributeDescriptorImpl(getAttributeType(valu != null ? valu.getClass() : Object.class), att, 0, -1, false, null);
                childSchema.add(attDescriptor);
                if (geometry != null) {
                    att = new NameImpl("EMSA", "geometry");
                    GeometryType geoType = new GeometryTypeImpl(att, geometry != null ? geometry.getClass() : Object.class, crs, 
                            /* identified: */ false,
                            /* abstract: */ false,
                            /* restrictions: */ Collections.<Filter>emptyList(),
                            /* superType: */ ANYTYPE_TYPE,
                            /* description: */ null);
                    ptor = new GeometryDescriptorImpl(geoType, att, 0, -1, true, null);
                    childSchema.add(ptor);
                }
                

            }
        }
        

        FeatureType childType =
                new FeatureTypeImpl(
                        new NameImpl("EMSA", "countries"),
                        childSchema,
                        ptor,
                        false,
                        null,
                        null,
                        null);
        ComplexFeatureBuilder builder = new ComplexFeatureBuilder(childType);
        /*FeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("feature");
        typeBuilder.setNamespaceURI("http://geotools.org");
        typeBuilder.setCRS(crs);*/


        return builder;
    }

    private AttributeType getAttributeType(Class<?> cls) {
        if (String.class.isAssignableFrom(cls)) {
            return STRING_TYPE;
        }
        if (Integer.class.isAssignableFrom(cls) || Long.class.isAssignableFrom(cls)) {
            return INT_TYPE;
        }
        if (Double.class.isAssignableFrom(cls)) {
            return DOUBLE_TYPE;
        }
        if (Boolean.class.isAssignableFrom(cls)) {
            return BOOLEAN_TYPE;
        }
        if (Geometry.class.isAssignableFrom(cls)) {
            return GEOMETRYPROPERTYTYPE_TYPE;
        }
        return ANYSIMPLETYPE_TYPE;
    }

    void addGeometryType(SimpleFeatureTypeBuilder typeBuilder, Geometry geometry) {
        typeBuilder.add("geometry", geometry != null ? geometry.getClass() : Geometry.class);
        typeBuilder.setDefaultGeometry("geometry");
    }

    Feature buildFeature() {

        FeatureBuilder builder = this.builder != null ? this.builder : createBuilder();
        FeatureType featureType = builder.getFeatureType();
        Feature f = builder.buildFeature(getFID());
        /*if (geometry != null) {
            if (featureType.getGeometryDescriptor() == null) {
                // GEOT-4293, case of geometry coming after properties, we have to retype
                // the builder
                // JD: this is ugly, we should really come up with a better way to store internal
                // state of properties, and avoid creating the builder after the properties object
                // is completed
                SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
                typeBuilder.init(featureType);
                addGeometryType(typeBuilder, geometry);

                featureType = typeBuilder.buildFeatureType();
                FeatureBuilder newBuilder = new ComplexFeatureBuilder(featureType);
                newBuilder.init(f);
                f = newBuilder.buildFeature(getFID());
            }
            ((ComplexFeatureBuilder)builder).append(featureType.getGeometryDescriptor().getName(), new AttributeImpl(geometry, featureType,  null));
        }*/
        incrementFID();
        return f;
    }
    // "{" +
    // " 'type': 'Feature'," +
    // " 'geometry': {" +
    // " 'type': 'Point'," +
    // " 'coordinates': [" + val + "," + val + "]" +
    // " }, " +
    // "' properties': {" +
    // " 'int': 1," +
    // " 'double': " + (double)val + "," +
    // " 'string': '" + toString(val) + "'" +
    // " }," +
    // " 'id':'widgets." + val + "'" +
    // "}";

    /** set the ID to 0 */
    private void resetFID() {
        fid = 0;
    }

    /** Add one to the current ID */
    private void incrementFID() {
        fid = fid + 1;
    }

    private void setFID(String f) {
        int index = f.lastIndexOf('.');
        if (index < 0) {
            index = f.indexOf('-');
            if (index >= 0) {
                separator = "-";
            } else {
                autoFID = false;
                id = f;
                return;
            }
        } else {
            separator = ".";
        }
        baseId = f.substring(0, index);
        try {
            fid = Integer.parseInt(f.substring(index + 1));
        } catch (NumberFormatException e) {
            autoFID = false;
            id = f;
        }
    }

    private String getFID() {
        if (id == null || autoFID) {
            return baseId + separator + fid;
        } else {
            return id;
        }
    }
}
