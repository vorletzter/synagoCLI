package de.librechurch.synagocli.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.model.Event;

import java.util.List;

import de.librechurch.synagocli.ChatActivity;
import de.librechurch.synagocli.HomeActivity;
import de.librechurch.synagocli.Matrix;
import de.librechurch.synagocli.R;

public class ListenerService extends Service {

    String NOTIFICATION_SERVICE_CHANNEL_ID = "de.librechurch.synago.service";
    String NOTIFICATION_SERVICE_CHANNEL_NAME = "Service";
    String NOTIFICATION_SERVICE_CHANNEL_TITLE = "Synago";
    String NOTIFICATION_SERVICE_CHANNEL_DESC = "Background Service running";
    Integer NOTIFICATION_SERVICE_ID = 2;

    String NOTIFICATION_EVENT_CHANNEL_ID = "de.librechurch.synago.events";
    String NOTIFICATION_EVENT_CHANNEL_NAME = "Events";
    String NOTIFICATION_EVENT_CHANNEL_DESC = "Used to show events";
    Integer NOTIFICATION_EVENT_ID = 3;
    NotificationManager notificationManager;

    /** indicates how to behave if the service is killed */
    int mStartMode;

    /** interface for clients that bind */
    IBinder mBinder;

    /** indicates whether onRebind should be used */
    boolean mAllowRebind;

    private static final String LOG_TAG = ListenerService.class.getSimpleName();

    // List of all Sessions.
    private List<MXSession> mSessions;
    Matrix matrix;


    /** Called when the service is being created. */
    @Override
    public void onCreate() {
    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        matrix = Matrix.getInstance(getApplicationContext());
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        this.mSessions = Matrix.getInstance(getApplicationContext()).getSessions();
        createChannels();
        startInForeground();

        for (MXSession session : mSessions) {
            Log.d(LOG_TAG, "starting Listener Service for "+session.getMyUserId());
            final MXSession s = session;
            MXEventListener m = new MXEventListener() {
                @Override
                public void onLiveEvent(Event event, RoomState roomState) {
                    super.onLiveEvent(event, roomState);
                    if (event.getType().matches(Event.EVENT_TYPE_MESSAGE)) {
                        String message = "";
                        if (!TextUtils.equals(s.getMyUserId(), event.getSender())) {
                            try {
                                message = event.content.getAsJsonObject().get("body").toString().replaceAll("\"", "");
                            } catch (NullPointerException e) {
                                Log.e(LOG_TAG, "Could not extract Message from Event");
                            }
                            msgNotification(message, event.getSender(), roomState.roomId, roomState.getDataHandler().getUserId());
                        }
                    }
                }
            };

            session.getDataHandler().addListener(m);
        }
        return mStartMode;
    }

    private void createChannels(){
        if(Build.VERSION.SDK_INT>=26) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel serviceChannel = new NotificationChannel(NOTIFICATION_SERVICE_CHANNEL_ID, NOTIFICATION_SERVICE_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
            serviceChannel.setDescription(NOTIFICATION_SERVICE_CHANNEL_DESC);
            notificationManager.createNotificationChannel(serviceChannel);

            NotificationChannel eventChannel = new NotificationChannel(NOTIFICATION_EVENT_CHANNEL_ID, NOTIFICATION_EVENT_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            eventChannel.setDescription(NOTIFICATION_EVENT_CHANNEL_DESC);

            notificationManager.createNotificationChannel(eventChannel);
        }
    }

    private void msgNotification(String msg, String from, String roomId, String userId){
        Log.d(LOG_TAG, "new Notification from "+from+"; RoomId: "+roomId+" UserId: "+userId );

        Intent notificationIntent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("roomId", roomId);
        bundle.putString("userId", userId);
        notificationIntent.putExtras(bundle);

        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,NOTIFICATION_EVENT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_whatshot_black_24dp)
                .setContentTitle(from)
                .setContentText(msg)
                .setAutoCancel(true)
                .setOngoing(false)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent);

        Notification notification=builder.build();
        notificationManager.notify(NOTIFICATION_EVENT_ID, notification);
    }


    /* Used to build and start foreground service. */
    private void startInForeground() {
        Log.d(LOG_TAG, "Start foreground service.");
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notificationIntent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,NOTIFICATION_SERVICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_whatshot_black_24dp)
                .setContentTitle(NOTIFICATION_SERVICE_CHANNEL_TITLE)
                .setContentText(NOTIFICATION_SERVICE_CHANNEL_DESC)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent);
        Notification notification=builder.build();
        startForeground(NOTIFICATION_SERVICE_ID, notification);
    }



    /** A client is binding to the service with bindService() */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** Called when all clients have unbound with unbindService() */
    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    /** Called when a client is binding to the service with bindService()*/
    @Override
    public void onRebind(Intent intent) {
    }

    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy() {

    }

}
