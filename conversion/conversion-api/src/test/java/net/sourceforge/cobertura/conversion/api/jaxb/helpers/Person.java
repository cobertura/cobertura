/*
 * #%L
 * cobertura-conversion-api
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
package net.sourceforge.cobertura.conversion.api.jaxb.helpers;

import net.sourceforge.cobertura.conversion.api.jaxb.AtomicLongAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = "http://cobertura/tests", propOrder = {"name", "age"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Person {

    // Internal state
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(AtomicLongAdapter.class)
    private AtomicLong age;

    @XmlElement(required = true, nillable = false)
    private String name;

    public Person() {
    }

    public Person(String name, AtomicLong age) {
        this.age = age;
        this.name = name;
    }

    public AtomicLong getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return name.hashCode() + age.hashCode();
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
        if(obj == this) {
            return true;
        }

        // All done.
        if(!(obj instanceof Person)) {
            return false;
        }

        final Person that = (Person) obj;
        return getName().equals(that.getName()) && getAge().get() == that.getAge().get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Person [" + getName() + ", " + getAge() + "]";
    }
}
