package com.ong.compta.repository;

import com.ong.compta.domain.Destination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DestinationRepository extends JpaRepository<Destination, Long> {
    Optional<Destination> findByCode(String code);
}
