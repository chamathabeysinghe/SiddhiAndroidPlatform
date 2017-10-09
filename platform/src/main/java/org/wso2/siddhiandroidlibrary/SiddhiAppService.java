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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;


public class SiddhiAppService extends Service {

    public static SiddhiAppService instance;
    private RequestController requestController=new RequestController();
    private AppManager appManager;
    public static final String SIDDHI_CHANNEL_ID = "org.wso2.ANDROID_SIDDHI_PLATFORM";
    public static final String NOTIFICATION_CHANNEL_NAME = "SIDDHI_CHANNEL";
    public static final int NOTIFICATION_ID = 100;
    public static final String NOTIFICATION_TITLE = "Siddhi";
    public static final String NOTIFICATION_BODY = "Sidddhi Platform Service started";

    public SiddhiAppService() {
        SiddhiAppService.instance=this;
        appManager=new AppManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel androidChannel = new NotificationChannel(SIDDHI_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            androidChannel.enableLights(true);
            androidChannel.enableVibration(true);
            androidChannel.setLightColor(Color.GREEN);
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(androidChannel);
            Notification.Builder nb = new Notification.Builder(getApplicationContext(),
                    SIDDHI_CHANNEL_ID)
                    .setContentTitle(NOTIFICATION_TITLE)
                    .setContentText(NOTIFICATION_BODY)
                    .setSmallIcon(android.R.drawable.stat_notify_more)
                    .setAutoCancel(true);
            Notification notification = nb.build();
            notificationManager.notify(NOTIFICATION_ID,notification);
            startForeground(NOTIFICATION_ID,notification);
        }
        else{
            Notification notification = new NotificationCompat.Builder(this,
                    NOTIFICATION_CHANNEL_NAME)
                    .setContentTitle(NOTIFICATION_TITLE)
                    .setContentText(NOTIFICATION_BODY)
                    .setSmallIcon(R.drawable.icon).build();
            notificationManager.notify(NOTIFICATION_ID,notification);
            startForeground(NOTIFICATION_ID,notification);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return requestController;
    }

    public static int getAppIcon(){
        return R.drawable.icon;
    }

    private class RequestController extends IRequestController.Stub{

        @Override
        public boolean startSiddhiApp(String definition, String identifier) throws RemoteException {
            return appManager.startApp(definition);
        }

        @Override
        public boolean stopSiddhiApp(String identifier) throws RemoteException {
            return appManager.stopApp(identifier);
        }
    }
}

