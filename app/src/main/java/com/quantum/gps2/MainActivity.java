package com.quantum.gps2;

import static java.lang.Math.abs;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.util.Hex;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private Button bt_start, bt_stop, btmas;
    private Button bstart, bstop, clear;
    private TextView textData;
    private TextView textgga;

    public static int progressvalue=10;
    private TextView Textimei;
    private BroadcastReceiver broadcastReceiver;
    final static TelephonyManager telecomManager = null;
    private Button btimei;
    private TextView txtimei;
    public static java.lang.String timei;

    private TextView txtCounter;
    private TextView txtCounter2;
    private TextView txtCounter3;
    private Context context;
    private Button bt_send;
    private Button bt_pdu;
    private static TextView txtStatusSck;
    private TextView eventos_txt, texto;
    public static String contador;
    /*// Socket*/

    private Socket socket;
    //private static final int SERVERPORT = 9000;
    private static final int SERVERPORT = 9895; //9895
    private static final java.lang.String SERVER_IP = "sai.quantum-aviation.com";
    //private static final java.lang.String SERVER_IP = "136.243.95.159";

    /*       Variables para Tramas              */
    public static ArrayList<String> tramas = null;
    public static int contador_tramas = 0;
    public static String imei = null;
    private static final String MILES = "12";
    private static String name;
    private static final java.lang.String TERMINATOR = ",,>";
    private static final String IOSTATUS = "100000000000";
    private static final String EVENTO44 = "0x44";
    private static final String ANALOG = "0.0 0 0 0.0";
    public static String rmc;
    public static String gga;

    public static ArrayAdapter deviceAdapter;
    private Button btMas;

    private Button btToken;
    private BluetoothAdapter mBluetoothAdapter;
    public final String TAG = "ScanRecord";
    public final String TAG2 = "JobService";

    public final String TAGFIREBASE="FirebaseW";
    private Handler mHandler;
    private boolean mScanning;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 15000;
    final public static ArrayList<deviceblu> mdeviceblu = new ArrayList<deviceblu>();

    ImageView _logo;
    private static final int TEMPO = 60;
    private Handler myhandler;
    public long temporizador = TEMPO;
    public boolean bandera_acceso = false;
    public static int quantity = 0;

    public static double velocidad = 0;
    public static String gps_valido;
    public static int ciclos = 0;
    public static boolean answer_tx=false;

    //file data...
    public static final String DIR_NAME = "datalog";
    public static final String FILE_NAME = "datalog.txt";

    public static int numberlines_text=0;
    public static int numberlines_text2=0;

    ProgressBar progressBar2;
    public boolean progAsc;

    //
    @Override
    protected void onResume() {
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        progressvalue=0;
        progressBar2.setProgress(progressvalue,true);
        progAsc=true;

        //bt_start.callOnClick();
        //scanLeDevice(false);

        mdeviceblu.clear();
        scanLeDevice(true);
        eventos_txt.setMovementMethod(new ScrollingMovementMethod());
        //texto.setMovementMethod(new ScrollingMovementMethod());
        //Toast.makeText(this, "Notification for Resume", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Intent i = new Intent(getApplicationContext(), GPS_Service.class);
        //stopService(i);
        //startService(i);
        //scanLeDevice(false);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // sept26 Test..
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //bt_start = (Button) findViewById(R.id.bt_start);
        //bt_stop = (Button) findViewById(R.id.bt_stop);
        //textData = (TextView) findViewById(R.id.texto);
        //btimei = (Button) findViewById(R.id.btimei);
        txtimei = (TextView) findViewById(R.id.Textmei);
        textgga = (TextView) findViewById(R.id.textgga);
        //bt_send = (Button) findViewById(R.id.btsend);
        //bt_pdu = (Button) findViewById(R.id.btpdu);
        btmas = (Button) findViewById(R.id.btMas);
        clear = (Button) findViewById(R.id.clear);
        //txtStatusSck=(TextView)findViewById(R.id.txtSocket);
        eventos_txt = (TextView) findViewById(R.id.eventos);
        //texto = (TextView) findViewById(R.id.texto);
        bstart = (Button) findViewById(R.id.bStart);
        bstop = (Button) findViewById(R.id.bStop);
        Textimei = (TextView) findViewById(R.id.Textmei);
        _logo = (ImageView) findViewById(R.id.logo);
        btToken=(Button)findViewById(R.id.ButtonToken);
        txtCounter=(TextView)findViewById(R.id.txtCount);
        txtCounter2=(TextView)findViewById(R.id.txtCount2);
        txtCounter3=(TextView)findViewById(R.id.txtCount3);

        progressBar2=(ProgressBar) findViewById(R.id.progress2);

        progAsc=true;

        final deviceAdapter adapter = new deviceAdapter(this, mdeviceblu);
        // Attach the adapter to a ListView
        //ListView listView = (ListView) findViewById(R.id.lvitems);
        Button btMas = (Button) findViewById(R.id.btMas);
        Global.contadorRB = 0;
        //listView.setAdapter(adapter);
        mHandler = new Handler();
        Date currentTime = Calendar.getInstance().getTime();
                ////////////////////////////////////////////////////////////////////
      /*  try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            socket = new Socket(serverAddr, SERVERPORT);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        if (socket.isConnected() && socket.isBound() && socket.isClosed() == false) {
            Log.d("Socket", "Conectado");
        }*/
        ////////////////////////////////////
        //writeEmptyToFile(this);

        ///////////////////////////////
        File f = new File(FILE_NAME);
        if(!f.exists()){
            try {
                //f.createNewFile();
                new FileOutputStream(FILE_NAME, true).close();
                Log.w("FILE","File created");
            } catch (IOException e) {
                //throw new RuntimeException(e);
                Log.w("FILE","File only read");
            }
        }else{
            Log.w("FILE","File already exists");
        }
        ///////////////////begin timer //////////////////////////////////////
        myhandler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //Log.d("Timer", "Handler run");
                myhandler.postDelayed(this, 1000);

                if (Global.flag_scan == 1) {
                    Global.flag_scan = 0;
                    mdeviceblu.clear();
                    scanLeDevice(true);
                    Global.contadorRB++;
                    Log.w("CounterRebirth:"," "+Global.contadorRB);
                    if (Global.contadorRB > 2) {
                        Global.contadorRB = 0;
                        addNotification2();
                        //triggerRebirth(MainActivity.this);
                    }
                }

                if (temporizador > 0) {
                    temporizador = temporizador - 1;
                    Log.d("Timer", "tiempo" + Long.toString(temporizador));
                }

                if (temporizador == 0) {
                    //Log.d("Timer", "fin tiempo");
                    temporizador = TEMPO;
                }
                if (temporizador == 1) {  //cada 4 minutos envia keep alive..4x60s
                    String time2 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    ciclos++;
                    if (ciclos >= 4)  //4
                    {
                        ciclos = 0;
                        try {
                            Date currentTime = Calendar.getInstance().getTime();
                            String datos = frame_keep();
                            if (Global.rmc1 != null) {
                                if (Global.rmc1.contains(",A,")) {
                                    //writeLog(datos);
                                    //writeToFile(datos, context);
                                    answer_tx=enviar(datos);
                                    if(numberlines_text > 20){
                                        eventos_txt.setText("");
                                        numberlines_text=0;
                                    }
                                    if(answer_tx) {
                                        eventos_txt.append(getString(R.string.evento11) + " " + time2 + " ACK : Ok");
                                        numberlines_text++;
                                    }else{
                                        eventos_txt.append(getString(R.string.evento11) + " " + time2 + " ACK : Error");
                                        numberlines_text++;
                                    }

                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        eventos_txt.setMovementMethod(new ScrollingMovementMethod());
                    }
                }
                if (temporizador == 15 || temporizador == 45) {   //                if(temporizador == 15  ){   //
                    quantity = mdeviceblu.size();
                    txtCounter.setText("Devices: "+ quantity);
                    String time2 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    if (quantity > 0) { // restringir la transmision a 5 unidades..
                        Date currentTime = Calendar.getInstance().getTime();
                        if (quantity > 20)
                            quantity = 20;
                        for (int i = 0; i < quantity; i++) {
                            try {
                                if (Global.gga1 != null) {
                                    String datos = evento44(i);
                                    if (datos.contains(",A,")) {
                                        answer_tx=enviar(datos);
                                        eventos_txt.append(getString(R.string.evento44) + " " + time2);
                                        if(answer_tx){
                                            eventos_txt.append(" ACK : Ok");
                                        }else{
                                            eventos_txt.append(" ACK : Err");
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.w("Error", "Socket" + e.getMessage());
                                //eventos_txt.append("\nError Socket"+ currentTime.getTime());
                            }
                            // eventos_txt.setMovementMethod(new ScrollingMovementMethod());
                        }
                    }
                    mdeviceblu.clear();
                    //txtCounter.setText("Devices: 0");
                    //scanLeDevice(true);
                }
                if (temporizador % 10 == 0) {  // cambio de prueba de 10 a 5 ....
                    Date currentTime = Calendar.getInstance().getTime();
                    //textgga.setText("SPEED:" + velocidad + " ALT:"+ Global.altitude2.);
                    String time2 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    if (Global.rmc1 != null && Global.rmc1.length() > 50) {
                        String[] arreglo = Global.rmc1.split(",");
                        if (arreglo[2].contains("A")) {
                            gps_valido = arreglo[2];
                            if (Global.velo > 0) {
                                 DecimalFormat f = new DecimalFormat("##.##");
                                velocidad= Global.velo;
                            } else {
                                velocidad = 0.0;
                            }
                            //velocidad= Global.velo.;
                            String altura= Double.toString(Global.altitude2);
                            if (altura.length()>=7) {
                                altura = Double.toString(Global.altitude2).substring(0, 7);
                            }else{
                                altura = Double.toString(Global.altitude2).substring(0, altura.length());
                            }

                            textgga.setText("SPEED:" + velocidad + " ALT:"+ altura);
                            if ((gps_valido.contains("A") && (velocidad >= 0.4)) || (abs(Global.rumbo - Global.rumbo_ant) > 20 && (velocidad >= 0.4))) {
                                try {
                                    Global.rumbo_ant = Global.rumbo;
                                    answer_tx=enviar(frame_evento("0x96"));
                                    if(numberlines_text > 20){
                                        eventos_txt.setText("");
                                        numberlines_text=0;
                                    }
                                    eventos_txt.append(getString(R.string.evento90) + " " + time2 + " Speed:" + Global.velo );
                                    if(answer_tx){
                                        eventos_txt.append(" ACK : Ok");
                                    }else{
                                        eventos_txt.append(" ACK : Err");
                                    }
                                    numberlines_text++;
                                    //textgga.setText("SPEED: " + velocidad);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                //eventos_txt.setMovementMethod(new ScrollingMovementMethod());

                            }
                            if (gps_valido.contains("A") && (velocidad < 0.4) && (temporizador%20 == 0)) {
                                try {
                                    Global.rumbo_ant = Global.rumbo;
                                    enviar(frame_evento("0x92"));
                                    eventos_txt.append(getString(R.string.evento91) + " " + time2 + " Speed:" + Global.velo);
                                    if(answer_tx){
                                        eventos_txt.append(" ACK : Ok");
                                    }else{
                                        eventos_txt.append(" ACK : Err");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                //eventos_txt.setMovementMethod(new ScrollingMovementMethod());
                            }
                        }
                    }
                }
                if (temporizador == 20 || temporizador == 40) {

                    if(socket.isClosed()){
                        try {
                            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                            socket = new Socket(serverAddr, SERVERPORT);
                        } catch (UnknownHostException e1) {
                            //e1.printStackTrace();
                            Log.w("Error:","Network error 1");
                        } catch (IOException e1) {
                            //e1.printStackTrace();
                            Log.w("Error:","Network error 2");
                        }
                    }

                   if(!(socket ==null)) {
                       if (socket.isClosed()) {
                           //bt_pdu.callOnClick();
                           Log.w("Socket","Conection Closed");
                       }
                   }else {
                       try {
                           InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                           socket = new Socket(serverAddr, SERVERPORT);
                       } catch (UnknownHostException e1) {
                           //e1.printStackTrace();
                           Log.w("Error:","Sin acceso a internet");
                       } catch (IOException e1) {
                           //e1.printStackTrace();
                           Log.w("Error:","Sin acceso a internet");
                       }

                       if(!(socket ==null)) {
                           if (socket.isConnected() && socket.isBound() && socket.isClosed() == false) {
                               Log.d("Socket", "Conectado");
                           }
                       }
                   }
                    send_saved();
                    //Toast.makeText(null, "Application working...", Toast.LENGTH_SHORT).show();
                    if (isMyServiceRunning(LocationService.class)) {
                        Log.w("GPS", "Service is running");
                    } else {
                        Log.w("GPS", "Service is Stopped");
                        //Intent i = new Intent(getApplicationContext(), LocationService.class);
                        //startService(i);
                    }
                }
                if (temporizador % 2 == 0) {
                    if (progressBar2 != null) {
                        if (progAsc) {
                            if (progressvalue >= 0 && progressvalue < 100) {
                                progressvalue += 4;
                                progressBar2.setProgress(progressvalue, true);
                            }
                            if (progAsc == true && progressvalue >= 100) {
                                progAsc = false;
                            }
                        } else {
                            /*if (progressvalue >= 5) {
                                progressvalue -= 5;
                                progressBar2.setProgress(progressvalue, true);
                                if (progAsc == false && progressvalue <= 5) {
                                    progAsc = true;
                                }
                            }*/
                            progAsc=true;
                            progressvalue=0;
                        }
                    }
                }

            }//fin run...
        };
        myhandler.postDelayed(runnable, 1000);
        //////////////////////////


        //////////////en timer.////////////
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_no_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ///////////////////////////////////
        TelephonyManager telecomManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            {
                if (telecomManager != null) {
                     //timei = telecomManager.getImei();
                     //  timei=telecomManager.getMeid();
                     //imei = timei.toString();*/
                     //imei = "867122048894467";
                }
            }
        }
        //String UID= UUID.randomUUID().toString().replace("-","");

        String UUID= Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);
        imei=UUID.substring(3,16);
        long temp1 = hex2decimal(imei);
        imei=Long.toString(temp1);
        if(imei.length() > 15) {
            imei = Long.toString(temp1).substring(1, 16);
        }
        //imei = "CODE ID:867122048894467";
        //imei = "867122048894467";
        //imei="355144112171704";
        Textimei.setText(imei);
        txtimei.setText(imei);
        //final String uuid = UUID.randomUUID().toString().replace("-", "");
        //System.out.println("uuid = " + uuid);
        /////////////////////////////////
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(getApplicationContext(), "Sin Permiso Internet", Toast.LENGTH_SHORT);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Permiso Internet", Toast.LENGTH_SHORT);
        }


        btMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ArrayList<deviceblu> mdeviceblu = new ArrayList<deviceblu>();
                //mdeviceblu.add(new deviceblu("alejo","10","12","13"));
                //adapter.notifyDataSetChanged();
                mdeviceblu.clear();
                scanLeDevice(true);

            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //texto.setText("");
                eventos_txt.setText("");
                numberlines_text=0;
                load(v);
                progressvalue+=10;
                if(progressBar2 !=null) {
                    if (progressvalue > 10 && progressvalue < 360) {
                        progressBar2.setProgress(progressvalue);
                    }
                    if(progressvalue >=360){
                        progressvalue=0;
                        progressBar2.setProgress(progressvalue);
                    }
                }
            }
        });

        bstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationService();
                // Iniciar Schedule Job.
                scheduleJob();

            }
        });
        bstop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopLocationService();
                cancelJob();

            }
        });

        btToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                    return;
                                }

                                // Get new FCM registration token
                                String token = task.getResult();

                                // Log and toast
                                String msg = getString(R.string.msg_token_fmt, token);
                                Log.d(TAG, msg);
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });


        // Iniciar Schedule Job.
        scheduleJob();
        /////////////////////////////////////////////////////////////
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN
                ).withListener(new MultiplePermissionsListener() {
                    @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                        startLocationService();
                    }
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "You must accept Location Permission", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
                }).check();
        ////////////////////////////////////////////////////////////////////
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            socket = new Socket(serverAddr, SERVERPORT);
            if (socket.isConnected() && socket.isBound() && socket.isClosed() == false) {
                Log.d("Socket", "Conectado");
            }
        } catch (UnknownHostException e1) {
            //e1.printStackTrace();
            //Toast.makeText(this,"Advertencia: Sin acceso a internet",Toast.LENGTH_SHORT).show();
            Log.w("Error:","Sin accesso a internet");
        } catch (IOException e1) {
            //e1.printStackTrace();
            Log.w("Error:","Sin accesso al dispositivo de conexion");
        }
        ////////////////////////////////////////////////////////////////////
        BroadcastReceiver br = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        //filter.addAction(Intent.ACTION_SCREEN_ON);
        this.registerReceiver(br, filter);

        /////////////////////////////
        //ProgressBar progressBar2=new ProgressBar(this);
        //progressBar2.setProgress(progressvalue);
        String pattern = "ddMMyy hh:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(new Date());
        System.out.println(date);
        txtCounter2.setText("Running: "+date);
        startLocationService();

    }

    private void addNotification2() {

        String channel_id ="Location Service";    // "job_notification_channel";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Notifications JobService")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOngoing(true)
                .setTimeoutAfter(10000L)
                .setSmallIcon(R.drawable.ic_alert)
                .setChannelId(channel_id)
                .setContentText("This is a Quantum notification from Background");

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


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void scheduleJob() {
        ComponentName componentName = new ComponentName(this, MyJobService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                //.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresBatteryNotLow(true)
                .setPersisted(true)
                .setPeriodic(20 * 60 * 1000)  //15
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Toast.makeText(this,TAG2+"JobScheduled",Toast.LENGTH_LONG);
        }
        if(resultCode == JobScheduler.RESULT_FAILURE){
            Log.d(TAG2, "Job Scheduled Failed");
        }
    }

    public void cancelJob() {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(123);
        Log.d(TAG, "Job Cancelled");
    }

    public void onRequestPermissionsResults(int requestcode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestcode, permissions, grantResults);
        if (requestcode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
                Toast.makeText(this, "Servicio de Localizacion Iniciado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Location denied", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service :
                    activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }

                }
            }
            return false;
        }
        return false;
    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.START_LOCATION_SERVICE);
            //startService(intent);
            startForegroundService(intent);
            Toast.makeText(this, "Location Service Started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService() {
       if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.STOP_LOCATION_SERVICE);
            stopService(intent);
            Toast.makeText(this, "Location Service Stopped", Toast.LENGTH_SHORT).show();
        }else{
           Toast.makeText(this, "Location Is not Running", Toast.LENGTH_SHORT).show();
       }
    }

    public boolean isServiceIsRunningForeground(Context context){
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service:manager.getRunningServices(Integer.MAX_VALUE))
            if(getClass().getName().equals(service.service.getClassName()))
                if(service.foreground)
                    return  true;
        return false;
    }

    private void enable_buttons() {
        bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), LocationService.class);
                stopService(i);
            }
        });
    }

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        } else
            return false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enable_buttons();
            } else {
                runtime_permissions();
            }

        }
    }

    public String frame44(String nombre, String drssi, String datos) {
        String frame = imei + " ";
        frame += EVENTO44;
        frame += " ";
        frame += MILES;
        frame += " ";
        frame += IOSTATUS;
        frame += " ";
        frame += ANALOG;
        frame += " ";
        frame += drssi;
        frame += "-";
        int long1 = datos.length();
        String datos2 = datos.substring(long1 - 4, long1);
        frame += datos2;
        frame += "-";
        frame += nombrepdu(nombre);
        frame += "\r\n";
        frame += Global.gga1.trim();
        frame += "\r\n";
        frame += Global.rmc1.trim();
        frame += ",,";
        frame += "\r\n";
        frame += ">";
        frame += "\r\n";

        Log.w("TRAMA:", frame);
        //eventos_txt.append("\n" + nombre + "\r");
        //eventos_txt.setMovementMethod(new ScrollingMovementMethod());
        return frame;
    }

    public boolean enviar(String frame) throws IOException {
        try {
            if(socket==null){
                txtCounter3.setText("Connection: NUll");
                return false;
            }
            String str = frame.toString();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            out.println(frame);
            out.flush();
            Thread.sleep(1500);
            if (in.ready()) {
                int response = in.read();
                String resp = Integer.toString(response);
                Log.w("Socket", "Response:" + resp);
                if(resp.contains("65")){
                    Date currentTime = Calendar.getInstance().getTime();
                    txtCounter3.setText("Connection: Sent"+ currentTime.toString().substring(3,16));
                }
                return true;
                //eventos_txt.append(" Resp:"+ resp);

            } else {
                Thread.sleep(1000);
                if (in.ready()) {
                    int response = in.read();
                    String resp = Integer.toString(response);
                    Log.w("Socket", "Response:" + resp);
                    if(resp.contains("65")){
                        Date currentTime = Calendar.getInstance().getTime();
                        txtCounter3.setText("Connection:Sent "+ currentTime.toString().substring(3,16));
                    }
                    return true;
                    //txtStatusSck.setText("Connected");
                    //eventos_txt.append(" Resp1:"+ resp);
                } else {
                    //in.close();
                    //out.close();
                    //eventos_txt.append(" Resp2: Not answer");
                    Log.w("Socket", "No answer");
                    //txtStatusSck.setText("DisConnected");
                    txtCounter3.setText("Connection: Fail");
                    writeToFile(frame, context);
                    socket.close();
                    Date currentTime = Calendar.getInstance().getTime();
                    txtCounter3.setText("Connection:Closed "+ currentTime.toString().substring(3,16));
                    Thread.sleep(2000); //reconnection test
                    if (socket.isClosed()) {
                        try {
                            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                            socket = new Socket(serverAddr, SERVERPORT);
                            txtCounter3.setText("Connection:Begin "+ currentTime.toString().substring(3,16));
                            return false;
                        } catch (UnknownHostException e1) {
                            //e1.printStackTrace();
                            writeToFile(frame, context);
                            eventos_txt.append("Resp2: Not answer -Err1");
                            return false;
                        } catch (IOException e1) {
                            //e1.printStackTrace();
                            writeToFile(frame, context);
                            eventos_txt.append("Resp2: Not answer -Err2");
                            return false;
                        }
                    }
                    return false;
                }
            }
            //Toast.makeText(MainActivity.this, resp, Toast.LENGTH_SHORT).show();
        } catch (IOException | InterruptedException e) {
            //e.printStackTrace();
            Log.w("ErrorTX", e.getMessage());
            writeToFile(frame, context);
            Date currentTime = Calendar.getInstance().getTime();
            txtCounter3.setText("Connection:Fail"+ currentTime.toString().substring(3,16));
            socket.close();
            return false;
        }
    }

    public String evento44(int posicion) {
        String nombre = mdeviceblu.get(posicion).getMname();
        String rssi2 = mdeviceblu.get(posicion).mrssi;
        String data1 = mdeviceblu.get(posicion).mmsb;
        int msb1 = Integer.parseInt(data1);
        String hex1 = String.format("%02X", (int) msb1);
        String data2 = mdeviceblu.get(posicion).mlbs;
        int lsb1 = Integer.parseInt(data2);
        String hex2 = String.format("%02X", (int) lsb1);
        String datamov = hex1 + hex2;
        String frame = frame44(nombre, rssi2, datamov).trim();
        return frame;
    }

    public String frame_keep() {

        String frame = null;
        if (Global.gga1 != null) {
            frame = imei.trim();
            frame += " ";
            frame += "0x11";
            frame += " 13 ";
            frame += IOSTATUS;
            frame += " 0 0 0 0.000 ";
            frame += "\r\n";
            frame += Global.gga1.trim();
            frame += "\r\n";
            frame += Global.rmc1.trim();
            frame += ",,";
            frame += "\r\n";
            frame += ">";
            frame += "\r\n";
            Log.w("TRAMA:", frame);
        } else {
            Log.w("TRAMA:", "Sin datos localizacion");
        }
        return frame;
    }

    public String frame_evento(String evento) {

        String frame = imei.trim();
        frame += " ";
        frame += evento.trim();
        frame += " 13 ";
        frame += IOSTATUS;
        frame += " 0 0 0 0.000 ";
        frame += "\r\n";
        frame += Global.gga1.trim();
        frame += "\r\n";
        frame += Global.rmc1.trim();
        frame += ",,";
        frame += "\r\n";
        frame += ">";
        frame += "\r\n";
        Log.w("TRAMA:", frame);
        return frame;
    }

    private void sendkeep(String frame) {

        if (socket.isConnected()) {
            try {

                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                out.println(frame);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            } catch (Exception e) {
                e.printStackTrace();
            }

        /*try (DataInputStream is = new DataInputStream(socket.getInputStream())) {
            final String response = is.readLine();
            Log.w("Respuesta envio", response);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        } else {
            txtStatusSck.setText("Disconnected");

        }
    }

    private String nombrepdu(String nombre) {
        String resultado = "";
        byte[] datos = nombre.getBytes();
        int longitud = datos.length;
        for (int i = 0; i < longitud; i++) {
            String hex = String.format("%02X", (int) datos[i]);
            resultado += hex;
            //Log.w("Conversion","Trama:"+ resultado);
        }
        return resultado;
    }

    public void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = true;
                    //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

        }
    }

    public void scanLeDevice2(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = true;
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

        }
    }

    // Device scan callback.
    @Deprecated
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //final ArrayList<deviceblu> mdeviceblu = new ArrayList<deviceblu>();
                            //deviceAdapter adapter;
                            //adapter = new deviceAdapter(this,mdeviceblu);
                            //final deviceAdapter adapter = new deviceAdapter(this,mdeviceblu);
                            //final deviceAdapter adapter = new deviceAdapter(this,mdeviceblu);
                            String time2 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                            String nombre = null;
                            String nombre2 = null;
                            int msb = scanRecord[7];
                            int lsb = scanRecord[8];
                            nombre2 = device.getName();
                            if (nombre2 != null && searchitem(mdeviceblu, nombre2) == false) {
                                nombre = device.getName();
                                //Log.w(TAG, "Name:" + device.getName() + " RSSI:" + Integer.toString(rssi) +" MSB:" + msb + " LSB:" + lsb);
                                if (nombre != null && nombre.contains("P MOV")) {
                                    //mLeDeviceListAdapter.addDevice(device);
                                    //mLeDeviceListAdapter.notifyDataSetChanged();
                                    Log.w(TAG, "Name:" + device.getName() + " RSSI:" + Integer.toString(rssi) +
                                            " MSB:" + msb + " LSB:" + lsb);
                                    mdeviceblu.add(new deviceblu(nombre, "" + Integer.toString(rssi), "" + Integer.toString(msb), Integer.toString(lsb)));

                                    txtCounter.setText("Devices: "+ mdeviceblu.size());

                                    if(numberlines_text2 > 20){
                                        //texto.setText("");
                                        numberlines_text2=0;
                                    }
                                    eventos_txt.append(getString(R.string.tag) + " " + nombre2 + " RSSI:" + Integer.toString(rssi) + " " + time2);
                                    numberlines_text2++;
                                    //deviceAdapter.notifyDataSetChanged();
                                    //if (nombre.contains("B00089")) {
                                    //   Log.w(TAG, nombre + " RSSI:" + rssi);
                                    //}
                                }
                            }
                        }
                    });
                }
            };

    private boolean searchitem(ArrayList lista, String nombre) {

        for (int i = 0; i < lista.size(); i++) {
            if (mdeviceblu.get(i).getMname().equals(nombre)){
                System.out.println("Item found" + nombre);
                return true;
            }
        }
        return false;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void triggerRebirth(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    // Create LogFile
    private void writeToFile(String data, Context context) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(FILE_NAME, MODE_APPEND);
            fos.write(data.getBytes());
            //Toast.makeText(this, "Saved to " + getFilesDir() + "/" + FILE_NAME, Toast.LENGTH_LONG).show();
            Log.w("DATA: ", "Frame saved");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void load(View v) {
        FileInputStream fis = null;
        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
                Log.w("DATA: ", text);
            }
            eventos_txt.setText(sb.toString());

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    public boolean send_saved() {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        int counter = 0;
        int count_lines=0;
        boolean answer=false;
        String datanull=" ";

        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text=null;
            counter=1;
            while (counter==1){
                text=br.readLine();
                if(text != null) {
                    sb.append(text).append("\r\n");
                    counter=1;
                    count_lines++;
                }else {
                    counter=0;
                }
                if(count_lines>=4){
                    Log.w("DATA: ", sb.toString());
                    answer = enviar_saved(sb.toString());
                    if (answer) {
                        Log.w("DATA:", "Stored Data frame sent");
                        count_lines=0;
                        sb.setLength(0);
                    }else{
                        counter=0;
                        Log.w("DATA:", "Stored Data frame Does not Sent");
                    }
                }

            }
              //erase file data after send to server.
            if(answer) {
                writeEmptyToFile(context);
            }
         } catch (FileNotFoundException e) {
            try {
                fos = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                try {
                    fos.write(datanull.getBytes());
                    fos.close();
                    return false;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                    return true;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    private void writeEmptyToFile(Context context) {
        FileOutputStream fos = null;
        String dataempty=null;
        try {
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(null);
            Toast.makeText(this, "Saved to " + getFilesDir() + "/" + FILE_NAME, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public boolean enviar_saved(String frame) throws IOException {
        try {
            String str = frame.toString();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            out.println(frame);
            out.flush();
            Thread.sleep(1000);
            if (in.ready()) {
                int response = in.read();
                String resp = Integer.toString(response);
                Log.w("Socket", "Response:" + resp);
                //eventos_txt.append(" Resp:"+ resp);

            } else {
                Thread.sleep(200);
                if (in.ready()) {
                    int response = in.read();
                    String resp = Integer.toString(response);
                    Log.w("Socket", "Response:" + resp);
                    //txtStatusSck.setText("Connected");
                    txtCounter3.setText("Connection:"+resp);
                    //eventos_txt.append(" Resp1:"+ resp);
                } else {
                    Log.w("Socket", "No answer");
                    socket.close();
                    if (socket.isClosed()) {
                        try {
                            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                            socket = new Socket(serverAddr, SERVERPORT);
                        } catch (UnknownHostException e1) {
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                         }
                    }
                }
            }
            //Toast.makeText(MainActivity.this, resp, Toast.LENGTH_SHORT).show();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Log.w("ErrorTX", e.getMessage());
            socket.close();
        }
        return true;
    }
    public static String convertStringToHex(String str) {

        // display in uppercase
        //char[] chars = Hex.encodeHex(str.getBytes(StandardCharsets.UTF_8), false);

        // display in lowercase, default
        byte[] chars = Hex.stringToBytes(String.valueOf(str.getBytes(StandardCharsets.UTF_8)));

        return String.valueOf(chars);
    }

    public static long hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        long val1 = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val1 = 16 * val1 + d;
        }
        return val1;
    }

    private void unlockScreen() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    public void notification(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAGFIREBASE, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.w(TAGFIREBASE, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
