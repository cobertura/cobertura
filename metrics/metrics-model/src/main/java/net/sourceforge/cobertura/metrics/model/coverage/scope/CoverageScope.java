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
package net.sourceforge.cobertura.metrics.model.coverage.scope;

import net.sourceforge.cobertura.metrics.model.LocationScope;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Specification for how to aggregate CoverageRecords into a sensibly
 * sized scope, typically used for generating reports or validation criteria.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface CoverageScope extends Serializable, Comparable<CoverageScope> {

    /**
     * Path element separator.
     */
    String SEPARATOR = "/";

    /**
     * Retrieves a Pattern for the supplied LocationScope; the Pattern is used
     * to identify all SourceLocations within this CoverageScope.
     *
     * @param scope The LocationScope for which the identifying Pattern should be acquired.
     * @return an identifying Pattern for the supplied LocationScope, or {@code null} if no
     * Pattern was found for this CoverageScope.
     */
    Pattern get(LocationScope scope);

    /**
     * Retrieves the LocationScope of this CoverageScope, implying the smallest
     * syntactic scope for which this CoverageScope has Pattern definitions.
     * As an example, if a CoverageScope is of scope {@code LocationScope.CLASS},
     * then the {@code getPatterns() } method must have non-null Pattern values for
     * all LocationScopes down to the LocationScope.CLASS.
     *
     * @return The lowest scope for which this CoverageScope has a pattern definition.
     */
    LocationScope getLowestScope();

    /**
     * @return The human-readable name for this CoverageScope, such as the
     * fully qualified package or class name.
     */
    String getName();
}
