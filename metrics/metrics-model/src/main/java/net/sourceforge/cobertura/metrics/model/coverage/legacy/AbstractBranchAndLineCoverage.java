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
package net.sourceforge.cobertura.metrics.model.coverage.legacy;

import net.sourceforge.cobertura.metrics.model.Namespace;
import org.apache.commons.lang3.Validate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Abstract implementation of the BranchAndLineCoverage specification, sporting standard implementations
 * and simple state.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Namespace.COBERTURA_NAMESPACE, propOrder = {"validLineCount", "coveredLineCount"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractBranchAndLineCoverage<T extends AbstractBranchAndLineCoverage>
        extends AbstractBranchCoverage<T> implements BranchAndLineCoverage {

    private static final long serialVersionUID = 0xC0BE003;

    // Internal state
    @XmlAttribute(required = false)
    private int coveredLineCount;

    @XmlAttribute(required = true)
    private int validLineCount;

    /**
     * JAXB-friendly constructor, which implies that the AbstractBranchAndLineCoverage
     * instance is created with 0 valid and covered branch and line count values.
     */
    public AbstractBranchAndLineCoverage() {
    }

    /**
     * Compound constructor, creating an AbstractBranchAndLineCoverage instance wrapping the supplied data.
     *
     * @param coveredBranchCount The number of covered branches (execution paths through the source code),
     *                           which must be positive and lesser than or equal to the validBranchCount.
     * @param validBranchCount   The number of valid branches (execution paths through the source code),
     *                           which must be positive and greater than or equal to the coveredBranchCount.
     */
    protected AbstractBranchAndLineCoverage(final int coveredBranchCount,
                                            final int validBranchCount,
                                            final int coveredLineCount,
                                            final int validLineCount) {

        super(coveredBranchCount, validBranchCount);

        // Assign internal state
        setLineCounts(coveredLineCount, validLineCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLineCoverageRate() throws IllegalStateException {
        return getDefaultCoverageRate(validLineCount, coveredLineCount, "validLineCount", "coveredLineCount");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCoveredLineCount() {
        return coveredLineCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getValidLineCount() {
        return validLineCount;
    }

    /**
     * Assigns the coveredLineCount of this AbstractBranchAndLineCoverage instance.
     *
     * @param coveredLineCount The number of covered branches (execution paths through the source code),
     *                         which must be positive and lesser than or equal to the validLineCount.
     * @throws IllegalArgumentException if the coveredLineCount parameter is negative or larger than the
     *                                  validLineCount.
     */
    protected final void setCoveredLineCount(int coveredLineCount) throws IllegalArgumentException {

        // Check sanity
        Validate.isTrue(coveredLineCount >= 0, "Cannot handle negative coveredLineCount argument.");
        Validate.isTrue(validLineCount >= coveredLineCount, "coveredLineCount argument must be >= validLineCount (Got"
                + " coveredBranchCount: " + coveredLineCount + ", validLineCount: " + validLineCount + ").");

        // Assign internal state
        this.coveredLineCount = coveredLineCount;
    }

    /**
     * Assigns the coveredLineCount and validLineCount counters.
     *
     * @param coveredLineCount The number of covered branches (execution paths through the source code),
     *                         which must be positive and lesser than or equal to the validLineCount.
     * @param validLineCount   The number of valid branches (execution paths through the source code),
     *                         which must be positive and greater than or equal to the coveredLineCount.
     * @throws IllegalArgumentException if any argument is negative or coveredLineCount is larger
     *                                  than validLineCount.
     */
    protected final void setLineCounts(final int coveredLineCount, final int validLineCount)
            throws IllegalArgumentException {

        // Check sanity
        Validate.isTrue(validLineCount >= 0, "Cannot handle negative validLineCount argument.");

        // Assign internal state
        this.validLineCount = validLineCount;
        setCoveredLineCount(coveredLineCount);
    }
}
