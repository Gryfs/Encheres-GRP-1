package fr.eni.enchere.bll;

import java.util.List;

import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Categories;
import fr.eni.enchere.bo.Enchere;
import fr.eni.enchere.bo.Utilisateur;

public interface EnchereService {

	List<ArticleVendu> consulterArticle();

	ArticleVendu consulterArticleParId(long id);

	List<Categories> consulterCategories();

	void creerArticle(ArticleVendu article);

	Categories consulterCategorieparId(long id);

	List<ArticleVendu> consulterArticleparCategorie(Integer id);
	
	List<ArticleVendu> consulterArticleparCategorieUtilisateur(Integer id, Utilisateur utilisateur);

	List<ArticleVendu> rechercherArticlesParNom(String nom);

	void encherir(ArticleVendu article, Float nouveauPrix, Utilisateur utilisateur);

	void updateArticle(ArticleVendu article);
	
	List<ArticleVendu> consulterArticlesParUtilisateur(long id);
	
	void deleteArticle(long id);
	
	List<ArticleVendu> obtenirArticlesParEncheresUtilisateur(long idUtilisateur);

	List<ArticleVendu> consulterGains(Utilisateur utilisateur);

	List<ArticleVendu> rechercherArticlesParNomEtUtilisateur(String nom, Utilisateur utilisateur);

	List<ArticleVendu> obtenirArticlesParEncheresEtNom(long idUtilisateur, String nomRecherche);

	List<ArticleVendu> consulterGainsAvecRecherche(Utilisateur utilisateur, String nomRecherche);

	List<ArticleVendu> consulterGainsParCategorie(Utilisateur utilisateur, long idCategorie);


}
