/*
 * #%L
 * cobertura-metrics-api
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
package net.sourceforge.cobertura.metrics.api;

import net.sourceforge.cobertura.metrics.api.event.SourceLocationListener;
import net.sourceforge.cobertura.metrics.api.helpers.DebugCoverageCalculator;
import net.sourceforge.cobertura.metrics.api.location.SourceLocationFilter;
import net.sourceforge.cobertura.metrics.model.coverage.Rate;
import net.sourceforge.cobertura.metrics.model.location.SourceLocation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class T_AbstractCoverageCalculatorTest {

    // Shared state
    private DebugCoverageCalculator unitUnderTest;
    private CoverageType coverageType = CoverageType.LINE;
    private SourceLocationFilter toStringFilter;
    private SourceLocationFilter rateClassFilter;

    @Before
    public void setupSharedState() throws Exception {

        unitUnderTest = new DebugCoverageCalculator();
        final SourceLocationListener sourceLocationListener = unitUnderTest.getSourceLocationListener();

        // Add some state to the DebugCoverageCalculator.
        for(int i = 0; i < 20; i += 2) {

            final String methodName = i < 9 ? "toString" : "hashCode";
            final SourceLocation location = getLocation(Rate.class.getMethod(methodName, null), i);

            sourceLocationListener.addExecutionSteps(location, i % 5);
        }

        // Create the filters.
        final String ratePackageName = Rate.class.getPackage().getName().replaceAll("\\.", "\\\\.");
        final String rateClassName = Rate.class.getSimpleName();

        toStringFilter = new SourceLocationFilter(ratePackageName, rateClassName, "toString");
        rateClassFilter = new SourceLocationFilter(ratePackageName, rateClassName);

        /*
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:0,segment:0: 0,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:2,segment:0: 2,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:4,segment:0: 4,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:6,segment:0: 1,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:8,segment:0: 3

        net.sourceforge.cobertura.metrics.model.coverage.Rate::hashCode,line:10,segment:0: 0,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::hashCode,line:12,segment:0: 2,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::hashCode,line:14,segment:0: 4,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::hashCode,line:16,segment:0: 1,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::hashCode,line:18,segment:0: 3,
         */
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullSourceLocationFilter() {

        // Act & Assert
        unitUnderTest.getCoverage(null, coverageType);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullCoverageType() {

        // Act & Assert
        unitUnderTest.getCoverage(rateClassFilter, null);
    }

    @Test
    public void validateCoverageOnToStringMethod() {

        /*
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:0,segment:0: 0,  <--- miss!
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:2,segment:0: 2,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:4,segment:0: 4,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:6,segment:0: 1,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:8,segment:0: 3
         */

        // Assemble
        final int expectedMax = 5;
        final int expectedHits = 4;

        // Act
        final Rate coverageRate = unitUnderTest.getCoverageRate(toStringFilter, CoverageType.LINE);
        final double coverage = unitUnderTest.getCoverage(toStringFilter, CoverageType.LINE);

        // Assert
        Assert.assertEquals(expectedMax, coverageRate.getMaximum());
        Assert.assertEquals(expectedHits, coverageRate.getActual());
        Assert.assertEquals(((double) expectedHits)/((double) expectedMax), coverage, 0.1d);
    }

    @Test
    public void validateCoverageOnRateClass() {

        /*
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:0,segment:0: 0,  <-- Miss!
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:2,segment:0: 2,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:4,segment:0: 4,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:6,segment:0: 1,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::toString,line:8,segment:0: 3

        net.sourceforge.cobertura.metrics.model.coverage.Rate::hashCode,line:10,segment:0: 0, <-- Miss!
        net.sourceforge.cobertura.metrics.model.coverage.Rate::hashCode,line:12,segment:0: 2,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::hashCode,line:14,segment:0: 4,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::hashCode,line:16,segment:0: 1,
        net.sourceforge.cobertura.metrics.model.coverage.Rate::hashCode,line:18,segment:0: 3,
         */

        // Assemble
        final double expectedClassCoverage = 8d/10d;

        // Act
        final double classCoverage = unitUnderTest.getCoverage(rateClassFilter, coverageType);

        // Assert
        Assert.assertEquals(expectedClassCoverage, classCoverage, 0.1d);
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
