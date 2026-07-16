package com.ong.compta.web;

import com.ong.compta.service.ListePaiementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ListePaiementController.class)
class ListePaiementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListePaiementService listePaiementService;

    @Test
    void acces_non_authentifie_est_refuse() throws Exception {
        mockMvc.perform(get("/listes-paiement"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "COMPTABLE")
    void liste_retourne_vue() throws Exception {
        when(listePaiementService.lister()).thenReturn(List.of());

        mockMvc.perform(get("/listes-paiement"))
                .andExpect(status().isOk())
                .andExpect(view().name("listes-paiement/liste"));
    }

    @Test
    @WithMockUser(roles = "DIRECTEUR")
    void directeur_peut_acceder_listes() throws Exception {
        when(listePaiementService.lister()).thenReturn(List.of());

        mockMvc.perform(get("/listes-paiement"))
                .andExpect(status().isOk());
    }
}
