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

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;


import java.util.HashMap;


public class AppManager {

    //SiddhiAppManager

    private SiddhiManager siddhiManager;
    private volatile HashMap<String,SiddhiAppRuntime> siddhiAppList;

    public AppManager(){
        siddhiAppList=new HashMap<>();
        this.siddhiManager=new SiddhiManager();
//        siddhiManager.setExtension("source:proximity",ProximitySensorSource.class);
//        siddhiManager.setExtension("source:temperature",TemperatureSensorSource.class);
//        siddhiManager.setExtension("sink:broadcast", BroadcastIntentSink.class);
    }

    /**
     * Start a new Siddhi App
     * @param inStream Siddhi App definition
     * @param identifier unique identifier to identify the app
     */
    public void startApp(String inStream,String identifier){
        SiddhiAppRuntime siddhiAppRuntime=siddhiManager.createSiddhiAppRuntime(inStream);
        synchronized (this){
            if(siddhiAppList.containsKey(identifier)){  //what to do?
                siddhiAppList.get(identifier).shutdown();
            }
            siddhiAppList.put(identifier,siddhiAppRuntime);
        }

        siddhiAppRuntime.start();
    }

    /**
     * Shutdown a running Siddhi App
     * @param identifier unique identifier to identify the app (as previously provided when starting the app)
     */
    public void stopApp(String identifier){
        SiddhiAppRuntime siddhiAppRuntime;
        synchronized (this){
            siddhiAppRuntime=siddhiAppList.get(identifier);
        }

        siddhiAppRuntime.shutdown();
    }
}
