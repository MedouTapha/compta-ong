package com.ong.compta.web;

import com.ong.compta.service.ListePaiementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/listes-paiement")
public class ListePaiementController {

    private final ListePaiementService listePaiementService;

    public ListePaiementController(ListePaiementService listePaiementService) {
        this.listePaiementService = listePaiementService;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("listes", listePaiementService.lister());
        return "listes-paiement/liste";
    }

    @PostMapping("/generer")
    public String generer(@RequestParam int annee, @RequestParam int mois, Authentication auth) {
        listePaiementService.generer(annee, mois, auth.getName());
        return "redirect:/listes-paiement";
    }

    @PostMapping("/{id}/valider")
    public String valider(@PathVariable Long id, Authentication auth) {
        listePaiementService.valider(id, auth.getName());
        return "redirect:/listes-paiement/" + id;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("liste", listePaiementService.trouver(id));
        return "listes-paiement/detail";
    }

    @PostMapping("/{id}/cloturer")
    public String cloturer(@PathVariable Long id, Authentication auth) {
        listePaiementService.cloturer(id, auth.getName());
        return "redirect:/listes-paiement/" + id;
    }

    @PostMapping("/lignes/{ligneId}/verser")
    public String marquerVersee(@PathVariable Long ligneId,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateVersement,
                                 @RequestParam String referenceDecharge,
                                 @RequestParam Long listeId) {
        listePaiementService.marquerLigneVersee(ligneId, dateVersement, referenceDecharge);
        return "redirect:/listes-paiement/" + listeId;
    }

    @PostMapping("/lignes/{ligneId}/non-verser")
    public String marquerNonVersee(@PathVariable Long ligneId,
                                    @RequestParam String motif,
                                    @RequestParam Long listeId) {
        listePaiementService.marquerLigneNonVersee(ligneId, motif);
        return "redirect:/listes-paiement/" + listeId;
    }
}
