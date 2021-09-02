package com.github.engarik.pdfconverterbot.utils;

import com.github.engarik.pdfconverterbot.PdfConverterBot;
import com.github.engarik.pdfconverterbot.download.DownloadHandler;
import com.github.engarik.pdfconverterbot.download.impl.TelegramDownloadHandler;
import com.github.engarik.pdfconverterbot.image.ImageHandler;
import com.github.engarik.pdfconverterbot.image.impl.AffineImageHandler;
import com.github.engarik.pdfconverterbot.pdf.PdfHandler;
import com.github.engarik.pdfconverterbot.pdf.impl.ITextPdfHandler;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StateController {

    private DownloadHandler downloadHandler;
    private ImageHandler imageHandler;
    private PdfHandler pdfHandler;
    private Map<String, Integer> numberOfFilesDownloaded;

    public StateController() {
        downloadHandler = new TelegramDownloadHandler();
        imageHandler = new AffineImageHandler();
        pdfHandler = new ITextPdfHandler();
        numberOfFilesDownloaded = new ConcurrentHashMap<>();
        clearDirectory(new File("userFiles/"));
    }

    public void convertToPdf(String chatId) throws TelegramApiException {
        File directory = new File("userFiles/" + chatId);

        if (!numberOfFilesDownloaded.containsKey(chatId) || numberOfFilesDownloaded.get(chatId) == 0) {
            PdfConverterBot.getInstance().execute(SendMessage.builder()
                            .chatId(chatId)
                            .text("Send some files first.")
                    .build());
            return;
        }

        PdfConverterBot.getInstance().execute(SendMessage.builder()
                .chatId(chatId)
                .text("Processing\nPlease wait...")
                .build());

        while (directory.listFiles().length != numberOfFilesDownloaded.get(chatId)) {
            System.out.println(numberOfFilesDownloaded);
            System.out.println(directory.listFiles().length);
        }
        try {
            pdfHandler.convert("userFiles/" + chatId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PdfConverterBot.getInstance().execute(SendDocument.builder()
                        .chatId(chatId)
                        .document(new InputFile(new File("userFiles/" + chatId + "/output.pdf")))
                .build());

        clearDirectory(directory);
        numberOfFilesDownloaded.remove(chatId);
    }

    public void downloadDocument(String chatId, Document document) {
        new Thread(() -> {
            try {
                File outputFile = new File("userFiles/" + chatId + "/" + System.nanoTime() + "_" + document.getFileName());
                downloadHandler.downloadDocument(outputFile, document);
                imageHandler.handle(outputFile);
                if (!numberOfFilesDownloaded.containsKey(chatId)) {
                    numberOfFilesDownloaded.put(chatId, 0);
                }
                numberOfFilesDownloaded.put(chatId, numberOfFilesDownloaded.get(chatId) + 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void downloadPhoto(String chatId, List<PhotoSize> photos) {

    }

    private void clearDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                clearDirectory(file);
            }
        }
        directory.delete();
    }

}
