package com.quantum.gps2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.location.Location;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Random;

public class Common {
    public static String getLocationText(Location mLocation){
        return mLocation== null ? "Unknown Location" :new StringBuilder()
                .append(mLocation.getLongitude())
                .append("/")
                .append(mLocation.getLatitude())
                .toString();
    }

    public static void showNotification(Context context, String title, String body){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"125");

        builder.setSmallIcon(R.drawable.logo);
        builder.setContentTitle(title);
        builder.setContentText(body);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        //Style
        NotificationCompat.BigTextStyle bigTextStyle= new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(title);
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.setSummaryText(title);

        builder.setStyle(bigTextStyle);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId=Constants.CHANNEL_ID;
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID,"Firebase Message",NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("This channel is used by Firebase");
                channel.enableVibration(true);
                channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
                builder.setChannelId(channelId);
        }
        manager.notify(new Random().nextInt(), builder.build());
    }
}
