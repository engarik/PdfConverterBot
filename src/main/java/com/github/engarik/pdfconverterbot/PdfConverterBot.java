package com.github.engarik.pdfconverterbot;

import com.github.engarik.pdfconverterbot.utils.AllowedMimeTypes;
import com.github.engarik.pdfconverterbot.utils.StateController;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class PdfConverterBot extends TelegramLongPollingBot {

    private static PdfConverterBot instance;
    private final StateController stateController;

    private final String botToken;
    private final String botName;

    public static PdfConverterBot getInstance() {
        if (instance == null) {
            instance = new PdfConverterBot();
        }
        return instance;
    }

    private PdfConverterBot() {
        stateController = new StateController();
        Properties properties = new Properties();
        try {
            properties.load(new FileReader("bot.properties"));
        } catch (IOException e) {
            System.err.println("Error while trying to read bot.properties file.");
            System.exit(-1);
        }
        this.botName = properties.getProperty("bot.name");
        this.botToken = properties.getProperty("bot.token");
        if (botName == null || botToken == null) {
            System.err.println("Error while trying to read bot.properties file.");
            System.exit(-1);
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        }
    }

    private void handleMessage(Message message) throws TelegramApiException {
        if (message.hasText() && message.hasEntities()) {
            Optional<MessageEntity> commandEntity =
                    message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
            if (commandEntity.isPresent()) {
                String command = message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());
                switch (command) {
                    case "/start" -> execute(SendMessage.builder()
                            .chatId(message.getChatId().toString())
                            .text("Send some JPG, PNG or GIF files. Type /help for more information.")
                            .build());
                    case "/help" -> execute(SendMessage.builder()
                            .chatId(message.getChatId().toString())
                            .text("1. Send your JPG, PNG or GIF files via photos or documents.\n" +
                                    "2. Send /e command to get the PDF file.")
                            .build());
                    case "/e" -> {
                        stateController.convertToPdf(message.getChatId().toString());
                        System.out.println("Close PDF file.");
                    }
                }
            }
        } else if (message.hasDocument()) {
            if (AllowedMimeTypes.isAllowed(message.getDocument().getMimeType())) {
                System.out.println("Got document");
                stateController.downloadDocument(message.getChatId().toString(), message.getDocument());
            } else {
                execute(SendMessage.builder()
                        .chatId(message.getChatId().toString())
                        .text("Invalid document format.\nAllowed extensions:\n\".jpg\"\n\".png\"\n\".gif\"")
                        .build());
            }
        } else if (message.hasPhoto()) {
            System.out.println("Got photo");
            stateController.downloadPhoto(message.getChatId().toString(), message.getPhoto());
        }
    }
}
