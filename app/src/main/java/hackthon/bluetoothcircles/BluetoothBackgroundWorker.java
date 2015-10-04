package hackthon.bluetoothcircles;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BluetoothBackgroundWorker extends IntentService {

    private static final String TAG = BluetoothBackgroundWorker.class.getSimpleName();

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_NOTIFICATION = "NOTIFICATION";
    private static final String ACTION_SEARCH = "SEARCH";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "hackthon.bluetoothcircles.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "hackthon.bluetoothcircles.extra.PARAM2";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionSearch(Context context, String param1, String param2) {
        Intent intent = new Intent(context, BluetoothBackgroundWorker.class);
        intent.setAction(ACTION_SEARCH);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public BluetoothBackgroundWorker() {
        super("BluetoothBackgroundWorker");
        Log.d(TAG, "Started Background Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEARCH.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionSearch(param1, param2);
            } else if (ACTION_NOTIFICATION.equals(action)) {
                handleActionNotification();
            }
        }
    }

    private void handleActionNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_action_device_access_bluetooth_searching)
                .setContentTitle("Bluetooth Circles")
                .setContentText("Click here to view connected devices");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, builder.build());


    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSearch(String param1, String param2) {
        // TODO: Handle action Search
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
