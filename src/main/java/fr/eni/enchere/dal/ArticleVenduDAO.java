package fr.eni.enchere.dal;

import java.util.List;

import fr.eni.enchere.bo.ArticleVendu;


public interface ArticleVenduDAO {
	
	List<ArticleVendu> findAll();
	
	void create(ArticleVendu article);
	
	List<ArticleVendu> findAllByCategorie(Integer id);
	
	List<ArticleVendu> rechercherArticlesParNom(String search);


}
