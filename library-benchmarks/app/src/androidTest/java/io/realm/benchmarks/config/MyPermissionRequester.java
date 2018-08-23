package io.realm.benchmarks.config;

import android.app.UiAutomation;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.uiautomator.UiDevice;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;

/**
 * Credit: https://stackoverflow.com/questions/49198675/write-external-storage-in-androidtest
 */
public class MyPermissionRequester {

    public static final String TAG = MyPermissionRequester.class.getSimpleName();

    public static void request(String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            String cmd = "pm grant " + InstrumentationRegistry.getTargetContext().getPackageName() + " %1$s";
            String cmdTest = "pm grant " + InstrumentationRegistry.getContext().getPackageName() + " %1$s";
            String currCmd;
            for (String perm : permissions) {
                execute(String.format(cmd, perm), device);
                execute(String.format(cmdTest, perm), device);
            }
        }
        GrantPermissionRule.grant(permissions);
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(InstrumentationRegistry.getTargetContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                throw new IllegalStateException("Permission '" + permission + "' was not granted for targetContext().");
            }
            if (ActivityCompat.checkSelfPermission(InstrumentationRegistry.getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                throw new IllegalStateException("Permission '" + permission + "' was not granted for context().");
            }
        }
    }

    private static void execute(String currCmd, UiDevice auto){
        Log.d(TAG, "exec cmd: " + currCmd);
        try {
            String result = auto.executeShellCommand(currCmd);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }


    }
}