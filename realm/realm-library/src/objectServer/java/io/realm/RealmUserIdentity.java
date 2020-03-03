/*
 * Copyright 2020 Realm Inc.
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

public class RealmUserIdentity {

    private String userId;
//    private RealmCredentials.IdentityProvider providerType;

    public RealmUserIdentity(String userId/*, RealmCredentials.IdentityProvider providerType*/) {
        this.userId = userId;
//        this.providerType = providerType;
    }

    public String getUserId() {
        return userId;
    }

//    public RealmCredentials.IdentityProvider getProviderType() {
//        return providerType;
//    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        RealmUserIdentity that = (RealmUserIdentity) o;
//
//        if (!userId.equals(that.userId)) return false;
//        return providerType.equals(that.providerType);
//    }
//
//    @Override
//    public int hashCode() {
//        int result = userId.hashCode();
//        result = 31 * result + providerType.hashCode();
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        return "RealmUserIdentity{" +
//                "userId='" + userId + '\'' +
//                ", providerType=" + providerType +
//                '}';
//    }
}
