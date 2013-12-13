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

import net.sourceforge.cobertura.metrics.model.coverage.Rate;
import net.sourceforge.cobertura.metrics.model.location.SourceLocation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SourceLocationFilterTest {

    // Shared state
    private SourceLocation toStringInRateClass;
    private SourceLocation hashCodeInRateClass;

    @Before
    public void setupSharedState() throws Exception {

        try {
            toStringInRateClass = getLocation(Rate.class.getMethod("toString", null), 122);
            hashCodeInRateClass = getLocation(SourceLocation.class.getMethod("hashCode"), 145);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not create SourceLocations", e);
        }
    }

    @Test
    public void validateStandardJavaRegExps() {

        // Assemble
        final String pkg = Rate.class.getPackage().getName();
        final String escaped = pkg.replaceAll("\\.", "\\\\.");
        final Pattern packageNamePattern = Pattern.compile(pkg);
        final Pattern escapedNamePattern = Pattern.compile(escaped);

        final String invalidPackage = "net.sourceforge_cobertura.metrics.model.coverage";
        final String parentPackage  = "net.sourceforge.cobertura.metrics.model";
        final String subPackage  = "net.sourceforge.cobertura.metrics.model.foobar";

        // Act & Assert
        Assert.assertTrue(escapedNamePattern.matcher(pkg).matches());
        Assert.assertTrue(packageNamePattern.matcher(pkg).matches());
        Assert.assertFalse(escapedNamePattern.matcher(invalidPackage).matches());
        Assert.assertTrue(packageNamePattern.matcher(invalidPackage).matches());
        Assert.assertFalse(escapedNamePattern.matcher(parentPackage).matches());
        Assert.assertFalse(escapedNamePattern.matcher(subPackage).matches());
    }

    @Test
    public void validateConveniencePatternMethods() {

        // Assemble
        final Package ratePackage = Rate.class.getPackage(); // "net.sourceforge.cobertura.metrics.model"
        final String noSubpackagesExpectedPattern = ratePackage.getName().replaceAll("\\.", "\\\\.");
        final String withSubpackagesExpectedPattern = noSubpackagesExpectedPattern + "(\\.\\w*)*";

        final String invalidPackage = "net.sourceforge_cobertura.metrics.model.coverage";
        final String parentPackage  = "net.sourceforge.cobertura.metrics.model";
        final String exactPackage  = "net.sourceforge.cobertura.metrics.model";
        final String subPackage  = "net.sourceforge.cobertura.metrics.model.foobar";

        // Act
        final Pattern noSubpackages = Pattern.compile(SourceLocationFilter.getPattern(ratePackage, false));
        final Pattern withSubpackages = Pattern.compile(SourceLocationFilter.getPattern(ratePackage, true));

        // Assert
        Assert.assertEquals(noSubpackagesExpectedPattern, noSubpackages.pattern());
        Assert.assertEquals(withSubpackagesExpectedPattern, withSubpackages.pattern());

        System.out.println("Got: " + withSubpackages.pattern());
        Assert.assertTrue(withSubpackages.matcher(subPackage).matches());
        Assert.assertFalse(withSubpackages.matcher(parentPackage).matches());
        Assert.assertFalse(withSubpackages.matcher(invalidPackage).matches());
        Assert.assertTrue(withSubpackages.matcher(exactPackage).matches());

        Assert.assertTrue(noSubpackages.matcher(exactPackage).matches());
        Assert.assertFalse(noSubpackages.matcher(subPackage).matches());
        Assert.assertFalse(noSubpackages.matcher(parentPackage).matches());
        Assert.assertFalse(noSubpackages.matcher(invalidPackage).matches());
    }

    @Test
    public void validateRegexpFiltering() {

        // Assemble
        final String packageRegExp = Rate.class.getPackage().getName();
        final String classRegExp = Rate.class.getSimpleName();
        final SourceLocationFilter unitUnderTest = new SourceLocationFilter(packageRegExp, classRegExp, "toString");

        // Act
        final boolean toStringAccept = unitUnderTest.accept(toStringInRateClass);
        final boolean hashCodeAccept = unitUnderTest.accept(hashCodeInRateClass);

        // Assert
        Assert.assertTrue(toStringAccept);
        Assert.assertFalse(hashCodeAccept);
    }

    @Test
    public void validateProjectLevelAcceptanceFiltering() {

        // Assemble
        final SourceLocationFilter unitUnderTest = new SourceLocationFilter();

        // Act
        final boolean toStringAccept = unitUnderTest.accept(toStringInRateClass);
        final boolean hashCodeAccept = unitUnderTest.accept(hashCodeInRateClass);

        // Assert
        Assert.assertTrue(toStringAccept);
        Assert.assertTrue(hashCodeAccept);
    }

    //
    // Private helpers
    //

    private SourceLocation getLocation(final Method method, final int lineNumber) {

        final String methodName = method.getName();
        final String className = method.getDeclaringClass().getSimpleName();
        final String packageName = method.getDeclaringClass().getPackage().getName();

        // All Done.
        return new SourceLocation(packageName, className, methodName, lineNumber, 0);
    }
}
