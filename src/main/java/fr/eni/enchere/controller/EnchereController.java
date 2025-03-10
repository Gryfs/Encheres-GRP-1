package fr.eni.enchere.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.eni.enchere.bll.EnchereService;
import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Categories;
import fr.eni.enchere.bo.Enchere;
import fr.eni.enchere.bo.Utilisateur;
import jakarta.validation.Valid;

@Controller
@SessionAttributes({ "categorieSession", "utilisateurSession" })
public class EnchereController {

	private EnchereService enchereService;

	private final int PAGE_SIZE = 10;

	public EnchereController(EnchereService enchereService) {

		this.enchereService = enchereService;
	}

	@GetMapping
	public String afficherAcceuil() {

		return "redirect:/encheres";
	}

	@GetMapping("encheres")
	public String afficherArticles(Model model, @RequestParam(name = "id", required = false) Integer categoryId,
			@RequestParam(name = "search", required = false) String search,
			@RequestParam(name = "page", defaultValue = "0") int page) {
		List<ArticleVendu> allArticles;

		if ((categoryId == null || categoryId == 0) && (search == null || search.isEmpty())) {
			// Si aucun filtre n'est appliqué
			allArticles = enchereService.consulterArticle();
		} else if (categoryId != null && categoryId != 0) {
			// Filtrer par catégorie
			allArticles = enchereService.consulterArticleparCategorie(categoryId);
		} else {
			// Filtrer par nom d'article
			allArticles = enchereService.rechercherArticlesParNom(search);
		}

		// Calcul de la pagination
		int totalItems = allArticles.size();
		int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

		// Extraction de la page courante
		int startItem = page * PAGE_SIZE;
		List<ArticleVendu> listeArticles = allArticles.subList(startItem,
				Math.min(startItem + PAGE_SIZE, allArticles.size()));

		model.addAttribute("articles", listeArticles);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("search", search);

		return "encheres";
	}

	@GetMapping("/detail")
	public String afficherDetail(@RequestParam(name = "id") int idArticle, Model model,
			@SessionAttribute(name = "utilisateurSession", required = false) Utilisateur utilisateurConnecte) {

		ArticleVendu article = enchereService.consulterArticleParId(idArticle);
		
		// Charger les détails complets des utilisateurs pour chaque enchère
		if (article.getEncheres() != null) {
			for (Enchere enchere : article.getEncheres()) {
				if (enchere.getUtilisateur() != null) {
					Utilisateur utilisateurComplet = enchereService.consulterUtilisateurParId(enchere.getUtilisateur().getNoUtilisateur());
					enchere.setUtilisateur(utilisateurComplet);
				}
			}
		}

		model.addAttribute("article", article);
		model.addAttribute("utilisateurConnecte", utilisateurConnecte);

		return "detail";
	}

	@GetMapping("/article/edit")
	public String editArticle(@RequestParam(name = "id") Long id, Model model) {

		ArticleVendu article = enchereService.consulterArticleParId(id);

		model.addAttribute("article", article);

		return "edit-article";
	}

	@PostMapping("/article/delete")
	public String deleteArticle(@RequestParam(name = "id") Long id, Model model) {

		enchereService.deleteArticle(id);

		return "redirect:/encheres";
	}

	@PostMapping("/article/edit")
	public String editArticle(@Valid @ModelAttribute("article") ArticleVendu article, BindingResult bindingResult,
			Model model) {

		if (bindingResult.hasErrors()) {
			// Si oui, renvoie à la page de modification avec les erreurs
			model.addAttribute("categories", enchereService.consulterCategories());
			return "edit-article";
		}
		
		if (article.getDateFinEncheres().isBefore(article.getDateDebutEncheres())) {
	        bindingResult.rejectValue("dateFinEncheres", "error.dateFinEncheres", "La date de fin ne peut pas être antérieure à la date de début.");
	        model.addAttribute("dateFinEncheresError", true);
	        model.addAttribute("dateFinEncheresErrorMessage", "La date de fin ne peut pas être antérieure à la date de début.");
	        return "edit-article"; // Renvoyer vers le formulaire avec un message d'erreur
	    }

		// Enregistre les modifications de l'article
		enchereService.updateArticle(article);

		return "redirect:/detail?id=" + article.getNoArticle();
	}

	@GetMapping("/encherir")
	public String afficherEncherir(@RequestParam(name = "id") int idArticle, Model model) {

		ArticleVendu article = enchereService.consulterArticleParId(idArticle);

		model.addAttribute("article", article);

		return "encherir";
	}

	@PostMapping("/encherir")
	public String encherir(@RequestParam("id") Long id, @RequestParam("nouveauPrix") Float nouveauPrix,
			RedirectAttributes redirectAttributes,
			@SessionAttribute(name = "utilisateurSession", required = false) Utilisateur utilisateur) {
		ArticleVendu article = enchereService.consulterArticleParId(id);

		if (nouveauPrix < article.getPrixVente()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Le nouveau prix doit être supérieur au prix actuel.");

		} else if (nouveauPrix > utilisateur.getCredit()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Vos crédits ne sont pas suffisant");
		} else {
			enchereService.encherir(article, nouveauPrix, utilisateur);
			redirectAttributes.addFlashAttribute("successMessage", "Votre enchère a été acceptée !");
		}

		return "redirect:/encherir?id=" + article.getNoArticle();
	}

	@GetMapping("/creer")
	public String afficherFormulaireCreation(Model model) {
		model.addAttribute("article", new ArticleVendu());

		return "creer";
	}

	@PostMapping("/creer")
	public String creerArticle(@Valid @ModelAttribute("article") ArticleVendu article,
			@RequestParam("imageFile") MultipartFile imageFile, BindingResult bindingResult,
			@SessionAttribute(name = "utilisateurSession", required = false) Utilisateur utilisateur, Model model) {

		if (!imageFile.isEmpty()) {
			try {
				// Convertir l'image en Base64
				byte[] bytes = imageFile.getBytes();
				String base64Image = "data:" + imageFile.getContentType() + ";base64,"
						+ Base64.getEncoder().encodeToString(bytes);

				// Sauvegarder l'image en Base64 dans l'article
				article.setImage(base64Image);

			} catch (IOException e) {
				// Gérer l'erreur
				e.printStackTrace();
			}
		}

		if (utilisateur != null) {
			article.setUtilisateur(utilisateur);
		} else {
			System.out.println("Aucun utilisateur en session !");
			return "redirect:/login";
		}
		
		if (article.getDateFinEncheres().isBefore(article.getDateDebutEncheres())) {
	        bindingResult.rejectValue("dateFinEncheres", "error.dateFinEncheres", "La date de fin ne peut pas être antérieure à la date de début.");
	        model.addAttribute("dateFinEncheresError", true);
	        model.addAttribute("dateFinEncheresErrorMessage", "La date de fin ne peut pas être antérieure à la date de début.");
	        return "creer"; // Renvoyer vers le formulaire avec un message d'erreur
	    }

		System.out.println("creerarticle = " + article);
		this.enchereService.creerArticle(article);

		return "redirect:/encheres";
	}

	@GetMapping("/mes-articles")
	public String afficherMesArticles(Model model, @RequestParam(name = "id", required = false) Integer categoryId,
			@RequestParam(name = "search", required = false) String search,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@SessionAttribute(name = "utilisateurSession", required = false) Utilisateur utilisateur) {
		List<ArticleVendu> allArticles;

		if ((categoryId == null || categoryId == 0) && (search == null || search.isEmpty())) {
			// Si aucun filtre n'est appliqué
			allArticles = enchereService.consulterArticlesParUtilisateur(utilisateur.getNoUtilisateur());
		} else if (categoryId != null && categoryId != 0) {
			// Filtrer par catégorie
			allArticles = enchereService.consulterArticleparCategorieUtilisateur(categoryId, utilisateur);
		} else {
			// Filtrer par nom d'article
			allArticles = enchereService.rechercherArticlesParNomEtUtilisateur(search, utilisateur);
		}

		// Calcul de la pagination
		int totalItems = allArticles.size();
		int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

		// Extraction de la page courante
		int startItem = page * PAGE_SIZE;
		List<ArticleVendu> listeArticles = allArticles.subList(startItem,
				Math.min(startItem + PAGE_SIZE, allArticles.size()));

		model.addAttribute("articles", listeArticles);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("search", search);

		return "mes-articles";
	}

	@GetMapping("/mes-encheres")
	public String afficherMesEncheres(Model model, @RequestParam(name = "id", required = false) Integer categoryId,
			@RequestParam(name = "search", required = false) String search,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@SessionAttribute(name = "utilisateurSession", required = false) Utilisateur utilisateur) {
		List<ArticleVendu> allArticles;

		if ((search == null || search.isEmpty())) {
			// Si aucun filtre n'est appliqué
			allArticles = enchereService.obtenirArticlesParEncheresUtilisateur(utilisateur.getNoUtilisateur());
		} else {
			// Filtrer par nom d'article
			allArticles = enchereService.obtenirArticlesParEncheresEtNom(utilisateur.getNoUtilisateur(), search);
		}

		// Calcul de la pagination
		int totalItems = allArticles.size();
		int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

		// Extraction de la page courante
		int startItem = page * PAGE_SIZE;
		List<ArticleVendu> listeArticles = allArticles.subList(startItem,
				Math.min(startItem + PAGE_SIZE, allArticles.size()));

		model.addAttribute("articles", listeArticles);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("search", search);

		return "mes-encheres";
	}

	@GetMapping("/mes-gains")
	public String afficherMesGains(Model model, @RequestParam(name = "id", required = false) Integer categoryId,
			@RequestParam(name = "search", required = false) String search,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@SessionAttribute(name = "utilisateurSession", required = false) Utilisateur utilisateur) {
		List<ArticleVendu> allArticles;

		if ((categoryId == null || categoryId == 0) && (search == null || search.isEmpty())) {
			// Si aucun filtre n'est appliqué
			allArticles = enchereService.consulterGains(utilisateur);
		} else if (categoryId != null && categoryId != 0) {
			// Filtrer par catégorie
			allArticles = enchereService.consulterGainsParCategorie(utilisateur, categoryId);
		} else {
			// Filtrer par nom d'article
			allArticles = enchereService.consulterGainsAvecRecherche(utilisateur, search);
		}

		// Calcul de la pagination
		int totalItems = allArticles.size();
		int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

		// Extraction de la page courante
		int startItem = page * PAGE_SIZE;
		List<ArticleVendu> listeArticles = allArticles.subList(startItem,
				Math.min(startItem + PAGE_SIZE, allArticles.size()));

		model.addAttribute("articles", listeArticles);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("search", search);

		return "mes-gains";
	}

	@ModelAttribute("categorieSession")
	public List<Categories> chargerCategorieEnSession() {
		return this.enchereService.consulterCategories();
	}

}
