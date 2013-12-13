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
package net.sourceforge.cobertura.conversion.api.jaxb;

import net.sourceforge.cobertura.conversion.api.JaxbUtils;
import net.sourceforge.cobertura.conversion.api.jaxb.helpers.Person;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AtomicLongAdapterTest {

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String name = "Lennart";
        final AtomicLong age = new AtomicLong(45);

        final Person unitUnderTest = new Person(name, age);
        final String expected = XmlTestUtils.readFully("testdata/person.xml");

        // Act
        final String result = JaxbUtils.marshal(unitUnderTest, "person");
        // System.out.println("Got: " + result);

        // Assert
        final Diff diff = XmlTestUtils.compareXmlIgnoringWhitespace(expected, result);
        Assert.assertTrue("Detailed Diff: " + new DetailedDiff(diff), diff.identical());
    }

    @Test
    public void validateUnmarshalling() {

        // Assemble
        final String name = "Lennart";
        final AtomicLong age = new AtomicLong(45);

        final Person expected = new Person(name, age);
        final String data = XmlTestUtils.readFully("testdata/person.xml");

        // Act
        final Person unmarshalled = JaxbUtils.unmarshalInstance(data, Person.class);

        // Assert
        Assert.assertNotNull(unmarshalled);
        Assert.assertEquals(expected.toString(), unmarshalled.toString());
        Assert.assertEquals(expected, unmarshalled);
    }
}
