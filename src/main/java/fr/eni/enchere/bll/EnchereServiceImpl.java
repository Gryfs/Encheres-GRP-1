package fr.eni.enchere.bll;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Categories;
import fr.eni.enchere.bo.Enchere;
import fr.eni.enchere.bo.Utilisateur;
import fr.eni.enchere.dal.ArticleVenduDAO;
import fr.eni.enchere.dal.CategorieDAO;
import fr.eni.enchere.dal.EnchereDAO;
import fr.eni.enchere.dal.RetraitDAO;
import fr.eni.enchere.dal.UtilisateurDAO;

@Service
public class EnchereServiceImpl implements EnchereService {

	private ArticleVenduDAO articleVenduDAO;
	private CategorieDAO categorieDAO;
	private UtilisateurDAO utilisateurDAO;
	private RetraitDAO retraitDAO;
	private EnchereDAO enchereDAO;

	public EnchereServiceImpl(ArticleVenduDAO articleVenduDAO, CategorieDAO categorieDAO, UtilisateurDAO utilisateurDAO,
			RetraitDAO retraitDAO, EnchereDAO enchereDAO) {

		this.articleVenduDAO = articleVenduDAO;
		this.categorieDAO = categorieDAO;
		this.utilisateurDAO = utilisateurDAO;
		this.retraitDAO = retraitDAO;
		this.enchereDAO = enchereDAO;

	}

	@Override
	public List<ArticleVendu> consulterArticle() {

		List<ArticleVendu> listeArticle = articleVenduDAO.findAll();
		for (ArticleVendu article : listeArticle) {
			article.setCategorie(categorieDAO.read(article.getCategorie().getId()));
			article.setUtilisateur(utilisateurDAO.read(article.getUtilisateur().getNoUtilisateur()));

		}
		return listeArticle;
	}

	@Override
	public List<ArticleVendu> consulterArticlesParUtilisateur(long id) {

		List<ArticleVendu> listeArticle = articleVenduDAO.findAllByUser(id);
		for (ArticleVendu article : listeArticle) {
			article.setCategorie(categorieDAO.read(article.getCategorie().getId()));
			article.setUtilisateur(utilisateurDAO.read(article.getUtilisateur().getNoUtilisateur()));
			article.setRetrait(retraitDAO.consulterRetraitParIdarticle(article.getNoArticle()));
			article.setEncheres(enchereDAO.SelectEnchereByIdArticle(article.getNoArticle()));

			if (article.getDateDebutEncheres().isBefore(LocalDate.now())
					&& article.getDateFinEncheres().isAfter(LocalDate.now())) {
				article.setEtatVente("OPEN");
			} else {
				article.setEtatVente("CLOSE");
			}

		}
		return listeArticle;
	}

	@Override
	public List<ArticleVendu> consulterArticleparCategorieUtilisateur(Integer id, Utilisateur utilisateur) {

		List<ArticleVendu> listeArticles = articleVenduDAO.findAllByCategorieUtilisateur(id, utilisateur);
		for (ArticleVendu article : listeArticles) {
			article.setCategorie(categorieDAO.read(article.getCategorie().getId()));
			article.setUtilisateur(utilisateurDAO.read(article.getUtilisateur().getNoUtilisateur()));

		}
		return listeArticles;
	}

	@Override
	public List<Categories> consulterCategories() {

		return categorieDAO.findAll();
	}

	@Override
	public void creerArticle(ArticleVendu article) {
		article.setUtilisateur(utilisateurDAO.read(article.getUtilisateur().getNoUtilisateur()));
		article.setCategorie(categorieDAO.read(article.getCategorie().getId()));

		articleVenduDAO.create(article);

	}

	@Override
	public Categories consulterCategorieparId(long id) {

		return categorieDAO.read(id);

	}

	@Override
	public List<ArticleVendu> consulterArticleparCategorie(Integer id) {

		return articleVenduDAO.findAllByCategorie(id);
	}

	@Override
	public List<ArticleVendu> rechercherArticlesParNom(String nom) {

		return articleVenduDAO.rechercherArticlesParNom(nom);
	}

	@Override
	public List<ArticleVendu> rechercherArticlesParNomEtUtilisateur(String nom, Utilisateur utilisateur) {

		return articleVenduDAO.rechercherArticlesParNomEtUtilisateur(nom, utilisateur);
	}

	@Override
	public ArticleVendu consulterArticleParId(long id) {
		ArticleVendu article = articleVenduDAO.rechercherArticleParId(id);
		article.setCategorie(categorieDAO.read(article.getCategorie().getId()));
		article.setUtilisateur(utilisateurDAO.read(article.getUtilisateur().getNoUtilisateur()));
		article.setRetrait(retraitDAO.consulterRetraitParIdarticle(article.getNoArticle()));
		article.setEncheres(enchereDAO.SelectEnchereByIdArticle(id));

		if (article.getDateDebutEncheres().isBefore(LocalDate.now())
				&& article.getDateFinEncheres().isAfter(LocalDate.now())) {
			article.setEtatVente("OPEN");
		} else {
			article.setEtatVente("CLOSE");
		}
		return article;
	}

	@Override
	@Transactional
	public void encherir(ArticleVendu article, Float nouveauPrix, Utilisateur utilisateur) {

		utilisateur.setCredit(utilisateur.getCredit() - nouveauPrix);
		utilisateurDAO.updateCredit(utilisateur);

		if (article.getEncheres().size() != 0) {
			Enchere derniereEnchere = article.getEncheres().get(article.getEncheres().size() - 1);
			Utilisateur utilisateurDerniereEnchere = utilisateurDAO
					.read(derniereEnchere.getUtilisateur().getNoUtilisateur());
			Float montantDerniereEnchere = derniereEnchere.getMontantEnchere();

			// Rembourser le montant de la dernière enchère à l'utilisateur précédent
			float testCredit = utilisateurDerniereEnchere.getCredit() + montantDerniereEnchere;
			System.out.println(testCredit);
			utilisateurDerniereEnchere.setCredit(utilisateurDerniereEnchere.getCredit() + montantDerniereEnchere);
			utilisateurDAO.updateCredit(utilisateurDerniereEnchere);
		}

		Enchere nouvelleEnchere = new Enchere();
		nouvelleEnchere.setArticle(article);
		nouvelleEnchere.setMontantEnchere(nouveauPrix);
		nouvelleEnchere.setUtilisateur(utilisateur);
		nouvelleEnchere.setDateEnchere(LocalDate.now());

		enchereDAO.create(nouvelleEnchere);
		article.ajouterEnchere(nouvelleEnchere);

		articleVenduDAO.updatePrixVente(article, nouveauPrix);

	}

	@Override
	public List<ArticleVendu> consulterGains(Utilisateur utilisateur) {
		LocalDate today = LocalDate.now();
		// Récupérer les articles gagnés par l'utilisateur
		List<ArticleVendu> articlesGagnes = articleVenduDAO
				.findArticlesGagnesByUtilisateur(utilisateur.getNoUtilisateur(), today);

		// Remplir les détails des articles (catégorie, utilisateur)
		for (ArticleVendu article : articlesGagnes) {
			article.setCategorie(categorieDAO.read(article.getCategorie().getId()));
			article.setUtilisateur(utilisateurDAO.read(article.getUtilisateur().getNoUtilisateur()));
		}

		return articlesGagnes;
	}

	@Override
	public List<ArticleVendu> consulterGainsAvecRecherche(Utilisateur utilisateur, String nomRecherche) {
		LocalDate today = LocalDate.now();
		// Récupérer les articles gagnés par l'utilisateur et filtrer par nom
		List<ArticleVendu> articlesGagnes = articleVenduDAO
				.findArticlesGagnesByUtilisateurAndNom(utilisateur.getNoUtilisateur(), today, nomRecherche);

		// Remplir les détails des articles (catégorie, utilisateur)
		for (ArticleVendu article : articlesGagnes) {
			article.setCategorie(categorieDAO.read(article.getCategorie().getId()));
			article.setUtilisateur(utilisateurDAO.read(article.getUtilisateur().getNoUtilisateur()));
		}

		return articlesGagnes;
	}

	@Override
    public List<ArticleVendu> consulterGainsParCategorie(Utilisateur utilisateur, long idCategorie) {
        LocalDate today = LocalDate.now();
        // Récupérer les articles gagnés par l'utilisateur et filtrer par catégorie
        List<ArticleVendu> articlesGagnes = articleVenduDAO.findArticlesGagnesByUtilisateurAndCategorie(utilisateur.getNoUtilisateur(), today, idCategorie);

        // Remplir les détails des articles (catégorie, utilisateur)
        for (ArticleVendu article : articlesGagnes) {
            article.setCategorie(categorieDAO.read(article.getCategorie().getId()));
            article.setUtilisateur(utilisateurDAO.read(article.getUtilisateur().getNoUtilisateur()));
        }

        return articlesGagnes;
    }
	
	
	@Override
	public void updateArticle(ArticleVendu article) {
		if (article.getDateDebutEncheres().isAfter(LocalDate.now())) {
			articleVenduDAO.updateArticle(article);
		}

	}

	@Override
	public void deleteArticle(long id) {
		ArticleVendu article = articleVenduDAO.rechercherArticleParId(id);
		if (article.getDateDebutEncheres().isAfter(LocalDate.now())) {
			articleVenduDAO.deleteArticle(article);
			article.getNoArticle();
		}

	}

	@Override
	public List<ArticleVendu> obtenirArticlesParEncheresUtilisateur(long idUtilisateur) {
		// Récupérer toutes les enchères de l'utilisateur
		List<Enchere> encheresUtilisateur = enchereDAO.findEncheresByUtilisateur(idUtilisateur);

		// Extraire les identifiants des articles associés à ces enchères
		Set<Long> articleIds = encheresUtilisateur.stream().map(Enchere::getArticle).map(ArticleVendu::getNoArticle)
				.collect(Collectors.toSet());

		// Récupérer les articles à partir des identifiants en une seule requête
		List<ArticleVendu> articles = articleVenduDAO.findArticlesByIds(articleIds);

		// Remplir les détails des articles (catégorie, utilisateur)
		for (ArticleVendu article : articles) {
			article.setCategorie(categorieDAO.read(article.getCategorie().getId()));
			article.setUtilisateur(utilisateurDAO.read(article.getUtilisateur().getNoUtilisateur()));
		}

		return articles;
	}

	@Override
	public List<ArticleVendu> obtenirArticlesParEncheresEtNom(long idUtilisateur, String nomRecherche) {
		// Récupérer toutes les enchères de l'utilisateur
		List<Enchere> encheresUtilisateur = enchereDAO.findEncheresByUtilisateur(idUtilisateur);

		// Extraire les identifiants des articles associés à ces enchères
		Set<Long> articleIds = encheresUtilisateur.stream().map(Enchere::getArticle).map(ArticleVendu::getNoArticle)
				.collect(Collectors.toSet());

		// Récupérer les articles à partir des identifiants et du nom recherché
		List<ArticleVendu> articles = articleVenduDAO.findArticlesByIdsAndNom(articleIds, nomRecherche);

		// Remplir les détails des articles (catégorie, utilisateur)
		for (ArticleVendu article : articles) {
			article.setCategorie(categorieDAO.read(article.getCategorie().getId()));
			article.setUtilisateur(utilisateurDAO.read(article.getUtilisateur().getNoUtilisateur()));
		}

		return articles;
	}

}
