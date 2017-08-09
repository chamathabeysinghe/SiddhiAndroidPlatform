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

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;


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
            Runnable r=new Runnable() {
                @Override
                public void run() {
                    appManager.startApp(definition,identifier);
                }
            };

//            Runnable r= () -> {
//                appManager.startApp(definition,identifier);
//            };
            Thread t=new Thread(r);
            t.setContextClassLoader(this.getClass().getClassLoader());
            t.start();
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

}
