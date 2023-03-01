package kz.bot.dao;

import kz.bot.entity.AppPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppPhotoDao extends JpaRepository<AppPhoto, Long> {
}
