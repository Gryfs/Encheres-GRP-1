package fr.eni.enchere.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import fr.eni.enchere.bll.EnchereService;
import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Categories;
import fr.eni.enchere.bo.Utilisateur;
import jakarta.validation.Valid;

@Controller
@SessionAttributes({ "categorieSession", "utilisateurSession" })
public class EnchereController {

	private EnchereService enchereService;

	public EnchereController(EnchereService enchereService) {

		this.enchereService = enchereService;
	}

	
	@GetMapping("articles")
	public String afficherArticles(Model model, 
	                               @RequestParam(name = "id", required = false) Integer categoryId,
	                               @RequestParam(name = "search", required = false) String search) {
	    List<ArticleVendu> listeArticles;

	    System.out.println("Catégorie reçue : " + categoryId);
	    System.out.println("Recherche reçue : " + search);

	    if ((categoryId == null || categoryId == 0) && (search == null || search.isEmpty())) {
	        // Si aucun filtre n'est appliqué
	        listeArticles = enchereService.consulterArticle();
	    } else if (categoryId != null && categoryId != 0) {
	        // Filtrer par catégorie
	        listeArticles = enchereService.consulterArticleparCategorie(categoryId);
	    } else {
	        // Filtrer par nom d'article
	        listeArticles = enchereService.rechercherArticlesParNom(search);
	    }

	    model.addAttribute("articles", listeArticles);
	    model.addAttribute("search", search); 

	    return "articles";
	}





	


	@GetMapping("/index")
	public String retourIndex() {
		return "index";
	}

	@GetMapping("/creer")
	public String afficherFormulaireCreation(Model model) {
		model.addAttribute("article", new ArticleVendu());

		return "creer";
	}

	
	@PostMapping("/creer")
	public String creerArticle(
	        @Valid @ModelAttribute("article") ArticleVendu article, 
	        BindingResult bindingResult,
	        @SessionAttribute(name = "utilisateurSession", required = false) Utilisateur utilisateur) {

	    if (utilisateur != null) {
	        article.setUtilisateur(utilisateur);
	    } else {
	        System.out.println("Aucun utilisateur en session !");
	        return "redirect:/login";
	    }

	    System.out.println("creerarticle = " + article);
	    this.enchereService.creerArticle(article);

	    return "redirect:/articles";
	}


	@ModelAttribute("categorieSession")
	public List<Categories> chargerCategorieEnSession() {
		return this.enchereService.consulterCategories();
	}

}
