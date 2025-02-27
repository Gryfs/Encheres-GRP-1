package fr.eni.enchere.bll;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Categories;
import fr.eni.enchere.dal.ArticleVenduDAO;
import fr.eni.enchere.dal.CategorieDAO;
import fr.eni.enchere.dal.RetraitDAO;
import fr.eni.enchere.dal.UtilisateurDAO;

@Service
public class EnchereServiceImpl implements EnchereService {

	private ArticleVenduDAO articleVenduDAO;
	private CategorieDAO categorieDAO;
	private UtilisateurDAO utilisateurDAO;
	private RetraitDAO retraitDAO;

	public EnchereServiceImpl(ArticleVenduDAO articleVenduDAO, CategorieDAO categorieDAO, UtilisateurDAO utilisateurDAO, RetraitDAO retraitDAO) {

		this.articleVenduDAO = articleVenduDAO;
		this.categorieDAO = categorieDAO;
		this.utilisateurDAO = utilisateurDAO;
		this.retraitDAO = retraitDAO;
	}

	@Override
	public List<ArticleVendu> consulterArticle() {

		List<ArticleVendu> listeArticle = articleVenduDAO.findAll();
		for (ArticleVendu article : listeArticle) {
			int idCategorie = article.getCategorie().getId();
			article.setCategorie(categorieDAO.read(article.getCategorie().getId()));
			article.setUtilisateur(utilisateurDAO.read(article.getUtilisateur().getNoUtilisateur()));

		}
		return listeArticle;
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
	public ArticleVendu consulterArticleParId(long id) {
		ArticleVendu article = articleVenduDAO.rechercherArticleParId(id);
		article.setCategorie(categorieDAO.read(article.getCategorie().getId()));
		article.setUtilisateur(utilisateurDAO.read(article.getUtilisateur().getNoUtilisateur()));
		article.setRetrait(retraitDAO.consulterRetraitParIdarticle(article.getNoArticle()));
		return article;
	}

}
