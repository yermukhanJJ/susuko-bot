package kz.bot.node.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface ProduceService {
    void producerAnswer(SendMessage message);
}
