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
package net.sourceforge.cobertura.metrics.model.location;

import net.sourceforge.cobertura.metrics.model.LocationScope;
import net.sourceforge.cobertura.metrics.model.Namespace;
import org.apache.commons.lang3.Validate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Simple Source code Location definition, refraining from using java reflection
 * types to reduce class loading requirements.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Namespace.COBERTURA_NAMESPACE,
        propOrder = {"packageName", "className", "methodName", "lineNumber", "branchSegment"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SourceLocation implements Serializable, Comparable<SourceLocation> {

    private static final long serialVersionUID = 0xC0BE001;

    // Internal state
    @XmlElement(required = true, nillable = false)
    private String packageName;

    @XmlElement(required = true, nillable = false)
    private String className;

    @XmlElement(required = true, nillable = false)
    private String methodName;

    @XmlAttribute(required = true)
    private int lineNumber;

    @XmlAttribute(required = false)
    private int branchSegment;

    /**
     * JAXB-friendly constructor.
     */
    public SourceLocation() {
    }

    /**
     * Compound constructor, wrapping the supplied data.
     *
     * @param packageName   The name of the package containing this SourceLocation, corresponding
     *                      to {@code Package.getName() }.
     * @param className     The simple name of the class containing this SourceLocation,
     *                      corresponding to {@code Class.getSimpleName() }.
     * @param methodName    The name of the method containing this SourceLocation, corresponding to
     *                      {@code Method.getName() }.
     * @param lineNumber    The line number in the class containing this SourceLocation.
     * @param branchSegment The branch segment on the lineNumber in the class containing this SourceLocation.
     */
    public SourceLocation(final String packageName,
                          final String className,
                          final String methodName,
                          final int lineNumber,
                          final int branchSegment) {

        // Check sanity
        Validate.notEmpty(packageName, "Cannot handle null or empty packageName argument.");
        Validate.notEmpty(className, "Cannot handle null or empty className argument.");
        Validate.notEmpty(methodName, "Cannot handle null or empty methodName argument.");
        Validate.isTrue(lineNumber >= 0, "Cannot handle negative lineNumber argument.");
        Validate.isTrue(branchSegment >= 0, "Cannot handle negative branchSegment argument.");

        // Assign internal state
        this.packageName = packageName;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.branchSegment = branchSegment;
    }

    /**
     * Convenience constructor, wrapping the supplied data and assigning a branchSegment of 0,
     * indicating the first (only?) branchSegment on the supplied code line.
     *
     * @param packageName The name of the package containing this SourceLocation, corresponding
     *                    to {@code Package.getName() }.
     * @param className   The simple name of the class containing this SourceLocation,
     *                    corresponding to {@code Class.getSimpleName() }.
     * @param methodName  The name of the method containing this SourceLocation, corresponding to
     *                    {@code Method.getName() }.
     * @param lineNumber  The line number in the class containing this SourceLocation.
     */
    public SourceLocation(final String packageName,
                          final String className,
                          final String methodName,
                          final int lineNumber) {

        // Delegate
        this(packageName, className, methodName, lineNumber, 0);
    }

    /**
     * Convenience constructor digging out package class and method names from
     * the supplied Method object.
     *
     * @param method        The non-null Method object holding package, class and method names.
     * @param lineNumber    The line number in the class containing this SourceLocation.
     * @param branchSegment The branch segment on the lineNumber in the class containing this SourceLocation.
     */
    public SourceLocation(final Method method,
                          final int lineNumber,
                          final int branchSegment) {
        // Delegate
        this(method.getDeclaringClass().getPackage().getName(),
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                lineNumber,
                branchSegment);
    }

    /**
     * Convenience constructor digging out package class and method names from
     * the supplied Method object and using a default branchSegment of 0, implying the
     * first (only?) branch on the given line.
     *
     * @param method     The non-null Method object holding package, class and method names.
     * @param lineNumber The line number in the class containing this SourceLocation.
     */
    public SourceLocation(final Method method,
                          final int lineNumber) {
        // Delegate
        this(method, lineNumber, 0);
    }

    /**
     * @return The name of the package containing this SourceLocation, corresponding
     * to {@code Package.getName() }.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @return The simple name of the class containing this SourceLocation,
     * corresponding to {@code Class.getSimpleName() }.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return The name of the method containing this SourceLocation, corresponding to
     * {@code Method.getName() }.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return The line number in the class containing this SourceLocation.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Retrieves the branch segment for this SourceLocation.
     * The branch segment is 0 for a line holding only one statement, and
     * increases by 1 for each additional segment introduced.
     * A new branch segment is introduced whenever an additional branch can
     * be reached within this SourceLocation.
     *
     * @return The branch segment on the lineNumber in the class containing this SourceLocation.
     */
    public int getBranchSegment() {
        return branchSegment;
    }

    /**
     * Alternate accessor method, which delegates the actual accessing to the
     * appropriate getter.
     *
     * @param scope The scope for which the SourceLocation value should be retrieved.
     *              Cannot be null or {@code LocationScope.PROJECT } (which does not make sense).
     * @return A String for package, class or method name and an Integer for line number and branch segment.
     */
    public Object get(final LocationScope scope) {

        // Check sanity
        Validate.notNull(scope, "Cannot handle null scope argument.");
        Validate.isTrue(scope != LocationScope.PROJECT, "'PROJECT' scope argument does not make sense.");

        Object toReturn = null;
        switch (scope) {
            case PACKAGE:
                toReturn = getPackageName();
                break;
            case CLASS:
                toReturn = getClassName();
                break;
            case METHOD:
                toReturn = getMethodName();
                break;
            case LINE:
                toReturn = getLineNumber();
                break;
            case SEGMENT:
                toReturn = getBranchSegment();
                break;
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final SourceLocation that) {

        // Check sanity
        if (that == null) {
            return Integer.MIN_VALUE;
        }
        if (that == this) {
            return 0;
        }

        // All done.
        return toString().compareTo(that.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getPackageName() + "." + getClassName() + "::" + getMethodName() + ",line:" + getLineNumber()
                + ",segment:" + getBranchSegment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getPackageName().hashCode()
                + getClassName().hashCode()
                + getMethodName().hashCode()
                + getLineNumber()
                + getBranchSegment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        // Check sanity
        if (obj == null) {
            return false;
        }

        // All done.
        return (obj instanceof SourceLocation) && (hashCode() == obj.hashCode());
    }
}
