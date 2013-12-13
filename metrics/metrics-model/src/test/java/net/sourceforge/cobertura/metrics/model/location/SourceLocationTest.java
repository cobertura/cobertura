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
package net.sourceforge.cobertura.metrics.model.location;

import net.sourceforge.cobertura.metrics.model.JaxbUtils;
import net.sourceforge.cobertura.metrics.model.LocationScope;
import net.sourceforge.cobertura.metrics.model.coverage.Rate;
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
public class SourceLocationTest {

    // Shared state
    private String packageName;
    private String className;
    private String methodName;
    private int lineNumber;
    private int branchSegment;

    @Before
    public void setupSharedState() {

        Method toStringMethod = null;
        try {
            toStringMethod = Rate.class.getMethod("toString", null);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find toString() method in Rate class.", e);
        }

        methodName = toStringMethod.getName();
        className = toStringMethod.getDeclaringClass().getSimpleName();
        packageName = toStringMethod.getDeclaringClass().getPackage().getName();
        lineNumber = 122;
        branchSegment = 0;
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyPackageName() {

        // Act & Assert
        new SourceLocation("", className, methodName, lineNumber, branchSegment);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullPackageName() {

        // Act & Assert
        new SourceLocation(null, className, methodName, lineNumber, branchSegment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyMethodName() {

        // Act & Assert
        new SourceLocation(packageName, className, "", lineNumber, branchSegment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNegativeLineNumber() {

        // Act & Assert
        new SourceLocation(packageName, className, methodName, -42, branchSegment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNegativeBranchSegment() {

        // Act & Assert
        new SourceLocation(packageName, className, methodName, lineNumber, -4);
    }

    @Test
    public void validateComparisonAndEquality() {

        // Assemble
        final SourceLocation sl1 = new SourceLocation(packageName, className, methodName, lineNumber, branchSegment);
        final SourceLocation sl2 = new SourceLocation(packageName, className, methodName, lineNumber, branchSegment);

        // Act & Assert
        Assert.assertEquals(sl1, sl2);
        Assert.assertNotSame(sl1, sl2);
        Assert.assertEquals(0, sl1.compareTo(sl2));

        Assert.assertFalse(sl1.equals(null));
        Assert.assertTrue(sl1.equals(sl1));
        Assert.assertTrue(sl1.equals(sl2));
        Assert.assertFalse(sl1.equals(new Rate()));

        Assert.assertEquals(0, sl1.compareTo(sl1));
        Assert.assertEquals(0, sl1.compareTo(sl2));
        Assert.assertEquals(Integer.MIN_VALUE, sl1.compareTo(null));

        Assert.assertEquals(sl1.hashCode(), sl2.hashCode());
    }

    @Test
    public void validateLocationScopeAddressing() {

        // Assemble
        final SourceLocation unitUnderTest = new SourceLocation(
                packageName, className, methodName, lineNumber, branchSegment);

        // Act & Assert
        Assert.assertEquals(packageName, unitUnderTest.get(LocationScope.PACKAGE));
        Assert.assertEquals(className, unitUnderTest.get(LocationScope.CLASS));
        Assert.assertEquals(methodName, unitUnderTest.get(LocationScope.METHOD));
        Assert.assertEquals(lineNumber, unitUnderTest.get(LocationScope.LINE));
        Assert.assertEquals(branchSegment, unitUnderTest.get(LocationScope.SEGMENT));
    }

    @Test
    public void validateStringForm() {

        // Assemble
        final String expected = "net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:122,segment:0";
        final SourceLocation sl1 = new SourceLocation(packageName, className, methodName, lineNumber, branchSegment);

        // Act & Assert
        Assert.assertEquals(expected, sl1.toString());
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final SourceLocation unitUnderTest = new SourceLocation(
                packageName, className, methodName, lineNumber, branchSegment);
        final String expected = XmlTestUtils.readFully("testdata/sourcelocation.xml");

        // Act
        final String result = JaxbUtils.marshal(unitUnderTest, "sourceLocation");
        // System.out.println("Got: " + result);

        // Assert
        final Diff diff = XmlTestUtils.compareXmlIgnoringWhitespace(expected, result);
        Assert.assertTrue("Detailed Diff: " + new DetailedDiff(diff), diff.identical());
    }
}
