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
package io.realm;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class RealmIntegerTests {


    @Test
    public void testBasic() {
        RealmInteger int1 = new RealmInteger(10);
        RealmInteger int2 = new RealmInteger("10");
        assertEquals(int1, int2);

        int1.set(15);
        int1.decrement(2);
        int2.increment(3);
        assertEquals(int1, int2);
    }

    @Test
    public void testGetters() {
        RealmInteger int1 = new RealmInteger(0x04433332211L);

        // positive
        assertEquals(int1.longValue(), 0x04433332211L);
        assertEquals(int1.intValue(), 0x033332211);
        assertEquals(int1.shortValue(), 0x02211);
        assertEquals(int1.byteValue(), 0x011);

        assertEquals(int1.floatValue(), 2.92916756E11F);
        assertEquals(int1.doubleValue(),  2.92916765201E11);

        // negative
        int1.set(0x8888444483338281L);
        assertEquals(int1.longValue(), 0x8888444483338281L);
        assertEquals(int1.intValue(), 0x083338281);
        assertEquals(int1.shortValue(), (short) 0xf8281);
        assertEquals(int1.byteValue(), (byte) 0xf81);

        assertEquals(int1.floatValue(), -8.6085554E18F);
        assertEquals(int1.doubleValue(),  -8.6085556266690468E18);
    }
}
