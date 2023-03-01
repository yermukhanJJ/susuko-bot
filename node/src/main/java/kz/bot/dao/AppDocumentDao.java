package kz.bot.dao;

import kz.bot.entity.AppDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppDocumentDao extends JpaRepository<AppDocument, Long> {
}
