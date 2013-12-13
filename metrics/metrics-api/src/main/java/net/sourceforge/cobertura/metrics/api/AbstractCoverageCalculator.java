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

import net.sourceforge.cobertura.metrics.api.location.SourceLocationFilter;
import net.sourceforge.cobertura.metrics.model.coverage.Rate;
import org.apache.commons.lang3.Validate;

/**
 * Abstract implementation of a CoverageCalculator, providing
 * some utility method implementations and synchronization primitives.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractCoverageCalculator implements CoverageCalculator {

    /**
     * {@inheritDoc}
     */
    @Override
    public final Rate getCoverageRate(final SourceLocationFilter filter, final CoverageType type) {

        // Check sanity
        Validate.notNull(type, "Cannot handle null type argument.");
        Validate.notNull(filter, "Cannot handle null filter argument.");

        // Delegate
        return getRate(filter, type);
    }

    /**
     * Retrieves the coverage rate for the supplied CoverageType.
     * <p/>
     * The getRate method should be implemented to be atomic/synchronized, in regards
     * to any call to the addExecutionStep methods. In plain english, the internal state of this
     * AbstractCoverageCalculator subclass must be permitted to complete changing its internal
     * storage state before enabling the {@code getRate } method to calculate its value.
     *
     * @param nonNullCoverageType The CoverageType for which coverage is desired. Guaranteed not to be {@code null}.
     * @param nonNullFilter       The SourceLocationFilter instance used to filter out all relevant CoverageRate
     *                            object on record, and collect a correct Rate from them all.
     * @return the coverage Rate for the given CoverageType.
     */
    protected abstract Rate getRate(final SourceLocationFilter nonNullFilter, final CoverageType nonNullCoverageType);

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getCoverage(final SourceLocationFilter filter, final CoverageType type) {

        // Delegate
        final Rate coverageRate = getCoverageRate(filter, type);

        // Handle fringe cases.
        if (coverageRate == null) {
            return 0;
        }

        // Divide and return.
        return ((double) coverageRate.getActual()) / ((double) coverageRate.getMaximum());
    }
}
