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
package net.sourceforge.cobertura.metrics.api.location;

import net.sourceforge.cobertura.metrics.model.LocationScope;
import net.sourceforge.cobertura.metrics.model.location.SourceLocation;
import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter separating valid from invalid SourceLocation instances.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class SourceLocationFilter implements Filter<SourceLocation> {

    // Internal state
    private static final LocationScope[] COMPARISON_ORDER = {LocationScope.PACKAGE,
            LocationScope.CLASS, LocationScope.METHOD, LocationScope.LINE, LocationScope.SEGMENT};
    private LocationScope scope;
    private boolean acceptAllInProject;
    private SortedMap<LocationScope, Pattern> locationPatternMap;

    /**
     * Creates a SourceLocationFilter instance which accepts all SourceLocations.
     */
    public SourceLocationFilter() {
        setupInternalState(LocationScope.PROJECT);
    }

    /**
     * Creates a SourceLocationFilter instance with the supplied Java regular expression
     * to be applied within package scope. No pattern may be {@code null} or empty.
     *
     * @param packagePattern The regexp to be matched to SourceLocation package properties.
     */
    public SourceLocationFilter(final String packagePattern) {

        // Assign internal state
        setupInternalState(LocationScope.PACKAGE);

        // Compile patterns and check sanity
        compilePattern(LocationScope.PACKAGE, packagePattern);
    }

    /**
     * Creates a SourceLocationFilter instance with the supplied Java regular expressions
     * to be applied within the appropriate scope. No pattern may be {@code null } or empty.
     *
     * @param packagePattern The regexp to be matched to SourceLocation package properties.
     * @param classPattern   The regexp to be matched to SourceLocation class properties.
     */
    public SourceLocationFilter(final String packagePattern,
                                final String classPattern) {

        // Assign internal state
        setupInternalState(LocationScope.CLASS);

        // Compile patterns and check sanity
        compilePattern(LocationScope.PACKAGE, packagePattern);
        compilePattern(LocationScope.CLASS, classPattern);
    }

    /**
     * Creates a SourceLocationFilter instance with the supplied Java regular expressions
     * to be applied within the appropriate scope. No pattern may be {@code null } or empty.
     *
     * @param packagePattern The regexp to be matched to SourceLocation package properties.
     * @param classPattern   The regexp to be matched to SourceLocation class properties.
     * @param methodPattern  The regexp to be matched to SourceLocation method properties.
     */
    public SourceLocationFilter(final String packagePattern,
                                final String classPattern,
                                final String methodPattern) {
        // Assign internal state
        setupInternalState(LocationScope.METHOD);

        // Compile patterns and check sanity
        compilePattern(LocationScope.PACKAGE, packagePattern);
        compilePattern(LocationScope.CLASS, classPattern);
        compilePattern(LocationScope.METHOD, methodPattern);
    }

    /**
     * Creates a SourceLocationFilter instance with the supplied Java regular expressions
     * to be applied within the appropriate scope. No pattern may be {@code null } or empty.
     *
     * @param packagePattern    The regexp to be matched to SourceLocation package properties.
     * @param classPattern      The regexp to be matched to SourceLocation class properties.
     * @param methodPattern     The regexp to be matched to SourceLocation method properties.
     * @param lineNumberPattern The regexp to be matched to SourceLocation line number properties.
     */
    public SourceLocationFilter(final String packagePattern,
                                final String classPattern,
                                final String methodPattern,
                                final String lineNumberPattern) {

        // Assign internal state
        setupInternalState(LocationScope.LINE);

        // Compile patterns and check sanity
        compilePattern(LocationScope.PACKAGE, packagePattern);
        compilePattern(LocationScope.CLASS, classPattern);
        compilePattern(LocationScope.METHOD, methodPattern);
        compilePattern(LocationScope.LINE, lineNumberPattern);
    }

    /**
     * Creates a SourceLocationFilter instance with the supplied Java regular expressions
     * to be applied within the appropriate scope. No pattern may be {@code null } or empty.
     *
     * @param packagePattern       The regexp to be matched to SourceLocation package properties.
     * @param classPattern         The regexp to be matched to SourceLocation class properties.
     * @param methodPattern        The regexp to be matched to SourceLocation method properties.
     * @param lineNumberPattern    The regexp to be matched to SourceLocation line number properties.
     * @param branchSegmentPattern The regexp to be matched to SourceLocation branch segment index properties.
     */
    public SourceLocationFilter(final String packagePattern,
                                final String classPattern,
                                final String methodPattern,
                                final String lineNumberPattern,
                                final String branchSegmentPattern) {

        // Assign internal state
        setupInternalState(LocationScope.SEGMENT);

        // Compile patterns and check sanity
        compilePattern(LocationScope.PACKAGE, packagePattern);
        compilePattern(LocationScope.CLASS, classPattern);
        compilePattern(LocationScope.METHOD, methodPattern);
        compilePattern(LocationScope.LINE, lineNumberPattern);
        compilePattern(LocationScope.SEGMENT, branchSegmentPattern);
    }

    /**
     * Retrieves the lowest LocationScope used to filter incoming SourceLocations.
     * For example, if this SourceLocationFilter has {@code LocationScope.CLASS }, then
     * package name and class name attributes are used to filter SourceLocations (but
     * method, lineNumber and segment attributes are not).
     *
     * @return The lowest scope used to filter SourceLocation instances, implying that Patterns for the given
     * scope and all above it are used to filter SourceLocations.
     */
    public final LocationScope getScope() {
        return scope;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final SourceLocation candidate) {

        if (acceptAllInProject) {
            return true;
        } else {
            for (LocationScope current : COMPARISON_ORDER) {

                // Only compare down to the level defined within this SourceLocationFilter.
                if (current.compareTo(scope) > 0) {
                    break;
                }

                // Compare scope values to actual ones.
                final String toMatch = "" + candidate.get(current);
                final Matcher matcher = locationPatternMap.get(current).matcher(toMatch);

                if (!matcher.matches()) {
                    return false;
                }
            }
        }

        // All OK.
        return true;
    }

    /**
     * Convenience method which translates a string containing dots ("{@code .}"), such as a
     * java package name, to a valid Java Regular Expressions where the dots are interpreted
     * literally.
     *
     * @param patternWithDots The pattern containing verbatim dots (such as "com.foo.bar.some.package").
     * @return The pattern, transformed to a Java Regular Expression requiring the dots verbatim
     * For example, {@code "com.foo.bar.some.package"} would be transformed into
     * {@code "com\\.foo\\.bar\\.some\\.package"}
     */
    public static String escapeDots(final String patternWithDots) {

        // Check sanity
        Validate.notEmpty(patternWithDots, "Cannot handle null or empty patternWithDots argument.");

        // Escape dots and return
        return patternWithDots.replaceAll("\\.", "\\\\.");
    }

    /**
     * Convenience method to generate a Java Regular Expression pattern string which includes the
     * supplied Package, and optionally all subpackages to it.
     *
     * @param thePackage         The Package to acquire a filter for. Cannot be {@code null}.
     * @param includeSubPackages if {@code true}, generates a pattern string which accepts/hits
     *                           subpackages of the supplied Package in addition to the given Package.
     * @return a Java Regular Expression pattern string which includes the
     * supplied Package, and optionally all subpackages to it.
     */
    public static String getPattern(final Package thePackage, boolean includeSubPackages) {

        // Check sanity
        Validate.notNull(thePackage, "Cannot handle null thePackage argument.");

        // Ensure that subpackages can be included if so requested.
        final String exactPackagePattern = escapeDots(thePackage.getName());
        return includeSubPackages ? exactPackagePattern + "(\\.\\w*)*" : exactPackagePattern;
    }

    //
    // Private helpers
    //

    private void setupInternalState(final LocationScope scope) {

        locationPatternMap = new TreeMap<LocationScope, Pattern>();
        acceptAllInProject = scope == LocationScope.PROJECT;
        this.scope = scope;
    }

    private void compilePattern(final LocationScope location, final String javaRegExp) {

        // Check sanity
        Validate.notEmpty(javaRegExp, "Cannot handle null or empty " + location.toString().toLowerCase()
                + " scope java regexp.");

        // All done.
        locationPatternMap.put(location, Pattern.compile(javaRegExp));
    }
}
