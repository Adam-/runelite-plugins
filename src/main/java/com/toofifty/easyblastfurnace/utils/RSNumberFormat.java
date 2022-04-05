package com.toofifty.easyblastfurnace.utils;

public class RSNumberFormat
{
    public static String format(int number)
    {
        if (number < 100000) {
            return String.format("%,d", number);
        }

        if (number < 10000000) {
            return String.format("%,dK", number / 1000);
        }

        return String.format("%,dM", number / 1000);
    }

    public static String format(double number)
    {
        return format((int) number);
    }
}
