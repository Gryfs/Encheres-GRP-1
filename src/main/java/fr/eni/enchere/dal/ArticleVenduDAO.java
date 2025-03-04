package fr.eni.enchere.dal;

import java.time.LocalDate;
import java.util.List;

import fr.eni.enchere.bo.ArticleVendu;


public interface ArticleVenduDAO {
	
	List<ArticleVendu> findAll();
	
	void create(ArticleVendu article);
	
	List<ArticleVendu> findAllByCategorie(Integer id);
	
	List<ArticleVendu> rechercherArticlesParNom(String search);
	
	ArticleVendu rechercherArticleParId(long id);
	
	void updatePrixVente(ArticleVendu article, Float nouveauPrix);
	
	void updateArticle(ArticleVendu article);
	
	List<ArticleVendu> findAllByUser(long id);
	
	void deleteArticle(ArticleVendu article);
	
	List<ArticleVendu> findExpiredAuctions(LocalDate today);
	
	void updateEtatVente(long idArticle);


	


}
