package kz.bot.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface ProduceService {
    void producerAnswer(SendMessage message);
}
