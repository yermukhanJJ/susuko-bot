package kz.bot.dao;

import kz.bot.entity.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BinaryContentDao extends JpaRepository<BinaryContent, Long> {
}
