package com.ong.compta.repository;

import com.ong.compta.domain.Compte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompteRepository extends JpaRepository<Compte, Long> {
    Optional<Compte> findByNumero(String numero);
}
