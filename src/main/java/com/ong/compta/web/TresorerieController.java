package com.ong.compta.web;

import com.ong.compta.domain.enums.PerimetreType;
import com.ong.compta.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Controller
@RequestMapping("/tresorerie")
public class TresorerieController {

    private final TresorerieService tresorerieService;
    private final CompteService compteService;
    private final FinancementService financementService;
    private final DestinationService destinationService;
    private final LigneBudgetService ligneBudgetService;

    public TresorerieController(TresorerieService tresorerieService, CompteService compteService,
                                 FinancementService financementService, DestinationService destinationService,
                                 LigneBudgetService ligneBudgetService) {
        this.tresorerieService = tresorerieService;
        this.compteService = compteService;
        this.financementService = financementService;
        this.destinationService = destinationService;
        this.ligneBudgetService = ligneBudgetService;
    }

    private void ajouterReferentiels(Model model) {
        model.addAttribute("comptes", compteService.lister());
        model.addAttribute("financements", financementService.lister());
        model.addAttribute("destinations", destinationService.lister());
        model.addAttribute("lignesBudget", ligneBudgetService.lister());
    }

    @GetMapping("/encaissement")
    public String formEncaissement(Model model) {
        ajouterReferentiels(model);
        return "tresorerie/encaissement";
    }

    @PostMapping("/encaissement")
    public String encaisser(@RequestParam PerimetreType perimetre, @RequestParam(required = false) Long financementId,
                             @RequestParam @DateTimeFormat(iso = DATE) LocalDate dateOperation,
                             @RequestParam Long compteTresorerieId, @RequestParam Long compteProduitId,
                             @RequestParam(required = false) Long destinationId, @RequestParam BigDecimal montant,
                             @RequestParam String libelle, @RequestParam String referencePiece,
                             Authentication authentication) {
        tresorerieService.encaisser(perimetre,
                financementId == null ? null : financementService.trouver(financementId),
                dateOperation,
                compteService.trouver(compteTresorerieId),
                compteService.trouver(compteProduitId),
                destinationId == null ? null : destinationService.trouver(destinationId),
                montant, libelle, referencePiece, authentication.getName());
        return "redirect:/ecritures/brouillons";
    }

    @GetMapping("/decaissement")
    public String formDecaissement(Model model) {
        ajouterReferentiels(model);
        return "tresorerie/decaissement";
    }

    @PostMapping("/decaissement")
    public String decaisser(@RequestParam PerimetreType perimetre, @RequestParam(required = false) Long financementId,
                             @RequestParam @DateTimeFormat(iso = DATE) LocalDate dateOperation,
                             @RequestParam Long compteChargeId, @RequestParam Long compteTresorerieId,
                             @RequestParam(required = false) Long destinationId,
                             @RequestParam(required = false) Long ligneBudgetId, @RequestParam BigDecimal montant,
                             @RequestParam String libelle, @RequestParam String referencePiece,
                             Authentication authentication) {
        tresorerieService.decaisser(perimetre,
                financementId == null ? null : financementService.trouver(financementId),
                dateOperation,
                compteService.trouver(compteChargeId),
                compteService.trouver(compteTresorerieId),
                destinationId == null ? null : destinationService.trouver(destinationId),
                ligneBudgetId == null ? null : ligneBudgetService.trouver(ligneBudgetId),
                montant, libelle, referencePiece, authentication.getName());
        return "redirect:/ecritures/brouillons";
    }

    @GetMapping("/transfert")
    public String formTransfert(Model model) {
        ajouterReferentiels(model);
        return "tresorerie/transfert";
    }

    @PostMapping("/transfert")
    public String transferer(@RequestParam PerimetreType perimetre, @RequestParam(required = false) Long financementId,
                              @RequestParam @DateTimeFormat(iso = DATE) LocalDate dateOperation,
                              @RequestParam Long compteSourceId, @RequestParam Long compteDestinationId,
                              @RequestParam BigDecimal montant, @RequestParam String libelle,
                              @RequestParam String referencePiece, Authentication authentication) {
        tresorerieService.transferer(perimetre,
                financementId == null ? null : financementService.trouver(financementId),
                dateOperation,
                compteService.trouver(compteSourceId),
                compteService.trouver(compteDestinationId),
                montant, libelle, referencePiece, authentication.getName());
        return "redirect:/ecritures/brouillons";
    }
}
