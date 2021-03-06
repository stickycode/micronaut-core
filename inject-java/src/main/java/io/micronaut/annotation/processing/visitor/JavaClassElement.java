/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.annotation.processing.visitor;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.inject.visitor.ClassElement;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/**
 * A class element returning data from a {@link TypeElement}.
 *
 * @author James Kleeh
 * @since 1.0
 */
public class JavaClassElement extends AbstractJavaElement implements ClassElement {

    private final TypeElement classElement;
    private final JavaVisitorContext visitorContext;

    /**
     * @param classElement       The {@link TypeElement}
     * @param annotationMetadata The annotation metadata
     * @param visitorContext The visitor context
     */
    JavaClassElement(TypeElement classElement, AnnotationMetadata annotationMetadata, JavaVisitorContext visitorContext) {
        super(classElement, annotationMetadata);
        this.classElement = classElement;
        this.visitorContext = visitorContext;
    }

    @Override
    public String getName() {
        return classElement.getQualifiedName().toString();
    }

    @Override
    public boolean isAssignable(String type) {
        TypeElement otherElement = visitorContext.getElements().getTypeElement(type);
        if (otherElement != null) {
            Types types = visitorContext.getTypes();
            TypeMirror thisType = types.erasure(classElement.asType());
            TypeMirror thatType = types.erasure(otherElement.asType());
            return types.isAssignable(thisType, thatType);
        }
        return false;
    }
}
