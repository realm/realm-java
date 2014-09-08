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

package io.tightdb.example.graph;

import java.util.Date;

public class Link {
    int         id1;
    int         link_type;
    int         id2;
    String      data;
    int         version;
    Date        time;

    Link(int id1, int link_type, int id2, int visibility, String data, int version, Date time) {
        this.id1        = id1;
        this.link_type  = link_type;
        this.id2        = id2;
        this.data       = data;
        this.version    = version;
        this.time       = time;
    }

    public String toString() {
        return String.format("id1: %d, type: %d, id2: %d, data: %s, version: %d, time: %s\n",
                this.id1, this.link_type, this.id2, this.data, this.version, this.time );
    }

}
