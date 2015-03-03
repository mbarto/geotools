/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2015, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gml3.bindings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.feature.AttributeImpl;
import org.geotools.feature.ComplexAttributeImpl;
import org.geotools.feature.NameImpl;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.geotools.feature.type.ComplexTypeImpl;
import org.geotools.gml3.GML3TestSupport;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.SchemaLocator;
import org.geotools.xml.XSD;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XSAnyTypeBindingTest extends GML3TestSupport {

    private static final String SAMPLE_CLASS_VALUE = "1.1.1";


    static class LANDCOVER extends XSD {
        /**
         * singleton instance
         */
        private static LANDCOVER instance = new LANDCOVER();

        public static final String NAMESPACE = "http://inspire.ec.europa.eu/schemas/lcv/3.0";

        public static final QName LANDCOVEROBSERVATION = new QName("http://inspire.ec.europa.eu/schemas/lcv/3.0",
                "LandCoverObservation");

        /**
         * private constructor.
         */
        private LANDCOVER() {
        }

        public static LANDCOVER getInstance() {
            return instance;
        }


        
        public String getNamespaceURI() {
            return NAMESPACE;
        }

        /**
         * Returns the location of 'LandCoverVector.xsd'.
         */
        public String getSchemaLocation() {
            return getClass().getResource("LandCoverVector.xsd").toString();
        }

        public SchemaLocator createSchemaLocator() {
            //we explicity return null here because of a circular dependnecy with 
            //gml3 schema... returning null breaks the circle when the schemas are 
            //being built
            return null;
        }
        
        /* Attributes */
    }
    
    class LandCoverConfiguration extends Configuration {
        public LandCoverConfiguration() {
            super(LANDCOVER.getInstance());
        }

        protected void registerBindings(MutablePicoContainer container) {
        }
    }
    
    class MyConfiguration extends Configuration {

        public MyConfiguration() {
            super(LANDCOVER.getInstance());
            addDependency(new GMLConfiguration());
        }
        
    }
    
    @Override
    protected void registerNamespaces(Element root) {
        super.registerNamespaces(root);
        root.setAttribute("xmlns:lcv", "http://inspire.ec.europa.eu/schemas/lcv/3.0");
    }

    
    
    @Override
    protected Configuration createConfiguration() {
        return new MyConfiguration();
    }



    public void testEncode() throws Exception {
        QName observation = LANDCOVER.LANDCOVEROBSERVATION;
        ComplexAttribute myCode = lcvLandCover(observation, SAMPLE_CLASS_VALUE);
        Document dom = encode(myCode, observation);
        print(dom);
        assertEquals("lcv:LandCoverObservation", dom.getDocumentElement().getNodeName());
        assertEquals(1, dom.getDocumentElement().getElementsByTagName("lcv:class").getLength());
        assertNotNull(dom.getDocumentElement().getElementsByTagName("lcv:class").item(0).getFirstChild());
        assertEquals(SAMPLE_CLASS_VALUE,dom.getDocumentElement().getElementsByTagName("lcv:class").item(0).getFirstChild().getNodeValue());
    }
    

    public ComplexAttribute lcvLandCover(QName typeName, String classValue) {
        Name myType = new NameImpl(typeName.getNamespaceURI(), typeName.getLocalPart());

        List<Property> properties = new ArrayList<Property>();
        List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();

        // assume attributes from same namespace as typename

        Name attName = new NameImpl(typeName.getNamespaceURI(), "class");
        // Name name, Class<?> binding, boolean isAbstract, List<Filter> restrictions,
        // PropertyType superType, InternationalString description
        AttributeType p = new AttributeTypeImpl(attName, String.class, false, false, null, null,
                null);
        AttributeDescriptor pd = new AttributeDescriptorImpl(p, attName, 0, 0, false, null);

        propertyDescriptors.add(pd);
        properties.add(new AttributeImpl(classValue, pd, null));

        ComplexTypeImpl at = new ComplexTypeImpl(myType, propertyDescriptors, false, false,
                Collections.EMPTY_LIST, null, null);

        AttributeDescriptorImpl ai = new AttributeDescriptorImpl(at, myType, 0, 0, false, null);

        return new ComplexAttributeImpl(properties, ai, null);
    }
}
