package kz.bot.service.impl;

import kz.bot.dao.AppDocumentDao;
import kz.bot.dao.AppPhotoDao;
import kz.bot.entity.AppDocument;
import kz.bot.entity.AppPhoto;
import kz.bot.entity.BinaryContent;
import kz.bot.service.FileService;
import kz.bot.util.CryptoTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final AppPhotoDao appPhotoDao;
    private final AppDocumentDao appDocumentDao;
    private final CryptoTool cryptoTool;

    @Override
    public AppDocument getDocument(String docId) {
        var id = cryptoTool.idOf(docId);
        return appDocumentDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document with id: " + id + " not found"));
    }

    @Override
    public AppPhoto getPhoto(String photoId) {
        var id = cryptoTool.idOf(photoId);
        return appPhotoDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Photo with id: " + id + " not found"));
    }

}
