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
 * Abstract implementation of the BranchCoverage specification, sporting standard implementations and simple state.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Namespace.COBERTURA_NAMESPACE, propOrder = {"validBranchCount", "coveredBranchCount"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractBranchCoverage<T extends AbstractBranchCoverage>
        implements BranchCoverage, Comparable<T> {

    private static final long serialVersionUID = 0xC0BE002;

    // Internal state
    @XmlAttribute(required = false)
    private int coveredBranchCount;

    @XmlAttribute(required = true)
    private int validBranchCount;

    /**
     * JAXB-friendly constructor, which implies that the AbstractBranchCoverage
     * instance is created with 0 valid and covered branch count values.
     */
    public AbstractBranchCoverage() {
    }

    /**
     * Compound constructor, creating an AbstractBranchCoverage instance wrapping the supplied data.
     *
     * @param coveredBranchCount The number of covered branches (execution paths through the source code),
     *                           which must be positive and lesser than or equal to the validBranchCount.
     * @param validBranchCount   The number of valid branches (execution paths through the source code),
     *                           which must be positive and greater than or equal to the coveredBranchCount.
     */
    protected AbstractBranchCoverage(final int coveredBranchCount,
                                     final int validBranchCount) {

        // Assign internal state
        setBranchCounts(coveredBranchCount, validBranchCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getValidBranchCount() {
        return validBranchCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCoveredBranchCount() {
        return coveredBranchCount;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * TODO: Move to API, to make the model simpler? (This is just a calculation of known data).
     */
    @Override
    public double getBranchCoverageRate() throws IllegalStateException {
        return getDefaultCoverageRate(validBranchCount, coveredBranchCount, "validBranchCount", "coveredBranchCount");
    }

    /**
     * Assigns the coveredBranchCount of this AbstractBranchCoverage instance.
     *
     * @param coveredBranchCount The number of covered branches (execution paths through the source code),
     *                           which must be positive and lesser than or equal to the validBranchCount.
     * @throws IllegalArgumentException if the coveredBranchCount parameter is negative or larger than the
     *                                  validBranchCount.
     */
    protected final void setCoveredBranchCount(int coveredBranchCount) throws IllegalArgumentException {

        // Check sanity
        Validate.isTrue(coveredBranchCount >= 0, "Cannot handle negative coveredBranchCount argument.");
        Validate.isTrue(validBranchCount >= coveredBranchCount, "coveredBranchCount argument must be >= validBranchCount (Got coveredBranchCount:"
                + " " + coveredBranchCount + ", validBranchCount: " + validBranchCount + ").");

        // Assign internal state
        this.coveredBranchCount = coveredBranchCount;
    }

    /**
     * Assigns the coveredBranchCount and validBranchCount counters.
     *
     * @param coveredBranchCount The number of covered branches (execution paths through the source code),
     *                           which must be positive and lesser than or equal to the validBranchCount.
     * @param validBranchCount   The number of valid branches (execution paths through the source code),
     *                           which must be positive and greater than or equal to the coveredBranchCount.
     * @throws IllegalArgumentException if any argument is negative or coveredBranchCount is larger
     *                                  than validBranchCount.
     */
    protected final void setBranchCounts(final int coveredBranchCount, final int validBranchCount)
            throws IllegalArgumentException {

        // Check sanity
        Validate.isTrue(validBranchCount >= 0, "Cannot handle negative validBranchCount argument.");

        // Assign internal state
        this.validBranchCount = validBranchCount;
        setCoveredBranchCount(coveredBranchCount);
    }

    /**
     * Retrieves the coverage rate for the supplied data.
     *
     * @param maxCount        The maximum/valid count of lines or branches.
     * @param actualCount     The actual/registered count of lines or branches.
     * @param maxCountTerm    The term for the maximum count, such as "validBranchCount".
     * @param actualCountTerm The term for the actual count, such as "coveredBranchCount".
     * @return the coverage percentage, as a number {@code 0 &lt; x &lt; 1}.
     * @throws java.lang.IllegalArgumentException if {@code maxCount &lt; actualCount }.
     */
    protected static double getDefaultCoverageRate(final int maxCount,
                                                   final int actualCount,
                                                   final String maxCountTerm,
                                                   final String actualCountTerm)
            throws IllegalArgumentException {

        final String maxStatement = maxCountTerm + " [" + maxCount + "]";
        final String actualStatement = actualCountTerm + " [" + actualCount + "]";

        // Check sanity
        Validate.isTrue(maxCount >= 0, "Cannot handle negative " + maxStatement + ".");
        Validate.isTrue(actualCount >= 0, "Cannot handle negative " + actualStatement + ".");
        if (maxCount == 0) {

            if (actualCount != 0) {
                throw new IllegalStateException(maxStatement + " is invalid for " + actualStatement + ".");
            }

            // Both counts are 0; there are no branches and no coverage.
            return 0;
        }
        if (maxCount < actualCount) {
            throw new IllegalStateException(maxStatement + " should never be < " + actualStatement + ".");
        }

        // All done.
        return ((double) actualCount) / ((double) maxCount);
    }
}
