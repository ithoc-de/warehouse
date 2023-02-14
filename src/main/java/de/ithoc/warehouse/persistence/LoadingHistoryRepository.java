package de.ithoc.warehouse.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoadingHistoryRepository extends JpaRepository<LoadingHistory, Long> {

    LoadingHistory findTopByOrderByTimestampDesc();

}
