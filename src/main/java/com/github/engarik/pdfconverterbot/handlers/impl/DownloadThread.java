package com.github.engarik.pdfconverterbot.handlers.impl;

import com.github.engarik.pdfconverterbot.PdfConverterBot;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class DownloadThread extends Thread  {
    private final Document document;
    private final PhotoSize photoSize;
    private final java.io.File directory;

    public DownloadThread(java.io.File directory, Document document) {
        this.document = document;
        this.photoSize = null;
        this.directory = directory;
    }

    public DownloadThread(java.io.File directory, PhotoSize photoSize) {
        this.document = null;
        this.photoSize = photoSize;
        this.directory = directory;
    }

    @Override
    public void run() {
        System.out.println("Created new Thread for file downloading");
        GetFile getFile = new GetFile();
        if (photoSize == null) {
            getFile.setFileId(document.getFileId());
        } else {
            getFile.setFileId(photoSize.getFileId());
        }
        try {
            File file = PdfConverterBot.getInstance().execute(getFile);
            PdfConverterBot.getInstance().downloadFile(file, directory);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
