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
import java.io.IOException;
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

    public void convertToPdf(String chatId) throws TelegramApiException, IOException {
        File directory = new File("userFiles/" + chatId + "/out");

        if (!(directory.exists() && directory.isDirectory())) {
            PdfConverterBot.getInstance().execute(SendMessage.builder()
                            .chatId(chatId)
                            .text("Can't make PDF.\nFiles may be corrupted, please check them and send again.")
                    .build());
            clearDirectory(new File("userFiles/" + chatId));
            throw new IOException("Directory not found: " + directory.getName());
        }

        if (!numberOfFilesDownloaded.containsKey(chatId) || numberOfFilesDownloaded.get(chatId) == 0) {
            PdfConverterBot.getInstance().execute(SendMessage.builder()
                            .chatId(chatId)
                            .text("Send some files first.")
                    .build());
            return;
        }

        File[] listFiles = directory.listFiles();
        if (listFiles == null) {
            throw new IOException("No files in directory: " + directory.getName());
        }

        PdfConverterBot.getInstance().execute(SendMessage.builder()
                .chatId(chatId)
                .text("Processing\nPlease wait...")
                .build());

        while (listFiles.length != numberOfFilesDownloaded.get(chatId)) {
            System.out.println(numberOfFilesDownloaded);
            System.out.println(listFiles.length);
        }
        try {
            pdfHandler.convert(directory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PdfConverterBot.getInstance().execute(SendDocument.builder()
                        .chatId(chatId)
                        .document(new InputFile(new File("userFiles/" + chatId + "/out/output.pdf")))
                .build());

        clearDirectory(directory.getParentFile());
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
                System.out.println("HERE");
                e.printStackTrace();
            }
        }).start();
    }

    public void downloadPhoto(String chatId, List<PhotoSize> photos) {
        PhotoSize photoSize = photos.get(photos.size() - 1);
        new Thread(() -> {
            try {
                File outputFile = new File("userFiles/" + chatId + "/" + System.nanoTime() + "_" + photoSize.getFileId() + ".jpg");
                downloadHandler.downloadPhoto(outputFile, photoSize);
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
