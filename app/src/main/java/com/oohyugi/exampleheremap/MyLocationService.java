package com.oohyugi.exampleheremap;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.LocationDataSourceHERE;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.positioning.StatusListener;
import com.here.android.positioning.StatusListenerAdapter;
import com.here.services.internal.LocationService;

import java.lang.ref.WeakReference;

/**
 * Created by oohyugi on 9/14/17.
 */

public class MyLocationService extends Service {


    LocationDataSourceHERE m_hereDataSource;
    MyLocationCallBack myLocationCallBack;
    PositioningManager pm;
    static MyLocationService myLocationService;

    public static MyLocationService instance() {
        if (myLocationService == null)
            myLocationService = new MyLocationService();
        return myLocationService;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        startForeground(1, updateNotif(null));
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

    }

    private Notification updateNotif(String t) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notif = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(t)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build();
//        notif.
//        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1, notif);


//        notification.setLatestEventInfo(context, title, message, intent);
//        notif.flags |= Notification.FLAG_AUTO_CANCEL;
//        notificationManager.notify(0, notif);

        return notif;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        m_hereDataSource = LocationDataSourceHERE.getInstance();
        PositioningManager.OnPositionChangedListener locationPosition = new LocationUpdate();
        if (m_hereDataSource != null) {

            pm = PositioningManager.getInstance();
            pm.setDataSource(m_hereDataSource);
            pm.addListener(new WeakReference<>(locationPosition));
            if (pm.start(PositioningManager.LocationMethod.GPS_NETWORK_INDOOR)) {
                // Position updates started successfully.
                pm.addListener(new WeakReference<>(locationPosition));
            }
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        PendingIntent service = PendingIntent.getService(getApplicationContext(),
                1,
                new Intent(this,MyLocationService.class),
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, service);

    }

    public void setCurrentLocation(MyLocationCallBack myLocationCallBack) {
        this.myLocationCallBack = myLocationCallBack;
    }

    private class LocationUpdate implements PositioningManager.OnPositionChangedListener {

        @Override
        public void onPositionUpdated(PositioningManager.LocationMethod locationMethod, GeoPosition geoPosition, boolean b) {
            Log.e("onPositionUpdated: ", geoPosition.getCoordinate().getLatitude() + " long :" + geoPosition.getCoordinate().getLongitude());

                myLocationService.refreshMap(geoPosition);

        }

        @Override
        public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {

        }
    }

    private void refreshMap(GeoPosition geoPosition) {
        myLocationCallBack.initGeoPositon(geoPosition);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
