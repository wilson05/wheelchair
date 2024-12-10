package com.quantum.gps2;


import static android.app.Notification.VISIBILITY_PUBLIC;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;



@SuppressWarnings("ALL")
public class LocationService extends Service {

    private final LocationCallback locationCallback = new LocationCallback() {
        private static final String TAG = "LOCATION_UPDATE";
        public  double latitude = 0;
        public LocationManager locationManager;
        public LocationListener locationListener;
        public grmc gprmc;
        public String horatxt;
        //private Location mlocation2;


        boolean fechavalida(String fecha){
            String dia;
            String mes;
            String anio;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            System.out.println("Current Date and Time: " + dtf.format(now));
            anio = now.toString().substring(2,4);
            mes = now.toString().substring(5,7);
            dia = now.toString().substring(8,10);
            String dia2=fecha.substring(0,2);
            String mes2=fecha.substring(2,4);
            String anio2=fecha.substring(4,6);
            if(dia.equals(dia2) && mes.equals(mes2) && anio.equals(anio2)) {
                return true;
            }else{
                return false;
            }

        }

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitu = locationResult.getLastLocation().getLongitude();
                mlocation2 = locationResult.getLastLocation();
                if (latitude >= 10) {
                    Global.ceromasLat = 0;
                } else {
                    Global.ceromasLat = 1;
                }
                ////////////////////////
                //locationResult.getLastLocation().
                ////////////////////////
                String rumbon;
                String rumbow;
                if (longitu < 0) {
                    longitu *= -1;
                    rumbow = "W";
                    double long2 = longitu;
                    long iPart = (long) long2;
                    double fPart = (long2 - iPart) * 6;
                    long2 = (iPart * 100) + fPart * 10;
                    longitu = long2;
                } else {
                    rumbow = "E";
                    double long2 = longitu;
                    long iPart = (long) long2;
                    double fPart = (long2 - iPart) * 6;
                    long2 = (iPart * 100) + fPart * 10;
                    longitu = long2;
                }
                /////////////////
                if (latitude < 0) {
                    latitude *= -1;
                    rumbon = "S";
                    double lat2 = latitude;
                    long iPart = (long) lat2;
                    double fPart = (lat2 - iPart) * 6;
                    lat2 = (iPart * 100) + fPart * 10;
                    latitude = lat2;
                } else {
                    rumbon = "N";
                    double lat2 = latitude;
                    long iPart = (long) lat2;
                    double fPart = (lat2 - iPart) * 6;
                    lat2 = (iPart * 100) + fPart * 10;
                    latitude = lat2;
                }
                ////////////

                long time = locationResult.getLastLocation().getTime();
                float acu = locationResult.getLastLocation().getAccuracy();
                double altitude = locationResult.getLastLocation().getAltitude();
                Global.altitude2 = altitude;
                Global.rumbo = locationResult.getLastLocation().getBearing();

                //String altitud = Double.toString(altitude);
                String altitud = (String) String.format("%.1f", altitude);
                altitud = altitud.replace(",", ".");

                String acu2 = Float.toString(acu);
                ////////////
                Date d = new Date(locationResult.getLastLocation().getTime());
                SimpleDateFormat sdf;
                sdf = new SimpleDateFormat("ddMMyy");
                //sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String fecha = sdf.format(d);
                //sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String currentTime = new SimpleDateFormat("HHmmss", Locale.UK).format(new Date());
                int ValorH = Integer.parseInt(currentTime);

                String resultado = gmth(ValorH);
                //String resultado=Integer.toString(ValorH);

                String hora = resultado + ".00";
                String hora2 = resultado + ".00";
                MainActivity.contador = hora2;
                String velocidad = String.format(Locale.ENGLISH, "%s",
                        locationResult.getLastLocation().getSpeed());
                DecimalFormat f = new DecimalFormat("##.##");
                velocidad = f.format(locationResult.getLastLocation().getSpeed());
                velocidad = velocidad.replace(",", ".");
                f = new DecimalFormat("##.##");
                velocidad = velocidad.replace(",", ".");
                Global.velo = Double.parseDouble(velocidad);

                String rumbo = String.format(Locale.ENGLISH, "%s",
                        locationResult.getLastLocation().getBearing());

                int satel = 8;
                try {
                    satel = locationResult.getLastLocation().getExtras().getInt("satellites", 6);
                } catch (Exception e) {
                    satel = 8;
                }
                //int satel2 = locationResult.getLastLocation().getExtras().getInt("satellites",0);
                Log.d(TAG, "onLocationResult: Satelite:" + satel);
                if (satel < 5) {
                    satel = 06;
                }

                String satelites = String.valueOf(satel);
                //Log.d("SAT", satelites);
                /////////////////////
                float horiAcc = locationResult.getLastLocation().getAccuracy();
                double hdop = (horiAcc / 6);   //previous /5 for 5 meters... indoor 6 meters is acceptable.
                Global.hdop_temp = hdop;
                Log.w(TAG, "HDOP TEMP:" + Global.hdop_temp);

                if (hdop >= 0.2 && hdop < 5.0) {

                    hdop = 0.6;  //2.9
                    Log.w(TAG, "HDOP Filtered:" + hdop);
                } else {
                    Log.w(TAG, "HDOP High:" + hdop);
                    hdop = 0.7;  //2.9
                }

                String hdop2 = (String) String.format("%.1f", hdop);
                //String hdop2=Integer.toString(hdop);

                /////////
                Log.d(TAG, latitude + " - " + longitu + " - " + altitude + " - " + " ACU:" + acu + " Fecha:" + fecha + " Hora:" + hora + " Speed:" + velocidad);

                if(fechavalida(fecha)==true) {
                    String rmc2 = create_rmc(latitude, longitu, hora, fecha, velocidad, rumbo, rumbon, rumbow);
                    String gga2 = create_gga(latitude, longitu, hora2, fecha, velocidad, rumbo, rumbon, rumbow, acu2, altitud, satelites, hdop2);
                    Log.d("GPRMC:", rmc2);
                    Log.d("GGA:", gga2);
                    Global.gga1 = gga2;
                    Global.rmc1 = rmc2;
                }
                else{
                    Log.d("Date in the future:",fecha);
                }
                ///////desde aqui ///////
                String channel_id = "location_blue";
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Intent resultIntent = new Intent();
                String text=Common.getLocationText(mlocation2);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                        0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        getApplicationContext(),
                        channel_id);
                builder.setSmallIcon(R.mipmap.ic_launcher_round);
                builder.setContentTitle("Location Service Update");
                builder.setDefaults(NotificationCompat.PRIORITY_HIGH);
                builder.setContentText(text);
                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(false);
                builder.setTimeoutAfter(10000);
                builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (notificationManager != null &&
                            notificationManager.getNotificationChannel(channel_id) == null) {
                        NotificationChannel notificationChannel = new NotificationChannel(
                                channel_id,"Location Service",NotificationManager.IMPORTANCE_HIGH);
                        notificationChannel.setDescription("This channel is used by Location Service");
                        notificationChannel.enableVibration(true);
                        notificationManager.createNotificationChannel(notificationChannel);
                    }
                }
                //////hasta aqui...///
            }
        }
    };
    public boolean isServiceIsRunningForeground(Context context){
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service:manager.getRunningServices(Integer.MAX_VALUE))
            if(getClass().getName().equals(service.service.getClassName()))
                if(service.foreground)
                    return  true;
        return false;
    }
    private Location mlocation2;

    private String gmth(int valorh) {

        String dato=null;
        if(valorh <= 190000) {
            valorh+=50000;
            if(valorh < 10000){
                dato = "0" + String.valueOf(valorh);
            }
        }
        else{
               valorh=valorh-190000;
               if(valorh < 1000){
                    dato = "000" + String.valueOf(valorh);
               }else {
                        if (valorh < 10000) {
                           dato = "00" + String.valueOf(valorh);
                        } else {
                        dato = "0" + String.valueOf(valorh);
                       }
            }
            return  dato;
         }
        return String.valueOf(valorh);
    }

    private LocationManager locationManager;
    private LocationListener listener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private int startLocationService() {
        String channel_id = "location_notification_channel";
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        String text=Common.getLocationText(mlocation2);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channel_id);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.PRIORITY_HIGH);
        builder.setContentText(text);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null &&
                    notificationManager.getNotificationChannel(channel_id) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channel_id,"Location Service",NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by Location Service");
                notificationChannel.enableVibration(true);
                notificationChannel.setLockscreenVisibility(VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return START_STICKY;
        }
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(Constants.LOCATION_UPDATE_ID,builder.build());
        return START_STICKY;
    }
    private void stopLocationService(){
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(intent !=null){
            String action = intent.getAction();
            if(action !=null){
                if(action.equals(Constants.START_LOCATION_SERVICE)){
                    startLocationService();
                } else if(action.equals(Constants.STOP_LOCATION_SERVICE)){
                    stopLocationService();
                }
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }


    // $GPGGA,122336.00,0322.141610,N,07631.306282,W,1,10,1.0,985.5,M,20.3,M,,*69
    // $GPRMC,122336.00,A,0322.141610,N,07631.306282,W,0.0,,180920,3.7,W,A,V*70,,
    //    >
    //$GPRMC,122336.00,A,0322.141610,N,07631.306282,W,0.0,,180920,3.7,W,A,V*70,,

    public String create_rmc(double lat, double long1, String hora,String fecha, String speed1, String heading1,String rumbon, String rumbow)
    {
        String rmc="$GPRMC,";
        rmc+=hora;
        //rmc+=",A,0";   //rmc+=",A,0";
        //String lat2 = (String) String.format("%.6f",lat).replace(",",".");
        rmc+=",A,";   //rmc+=",A,0";
        if(Global.ceromasLat ==1){
            rmc+="0";
        }
        String lat2 = (String) String.format("%.5f",lat).replace(",",".");
        rmc+=lat2;
        rmc+=",";
        rmc+=rumbon;
        if (rumbon=="N"){
            rmc+=",0";
        }
        else {
            rmc += ",";
        }
        //rmc+=",0";
        String lot2 = (String) String.format("%.6f",long1).replace(",",".");
        rmc+=lot2;
        rmc+=",W,";
        rmc+=speed1;
        rmc+=",";
        if(heading1.length()>= 5){
            rmc+=heading1.substring(0,4);
        }else {
            rmc+=heading1;
        }
        rmc+=",";
        rmc+=fecha;
        rmc+=",3.7,W,A,V*70";
        //rmc+=",,";

        return rmc;
    }
    // //GPGGA,203446.000,0441.8612,N,07408.5924,W,2,10,1.0,2576.0,M,0.0,M,0.0,0000"
    public String create_gga(double lat, double long1, String hora,String fecha, String speed1, String heading1,String rumbon, String rumbow,String accu, String altitud,String satelites,String hdop)
    {
        String rmc="$GPGGA,";
        rmc+=hora;
        //rmc+=",0";
        //String lat3 = (String) String.format("%.5f", lat).replace(",",".");
        rmc+=",";
        if(Global.ceromasLat ==1){
            rmc+="0";
        }
        String lat3 = (String) String.format("%.5f", lat).replace(",",".");
        rmc+=lat3;
        rmc+=",";
        rmc+=rumbon;
        if (rumbon=="N"){
            rmc+=",0";
        }
        else {
            rmc += ",";
        }
        //rmc+=",0";
        String long3 = (String) String.format("%.6f", long1).replace(",",".");
        rmc+=long3;
        rmc+=",";
        rmc+=rumbow;
        rmc+=",1,";
        rmc+=satelites;
        rmc+=",";
        rmc+=hdop.replace(",",".");
        rmc+=",";
        rmc+=altitud;
        rmc+=",M,20.3,M,0.0,0000*69";
        //rmc+="45";  // xor...
        return rmc;
    }


    boolean fechavalida(String fecha) {
        String dia;
        String mes;
        String anio;
        mes = fecha.substring(1, 2);
        return true;
    }
}

