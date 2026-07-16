package com.ong.compta.repository;

import com.ong.compta.domain.Financement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinancementRepository extends JpaRepository<Financement, Long> {
    Optional<Financement> findByCode(String code);
}
