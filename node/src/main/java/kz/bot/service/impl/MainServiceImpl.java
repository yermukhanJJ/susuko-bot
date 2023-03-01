package kz.bot.service.impl;

import kz.bot.entity.AppDocument;
import kz.bot.entity.enums.LinkType;
import kz.bot.entity.enums.ServiceCommands;
import kz.bot.dao.AppUserDao;
import kz.bot.entity.AppPhoto;
import kz.bot.entity.AppUser;
import kz.bot.dao.RawDataDao;
import kz.bot.entity.RawData;
import kz.bot.exception.UploadFileException;
import kz.bot.service.AppUserService;
import kz.bot.service.FileService;
import kz.bot.service.MainService;
import kz.bot.service.ProduceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static kz.bot.entity.enums.UserState.BASIC_STATE;
import static kz.bot.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;

@Service
@RequiredArgsConstructor
@Slf4j
public class MainServiceImpl implements MainService {

    private final RawDataDao rawDataDao;
    private final ProduceService produceService;
    private final AppUserDao appUserDao;
    private final FileService fileService;
    private final AppUserService appUserService;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);

        var message = update.getMessage();
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getUserState();
        var text = update.getMessage().getText();
        var output = "";

        if (ServiceCommands.CANCEL.equals(text)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            output = appUserService.setEmail(appUser, text);
        } else {
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка! Введите /canсel и попробуйте снова!";
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);

    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);

        var appUser = findOrSaveAppUser(update);
        var charId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(charId, appUser)) {
            return;
        }

        try {
            AppDocument doc = fileService.processDoc(update.getMessage());
            var link = fileService.generateLink(doc.getId(), LinkType.GET_DOC);
            var answer = "Документ успешно загружен! Ссылка для скачивания: " + link;
            sendAnswer(answer, charId);
        } catch (UploadFileException e) {
            log.error(e.getMessage());
            String error = "К сожалению, загрузка файла не удалась. Повторите попытку позже.";
            sendAnswer(error, charId);
        }
    }

    private boolean isNotAllowToSendContent(Long charId, AppUser appUser) {
        var userState = appUser.getUserState();
        if (!appUser.getIsActive()) {
            var error = "Зарегистрируйтесь или активируйте "
                    + "свою учетную запись для загрузки контента.";
            sendAnswer(error, charId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "Отмените текущую команду с помощю /cancel для отправки файлов.";
            sendAnswer(error, charId);
            return true;
        }
        return false;
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);

        var appUser = findOrSaveAppUser(update);
        var charId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(charId, appUser)) {
            return;
        }

        try {
            AppPhoto photo = fileService.processPhoto(update.getMessage());
            var link = fileService.generateLink(photo.getId(), LinkType.GET_PHOTO);
            var answer = "Фото успешно загружен! Ссылка для скачивания: " + link;
            sendAnswer(answer, charId);
        } catch (UploadFileException e) {
            log.error(e.getMessage());
            String error = "К сожалению, загрузка фото не удалась. Повторите попытку позже.";
            sendAnswer(error, charId);
        }
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        produceService.producerAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String text) {
        if (ServiceCommands.REGISTRATION.equals(text)) {
            return appUserService.registerUser(appUser);
        } else if (ServiceCommands.HELP.equals(text)) {
            return help();
        } else if (ServiceCommands.START.equals(text)) {
            return "Приветствую! Чтобы посмотреть список доступных команд введите /help";
        } else {
            return "Неизвестная команда! Чтобы посмотреть список доступных команд введите /help";
        }
    }

    private String help() {
        return "Список доступных команд: \n"
                + "/cancel - отмена выполнения текущей команды;\n"
                + "/registration - регистрация пользователя";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setUserState(BASIC_STATE);
        appUserDao.save(appUser);
        return "Команда отменена";
    }

    private AppUser findOrSaveAppUser(Update update) {
        var telegramUser = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserDao.findAppUserByTelegramUserId(telegramUser.getId())
                .orElseThrow(() -> new UploadFileException("User not found"));
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .isActive(false)
                    .userState(BASIC_STATE)
                    .build();
            return appUserDao.save(transientAppUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                            .update(update)
                            .build();
        rawDataDao.save(rawData);
    }
}
