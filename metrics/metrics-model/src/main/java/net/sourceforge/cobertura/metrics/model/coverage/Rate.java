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
package net.sourceforge.cobertura.metrics.model.coverage;

import net.sourceforge.cobertura.metrics.model.Namespace;
import org.apache.commons.lang3.Validate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Simplified specification of a rate, holding maximum and actual numbers as well as
 * a description of the rate type to enable separation and grouping of different rates.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Namespace.COBERTURA_NAMESPACE, propOrder = {"maximum", "actual", "rateType"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Rate implements Serializable, Comparable<Rate> {

    private static final long serialVersionUID = 0xC0BE100;

    // Internal state
    @XmlAttribute(required = true)
    private int maximum;

    @XmlAttribute(required = false)
    private int actual;

    @XmlElement(required = true, nillable = false)
    private String rateType;

    /**
     * JAXB-friendly constructor.
     */
    public Rate() {
        rateType = "unknown";
    }

    /**
     * Compound constructor, creating a Rate wrapping the supplied values.
     *
     * @param maximum  The maximum value for this rate.
     * @param actual   The actual value for this rate.
     * @param rateType The type fo
     */
    public Rate(final int maximum, final int actual, final String rateType) {

        // Check sanity
        Validate.notEmpty(rateType, "Cannot handle null or empty rateType argument.");
        Validate.isTrue(actual >= 0, "Cannot handle negative actual argument.");
        Validate.isTrue(maximum >= 0, "Cannot handle negative maximum argument.");
        Validate.isTrue(maximum >= actual, "maximum argument must be >= actual argument.");

        // Assign internal state
        this.maximum = maximum;
        this.actual = actual;
        this.rateType = rateType;
    }

    /**
     * @return the maximum value for this rate, such as the maximum/valid branch or
     * line count within a java class.
     */
    public int getMaximum() {
        return maximum;
    }

    /**
     * @return the actual value for this rate, such as the covered/executed branch or
     * line count within a java class.
     */
    public int getActual() {
        return actual;
    }

    /**
     * Retrieves the type of this Rate, such as "branch", "line".
     *
     * @return the description of this Rate, such as "branch", "line".
     * The rateType cannot be {@code null} or empty - it is considered a classifier
     * of this rate.
     */
    public String getRateType() {
        return rateType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Rate that) {

        // Check sanity
        if (that == null) {
            return Integer.MIN_VALUE;
        }
        if (that == this) {
            return 0;
        }

        // Compare the values
        int result = getRateType().compareTo(that.getRateType());
        if (result == 0) {
            result = getMaximum() - that.getMaximum();
        }
        if (result == 0) {
            result = getActual() - that.getActual();
        }

        // All done.
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Rate (" + getRateType() + "): [" + getActual() + "/" + getMaximum() + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getRateType().hashCode() + getMaximum() + getActual();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {

        // Check sanity
        if(obj == null) {
            return false;
        }

        // All done.
        return (obj instanceof Rate) && (hashCode() == obj.hashCode());
    }
}
