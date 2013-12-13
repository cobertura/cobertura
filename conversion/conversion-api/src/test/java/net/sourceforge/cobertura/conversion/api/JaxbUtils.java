/*
 * #%L
 * cobertura-conversion-api
 * %%
 * Copyright (C) 2013 Cobertura
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package net.sourceforge.cobertura.conversion.api;

import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class JaxbUtils {

    // Internal state
    private static final String COBERTURA_NAMESPACE = "http://github.com/cobertura";
    private static final JaxbNamespacePrefixResolver namespacePrefixMapper = new JaxbNamespacePrefixResolver();

    static {
        namespacePrefixMapper.put(COBERTURA_NAMESPACE, "cobertura");
    }

    /*
     * Hide constructors for utility classes.
     */
    private JaxbUtils() {
    }

    /**
     * Retrieves a JAXB Marshaller which can marshal the supplied, JAXB-annotated types.
     *
     * @param jaxbAnnotatedTypes Types to be marshalled by the retrieved Marshaller. All types must be JAXB-annotated.
     * @return a JAXB Marshaller which can marshal the supplied, JAXB-annotated types,
     * and which will produce human-readable (rather than compressed) XML.
     */
    public static Marshaller getHumanReadableMarshallerFor(final Class... jaxbAnnotatedTypes) {

        try {
            final JAXBContext ctx = JAXBContext.newInstance(jaxbAnnotatedTypes);
            return se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbUtils.getHumanReadableStandardMarshaller(
                    ctx, namespacePrefixMapper, false);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a DocumentRoot-worthy JAXBElement holding the supplied single object
     * and sporting the supplied QName namespaceURI, localPart and prefix.
     *
     * @param object       The JAXB-annotated object to wrap in a JAXBElement.
     * @param namespaceURI The XML namespace URI.
     * @param localPart    The XML localPart of the returned JAXBElement.
     * @param prefix       The XML namespace prefix of the returned JAXBElement.
     * @param <T>          The type of the object.
     * @return a DocumentRoot-worthy JAXBElement holding the supplied single object.
     */
    public static <T> JAXBElement<T> getDocumentRoot(final T object,
                                                     final String namespaceURI,
                                                     final String localPart,
                                                     final String prefix) {

        final QName qName = new QName(namespaceURI, localPart, prefix);
        return new JAXBElement<T>(qName, (Class<T>) object.getClass(), object);
    }

    /**
     * Creates a DocumentRoot-worthy JAXBElement holding the supplied single object
     * and sporting the standard cobertura XML namespace URI, and the given localPart.
     *
     * @param object       The JAXB-annotated object to wrap in a JAXBElement.
     * @param localPart    The XML localPart of the returned JAXBElement.
     * @param <T>          The type of the object.
     * @return a DocumentRoot-worthy JAXBElement holding the supplied single object.
     */
    public static <T> JAXBElement<T> getDocumentRoot(final T object,
                                                     final String localPart) {
        return getDocumentRoot(object, COBERTURA_NAMESPACE, localPart, "unused");
    }

    public static <T> String marshal(final T toMarshal, final String elementName) {

        StringWriter result;
        try {
            Marshaller marshaller = JaxbUtils.getHumanReadableMarshallerFor(toMarshal.getClass());
            JAXBElement<T> rootElement = JaxbUtils.getDocumentRoot(toMarshal, elementName);

            result = new StringWriter();
            marshaller.marshal(rootElement, result);

            // All Done
            return result.toString();

        } catch (JAXBException e) {
            throw new RuntimeException("Could not marshal [" + elementName + "]", e);
        }
    }

    public static <T> T unmarshalInstance(final String data, final Class<T> expectedType) {

        try {
            // Assume we should only unmarshal a single type.
            final JAXBContext ctx = JAXBContext.newInstance(expectedType);
            final Unmarshaller unmarshaller = ctx.createUnmarshaller();

            // To ignore the namespace and xml element localPart,
            // unmarshal the instance as a JAXBElement.
            final StreamSource streamSource = new StreamSource(new StringReader(data));
            final JAXBElement<T> jaxbElement = unmarshaller.unmarshal(streamSource, expectedType);

            // All done.
            return jaxbElement.getValue();

        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
