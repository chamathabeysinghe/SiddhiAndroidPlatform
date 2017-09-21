package org.wso2.siddhiandroidplatform;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.wso2.siddhiandroidlibrary.IRequestController;
import org.wso2.siddhiandroidlibrary.SiddhiAppService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String APP_IDENTIFIER = "org.wso2.sampleapp";
    private final String SIDDHI_SERVICE_IDENTIFIER = "org.wso2.siddhiappservice.AIDL";

    private DataUpdateReceiver dataUpdateReceiver;

//    private String inStreamDefinition = "" +
//            "@app:name('foo2')" +
//            "@source(type='broadcast-receiver',identifier='" + Intent.ACTION_BATTERY_LOW + "' , @map(type='passThrough'))" +
//            "define stream streamTemperature ( k1 string, k2 string );" +
//            "@sink(type='broadcast' , identifier='TEMPERATURE_DETAILS' , @map(type='passThrough'))" +
//            "define stream broadcastOutputStream (k1 string, k2 string); " +
//            "from streamTemperature select k1,k2 insert into broadcastOutputStream";
//    private String inStreamDefinition = "" +
//            "@app:name('foo2')" +
//            "@source(type='location',identifier='" + Intent.ACTION_BATTERY_LOW + "' , @map(type='keyvalue'))" +
//            "define stream streamTemperature ( latitude double, longitude double );" +
//            "@sink(type='broadcast' , identifier='TEMPERATURE_DETAILS' , @map(type='keyvalue'))" +
//            "define stream broadcastOutputStream (latitude double, longitude double); " +
//            "from streamTemperature select latitude,longitude insert into broadcastOutputStream";
    private String inStreamDefinition = "" +
            "@app:name('foo2')" +
            "@source(type='proximity', @map(type='keyvalue',fail.on.missing.attribute='false'," +
            "@attributes(s='sensor',v='value')))" +
            "define stream streamTemperature ( s string, v float);" +
            "@sink(type='broadcast' , identifier='TEMPERATURE_DETAILS' , @map(type='keyvalue'," +
            "@payload(message='Proximity is {{v}} taken from {{s}}')))" +
            "define stream broadcastOutputStream (s string, v float); " +
            "from streamTemperature select * insert into broadcastOutputStream";
    private ListView listView;
    private ArrayList<String> messageList = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;

    private IRequestController comman;
    private ServiceConnection serviceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            comman = IRequestController.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private final int MY_PERMISSION_ACCESS_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.messageList);

        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, messageList);
        listView.setAdapter(listAdapter);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Location", "1");
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.e("Permission", "Show details");

            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_ACCESS_LOCATION);
                Log.e("Location", "2");
            }

        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.e("Locatoin","Permission given");

                } else {
                    Log.e("Locatoin","Permission not given");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter("TEMPERATURE_DETAILS");
        registerReceiver(dataUpdateReceiver, intentFilter);
    }

    /**
     * Bind the Android app to Sidddhi Service
     *
     * @param view
     */
    public void bindToSiddhiService(View view) {
        Toast.makeText(this, "Binding to the service", Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(SIDDHI_SERVICE_IDENTIFIER);
//        bindService(convertIntent(intent), serviceCon, BIND_AUTO_CREATE);
        Intent intent1=new Intent(this,SiddhiAppService.class);
        startService(intent1);
        bindService(intent1,serviceCon,BIND_AUTO_CREATE);
    }

    /**
     * Send the app stream to SiddhiService
     *
     * @param view
     */
    public void sendAQuery(View view) {
        try {
            comman.startSiddhiApp(inStreamDefinition, APP_IDENTIFIER);
            Toast.makeText(this, "Send the query : " + inStreamDefinition, Toast.LENGTH_LONG).show();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * stop a running siddhi app
     *
     * @param view
     */
    public void stopAQuery(View view) {
        try {
            comman.stopSiddhiApp(APP_IDENTIFIER);
            Toast.makeText(this, "Stop the query", Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private Intent convertIntent(Intent implicitIntent) {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> resolveInfoList = pm.queryIntentServices(implicitIntent, 0);
        if (resolveInfoList.size() != 1) {
            Log.e("Not null ", "Size error  problem  " + resolveInfoList.size());
        }
        if (resolveInfoList == null) {
            Log.e("NUll", "null error");
            return null;
        }
        ResolveInfo serviceInfo = resolveInfoList.get(0);
        ComponentName component = new ComponentName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name);
        Intent explicitIntent = new Intent(implicitIntent);
        explicitIntent.setComponent(component);
        return explicitIntent;
    }

    /**
     * Broadcast receiver to get intents from the Siddhi Service
     * Has a hardcoded intent filter to match the query
     */
    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("TEMPERATURE_DETAILS")) {
                messageList.add(intent.getStringExtra("events"));
                listAdapter.notifyDataSetChanged();
                Log.i("TEMPERATURE_DETAILS", intent.getStringExtra("events"));
            }
        }
    }
}
