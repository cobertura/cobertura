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
package net.sourceforge.cobertura.metrics.model.coverage.legacy;

/**
 * Specification for all types which contain branch and source code line coverage metrics.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface BranchAndLineCoverage extends BranchCoverage {

    /**
     * @return The number of valid source code lines measured by this BranchAndLineCoverage instance.
     * This number must be positive, and greater than or equal to the covered line count.
     */
    int getValidLineCount();

    /**
     * @return The number of source code lines traversed by tests and recorded in this BranchAndLineCoverage instance.
     * This number must be positive, but lower than or equal to the valid branch count.
     */
    int getCoveredLineCount();

    /**
     * Retrieves the source code line coverage rate, which is normally the covered line count
     * divided by the valid line count.
     *
     * @return The coverage rate, which should be a double in the range {@code 0 &lt; rate &lt; 1}. Normally,
     * this is calculated as {@code getCoveredLineCount() / getValidLineCount() }. Should the value of
     * {@code getCoveredLineCount() } be 0, this method should return 0.
     *
     * @throws java.lang.IllegalStateException if {@code getValidLineCount() &gt; getCoveredLineCount() }.
     */
    double getLineCoverageRate() throws IllegalStateException;
}
