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

import java.io.Serializable;

/**
 * Specification for all types which contain branch coverage metrics.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface BranchCoverage extends Serializable {

    /**
     * @return The number of valid branches measured by this BranchCoverage instance.
     * This number must be positive, and greater than or equal to the covered branch count.
     */
    int getValidBranchCount();

    /**
     * @return The number of branches traversed by tests and recorded in this BranchCoverage instance.
     * This number must be positive, but lower than or equal to the valid branch count.
     */
    int getCoveredBranchCount();

    /**
     * TODO: Move to API, to make the model simpler? (This is just a calculation of known data).
     *
     * Retrieves the branch coverage rate, which is normally the covered branch count
     * divided by the valid branch count.
     *
     * @return The coverage rate, which should be a double in the range {@code 0 &lt; rate &lt; 1}. Normally,
     * this is calculated as {@code getCoveredBranchCount() / getValidBranchCount() }. Should the value of
     * {@code getCoveredBranchCount() } be 0, this method should return 0.
     *
     * @throws java.lang.IllegalStateException if {@code getValidBranchCount() &gt; getCoveredBranchCount() }.
     */
    double getBranchCoverageRate() throws IllegalStateException;
}
