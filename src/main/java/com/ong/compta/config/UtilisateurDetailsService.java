package com.ong.compta.config;

import com.ong.compta.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifiant) throws UsernameNotFoundException {
        return utilisateurRepository.findByIdentifiant(identifiant)
                .map(UtilisateurPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur inconnu : " + identifiant));
    }
}
