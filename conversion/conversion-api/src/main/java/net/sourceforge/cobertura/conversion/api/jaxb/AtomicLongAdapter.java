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
package net.sourceforge.cobertura.conversion.api.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * XML adapter for AtomicLong instances.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AtomicLongAdapter extends XmlAdapter<Long, AtomicLong> {

    /**
     * {@inheritDoc}
     */
    @Override
    public AtomicLong unmarshal(final Long v) throws Exception {

        // Handle nulls.
        if(v == null) {
            return null;
        }

        // Convert and return.
        return new AtomicLong(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long marshal(final AtomicLong v) throws Exception {

        // Handle nulls.
        if(v == null) {
            return null;
        }

        // Convert and return.
        return v.get();
    }
}
