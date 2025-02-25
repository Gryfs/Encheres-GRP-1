package fr.eni.enchere.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import fr.eni.enchere.bll.ContexteService;
import fr.eni.enchere.bo.Utilisateur;
import jakarta.validation.Valid;

@Controller
public class ProfileController {
    
    private final ContexteService contexteService;

    public ProfileController(ContexteService contexteService) {
        this.contexteService = contexteService;
    }

    @GetMapping("/profile")
    public String showProfile(Principal principal, Model model) {
        String email = principal.getName();
        Utilisateur utilisateur = contexteService.charger(email);
        model.addAttribute("utilisateur", utilisateur);
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String showEditProfile(Principal principal, Model model) {
        String email = principal.getName();
        Utilisateur utilisateur = contexteService.charger(email);
        model.addAttribute("utilisateur", utilisateur);
        return "edit-profile";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute("utilisateur") Utilisateur utilisateur, 
                              BindingResult bindingResult, 
                              Principal principal) {
        if (bindingResult.hasErrors()) {
            return "edit-profile";
        }
        
        contexteService.updateUtilisateur(utilisateur);
        return "redirect:/profile";
    }
}