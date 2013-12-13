/*
 * #%L
 * cobertura-annotations-api
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
package net.sourceforge.cobertura.interaction.annotations.api.metrics;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which indicates that a particular construct should be ignored by Cobertura's instrumentation and
 * metrics. CoberturaIgnored will ensure that all code (lines and branches) within its scope are excepted from
 * instrumentation, implying the following semantics when annotating a particular scope:
 * <p/>
 * <dl>
 * <dt>Method annotation</dt>
 * <dd>The lines within the annotated Method will not be instrumented.</dd>
 * <dt>Class annotation</dt>
 * <dd>The lines within the annotated Class will not be instrumented.</dd>
 * <dt>Package annotation</dt>
 * <dd>The lines within the all Classes within the annotated Package will not be instrumented.</dd>
 * </dl>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE})
public @interface CoberturaIgnored {
}
