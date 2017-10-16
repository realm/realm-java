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
package io.realm.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Class used to generate random values that are guaranteed to be unique across all tests.
 */
public class RandomGenerator {

    private static final Random RANDOM = new Random();

    // Internal cache of all used UUID's.
    // Used to make sure that we are not re-using them by accident
    private static Set<UUID> usedUIDs = new HashSet<>();

    public static String newRandomUUID() {
        UUID uuid = UUID.randomUUID();
        if (!usedUIDs.add(uuid)) {
            throw new AssertionError("Two unique UUIDs where generated: " + uuid.toString());
        }
        return uuid.toString();
    }

    public static String newRandomEmail() {
        StringBuilder sb = new StringBuilder(newRandomUUID().toLowerCase());
        sb.append('@');
        sb.append("androidtest.realm.io");
        return sb.toString();
    }

    // Returns a random key used by encrypted Realms.
    public static byte[] getRandomKey() {
        byte[] key = new byte[64];
        RANDOM.nextBytes(key);
        return key;
    }

    // Returns a random key from the given seed. Used by encrypted Realms.
    public static byte[] getRandomKey(long seed) {
        byte[] key = new byte[64];
        new Random(seed).nextBytes(key);
        return key;
    }

}
