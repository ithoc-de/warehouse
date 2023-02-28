package de.ithoc.warehouse.persistence.repositories;

import de.ithoc.warehouse.persistence.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByName(String name);
    Optional<Product> findByExternalId(String externalId);

}
