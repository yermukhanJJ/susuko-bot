package kz.bot.service.impl;

import kz.bot.dao.AppUserDao;
import kz.bot.service.UserActivationService;
import kz.bot.util.CryptoTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserActivationServiceImpl implements UserActivationService {

    private final AppUserDao appUserDao;
    private final CryptoTool cryptoTool;

    @Override
    public boolean activation(String cryptoUserId) {
        var userId = cryptoTool.idOf(cryptoUserId);
        var optional = appUserDao.findById(userId);
        if (optional.isPresent()) {
            var user = optional.get();
            user.setIsActive(true);
            appUserDao.save(user);
            return true;
        }
        return false;
    }
}
