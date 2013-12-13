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
 * CoverageScope implementation for a single Package (without any subpackages included).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PackageCoverageScope extends AbstractCoverageScope {

    /**
     * JAXB-friendly constructor.
     */
    public PackageCoverageScope() {
    }

    /**
     * Compound constructor, creating a CoverageScope for a singular Package
     * (i.e. not including subpackages).
     *
     * @param scopePackage The package which should constitute this PackageCoverageScope.
     * @throws IllegalStateException if the patternMap, populated from the {@code setupPatternMap} method
     *                                         held null patterns.
     */
    public PackageCoverageScope(final Package scopePackage) throws IllegalStateException {
        super(scopePackage.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupPatternMap(final EnumMap<LocationScope, Pattern> patternMap) {
        patternMap.put(LocationScope.PACKAGE, Pattern.compile(getName().replaceAll("\\.", "\\\\.")));
    }
}
