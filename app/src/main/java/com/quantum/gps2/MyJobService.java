package com.quantum.gps2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;


@SuppressWarnings("ALL")
public class MyJobService extends JobService {
    public static final String TAG="MyJobService";
    private boolean jobCancelled=false;




    @Override
    public boolean onStartJob(JobParameters params) {
        Log.w(TAG,"onStartJob");
        doBackgroundJob(params);
        return true;
    }

    private void doBackgroundJob(JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0; i<5; i++) {
                    Log.w(TAG,"onBackgroundJob: "+i);
                try {
                    Thread.sleep(1000);
                 }catch (InterruptedException e){
                    e.printStackTrace();
                 }

                }
                Log.w(TAG,"JobFinished");
                ///////////////////////////////////////
                addNotification();
                Global.flag_scan=1;
                //////////////////////////////////////
                jobFinished(params,true);
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.w(TAG,"JobCancelled");
        jobCancelled=true;
        return true; //
    }

    public void addNotification() {

        String channel_id = "job_notification_channel";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setContentTitle("Notifications JobService")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setDefaults(NotificationCompat.PRIORITY_MAX)
                        .setAutoCancel(false)
                        .setTimeoutAfter(15000L)
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.ic_alert)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setWhen(1000)
                        .setTicker("Important Event")
                        .setChannelId(channel_id)
                        .setContentText("This is a Quantum notification");

        Intent notificationIntent = new Intent(this,MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0 , notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager != null &&
                    manager.getNotificationChannel(channel_id) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channel_id,"Location JOBService",NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by Location Service");
                notificationChannel.enableVibration(true);
                manager.createNotificationChannel(notificationChannel);
            }
        }
        manager.notify(0, builder.build());
    }
}
