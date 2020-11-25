package com.loottable.helpers;

import lombok.extern.slf4j.Slf4j;

/**
 * Prevents plugin from logging unless in DEV_MODE = true
 */
@Slf4j
public class Log {
    private static boolean DEV_MODE = false; // @todo Ensure this is set to false in repo

    public static void info(String message) {
        if (DEV_MODE) {
            System.out.println(message);
        }
    }
}