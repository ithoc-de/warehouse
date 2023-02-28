package de.ithoc.warehouse.persistence.repositories;

import de.ithoc.warehouse.persistence.entities.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {

    Optional<Package> findByName(String name);

}
