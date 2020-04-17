package net.frankheijden.insights.utils;

import java.util.Collection;

public class StringUtils {

    public static boolean matches(Collection<String> regexStrings, String str) {
        return matchesWith(regexStrings, str) != null;
    }

    public static String matchesWith(Collection<String> regexStrings, String str) {
        for (String regex : regexStrings) {
            if (str.matches(regex)) {
                return regex;
            }
        }
        return null;
    }

    public static String capitalizeName(String name) {
        name = name.toLowerCase();
        StringBuilder stringBuilder = new StringBuilder();
        for (String entry : name.split("[_ ]")) {
            stringBuilder.append(org.apache.commons.lang.StringUtils.capitalize(entry.toLowerCase())).append(" ");
        }
        String build = stringBuilder.toString();
        return build.substring(0, build.length() - 1);
    }
}