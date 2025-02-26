package fr.eni.enchere.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.eni.enchere.bll.ContexteService;
import fr.eni.enchere.bo.Utilisateur;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ContexteService contexteService;

    public AdminController(ContexteService contexteService) {
        this.contexteService = contexteService;
    }

    @GetMapping("")
    public String adminPage(Model model) {
        List<Utilisateur> utilisateurs = contexteService.getAllUtilisateurs();
        model.addAttribute("utilisateurs", utilisateurs);
        return "admin/dashboard";
    }

    @PostMapping("/disable/{id}")
    public String disableUser(@PathVariable("id") Integer id) {
        Utilisateur utilisateur = contexteService.chargerParId(id);
        utilisateur.setActif(!utilisateur.isActif());
        contexteService.updateUtilisateur(utilisateur);
        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}") 
    public String deleteUser(@PathVariable("id") Integer id) {
        contexteService.deleteUtilisateur(id);
        return "redirect:/admin";
    }
}