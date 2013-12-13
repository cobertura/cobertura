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
import net.sourceforge.cobertura.metrics.model.Namespace;
import org.apache.commons.lang3.Validate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Abstract CoverageScope implementation holding most values.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Namespace.COBERTURA_NAMESPACE,
        propOrder = {"name", "patterns"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractCoverageScope implements CoverageScope {

    // Internal state
    @XmlElement(required = true, nillable = false)
    private String name;

    @XmlTransient
    private transient EnumMap<LocationScope, Pattern> patternMap;

    @XmlElementWrapper(name = "patterns", required = true, nillable = false)
    @XmlElement(name = "pattern")
    private List<String> patterns;

    /**
     * JAXB-friendly constructor.
     */
    public AbstractCoverageScope() {
    }

    /**
     * Compound constructor invoked by subclasses.
     * The constructor will, in turn, call the template setup method {@code setupPatternMap} to configure
     * the patternMap.
     *
     * @param name The human-readable name of this AbstractCoverageScope instance,
     *             such as the fully qualified package or class name.
     * @throws java.lang.IllegalStateException if the patternMap, populated from the {@code setupPatternMap} method
     * held null patterns.
     */
    protected AbstractCoverageScope(final String name) throws IllegalStateException {

        // Check sanity
        Validate.notEmpty(name, "Cannot handle null or empty name argument.");

        // Assign internal state
        this.name = name;
        this.patternMap = new EnumMap<LocationScope, Pattern>(LocationScope.class);
        patternMap.put(LocationScope.PROJECT, Pattern.compile("\\*"));

        // Acquire the pattern map and validate its content
        setupPatternMap(patternMap);

        // Copy all Patterns to the patterns List
        for(Map.Entry<LocationScope, Pattern> current : patternMap.entrySet()) {
            patterns.add(current.getValue().pattern());
        }
    }

    /**
     * Template initialization method, invoked from within the constructor of AbstractCoverageScope
     * intended for the concrete subclass to setup the EnumMap relating LocationScope to
     * selection Pattern. The only method legal to call within this class from within the setupPatternMap
     * method is {@code getName() } - all other methods called from within this setupPatternMap method
     * will throw exceptions due to instance state not being initialized.
     *
     * @param patternMap An EnumMap holding one element:
     * {@code patternMap.put(LocationScope.PROJECT, Pattern.compile("\\*")); }
     */
    protected abstract void setupPatternMap(final EnumMap<LocationScope, Pattern> patternMap);

    /**
     * {@inheritDoc}
     */
    @Override
    public Pattern get(final LocationScope scope) {

        if(patternMap == null) {

            // Re-generate the patternMap
            this.patternMap = new EnumMap<LocationScope, Pattern>(LocationScope.class);

            for(int i = 0; i < Math.min(patterns.size(), LocationScope.values().length); i++) {

                // Patterns should not contain null elements.
                patternMap.put(LocationScope.values()[i], Pattern.compile(patterns.get(i)));
            }
        }


        // All done.
        return patternMap.get(scope);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final LocationScope getLowestScope() {
        LocationScope toReturn = null;

        // Simply find the last of the keys.
        for (LocationScope current : patternMap.keySet()) {
            toReturn = current;
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final CoverageScope that) {

        // Check sanity
        if (that == null) {
            return Integer.MIN_VALUE;
        }
        if (that == this) {
            return 0;
        }

        // Compare parts.
        int toReturn = getLowestScope().compareTo(that.getLowestScope());
        if (toReturn == 0) {

            for(LocationScope current : LocationScope.values()) {
                if(current.compareTo(getLowestScope()) > 0) {
                    break;
                }

                toReturn = get(current).pattern().compareTo(that.get(current).pattern());
                if(toReturn != 0) {
                    break;
                }
            }
        }

        // Compare the names
        if (toReturn == 0) {
            toReturn = this.getName().compareTo(that.getName());
        }

        // All done.
        return toReturn;
    }
}
