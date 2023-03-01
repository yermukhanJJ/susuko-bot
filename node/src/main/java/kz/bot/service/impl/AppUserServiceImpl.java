package kz.bot.service.impl;

import kz.bot.dao.AppUserDao;
import kz.bot.dto.MailParams;
import kz.bot.entity.AppUser;
import kz.bot.entity.enums.UserState;
import kz.bot.service.AppUserService;
import kz.bot.util.CryptoTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserServiceImpl implements AppUserService {
    private final AppUserDao appUserDao;
    private final CryptoTool cryptoTool;
    @Value("${service.mail.uri}")
    private String mailServiceUri;


    @Override
    public String registerUser(AppUser appUser) {
        if (appUser.getIsActive()) {
            return "Вы уже зарегистрированы!";
        } else if (appUser.getEmail() != null) {
            return "Вам на почту уже было отправлено письмо. "
                    + "Перейдите по ссылке в письме для подтверждения регистрации.";
        }
        appUser.setUserState(UserState.WAIT_FOR_EMAIL_STATE);
        appUserDao.save(appUser);
        return "Введите, пожалуйста, ваш email:";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException e) {
            e.printStackTrace();
            return "Введите, пожалуйста, корректный email. Для отмены команды введите /cancel";
        }
        var optional = appUserDao.findByEmail(email);
        if (optional.isEmpty()) {
            appUser.setEmail(email);
            appUser.setUserState(UserState.BASIC_STATE);
            appUser = appUserDao.save(appUser);

            var cryptoUserId = cryptoTool.hashOf(appUser.getId());
            var response = sendRequestToMailService(cryptoUserId,email);
            if (response.getStatusCode() != HttpStatus.OK) {
                var msg = String.format("Отправка эл. письма на почту %s не удалась.", email);
                log.error(msg);
                appUser.setEmail(null);
                appUserDao.save(appUser);
                return msg;
            }
            return "Вам на почту было отправлено письмо."
                    + "Перейдите по ссылке в письме для подтверждения регистрации.";
        } else {
            return "Этот email уже используется. Введите корректный email."
                    + " Для отмены команды введите /cancel";
        }
    }

    private ResponseEntity<String> sendRequestToMailService(String cryptoUserId, String email) {
        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var mailParams = MailParams.builder()
                .id(cryptoUserId)
                .emailTo(email)
                .build();
        var request = new HttpEntity<>(mailParams, headers);
        return restTemplate.exchange(mailServiceUri,
                HttpMethod.POST,
                request,
                String.class);
    }
}
