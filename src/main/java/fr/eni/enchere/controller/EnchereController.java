package fr.eni.enchere.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import fr.eni.enchere.bll.EnchereService;
import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Categories;

import jakarta.validation.Valid;

@Controller
@SessionAttributes({ "categorieSession" })
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

	@PostMapping("/creer")
	public String creerArticle(@Valid @ModelAttribute("article") ArticleVendu article, BindingResult bindingResult) {

		System.out.println("creerarticle = " + article);

		this.enchereService.creerArticle(article);
		return "redirect:/articles";

	}

	@ModelAttribute("categorieSession")
	public List<Categories> chargerCategorieEnSession() {
		return this.enchereService.consulterCategories();
	}

}
