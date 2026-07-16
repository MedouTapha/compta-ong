package com.ong.compta.repository;

import com.ong.compta.domain.Enfant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnfantRepository extends JpaRepository<Enfant, Long> {
}
