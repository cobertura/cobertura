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
package net.sourceforge.cobertura.metrics.api.helpers;

import net.sourceforge.cobertura.metrics.api.event.AbstractSourceLocationListener;
import net.sourceforge.cobertura.metrics.model.location.SourceLocation;

import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class DebugSourceLocationListener extends AbstractSourceLocationListener {

    public ConcurrentNavigableMap<SourceLocation, Integer> recordedSteps
            = new ConcurrentSkipListMap<SourceLocation, Integer>();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addInternalExecutionSteps(final SourceLocation nonNullLocation, final int positiveNumSteps) {

        Integer numSteps = recordedSteps.get(nonNullLocation);
        if(numSteps == null) {
            recordedSteps.put(nonNullLocation, positiveNumSteps);
        } else {
            recordedSteps.put(nonNullLocation, numSteps + positiveNumSteps);
        }
    }
}
