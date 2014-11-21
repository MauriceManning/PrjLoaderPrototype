package edu.berkeley.path.project_loader.shared;

import core.oraMonitor;

/**
 * Created by mauricemanning on 11/17/14.
 */
public class MDATestConfiguration {

    public static void dbSetup() {
        setSystemPropertyIfMissing("unitTest", "true");
        setSystemPropertyIfMissing("uname", "test_user");
        setSystemPropertyIfMissing("upass", "via_test");
        setSystemPropertyIfMissing("host", "cctest.path.berkeley.edu");
        setSystemPropertyIfMissing("port", "1521");
        setSystemPropertyIfMissing("sid", "viatest");
        setSystemPropertyIfMissing("debug", "true"); // Print stack traces

        oraMonitor.mon = false; // Turn off core library monitoring messages
        oraMonitor.debug = false; // Turn off debugging messaging
    }

    private static void setSystemPropertyIfMissing(String key, String value) {
        if (System.getProperty(key) == null)
            System.setProperty(key, value);
    }


}
