package com.github.engarik.pdfconverterbot.utils;

public class AllowedMimeTypes {
    public static boolean isAllowed(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg", "image/gif", "image/png" -> true;
            default -> false;
        };
    }
}
