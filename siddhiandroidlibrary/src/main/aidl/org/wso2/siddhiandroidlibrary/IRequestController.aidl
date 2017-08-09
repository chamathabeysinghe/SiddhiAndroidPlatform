// IRequestController.aidl
package org.wso2.siddhiandroidlibrary;

// Declare any non-default types here with import statements

interface IRequestController {

    String startSiddhiApp(String definition,String identifier);
    String stopSiddhiApp(String identifier);
}
