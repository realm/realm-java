/*
 * Copyright 2016 Realm Inc.
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

package io.realm.examples.newsreader.model.network;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.List;

import io.realm.RealmList;
import io.realm.examples.newsreader.model.entity.NYTimesMultimedium;

public class RealmListNYTimesMultimediumDeserializer extends JsonDeserializer<List<NYTimesMultimedium>> {

    ObjectMapper objectMapper;

    public RealmListNYTimesMultimediumDeserializer() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public List<NYTimesMultimedium> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        RealmList<NYTimesMultimedium> list = new RealmList<>();

        TreeNode treeNode = jp.getCodec().readTree(jp);
        if (!(treeNode instanceof ArrayNode)) {
            return list;
        }

        ArrayNode arrayNode = (ArrayNode) treeNode;
        for (JsonNode node : arrayNode) {
            NYTimesMultimedium nyTimesMultimedium =
                    objectMapper.treeToValue(node, NYTimesMultimedium.class);
            list.add(nyTimesMultimedium);
        }
        return list;
    }
}