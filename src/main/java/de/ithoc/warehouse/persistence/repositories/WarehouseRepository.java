package de.ithoc.warehouse.persistence.repositories;

import de.ithoc.warehouse.persistence.entities.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

}
