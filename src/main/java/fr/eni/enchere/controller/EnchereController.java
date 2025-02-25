package fr.eni.enchere.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import fr.eni.enchere.bll.EnchereService;
import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Categories;

@Controller
public class EnchereController {

	private EnchereService enchereService;

	public EnchereController(EnchereService enchereService) {

		this.enchereService = enchereService;
	}

	@GetMapping("articles")
	public String afficherFilms(Model model) {

		List<ArticleVendu> listeArticles = enchereService.consulterArticle();

		model.addAttribute("articles", listeArticles);

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

	@ModelAttribute("categorieSession")
	public List<Categories> chargerGenreEnSession() {
		System.out.println("Chargement des Categories");
		return this.enchereService.consulterCategories();
	}

}
