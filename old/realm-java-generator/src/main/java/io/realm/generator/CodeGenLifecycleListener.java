/*
 * Copyright 2014 Realm Inc.
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

package io.realm.generator;


public class CodeGenLifecycleListener {

    /*
    private static final CodeGenLifecycleListener INSTANCE = new CodeGenLifecycleListener();

    private long start;

    @Override
    public void beforeCodeGeneration(LifecycleEvent event) {
        event.getContext().getLogger().info("Handling 'before code generation' event");
        start = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public void afterCodeGeneration(LifecycleEvent event) {
        event.getContext().getLogger().info("Handling 'after code generation' event");
        GeneratedCodeCleaner cleaner = new GeneratedCodeCleaner();
        int count = cleaner.removeObsoleteGeneratedCode(event.getContext(), start);
        event.getContext().getLogger().info("Total {} obsolete generated files will be deleted...", count);
    }

    public static CodeGenLifecycleListener getInstance() {
        return INSTANCE;
    }

*/
}
