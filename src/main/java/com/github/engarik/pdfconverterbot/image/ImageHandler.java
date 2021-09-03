package com.github.engarik.pdfconverterbot.image;

import java.io.File;

public interface ImageHandler {
    void handle(File imageFile) throws Exception;
}
