package com.ong.compta.web;

import com.ong.compta.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/etats")
public class EtatController {

    private final EtatService etatService;
    private final FinancementService financementService;
    private final EnfantService enfantService;
    private final AideService aideService;

    public EtatController(EtatService etatService, FinancementService financementService,
                          EnfantService enfantService, AideService aideService) {
        this.etatService = etatService;
        this.financementService = financementService;
        this.enfantService = enfantService;
        this.aideService = aideService;
    }

    @GetMapping("/journal")
    public String journal(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
                           Model model) {
        if (debut == null) debut = LocalDate.now().withDayOfMonth(1);
        if (fin == null) fin = LocalDate.now();
        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        model.addAttribute("ecritures", etatService.journal(debut, fin));
        return "etats/journal";
    }

    @GetMapping("/balance")
    public String balance(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
                           Model model) {
        if (debut == null) debut = LocalDate.now().withDayOfMonth(1);
        if (fin == null) fin = LocalDate.now();
        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        model.addAttribute("lignes", etatService.balance(debut, fin));
        return "etats/balance";
    }

    @GetMapping("/rapport-projet")
    public String rapportProjet(@RequestParam(required = false) Long financementId, Model model) {
        model.addAttribute("financements", financementService.lister());
        if (financementId != null) {
            var financement = financementService.trouver(financementId);
            model.addAttribute("financement", financement);
            model.addAttribute("ecritures", etatService.rapportProjet(financement));
        }
        return "etats/rapport-projet";
    }

    @GetMapping("/historique-enfant")
    public String historiqueEnfant(@RequestParam(required = false) Long enfantId, Model model) {
        model.addAttribute("enfants", enfantService.lister());
        if (enfantId != null) {
            var enfant = enfantService.trouver(enfantId);
            model.addAttribute("enfant", enfant);
            model.addAttribute("aides", aideService.historique(enfant));
        }
        return "etats/historique-enfant";
    }
}
