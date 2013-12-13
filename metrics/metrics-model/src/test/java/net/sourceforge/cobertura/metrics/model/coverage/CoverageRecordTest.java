/*
 * #%L
 * cobertura-metrics-model
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
package net.sourceforge.cobertura.metrics.model.coverage;

import net.sourceforge.cobertura.metrics.model.JaxbUtils;
import net.sourceforge.cobertura.metrics.model.location.SourceLocation;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CoverageRecordTest {

    // Shared state
    private SourceLocation location1;
    private SourceLocation location2;

    @Before
    public void setupSharedState() {

        try {
            location1 = getLocation(Rate.class.getMethod("toString", null), 122);
            location2 = getLocation(SourceLocation.class.getMethod("toString", null), 145);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not create SourceLocations", e);
        }
    }

    @Test(expected = IllegalArgumentException.class)
     public void validateExceptionOnNegativeLineNumber() {

        // Act & Assert
        new CoverageRecord(location1, -1);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullSourceLocation() {

        // Act & Assert
        new CoverageRecord(null, 145);
    }

    @Test
    public void validateComparisonAndEquality() {

        // Assemble
        final CoverageRecord c1 = new CoverageRecord(location1, 5);
        final CoverageRecord c2 = new CoverageRecord(location1, 5);
        final CoverageRecord c3 = new CoverageRecord(location2, 3);
        
        // Act & Assert
        Assert.assertNotEquals(c1, c3);
        Assert.assertNotEquals(c1, c2);
        Assert.assertNotSame(c1, c2);
        Assert.assertEquals(0, c1.compareTo(c2));

        Assert.assertFalse(c1.equals(null));
        Assert.assertTrue(c1.equals(c1));
        Assert.assertFalse(c1.equals(new Rate()));

        Assert.assertEquals(0, c1.compareTo(c1));
        Assert.assertEquals(0, c1.compareTo(c2));
        Assert.assertEquals(Integer.MIN_VALUE, c1.compareTo(null));
    }

    @Test
    public void validateStringForm() {

        // Assemble
        final String expected = "net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:122,segment:0";

        // Act & Assert
        Assert.assertEquals(expected, location1.toString());
    }

    @Test
    public void validateHitCountArithmetics() {

        // Assemble
        final CoverageRecord unitUnderTest = new CoverageRecord(location1);

        // Act
        long hitCount1 = unitUnderTest.getHitCount();
        unitUnderTest.addHit();
        long hitCount2 = unitUnderTest.getHitCount();
        unitUnderTest.addHits(3);
        long hitCount3 = unitUnderTest.getHitCount();

        // Assert
        Assert.assertEquals(0, hitCount1);
        Assert.assertEquals(1, hitCount2);
        Assert.assertEquals(4, hitCount3);
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final CoverageRecord unitUnderTest = new CoverageRecord(location1, 5);
        final String expected = XmlTestUtils.readFully("testdata/coverageRecord.xml");

        // Act
        final String result = JaxbUtils.marshal(unitUnderTest, "coverageRecord");
        // System.out.println("Got: " + result);

        // Assert
        final Diff diff = XmlTestUtils.compareXmlIgnoringWhitespace(expected, result);
        Assert.assertTrue("Detailed Diff: " + new DetailedDiff(diff), diff.identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final CoverageRecord expected = new CoverageRecord(location1, 5);
        final String data = XmlTestUtils.readFully("testdata/coverageRecord.xml");

        // Act
        final CoverageRecord unmarshalled = JaxbUtils.unmarshalInstance(data, CoverageRecord.class);

        // Assert
        Assert.assertNotNull(unmarshalled);
        Assert.assertEquals(expected.toString(), unmarshalled.toString());
        Assert.assertEquals(0, expected.compareTo(unmarshalled));
    }

    //
    // Private helpers
    //

    private SourceLocation getLocation(final Method method, final int lineNumber) {

        final String methodName = method.getName();
        final String className = method.getDeclaringClass().getSimpleName();
        final String packageName = method.getDeclaringClass().getPackage().getName();

        // All Done.
        return new SourceLocation(packageName, className, methodName, lineNumber, 0);
    }
}
