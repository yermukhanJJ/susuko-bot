package kz.bot.node.service.impl;

import kz.bot.node.dao.RawDataDao;
import kz.bot.node.entity.RawData;
import kz.bot.node.service.MainService;
import kz.bot.node.service.ProduceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {

    private final RawDataDao rawDataDao;
    private final ProduceService produceService;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Hello from NODE!");
        produceService.producerAnswer(sendMessage);
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                            .update(update)
                            .build();
        rawDataDao.save(rawData);
    }
}
