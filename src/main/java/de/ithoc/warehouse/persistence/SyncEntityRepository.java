package de.ithoc.warehouse.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SyncEntityRepository extends JpaRepository<SyncEntity, Long> {

    Optional<SyncEntity> findByName(String name);
}
