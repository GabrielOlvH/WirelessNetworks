package me.steven.wirelessnetworks.utils;

public class Utils {

    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static String getDisplayId(String s) {
        int index = s.indexOf(":");
        if (index > -1) return s.substring(index + 1);
        return s;
    }

    public static String sanitizeId(String s) {
        return s.replace(":", "");
    }
}