package com.ong.compta.service;

import com.ong.compta.domain.Compte;
import com.ong.compta.domain.Ecriture;
import com.ong.compta.domain.Financement;
import com.ong.compta.domain.LigneEcriture;
import com.ong.compta.domain.enums.StatutEcriture;
import com.ong.compta.dto.LigneBalance;
import com.ong.compta.repository.EcritureRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EtatService {

    private final EcritureRepository ecritureRepository;

    public EtatService(EcritureRepository ecritureRepository) {
        this.ecritureRepository = ecritureRepository;
    }

    public List<Ecriture> journal(LocalDate debut, LocalDate fin) {
        return ecritureRepository.findByDateOperationBetweenOrderByDateOperationAscNumeroAsc(debut, fin);
    }

    public List<LigneBalance> balance(LocalDate debut, LocalDate fin) {
        List<Ecriture> ecritures = ecritureRepository.findByDateOperationBetweenOrderByDateOperationAscNumeroAsc(debut, fin);

        Map<Compte, BigDecimal[]> totaux = new LinkedHashMap<>();
        for (Ecriture e : ecritures) {
            if (e.getStatut() != StatutEcriture.VALIDEE) {
                continue;
            }
            for (LigneEcriture l : e.getLignes()) {
                totaux.computeIfAbsent(l.getCompte(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                BigDecimal[] dc = totaux.get(l.getCompte());
                dc[0] = dc[0].add(l.getDebit());
                dc[1] = dc[1].add(l.getCredit());
            }
        }

        return totaux.entrySet().stream()
                .map(entry -> new LigneBalance(entry.getKey(), entry.getValue()[0], entry.getValue()[1],
                        entry.getValue()[0].subtract(entry.getValue()[1])))
                .sorted(Comparator.comparing(lb -> lb.compte().getNumero()))
                .toList();
    }

    public List<Ecriture> rapportProjet(Financement financement) {
        return ecritureRepository.findByFinancementAndStatutOrderByDateOperationAscNumeroAsc(
                financement, StatutEcriture.VALIDEE);
    }
}
