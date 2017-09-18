/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhiandroidlibrary;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;


public class SiddhiAppService extends Service {

    public static SiddhiAppService instance; //remove this
    public static final String LOCAL_PARAMETER_NAME="local-service";
    private RequestController requestController=new RequestController();
    private LocalBinder localBinder=new LocalBinder();
    private AppManager appManager;
    public SiddhiAppService() {
        SiddhiAppService.instance=this;
        appManager=new AppManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationUtils mNotificationUtils = new NotificationUtils(this);
        String event = "Sensor service started";
        Notification n;
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            Notification.Builder nb = mNotificationUtils.getSiddhiChannelNotification("Event",event.toString());
            n=nb.build();
            mNotificationUtils.getManager().notify(201,n);
            startForeground(201,n);
        }
        else{
            n  = new NotificationCompat.Builder(this)
                    .setContentTitle("Event")
                    .setContentText(event.toString())
                    .setSmallIcon(R.drawable.icon)
                    .build();
            mNotificationUtils.getManager().notify(202, n);
            startForeground(202,n);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(intent.getBooleanExtra(LOCAL_PARAMETER_NAME,false)){
            return localBinder;
        }
        return requestController;

    }

    private class RequestController extends IRequestController.Stub{

        @Override
        public String startSiddhiApp(String definition, String identifier) throws RemoteException {

            appManager.startApp(definition,identifier);
//            Runnable r=new Runnable() {
//                @Override
//                public void run() {
//                    Looper.prepare();
//                    appManager.startApp(definition,identifier);
//
//
//                }
//            };


//            Thread t=new Thread(r);
//            t.setContextClassLoader(this.getClass().getClassLoader());
//            t.start();
            return "App Launched";
        }

        @Override
        public String stopSiddhiApp(String identifier) throws RemoteException {
            appManager.stopApp(identifier);
            return "App Stopped";
        }
    }

    //should be removed
    public class LocalBinder extends Binder {
        public SiddhiAppService getService(){
            return SiddhiAppService.this;
        }
    }

    public static int getAppIcon(){
        return R.drawable.icon;
    }

}

class NotificationUtils extends ContextWrapper {

    private NotificationManager mManager;
    public static final String SIDDHI_CHANNEL_ID = "org.wso2.SIDDHI";
    public static final String SIDDHI_CHANNEL_NAME = "SIDDHI CHANNEL";

    public NotificationUtils(Context base) {
        super(base);
        createChannels();
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createChannels() {

        NotificationChannel androidChannel = new NotificationChannel(SIDDHI_CHANNEL_ID,SIDDHI_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        androidChannel.enableLights(true);
        androidChannel.enableVibration(true);
        androidChannel.setLightColor(Color.GREEN);
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(androidChannel);

    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getSiddhiChannelNotification(String title, String body) {
        return new Notification.Builder(getApplicationContext(), SIDDHI_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setAutoCancel(true);
    }
}
