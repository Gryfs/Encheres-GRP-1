package fr.eni.enchere.dal;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Utilisateur;

public interface ArticleVenduDAO {

	List<ArticleVendu> findAll();

	void create(ArticleVendu article);

	List<ArticleVendu> findAllByCategorie(Integer id);

	List<ArticleVendu> findAllByCategorieUtilisateur(Integer id, Utilisateur utilisateur);

	List<ArticleVendu> rechercherArticlesParNom(String search);

	ArticleVendu rechercherArticleParId(long id);

	void updatePrixVente(ArticleVendu article, Float nouveauPrix);
	
	void update(ArticleVendu article);
	
	void updateArticle(ArticleVendu article);

	List<ArticleVendu> findAllByUser(long id);

	void deleteArticle(ArticleVendu article);
	
	void delete(long noArticle);
	
	List<ArticleVendu> findExpiredAuctions(LocalDate today);

	void updateEtatVente(long idArticle);

	List<ArticleVendu> rechercherArticlesParNomEtUtilisateur(String search, Utilisateur utilisateur);

	List<ArticleVendu> findArticlesByEncheresAndNom(long idUtilisateur, String nomRecherche);

	List<ArticleVendu> findArticlesByIds(Set<Long> articleIds);

	List<ArticleVendu> findArticlesByIdsAndNom(Set<Long> articleIds, String nomRecherche);

	List<ArticleVendu> findArticlesGagnesByUtilisateur(long idUtilisateur, LocalDate today);

	List<ArticleVendu> findArticlesGagnesByUtilisateurAndNom(long idUtilisateur, LocalDate today, String nomRecherche);

	List<ArticleVendu> findArticlesGagnesByUtilisateurAndCategorie(long idUtilisateur, LocalDate today,
			long idCategorie);

}
