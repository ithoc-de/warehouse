package de.ithoc.warehouse.persistence.repositories;

import de.ithoc.warehouse.persistence.entities.SyncHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SyncHistoryRepository extends JpaRepository<SyncHistory, Long> {

    Optional<SyncHistory> findTopByOrderByTimestampDesc();

}
