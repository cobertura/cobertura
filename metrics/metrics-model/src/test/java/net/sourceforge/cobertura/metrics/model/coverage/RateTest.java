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
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class RateTest {

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyType() {

        // Act & Assert
        new Rate(100, 10, "");
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullType() {

        // Act & Assert
        new Rate(100, 10, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNegativeActual() {

        // Act & Assert
        new Rate(100, -10, "line_coverage");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNegativeMax() {

        // Act & Assert
        new Rate(-100, 10, "line_coverage");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnMaxLesserThanActual() {

        // Act & Assert
        new Rate(5, 10, "line_coverage");
    }

    @Test
    public void validateComparisonAndEquality() {

        // Assemble
        final int maximum = 100;
        final int actual = 10;
        final String type = "line_coverage";

        final Rate rate1 = new Rate(maximum, actual, type);
        final Rate rate2 = new Rate(maximum, actual, type);

        // Act & Assert
        Assert.assertEquals(rate1, rate2);
        Assert.assertNotSame(rate1, rate2);
        Assert.assertEquals(0, rate1.compareTo(rate2));

        Assert.assertFalse(rate1.equals(null));
        Assert.assertTrue(rate1.equals(rate1));
        Assert.assertTrue(rate1.equals(rate2));
        Assert.assertFalse(rate1.equals(new Rate()));

        Assert.assertEquals(0, rate1.compareTo(rate1));
        Assert.assertEquals(0, rate1.compareTo(rate2));
        Assert.assertEquals(Integer.MIN_VALUE, rate1.compareTo(null));

        Assert.assertEquals(rate1.hashCode(), rate2.hashCode());
    }

    @Test
    public void validateStringForm() {

        // Assemble
        final int maximum = 100;
        final int actual = 10;
        final String type = "line_coverage";
        final String expected = "Rate (" + type + "): [" + actual + "/" + maximum + "]";

        final Rate rate1 = new Rate(maximum, actual, type);

        // Act & Assert
        Assert.assertEquals(expected, rate1.toString());
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final Rate unitUnderTest = new Rate(100, 10, "line_coverage");
        final String expected = XmlTestUtils.readFully("testdata/rate.xml");

        // Act
        final String result = JaxbUtils.marshal(unitUnderTest, "rate");

        // Assert
        final Diff diff = XmlTestUtils.compareXmlIgnoringWhitespace(expected, result);
        Assert.assertTrue("Detailed Diff: " + new DetailedDiff(diff), diff.identical());
    }
}
