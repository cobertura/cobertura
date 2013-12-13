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
import net.sourceforge.cobertura.metrics.api.location.SourceLocationFilter;
import net.sourceforge.cobertura.metrics.model.coverage.Rate;

/**
 * Specification for how to calculate test coverage rates. Typical usage
 * would be along the lines in the example shown below:
 * <p/>
 * <pre>
 *     <code>
 *         // Acquire a CoverageCalculator
 *         CoverageCalculator calc = new SomeCoverageCalculator();
 *
 *         // Start running the tests, and add execution steps for
 *         // all SourceLocations the tests.
 *         //
 *         // This is done in quite another form in reality, but
 *         // corresponding pseudo-code is shown below.
 *         UnitTestRunner runner = new SomeParticularTestRunner(config);
 *         List&lt;InstrumentedTest&gt; allTestsInProject = getAllInstrumentedTestsInProject();
 *
 *         for(InstrumentedTest currentTest : allTestsInProject) {
 *
 *             // Add the listener
 *             currentTest.addSourceLocationListener(calc.getSourceLocationListener());
 *
 *             // Now run the test
 *             runner.execute(currentTest);
 *         }
 *
 *         // Create some filters to define which measurements we are interested in.
 *         final SourceLocationFilter somePackageFilter = new SourceLocationFilter("some\\.package\\.name");
 *         final SourceLocationFilter someClassFilter = new SourceLocationFilter("some\\.package\\.name, "SomeClass");
 *
 *         // Calculate some coverage rates.
 *         double allPackageLineCoverage = calc.getCoverage(somePackageFilter, CoverageType.LINE);
 *         double allPackageBranchCoverage = calc.getCoverage(somePackageFilter, CoverageType.BRANCH);
 *         double allClassLineCoverage = calc.getCoverage(someClassFilter, CoverageType.LINE);
 *         double allClassBranchCoverage = calc.getCoverage(someClassFilter, CoverageType.BRANCH);
 *     </code>
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface CoverageCalculator {

    /**
     * Retrieves the coverage rate for the supplied CoverageType.
     *
     * @param filter The SourceLocationFilter defining which CoverageRecords should be
     *               included in the retrieved Rate.
     * @param type   The CoverageType for which coverage is desired.
     * @return the coverage Rate for the given CoverageType.
     */
    Rate getCoverageRate(SourceLocationFilter filter, CoverageType type);

    /**
     * Convenience method, retrieving Coverage rate as a supplied type
     *
     * @param filter The SourceLocationFilter defining which CoverageRecords should be
     *               included in the retrieved Rate.
     * @param type   The CoverageType for which coverage is desired.
     * @return the coverage, given as a value between 0 and 1.
     */
    double getCoverage(SourceLocationFilter filter, CoverageType type);

    /**
     * Retrieves a SourceLocationListener used to record execution steps ("hits"/"touches")
     * of all SourceLocations within tests into this CoverageCalculator.
     *
     * @return a SourceLocationListener dumping its event data updates into this CoverageCalculator.
     */
    SourceLocationListener getSourceLocationListener();
}
