package com.ong.compta.web;

import com.ong.compta.domain.enums.StatutEcriture;
import com.ong.compta.repository.EcritureRepository;
import com.ong.compta.service.EcritureService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class EcritureController {

    private final EcritureService ecritureService;
    private final EcritureRepository ecritureRepository;

    public EcritureController(EcritureService ecritureService, EcritureRepository ecritureRepository) {
        this.ecritureService = ecritureService;
        this.ecritureRepository = ecritureRepository;
    }

    @GetMapping("/ecritures/brouillons")
    public String brouillons(Model model) {
        model.addAttribute("ecritures", ecritureRepository.findByStatutOrderByDateOperationAscNumeroAsc(StatutEcriture.BROUILLON));
        return "ecritures/brouillons";
    }

    @PostMapping("/ecritures/{id}/valider")
    public String valider(@PathVariable Long id, Authentication authentication) {
        ecritureService.valider(id, authentication.getName());
        return "redirect:/ecritures/brouillons";
    }

    @PostMapping("/ecritures/{id}/contre-passer")
    public String contrePasser(@PathVariable Long id, Authentication authentication) {
        ecritureService.contrePasser(id, authentication.getName(), LocalDate.now());
        return "redirect:/etats/journal";
    }

    @PostMapping("/decaissements/{id}/debloquer-budget")
    public String debloquerBudget(@PathVariable Long id, Authentication authentication) {
        ecritureService.debloquerBudgetEtValider(id, authentication.getName());
        return "redirect:/ecritures/brouillons";
    }
}
