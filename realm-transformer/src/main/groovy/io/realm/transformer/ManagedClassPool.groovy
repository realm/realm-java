/*
 * Copyright 2017 Realm Inc.
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

package io.realm.transformer

import com.android.build.api.transform.TransformInput
import javassist.ClassPath
import javassist.ClassPool

/**
 * This class is a wrapper around JavaAssists {@code ClassPool} class that allows for correct cleanup
 * of the resources used.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class ManagedClassPool extends ClassPool implements Closeable {

    def List<ClassPath> pathElements = new ArrayList<ClassPath>()

    /**
     * Constructor for creating and populating the JavAssist class pool.
     * Remember to call {@link #close()} when done with it to avoid leaking file resources
     *
     * @param inputs the inputs provided by the Transform API
     * @param referencedInputs the referencedInputs provided by the Transform API
     * @return the populated ClassPool instance
     */
    ManagedClassPool(Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs) {
        // Don't use ClassPool.getDefault(). Doing consecutive builds in the same run (e.g. debug+release)
        // will use a cached object and all the classes will be frozen.
        super(null)
        appendSystemPath()

        inputs.each {
            it.directoryInputs.each {
                pathElements.add(appendClassPath(it.file.absolutePath))
            }

            it.jarInputs.each {
                pathElements.add(appendClassPath(it.file.absolutePath))
            }
        }

        referencedInputs.each {
            it.directoryInputs.each {
                pathElements.add(appendClassPath(it.file.absolutePath))
            }

            it.jarInputs.each {
                pathElements.add(appendClassPath(it.file.absolutePath))
            }
        }
    }

    /**
     * Detach all ClassPath elements, effectively closing the class pool.
     */
    @Override
    void close() throws IOException {
        // Cleanup class pool. Internally it keeps a list of JarFile references that are only
        // cleaned up if the the ClassPath element wrapping it is manually removed.
        // See https://github.com/jboss-javassist/javassist/issues/165
        def iter = pathElements.iterator()
        while (iter.hasNext()) {
            def cp = iter.next()
            removeClassPath(cp)
            iter.remove()
        }
    }
}
