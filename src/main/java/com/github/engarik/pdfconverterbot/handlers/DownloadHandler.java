package com.github.engarik.pdfconverterbot.handlers;

import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.io.File;

public interface DownloadHandler {

    void downloadDocument(File outputFile, Document document) throws InterruptedException;

    void downloadPhoto(File outputFile, PhotoSize photoSize) throws InterruptedException;
}
