package kz.bot.node.service.impl;

import kz.bot.node.service.ProduceService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static kz.bot.model.RabbitQueue.ANSWER_MESSAGE;

@Service
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProduceService {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void producerAnswer(SendMessage message) {
        rabbitTemplate.convertAndSend(ANSWER_MESSAGE,message);
    }
}
