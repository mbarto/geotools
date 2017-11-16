/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2017, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.process.vector;

import java.util.NoSuchElementException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.collection.DecoratingSimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.process.ProcessException;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;

/**
 * TODO
 * 
 * @author Mauro Bartolomeoli
 *
 * @source $URL$
 */
@DescribeProcess(title = "Join", description = "TODO")
public class JoinProcess implements VectorProcess {
    @DescribeResult(name = "result", description = "TODO")
    public SimpleFeatureCollection execute(
            @DescribeParameter(name = "features", description = "Input feature collection") SimpleFeatureCollection features)
            throws ProcessException {
        if (features != null) {
            return new JoinedFeatureCollection(features);
            /*
             * try(SimpleFeatureIterator iterator = features.features()) { while (iterator.hasNext()) { SimpleFeature f = iterator.next();
             * System.out.println(f); } }
             */
        }
        return null;
    }

    static class JoinedFeatureCollection extends DecoratingSimpleFeatureCollection {

        SimpleFeatureCollection features;

        SimpleFeatureType schema;

        public JoinedFeatureCollection(SimpleFeatureCollection delegate) {
            super(delegate);

            // Create schema containing the attributes from both the feature collections
            SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
            addAttributesFrom(delegate.getSchema(), tb, null);

            tb.setCRS(delegate.getSchema().getCoordinateReferenceSystem());
            tb.setNamespaceURI(delegate.getSchema().getName().getNamespaceURI());
            tb.setName(delegate.getSchema().getName());
            this.schema = tb.buildFeatureType();
        }

        private void addAttributesFrom(SimpleFeatureType schema, SimpleFeatureTypeBuilder tb,
                String prefix) {
            for (AttributeDescriptor descriptor : schema.getAttributeDescriptors()) {
                if (SimpleFeature.class.isAssignableFrom(descriptor.getType().getBinding())) {
                    addAttributesFrom(
                            (SimpleFeatureType) descriptor.getUserData().get("JoinedFeatureType"),
                            tb, descriptor.getLocalName() + "_");
                } else {
                    if (prefix == null) {
                        tb.add(descriptor);
                    } else {
                        if (descriptor instanceof GeometryDescriptor) {
                            tb.add(prefix + descriptor.getLocalName(),
                                    descriptor.getType().getBinding(), schema.getCoordinateReferenceSystem());
                        } else {
                            AttributeTypeBuilder builder = new AttributeTypeBuilder();
                            builder.setName(prefix + descriptor.getLocalName());
                            builder.setNamespaceURI(descriptor.getName().getNamespaceURI());
                            builder.setNillable(descriptor.isNillable());
                            builder.setBinding(descriptor.getType().getBinding());
                            builder.setMinOccurs(descriptor.getMinOccurs());
                            builder.setMaxOccurs(descriptor.getMaxOccurs());
                            builder.setDefaultValue(descriptor.getDefaultValue());
                            AttributeDescriptor attributeDescriptor = builder.buildDescriptor(
                                    prefix + descriptor.getName(), builder.buildType());
                            tb.add(attributeDescriptor);
                        }
                        
                    }
                }
            }
        }

        @Override
        public SimpleFeatureIterator features() {
            return new JoinedFeatureIterator(delegate.features(), delegate, getSchema(), delegate.getSchema());
        }

        @Override
        public SimpleFeatureType getSchema() {
            return this.schema;
        }
    }

    static class JoinedFeatureIterator implements SimpleFeatureIterator {

        SimpleFeatureIterator delegate;

        SimpleFeatureCollection features;

        SimpleFeatureCollection secondCollection;

        SimpleFeatureBuilder fb;

        SimpleFeature next;
        
        SimpleFeatureType originalSchema;

        int iterationIndex = 0;

        public JoinedFeatureIterator(SimpleFeatureIterator delegate,
                SimpleFeatureCollection features, SimpleFeatureType schema, SimpleFeatureType originalSchema) {
            this.delegate = delegate;
            this.features = features;
            this.originalSchema = originalSchema;
            fb = new SimpleFeatureBuilder(schema);
        }

        public void close() {
            delegate.close();
        }

        public boolean hasNext() {

            while (next == null && delegate.hasNext()) {
                SimpleFeature f = delegate.next();
                addProperties(f, originalSchema, "");
                next = fb.buildFeature(Integer.toString(iterationIndex));
                fb.reset();
                iterationIndex++;
            }
            return next != null;
        }

        private void addProperties(SimpleFeature f, SimpleFeatureType schema, String prefix) {
            for (PropertyDescriptor property : schema.getDescriptors()) {
                if (SimpleFeature.class.isAssignableFrom(property.getType().getBinding())) {
                    addProperties((SimpleFeature)f.getAttribute(property.getName()), 
                            (SimpleFeatureType) property.getUserData().get("JoinedFeatureType"), property.getName().getLocalPart() + "_");
                } else {
                    fb.set(prefix + property.getName(), f.getAttribute(property.getName()));
                }
            }
        }

        public SimpleFeature next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException("hasNext() returned false!");
            }

            SimpleFeature result = next;
            next = null;
            return result;
        }

    }
}
