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

	List<ArticleVendu> rechercherArticlesParNom(String nom);

	void encherir(ArticleVendu article, Float nouveauPrix, Utilisateur utilisateur);

	void updateArticle(ArticleVendu article);

}
