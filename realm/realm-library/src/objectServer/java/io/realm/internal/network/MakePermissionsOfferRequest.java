/*
 * Copyright 2019 Realm Inc.
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
package io.realm.internal.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.realm.permissions.PermissionOffer;

/**
 * Class wrapping request to `POST /auth/permissions/offers`
 */
public class MakePermissionsOfferRequest {

    private final PermissionOffer offer;

    public MakePermissionsOfferRequest(PermissionOffer offer) {
        this.offer = offer;
    }

    public String toJson() throws JSONException {
        JSONObject request = new JSONObject();
        Date expires = offer.getExpiresAt();
        if (expires != null) {
            request.put("expiresAt", expires.toString());
        }
        request.put("realmPath", offer.getRealmUrl());
        request.put("accessLevel", offer.getAccessLevel().getKey());
        return request.toString();
    }
}
