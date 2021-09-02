package com.github.engarik.pdfconverterbot.pdf;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

public interface PdfHandler {

    // This method gathers files from "rootDirectory" to output.pdf
    void convert(String rootDirectory) throws Exception;

    // This method gathers files from "rootDirectory" to "outputFilename".pdf
    void convert(String rootDirectory, String outputFilename) throws Exception;
}
