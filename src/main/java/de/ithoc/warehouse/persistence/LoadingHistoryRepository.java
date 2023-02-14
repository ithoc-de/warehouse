package de.ithoc.warehouse.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoadingHistoryRepository extends JpaRepository<LoadingHistory, Long> {

    Optional<LoadingHistory> findTopByOrderByTimestampDesc();

}
