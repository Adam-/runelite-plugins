package com.raidtracker.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Utils
{
    public static boolean containsCaseInsensitive(List<String> l, String s)
    {
        for (String string : l)
        {
            if (s.toLowerCase().contains(string.toLowerCase()))
            {
                return true;
            }
        }
        return false;
    };
    public static boolean containsCaseInsensitive(String s, String[] f)
    {
        for (String string : f)
        {
            if (s.toLowerCase().contains(string.toLowerCase()))
            {
                return true;
            }
        }
        return false;
    };
}
