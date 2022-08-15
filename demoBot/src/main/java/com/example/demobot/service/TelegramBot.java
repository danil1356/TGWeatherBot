package com.example.demobot.service;

import com.example.demobot.config.BotConfig;
import com.example.demobot.config.OpenWeatherMapJsonParser;
import com.example.demobot.config.WeatherParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private WeatherParser weatherParser = new OpenWeatherMapJsonParser();

    static final String HELP_TEXT = "Help text\n\n"+
            "next line\n"+
            "/start\n"+
            "/mydata\n"+
            "/deletemydata\n"+
            "/help\n"+
            "/settings";

    static final String START_TEXT = "Здравствуйте (Привет)! Я бот который поможет вам определить погоду.\n"+
            "Введите название вашего города, например: Moscow или St. Petersburg.";
    final BotConfig config;

    public TelegramBot(BotConfig config)
    {
        this.config = config;

        //меню бота
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "начать"));
        listOfCommands.add(new BotCommand("/mydata", "ваша информация"));
        listOfCommands.add(new BotCommand("/deletemydata", "удаление вашей информации"));
        listOfCommands.add(new BotCommand("/help", "информация по импользованию бота"));
        listOfCommands.add(new BotCommand("/settings", "настройки"));
        try
        {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e)
        {
            log.error("ошибка command list: "+e);
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText())
        {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();


            switch (messageText)
            {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;

                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;

                case "/mishGun":
                    sendMessageForMishgan(chatId);
                    break;

                default:
                    sendMessage(chatId,weatherParser.getReadyForecast(messageText));
                    //sendMessage(chatId, "Not recognized");
                    break;

            }
        }

    }


    private void startCommandReceived(long chatId, String name){
        String answer = START_TEXT;
        log.info("Доставлено пользователю: " +name);

        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        }
        catch (TelegramApiException e)
        {
            log.error("Ошибка типа: " +e.getMessage());
        }
    }

    private void sendMessageForMishgan(long chatId){
        //SendDocument video = new SendDocument();
        SendVideo video = new SendVideo();
        video.setChatId(String.valueOf(chatId));
        video.setVideo(new InputFile(new File("kot.mp4")));

        try {
            execute(video);
        }
        catch (TelegramApiException e)
        {
            log.error("Ошибка типа: " +e.getMessage());
        }
    }
}
