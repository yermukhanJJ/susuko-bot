package kz.bot.service;

import kz.bot.entity.AppDocument;
import kz.bot.entity.AppPhoto;
import kz.bot.entity.BinaryContent;
import org.springframework.core.io.FileSystemResource;

public interface FileService {
    AppDocument getDocument(String id);
    AppPhoto getPhoto(String id);
}
