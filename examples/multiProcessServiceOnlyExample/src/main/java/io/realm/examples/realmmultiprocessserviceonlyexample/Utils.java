package io.realm.examples.realmmultiprocessserviceonlyexample;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.realm.examples.realmmultiprocessserviceonlyexample.models.ProcessInfo;


public class Utils {

    public static String getMyProcessName(Context context) {
        String processName = "";
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        while(i.hasNext())
        {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo)(i.next());
            try
            {
                if(info.pid == android.os.Process.myPid())
                {
                    processName = info.processName;
                }
            } catch(Exception ignored) {
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
