package org.my.konkurrent;

/**
 * Util functions go here
 */
public class Utils {
    public static boolean enableLog = true;

    public static void LOG(String s) {
        if(enableLog)
            System.out.println(s);
    }
}
