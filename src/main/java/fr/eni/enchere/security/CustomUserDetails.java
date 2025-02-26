package fr.eni.enchere.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class CustomUserDetails extends User {
    private final String nom;
    private final String prenom;

    public CustomUserDetails(String username, String password, 
                           Collection<? extends GrantedAuthority> authorities,
                           String nom, String prenom) {
        super(username, password, authorities);
        this.nom = nom;
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }
}