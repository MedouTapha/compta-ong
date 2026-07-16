package com.ong.compta.service;

import com.ong.compta.repository.PeriodeClotureeRepository;
import com.ong.compta.service.exception.PeriodeClotureeException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * RG-6 : une ecriture ne peut pas etre saisie/validee sur une periode (annee, mois) deja cloturee.
 */
@Service
public class PeriodeClotureeService {

    private final PeriodeClotureeRepository periodeClotureeRepository;

    public PeriodeClotureeService(PeriodeClotureeRepository periodeClotureeRepository) {
        this.periodeClotureeRepository = periodeClotureeRepository;
    }

    public void verifierPeriodeOuverte(LocalDate dateOperation) {
        if (periodeClotureeRepository.existsByAnneeAndMois(dateOperation.getYear(), dateOperation.getMonthValue())) {
            throw new PeriodeClotureeException(
                    "La periode " + dateOperation.getMonthValue() + "/" + dateOperation.getYear() + " est cloturee.");
        }
    }
}
