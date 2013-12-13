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

import net.sourceforge.cobertura.conversion.api.jaxb.AtomicLongAdapter;
import net.sourceforge.cobertura.metrics.model.Namespace;
import net.sourceforge.cobertura.metrics.model.location.SourceLocation;
import org.apache.commons.lang3.Validate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Record of coverage, linking a SourceLocation with a hitCount (i.e. the number
 * of times an automated test has executed the SourceLocation statement).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Namespace.COBERTURA_NAMESPACE,
        propOrder = {"location", "hitCount"})
@XmlAccessorType(XmlAccessType.FIELD)
public class CoverageRecord implements Serializable, Comparable<CoverageRecord> {

    private static final long serialVersionUID = 0xC0BE101;

    // Internal state
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(AtomicLongAdapter.class)
    private AtomicLong hitCount;

    @XmlElement(required = true, nillable = false)
    private SourceLocation location;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public CoverageRecord() {
    }

    /**
     * Convenience constructor creating a CoverageRecord instance with the supplied
     * SourceLocation and a 0 hitcount.
     *
     * @param location The SourceLocation of this CoverageRecord.
     */
    public CoverageRecord(final SourceLocation location) {
        this(location, 0);
    }

    /**
     * Compound constructor creating a CoverageRecord instance wrapping the supplied data.
     *
     * @param location The SourceLocation of this CoverageRecord.
     * @param hitCount The hitCount, i.e. the number of times that the supplied SourceLocation has been executed by
     *                 automated tests. Cannot be negative.
     */
    public CoverageRecord(final SourceLocation location, final long hitCount) {

        // Check santiy
        Validate.notNull(location, "Cannot handle null location argument.");
        Validate.isTrue(hitCount >= 0, "Cannot handle negative hitCount.");

        // Assign internal state
        this.location = location;
        this.hitCount = new AtomicLong(hitCount);
    }

    /**
     * Increases the hitCount by 1.
     */
    public void addHit() {
        hitCount.incrementAndGet();
    }

    /**
     * Increases the hitCount by the supplied (positive) amount.
     *
     * @param hitCount The hitCount increase. Cannot be negative.
     */
    public void addHits(final long hitCount) {

        // Check sanity
        Validate.isTrue(hitCount >= 0, "Cannot handle negative hitCount argument.");

        // All done.s
        this.hitCount.addAndGet(hitCount);
    }

    /**
     * @return The hitCount, i.e. the number of times that the supplied SourceLocation has been executed by
     * automated tests. Cannot be negative.
     */
    public long getHitCount() {
        return hitCount.get();
    }

    /**
     * @return The SourceLocation of this CoverageRecord.
     */
    public SourceLocation getLocation() {
        return location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return location.toString() + ": " + hitCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final CoverageRecord that) {

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
}
