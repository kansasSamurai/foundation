package org.jwellman.foundation.framework;

/**
 *
 * @author Rick
 */
public class uUtility {

    /**
     * 
     * @param candidate
     * @param override
     * @return
     */
    public static String valueOrDefault(String candidate, String override) {
        return (candidate == null) ? override : candidate;
    }


    /**
     * 
     * 
     * @param obj
     * @return a String representation of the object, e.g. BasicDesktopPaneUI$BasicDesktopManager@2bdfda0c
     */
    public static String objString(Object obj) {
        String defaultString = obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
        return defaultString;
    }

}
