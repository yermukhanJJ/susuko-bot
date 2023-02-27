package kz.bot.dispatcher.service.impl;

import kz.bot.dispatcher.controller.UpdateController;
import kz.bot.dispatcher.service.AnswerConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static kz.bot.model.RabbitQueue.ANSWER_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerConsumerImpl implements AnswerConsumer {

    private final UpdateController updateController;

    @Override
    @RabbitListener(queues = ANSWER_MESSAGE)
    public void consume(SendMessage sendMessage) {
        updateController.setView(sendMessage);
    }
}
