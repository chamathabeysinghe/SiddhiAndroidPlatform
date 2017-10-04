// IRequestController.aidl
package org.wso2.siddhiandroidlibrary;

// Declare any non-default types here with import statements

interface IRequestController {

    boolean startSiddhiApp(String definition,String identifier);
    boolean stopSiddhiApp(String identifier);
}
