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
        List<RunningAppProcessInfo> infoList= am.getRunningAppProcesses();
        if (infoList == null) {
            throw new RuntimeException("getRunningAppProcesses() returns 'null'.");
        }
        for (RunningAppProcessInfo info : infoList) {
            try {
                if (info.pid == Process.myPid()) {
                    processName = info.processName;
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
