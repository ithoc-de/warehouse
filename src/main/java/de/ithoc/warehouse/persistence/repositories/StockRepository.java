package de.ithoc.warehouse.persistence.repositories;

import de.ithoc.warehouse.persistence.entities.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

}
