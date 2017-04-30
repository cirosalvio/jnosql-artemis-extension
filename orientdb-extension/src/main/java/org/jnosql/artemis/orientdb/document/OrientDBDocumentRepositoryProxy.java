/*
 * Copyright 2017 Otavio Santana and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jnosql.artemis.orientdb.document;


import org.jnosql.artemis.Repository;
import org.jnosql.artemis.document.DocumentTemplate;
import org.jnosql.artemis.document.query.AbstractDocumentRepository;
import org.jnosql.artemis.document.query.AbstractDocumentRepositoryProxy;
import org.jnosql.artemis.document.query.DocumentQueryDeleteParser;
import org.jnosql.artemis.document.query.DocumentQueryParser;
import org.jnosql.artemis.reflection.ClassRepresentation;
import org.jnosql.artemis.reflection.ClassRepresentations;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class OrientDBDocumentRepositoryProxy<T> extends AbstractDocumentRepositoryProxy<T> {

    private final Class<T> typeClass;

    private final OrientDBTemplate template;

    private final DocumentRepository repository;

    private final ClassRepresentation classRepresentation;

    private final DocumentQueryParser queryParser;

    private final DocumentQueryDeleteParser deleteParser;


    OrientDBDocumentRepositoryProxy(OrientDBTemplate template, ClassRepresentations classRepresentations, Class<?> repositoryType) {
        this.template = template;
        this.repository = new DocumentRepository(template);
        this.typeClass = Class.class.cast(ParameterizedType.class.cast(repositoryType.getGenericInterfaces()[0])
                .getActualTypeArguments()[0]);
        this.classRepresentation = classRepresentations.get(typeClass);
        this.queryParser = new DocumentQueryParser();
        this.deleteParser = new DocumentQueryDeleteParser();
    }


    @Override
    protected Repository getRepository() {
        return repository;
    }

    @Override
    protected DocumentQueryParser getQueryParser() {
        return queryParser;
    }

    @Override
    protected DocumentTemplate getTemplate() {
        return template;
    }

    @Override
    protected DocumentQueryDeleteParser getDeleteParser() {
        return deleteParser;
    }

    @Override
    protected ClassRepresentation getClassRepresentation() {
        return classRepresentation;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {

        SQL sql = method.getAnnotation(SQL.class);
        if (Objects.nonNull(sql)) {
            List<T> result = Collections.emptyList();
            if (args == null || args.length == 0) {
                result = template.find(sql.value());
            } else {
                result = template.find(sql.value(), args);
            }
            return ReturnTypeConverterUtil.returnObject(result, typeClass, method);
        }
        return super.invoke(o, method, args);
    }


    class DocumentRepository extends AbstractDocumentRepository implements Repository {

        private final DocumentTemplate template;

        DocumentRepository(DocumentTemplate template) {
            this.template = template;
        }


        @Override
        protected DocumentTemplate getTemplate() {
            return template;
        }
    }
}