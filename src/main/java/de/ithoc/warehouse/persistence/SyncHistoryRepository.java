package de.ithoc.warehouse.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SyncHistoryRepository extends JpaRepository<SyncHistory, Long> {

    Optional<SyncHistory> findTopByOrderByTimestampDesc();

}
