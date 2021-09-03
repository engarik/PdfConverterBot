package com.github.engarik.pdfconverterbot.pdf;

import java.io.File;

public interface PdfHandler {
    void convert(File directory) throws Exception;

    void convert(File directory, String outputFilename) throws Exception;
}
