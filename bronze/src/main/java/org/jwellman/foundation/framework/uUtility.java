package org.jwellman.foundation.framework;

/**
 * Utility methods used by the Foundation framework.
 * 
 * @author Rick
 */
public class uUtility {

    /**
     * Utility method to provide a default value if the original value is null.
     * 
     * @param candidate The String that might be null.
     * @param override The String to return if candidate is null.
     * @return The original value if not null.  Otherwise, the override value.
     */
    public static String valueOrDefault(String candidate, String override) {
        return (candidate == null) ? override : candidate;
    }


    /**
     * Get the same string representation of an Object as the Object class does.
     * <p>
     * Used when a class overrides toString() and you want the "original" version.
     * 
     * @param obj
     * @return a String representation of the object, e.g. BasicDesktopPaneUI$BasicDesktopManager@2bdfda0c
     */
    public static String objString(Object obj) {
        String defaultString = obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
        return defaultString;
    }

    /**
     * Log the current stack (without throwing an exception)
     * 
     * @param message a message placed at the top of the stacktrace
     */
    public static void logStackTrace(String message) {
        System.err.println("=== " + message + " ===");
        new Exception().printStackTrace(System.err);
    }

}
