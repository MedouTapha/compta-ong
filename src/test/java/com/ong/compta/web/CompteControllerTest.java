package com.ong.compta.web;

import com.ong.compta.domain.Compte;
import com.ong.compta.service.CompteService;
import com.ong.compta.service.FinancementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompteController.class)
class CompteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompteService compteService;

    @MockBean
    private FinancementService financementService;

    @Test
    void acces_non_authentifie_est_refuse() throws Exception {
        mockMvc.perform(get("/comptes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "COMPTABLE")
    void liste_comptes_retourne_vue() throws Exception {
        when(compteService.lister()).thenReturn(List.of());
        when(financementService.lister()).thenReturn(List.of());

        mockMvc.perform(get("/comptes"))
                .andExpect(status().isOk())
                .andExpect(view().name("comptes/liste"));
    }

    @Test
    @WithMockUser(roles = "COMPTABLE")
    void creer_compte_redirige() throws Exception {
        Compte compte = new Compte("5711", "Caisse", true, null);
        when(compteService.creer("5711", "Caisse", true, null)).thenReturn(compte);

        mockMvc.perform(post("/comptes")
                        .with(csrf())
                        .param("numero", "5711")
                        .param("libelle", "Caisse")
                        .param("estTresorerie", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/comptes"));
    }
}
