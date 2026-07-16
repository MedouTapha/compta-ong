package com.ong.compta.repository;

import com.ong.compta.domain.Aide;
import com.ong.compta.domain.Enfant;
import com.ong.compta.domain.enums.StatutAide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AideRepository extends JpaRepository<Aide, Long> {
    List<Aide> findByEnfant(Enfant enfant);

    List<Aide> findByStatut(StatutAide statut);
}
