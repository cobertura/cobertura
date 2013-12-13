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
package net.sourceforge.cobertura.metrics.api.event;

import net.sourceforge.cobertura.metrics.model.location.SourceLocation;
import org.apache.commons.lang3.Validate;

/**
 * Abstract implementation of the SourceLocationListener specification, used
 * to provide a base for subclassing.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractSourceLocationListener implements SourceLocationListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void addExecutionSteps(final SourceLocation location, final int numSteps) {

        // Check sanity
        Validate.notNull(location, "Cannot handle null location argument.");
        Validate.isTrue(numSteps >= 0, "Cannot handle negative numSteps argument. (Got: " + numSteps + ").");

        // Delegate and return.
        addInternalExecutionSteps(location, numSteps);
    }

    /**
     * Adds a given number of execution steps ("hits"/"touches") to the supplied SourceLocation.
     * <p/>
     * The addInternalExecutionSteps method should be implemented to be atomic/synchronized, in regards
     * to the data it updates. In plain english, the data updated by this SourceLocationListener
     * must fully change its internal storage state before this method returns.
     *
     * @param nonNullLocation  The SourceLocation where 1 execution step should be added.
     *                         Guaranteed never to be {@code null}.
     * @param positiveNumSteps The number of execution steps to add to the supplied SourceLocation.
     *                         Guaranteed not to be negative.
     */
    protected abstract void addInternalExecutionSteps(final SourceLocation nonNullLocation,
                                                      final int positiveNumSteps);

    /**
     * {@inheritDoc}
     */
    @Override
    public final void addExecutionStep(final SourceLocation location) {

        // Delegate
        addExecutionSteps(location, 1);
    }
}
