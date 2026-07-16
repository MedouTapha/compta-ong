package com.ong.compta.config;

import com.ong.compta.domain.Utilisateur;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public class UtilisateurPrincipal implements UserDetails {

    private final Utilisateur utilisateur;

    public UtilisateurPrincipal(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name()));
    }

    @Override
    public String getPassword() {
        return utilisateur.getMotDePasseHash();
    }

    @Override
    public String getUsername() {
        return utilisateur.getIdentifiant();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return utilisateur.isActif();
    }
}
