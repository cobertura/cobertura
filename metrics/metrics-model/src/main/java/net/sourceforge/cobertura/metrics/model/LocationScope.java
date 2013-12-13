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
package net.sourceforge.cobertura.metrics.model;

import net.sourceforge.cobertura.metrics.model.Namespace;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Scope definition for filtering/ordering SourceLocations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
@XmlType(namespace = Namespace.COBERTURA_NAMESPACE)
@XmlEnum
public enum LocationScope {

    /**
     * Full project scope, indicating all SourceLocations within the project.
     */
    PROJECT,

    /**
     * Package scope, indicating all SourceLocations within a given package (not including subpackages).
     */
    PACKAGE,

    /**
     * Class scope, indicating all SourceLocations within a supplied Class.
     * Inner classes are not included within the scope of their enclosing Class.
     */
    CLASS,

    /**
     * Method scope, indicating all SourceLocations within a supplied Method.
     * Closures are not included within the scope of their enclosing Method.
     */
    METHOD,

    /**
     * Line scope, indicating all SourceLocations within a supplied Line.
     */
    LINE,

    /**
     * Segment scope (atomic), indicating the SourceLocation of a supplied branch segment within a given Line.
     */
    SEGMENT
}
