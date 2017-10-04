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

import android.util.Log;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.HashMap;


public class AppManager {

    private SiddhiManager siddhiManager;
    private volatile HashMap<String,SiddhiAppRuntime> siddhiAppList;

    public AppManager(){
        siddhiAppList=new HashMap<>();
        this.siddhiManager=new SiddhiManager();
    }

    /**
     * Start a new Siddhi App
     * @param inStream Siddhi App definition
     */
    public boolean startApp(String inStream){

        try{
            SiddhiAppRuntime siddhiAppRuntime=siddhiManager.createSiddhiAppRuntime(inStream);
            String appIdentifier = siddhiAppRuntime.getName();
            if(siddhiAppList.containsKey(appIdentifier)){
                siddhiAppList.get(appIdentifier).shutdown();
                //stacktrace ??
                Log.e("Siddhi Platform","Similar App name already exists ins the list");
                return false;
            }
            siddhiAppList.put(appIdentifier,siddhiAppRuntime);
            siddhiAppRuntime.start();
            return true;
        }
        catch (SiddhiAppCreationException e){
            Log.e("Siddhi App Error",Log.getStackTraceString(e));
            return false;
        }
        catch (SiddhiAppValidationException e){
            Log.e("Siddhi App Error",Log.getStackTraceString(e));
            return false;
        }

    }

    /**
     * Shutdown a running Siddhi App
     * @param appName unique identifier to identify the app (as previously provided when starting the app)
     */
    public boolean stopApp(String appName){
        SiddhiAppRuntime siddhiAppRuntime=siddhiAppList.get(appName);

        if(siddhiAppRuntime==null){
            Log.e("Siddhi Platform","No app with name, '"+appName+"', is currently executing. ");
            return false;
        }
        siddhiAppRuntime.shutdown();
        return true;
    }
}
