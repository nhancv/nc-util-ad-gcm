package cvnhan.android.androidgcmexample;

/**
 * Created by Administrator on 30-Mar-15.
 */

import android.content.Context;
import android.content.Intent;

public final class CommonUtilities {
    // give your server registration url here
    static final String SERVER_URL = "http://192.168.1.26/gcm";
//    static final String SERVER_URL = "http://vannhan.comuv.com/gcm";
    // Google project id
    static final String SENDER_ID = "875274890887";

    static final String TAG = "Android GCM";
    static final String DISPLAY_MESSAGE_ACTION =
            "cvnhan.android.androidgcmexample.DISPLAY_MESSAGE";
    static final String EXTRA_MESSAGE = "message";

    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
}
