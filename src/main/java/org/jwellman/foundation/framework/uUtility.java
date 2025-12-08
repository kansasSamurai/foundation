package org.jwellman.foundation.framework;

/**
 *
 * @author Rick
 */
public class uUtility {

    public static String valueOrDefault (String candidate, String override) {
        return (candidate == null) ? override : candidate;
    }

}
