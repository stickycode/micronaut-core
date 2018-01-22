/*
 * Copyright 2017 original authors
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
package org.particleframework.core.io.scan;

import org.particleframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Loads resources from the classpath
 *
 * @author James Kleeh
 * @since 1.0
 */
public class ClasspathResourceLoader implements ResourceLoader {

    private final ClassLoader classLoader;
    private final String basePath;

    /**
     * Default constructor
     *
     * @param classLoader The class loader for loading resources
     */
    public ClasspathResourceLoader(ClassLoader classLoader) {
        this(classLoader, null);
    }

    /**
     * Use when resources should have a standard base path
     *
     * @param classLoader The class loader for loading resources
     * @param basePath The path to look for resources under
     */
    public ClasspathResourceLoader(ClassLoader classLoader, String basePath) {
        this.classLoader = classLoader;
        this.basePath = normalize(basePath);
    }

    /**
     * Obtains a resource as a stream
     *
     * @param path The path
     * @return An optional resource
     */
    public Optional<InputStream> getResourceAsStream(String path) {
        return Optional.ofNullable(classLoader.getResourceAsStream(prefixPath(path)));
    }

    /**
     * Obtains a resource URL
     *
     * @param path The path
     * @return An optional resource
     */
    public Optional<URL> getResource(String path) {
        return Optional.ofNullable(classLoader.getResource(prefixPath(path)));
    }

    /**
     * Obtains a stream of resource URLs
     *
     * @param path The path
     * @return A resource stream
     */
    public Stream<URL> getResources(String path) {
        Enumeration<URL> all;
        try {
            all = classLoader.getResources(prefixPath(path));
        } catch (IOException e) {
            return Stream.empty();
        }
        Stream.Builder<URL> builder = Stream.<URL>builder();
        while (all.hasMoreElements()) {
            URL url = all.nextElement();
            builder.accept(url);
        }
        return builder.build();
    }

    private String normalize(String path) {
        if (path != null) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (!path.endsWith("/")) {
                path = path + "/";
            }
        }
        return path;
    }

    private String prefixPath(String path) {
        if (basePath != null) {
            if (path.startsWith("/")) {
                return basePath + path.substring(1);
            } else {
                return basePath + path;
            }
        } else {
            return path;
        }
    }
}
