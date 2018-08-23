package io.realm.benchmarks.config;

import android.app.UiAutomation;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Credit: https://stackoverflow.com/questions/49198675/write-external-storage-in-androidtest
 */
public class MyPermissionRequester {

    public static final String TAG = MyPermissionRequester.class.getSimpleName();

    public static void request(String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiAutomation auto = InstrumentationRegistry.getInstrumentation().getUiAutomation();
            String cmd = "pm grant " + InstrumentationRegistry.getTargetContext().getPackageName() + " %1$s";
            String cmdTest = "pm grant " + InstrumentationRegistry.getContext().getPackageName() + " %1$s";
            String currCmd;
            for (String perm : permissions) {
                execute(String.format(cmd, perm), auto);
                execute(String.format(cmdTest, perm), auto);
            }
        }
        GrantPermissionRule.grant(permissions);
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(InstrumentationRegistry.getTargetContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                throw new IllegalStateException("Permission '" + permission + "' was not granted for targetContext().");
            }
            if (ActivityCompat.checkSelfPermission(InstrumentationRegistry.getTargetContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                throw new IllegalStateException("Permission '" + permission + "' was not granted for context().");
            }
        }
    }

    private static void execute(String currCmd, UiAutomation auto){
        Log.d(TAG, "exec cmd: " + currCmd);
        auto.executeShellCommand(currCmd);






    }
}