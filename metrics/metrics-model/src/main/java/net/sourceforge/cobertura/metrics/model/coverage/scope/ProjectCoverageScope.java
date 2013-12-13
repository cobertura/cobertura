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

import java.util.EnumMap;
import java.util.regex.Pattern;

/**
 * CoverageScope implementation for Projects, sporting only a project name.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ProjectCoverageScope extends AbstractCoverageScope {

    /**
     * JAXB-friendly constructor.
     */
    public ProjectCoverageScope() {
        super();
    }

    /**
     * Compound constructor invoked by subclasses.
     * The constructor will, in turn, call the template setup method {@code setupPatternMap} to configure
     * the patternMap.
     *
     * @param projectName The human-readable project name.
     * @throws IllegalStateException if the patternMap, populated from the {@code setupPatternMap} method
     *                               held null patterns.
     */
    public ProjectCoverageScope(final String projectName) throws IllegalStateException {
        super(projectName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupPatternMap(final EnumMap<LocationScope, Pattern> patternMap) {
        // Do nothing; the default setup for the patternMap works well in this case
    }
}
