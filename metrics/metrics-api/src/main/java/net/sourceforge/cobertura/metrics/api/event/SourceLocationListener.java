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

/**
 * Event listener invoked by instrumented tests when a particular SourceLocation has
 * been hit/touched during an automated test execution.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface SourceLocationListener {

    /**
     * Adds 1 to the recorded execution steps ("hits"/"touches") to the supplied SourceLocation.
     * The addExecutionStep method should be implemented to be atomic/synchronized, in regards
     * to the data state it updates.
     *
     * @param location The SourceLocation where 1 execution step should be added. Cannot be {@code null}.
     */
    void addExecutionStep(SourceLocation location);

    /**
     * Adds a given number of execution steps ("hits"/"touches") to the supplied SourceLocation.
     * The addExecutionSteps method should be implemented to be atomic/synchronized, in regards
     * to the data state it updates.
     *
     * @param location The SourceLocation where 1 execution step should be added.
     *                 Cannot be {@code null}.
     * @param numSteps The number of execution steps to add to the supplied SourceLocation.
     *                 Cannot be less than 1.
     */
    void addExecutionSteps(SourceLocation location, int numSteps);
}
