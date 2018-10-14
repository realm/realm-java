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

package io.realm.examples.realmmultiprocessexample;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Process;

import java.util.Date;
import java.util.List;

import io.realm.examples.realmmultiprocessexample.models.ProcessInfo;

public class Utils {

    public static String getMyProcessName(Context context) {
        String processName = "";
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> infoList = am.getRunningAppProcesses();
        if (infoList == null) {
            throw new RuntimeException("getRunningAppProcesses() returns 'null'.");
        }
        for (RunningAppProcessInfo info : infoList) {
            try {
                if (info.pid == Process.myPid()) {
                    processName = info.processName;
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        return processName;
    }

    public static ProcessInfo createStandaloneProcessInfo(Context context) {
        ProcessInfo processInfo = new ProcessInfo();
        processInfo.setName(getMyProcessName(context));
        processInfo.setPid(android.os.Process.myPid());
        processInfo.setLastResponseDate(new Date());

        return processInfo;
    }
}
