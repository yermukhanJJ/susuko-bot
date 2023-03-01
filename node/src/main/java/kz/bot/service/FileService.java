package kz.bot.service;

import kz.bot.entity.AppDocument;
import kz.bot.entity.AppPhoto;
import kz.bot.entity.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);
    AppPhoto processPhoto(Message telegramMessage);
    String generateLink(Long docId, LinkType linkType);
}
