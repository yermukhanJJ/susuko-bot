package kz.bot.service.impl;

import kz.bot.dao.AppDocumentDao;
import kz.bot.dao.BinaryContentDao;
import kz.bot.entity.AppDocument;
import kz.bot.entity.BinaryContent;
import kz.bot.dao.AppPhotoDao;
import kz.bot.entity.AppPhoto;
import kz.bot.entity.enums.LinkType;
import kz.bot.exception.UploadFileException;
import kz.bot.service.FileService;
import kz.bot.util.CryptoTool;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    @Value("${bot.token}")
    private String token; //"5784964304:AAFcK0owyXLuyecfs017CzfhSf1oCOjfRzI";
    @Value("${bot.fileInfoUri}")
    private String fileInfoUri; // "https://api.telegram.org/bot{token}/getFile?file_id={fileId}";
    @Value("${bot.fileStorageUri}")
    private String fileStorageUri; //"https://api.telegram.org/file/bot{token}/{filePath}";
    @Value("${link.address}")
    private String linkAddress; //"127.0.0.1:8086";

    private final AppDocumentDao appDocumentDao;
    private final AppPhotoDao appPhotoDao;
    private final BinaryContentDao binaryContentDao;
    private final CryptoTool cryptoTool;

    @Override
    public AppDocument processDoc(Message telegramMessage) {
        String fileId = telegramMessage.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);
            Document telegramDoc = telegramMessage.getDocument();
            AppDocument transientAppDoc = buildTransientAppDoc(telegramDoc, persistentBinaryContent);
            return appDocumentDao.save(transientAppDoc);
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }

    @Override
    public AppPhoto processPhoto(Message telegramMessage) {
        var photoSizeCount = telegramMessage.getPhoto().size();
        var photoIndex = photoSizeCount > 1 ? telegramMessage.getPhoto().size() - 1 : 0;
        PhotoSize telegramPhoto = telegramMessage.getPhoto().get(photoIndex);
        String fileId = telegramPhoto.getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);
            AppPhoto transientAppPhoto = buildTransientAppPhoto(telegramPhoto, persistentBinaryContent);
            return appPhotoDao.save(transientAppPhoto);
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }

    @Override
    public String generateLink(Long docId, LinkType linkType) {
        var hash = cryptoTool.hashOf(docId);
        return "http://" + linkAddress + "/" + linkType + "?id=" + hash;
    }

    private AppPhoto buildTransientAppPhoto(PhotoSize telegramPhoto, BinaryContent persistentBinaryContent) {
        return AppPhoto.builder()
                .telegramFileId(telegramPhoto.getFileId())
                .fileSize(telegramPhoto.getFileSize())
                .binaryContent(persistentBinaryContent)
                .build();
    }

    private BinaryContent getPersistentBinaryContent(ResponseEntity<String> response) {
        JSONObject jsonObject = new JSONObject(response.getBody());
        String filePath = String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
        byte[] fileInByte = downloadFile(filePath);
        BinaryContent transientBinaryContent = BinaryContent.builder()
                .fileAsArrayOfBytes(fileInByte)
                .build();
        return binaryContentDao.save(transientBinaryContent);
    }

    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri.replace("{token}", token)
                .replace("{filePath}", filePath);
        URL urlObj = null;
        try {
            urlObj = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw  new UploadFileException(e);
        }

        //TODO подумать над оптимизацией
        try(InputStream is = urlObj.openStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new UploadFileException(e);
        }
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

//        JSONObject jsonObject = new JSONObject(response.getBody());
//        return String.valueOf(jsonObject
//                .getJSONObject("result")
//                .getString("file_path"));
        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token,
                fileId
        );
    }

    private AppDocument buildTransientAppDoc(Document telegramDoc, BinaryContent persistentBinaryContent) {
        return AppDocument.builder()
                .telegramFileId(telegramDoc.getFileId())
                .docName(telegramDoc.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(telegramDoc.getMimeType())
                .fileSize(telegramDoc.getFileSize())
                .build();
    }
}
