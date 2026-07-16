package com.ong.compta.web;

import com.ong.compta.domain.Enfant;
import com.ong.compta.domain.Financement;
import com.ong.compta.domain.enums.StatutAide;
import com.ong.compta.service.AideService;
import com.ong.compta.service.EnfantService;
import com.ong.compta.service.FinancementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/aides")
public class AideController {

    private final AideService aideService;
    private final EnfantService enfantService;
    private final FinancementService financementService;

    public AideController(AideService aideService, EnfantService enfantService,
                          FinancementService financementService) {
        this.aideService = aideService;
        this.enfantService = enfantService;
        this.financementService = financementService;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("aides", aideService.lister());
        model.addAttribute("enfants", enfantService.lister());
        model.addAttribute("financements", financementService.lister());
        return "aides/liste";
    }

    @PostMapping
    public String creer(@RequestParam Long enfantId,
                         @RequestParam BigDecimal montantMensuel,
                         @RequestParam Long financementId,
                         @RequestParam String referenceDecision,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        Enfant enfant = enfantService.trouver(enfantId);
        Financement financement = financementService.trouver(financementId);
        aideService.creer(enfant, montantMensuel, financement, referenceDecision, dateDebut, dateFin);
        return "redirect:/aides";
    }

    @GetMapping("/{id}/edit")
    public String editer(@PathVariable Long id, Model model) {
        model.addAttribute("aide", aideService.trouver(id));
        return "aides/edit";
    }

    @PostMapping("/{id}/edit")
    public String modifier(@PathVariable Long id,
                            @RequestParam BigDecimal montantMensuel,
                            @RequestParam String referenceDecision,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                            @RequestParam StatutAide statut) {
        aideService.modifier(id, montantMensuel, referenceDecision, dateDebut, dateFin, statut);
        return "redirect:/aides";
    }
}
