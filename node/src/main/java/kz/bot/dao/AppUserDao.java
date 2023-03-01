package kz.bot.dao;

import kz.bot.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserDao extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findAppUserByTelegramUserId(Long id);
    Optional<AppUser> findById(String id);
    Optional<AppUser> findByEmail(String email);
}
