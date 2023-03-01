package kz.bot.service;

import kz.bot.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
