/*
 * Copyright 2015 Realm Inc.
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

package io.realm;

import android.test.AndroidTestCase;

import java.util.Arrays;

import io.realm.annotations.RealmModule;
import io.realm.entities.AllTypes;
import io.realm.entities.AnimalModule;
import io.realm.entities.Cat;
import io.realm.entities.CatOwner;
import io.realm.entities.Dog;
import io.realm.entities.HumanModule;
import io.realm.internal.modules.CompositeMediator;
import io.realm.internal.modules.FilterableMediator;

public class MediatorTest extends AndroidTestCase {

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public void testMediatorsEquality() {
        final DefaultRealmModuleMediator defaultMediator = new DefaultRealmModuleMediator();
        final CompositeMediator compositeMediator = new CompositeMediator(defaultMediator);
        final FilterableMediator filterableMediator = new FilterableMediator(defaultMediator, defaultMediator.getModelClasses());

        assertEquals(defaultMediator, defaultMediator);
        assertEquals(defaultMediator.hashCode(), defaultMediator.hashCode());
        assertEquals(defaultMediator, compositeMediator);
        assertEquals(defaultMediator.hashCode(), compositeMediator.hashCode());
        assertEquals(defaultMediator, filterableMediator);
        assertEquals(defaultMediator.hashCode(), filterableMediator.hashCode());

        assertEquals(compositeMediator, defaultMediator);
        assertEquals(compositeMediator.hashCode(), defaultMediator.hashCode());
        assertEquals(compositeMediator, compositeMediator);
        assertEquals(compositeMediator.hashCode(), compositeMediator.hashCode());
        assertEquals(compositeMediator, filterableMediator);
        assertEquals(compositeMediator.hashCode(), filterableMediator.hashCode());

        assertEquals(filterableMediator, defaultMediator);
        assertEquals(filterableMediator.hashCode(), defaultMediator.hashCode());
        assertEquals(filterableMediator, compositeMediator);
        assertEquals(filterableMediator.hashCode(), compositeMediator.hashCode());
        assertEquals(filterableMediator, filterableMediator);
        assertEquals(filterableMediator.hashCode(), filterableMediator.hashCode());
    }

    public void testCompositeMediatorModelClassesCount() {
        final CompositeMediator mediator = new CompositeMediator(
                new HumanModuleMediator(),
                new AnimalModuleMediator()
        );

        final int modelsInHumanModule = HumanModule.class.getAnnotation(RealmModule.class).classes().length;
        final int modelsInAnimalModule = AnimalModule.class.getAnnotation(RealmModule.class).classes().length;

        assertEquals(modelsInHumanModule + modelsInAnimalModule, mediator.getModelClasses().size());
    }

    public void testFilterableMediatorModelClassesCount() {
        //noinspection unchecked
        final FilterableMediator mediator = new FilterableMediator(new AnimalModuleMediator(), Arrays.<Class<? extends RealmModel>>asList(Cat.class, CatOwner.class));

        assertTrue(mediator.getModelClasses().contains(Cat.class));
        // CatOwner is not a member of AnimalModuleMediator
        assertFalse(mediator.getModelClasses().contains(CatOwner.class));
        assertFalse(mediator.getModelClasses().contains(Dog.class));
        assertFalse(mediator.getModelClasses().contains(AllTypes.class));
    }

    public void testDefaultMediatorWasTransformed() {
        final DefaultRealmModuleMediator defaultMediator = new DefaultRealmModuleMediator();
        assertTrue(defaultMediator.transformerApplied());
    }
}
