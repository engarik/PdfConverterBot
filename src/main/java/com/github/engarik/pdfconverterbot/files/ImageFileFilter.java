package com.github.engarik.pdfconverterbot.files;

import java.io.File;
import java.io.FilenameFilter;

public class ImageFileFilter implements FilenameFilter {

    private boolean checkExtension(String filename) {
        return filename.toLowerCase().endsWith(".jpg") ||
                filename.toLowerCase().endsWith(".jpeg") ||
                filename.toLowerCase().endsWith(".png") ||
                filename.toLowerCase().endsWith(".gif");
    }

    @Override
    public boolean accept(File dir, String name) {
        if (!dir.exists()) {
            return false;
        } else {
            return new File(dir + "/" + name).isFile() && checkExtension(name);
        }
    }
}
