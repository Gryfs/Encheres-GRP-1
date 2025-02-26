package fr.eni.enchere.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.eni.enchere.bll.ContexteService;
import fr.eni.enchere.bo.Categories;
import fr.eni.enchere.bo.Utilisateur;
import fr.eni.enchere.dal.CategorieDAO;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ContexteService contexteService;
    private final CategorieDAO categorieDAO;

    public AdminController(ContexteService contexteService, CategorieDAO categorieDAO) {
        this.categorieDAO = categorieDAO;
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

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categorieDAO.findAll());
        model.addAttribute("newCategorie", new Categories());
        return "admin/categories";
    }
    
    @PostMapping("/categories/add")
    public String addCategorie(@ModelAttribute Categories categorie) {
        categorieDAO.create(categorie);
        return "redirect:/admin/categories";
    }
    
    @PostMapping("/categories/edit/{id}")
    public String editCategorie(@PathVariable("id") long id, @ModelAttribute Categories categorie) {
        categorie.setId((int)id);
        categorieDAO.update(categorie);
        return "redirect:/admin/categories";
    }
    
    @PostMapping("/categories/delete/{id}")
    public String deleteCategorie(@PathVariable("id") long id) {
        categorieDAO.delete(id);
        return "redirect:/admin/categories";
    }
}