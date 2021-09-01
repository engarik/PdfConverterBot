package com.github.engarik.pdfconverterbot.handlers.impl;

import com.github.engarik.pdfconverterbot.handlers.DownloadHandler;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.io.File;

public class TelegramDownloadHandler implements DownloadHandler {

    @Override
    public void downloadDocument(File outputFile, Document document) throws InterruptedException {
        Thread documentDownloadThread = new DownloadThread(outputFile, document);
        documentDownloadThread.start();
        documentDownloadThread.join();
    }

    @Override
    public void downloadPhoto(File outputFile, PhotoSize photoSize) throws InterruptedException {
        Thread fileDownloadThread = new DownloadThread(outputFile, photoSize);
        fileDownloadThread.start();
        fileDownloadThread.join();
    }

}
