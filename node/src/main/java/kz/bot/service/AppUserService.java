package kz.bot.service;

import kz.bot.entity.AppUser;

public interface AppUserService {
    String registerUser(AppUser appUser);
    String setEmail(AppUser appUser, String email);
}
