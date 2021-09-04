package com.github.engarik.pdfconverterbot.utils;

public class AllowedMimeTypes {
    public static boolean isAllowed(String mimeType) {
        switch (mimeType) {
            case "image/jpeg":
            case "image/gif":
            case "image/png":
                return true;
            default:
                return false;
        }
    }
}
