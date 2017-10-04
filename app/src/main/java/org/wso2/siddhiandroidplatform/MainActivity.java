package org.wso2.siddhiandroidplatform;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.wso2.siddhiandroidlibrary.IRequestController;
import org.wso2.siddhiandroidlibrary.SiddhiAppService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String APP_IDENTIFIER = "org.wso2.sampleapp";

    private DataUpdateReceiver dataUpdateReceiver;
    private String broadcastIdentifier = "TEMPERATURE_DETAILS";
//    private String inStreamDefinition = "" +
//            "@app:name('foo')" +
//            "@source(type='android-accelerometer', @map(type='keyvalue',fail.on.missing.attribute='false'," +
//            "@attributes(s='sensor',v='accelerationX')))" +
//            "define stream streamTemperature ( s string, v float);" +
//            "@sink(type='android-notification' , title='Hello World',multiple.notifications = 'true', @map(type='keyvalue'))" +
//            "define stream broadcastOutputStream (s string, v float); " +
//            "from streamTemperature select * insert into broadcastOutputStream";

    private String inStreamDefinition = "" +
            "@app:name('foo')" +
            "@source(type='android-accelerometer', @map(type='keyvalue',fail.on.missing.attribute='false'," +
            "@attributes(s='sensor',v='accelerationX')))" +
            "define stream streamTemperature ( s string, v float);" +
            "@sink(type='android-broadcast' , identifier='"+broadcastIdentifier+"' , @map(type='keyvalue'))" +
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
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this,"Location permission given",Toast.LENGTH_SHORT).show();

            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_ACCESS_LOCATION);
                Toast.makeText(this,"Location permission denied",Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.e("Locatoin","Permission given");

                } else {
                    Log.e("Locatoin","Permission not given");
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter(broadcastIdentifier);
        registerReceiver(dataUpdateReceiver, intentFilter);
    }

    /**
     * Bind the Android app to Sidddhi Service
     *
     * @param view
     */
    public void bindToSiddhiService(View view) {
        Toast.makeText(this, "Binding to the service", Toast.LENGTH_SHORT).show();
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

    /**
     * Broadcast receiver to get intents from the Siddhi Service
     * Has a hardcoded intent filter to match the query
     */
    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(broadcastIdentifier)) {
                messageList.add(String.valueOf(intent.getFloatExtra("v",0)));
                listAdapter.notifyDataSetChanged();
            }
        }
    }
}
