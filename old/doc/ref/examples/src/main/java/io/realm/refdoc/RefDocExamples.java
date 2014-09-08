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

package io.realm.refdoc;

public class RefDocExamples {

    public static void main(String[] args) {
        System.out.println("------ Running all reference documentation examples. ------");

        try {
            TypedTableIntro.main(null);
            TypedTableExamples.main(null);

            TypedTableViewIntro.main(null);

            TypedQueryIntro.main(null);
            TypedQueryExamples.main(null);

            DynTableIntro.main(null);

            DynTableViewIntro.main(null);
            DynTableViewExamples.main(null);

            DynQueryIntro.main(null);
            DynQueryExamples.main(null);

            GroupIntro.main(null);
            GroupExamples.main(null);

            SharedGroupIntro.main(null);
            SharedGroupExamples.main(null);

            WriteTransactionIntro.main(null);
            WriteTransactionExamples.main(null);

            ReadTransactionExamples.main(null);

            DynamicReadTransactionIntro.main(null);
            TypedReadTransactionIntro.main(null);

            System.out.println("------ Successfully executed all ref-doc examples ------");

        } catch (Exception e) {
            System.err.println("!!!!! Ref-doc example FAILED !!!!!");
            e.printStackTrace(System.err);
            throw new RuntimeException();
        }
    }
}
