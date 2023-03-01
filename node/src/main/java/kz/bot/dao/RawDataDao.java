package kz.bot.dao;

import kz.bot.entity.RawData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawDataDao extends JpaRepository<RawData, Long> {

}
