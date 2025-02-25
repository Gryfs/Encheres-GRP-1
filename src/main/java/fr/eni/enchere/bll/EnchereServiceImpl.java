package fr.eni.enchere.bll;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Categories;
import fr.eni.enchere.dal.ArticleVenduDAO;
import fr.eni.enchere.dal.CategorieDAO;

@Service
public class EnchereServiceImpl implements EnchereService {

	private ArticleVenduDAO articleVenduDAO;
	private CategorieDAO categorieDAO;

	public EnchereServiceImpl(ArticleVenduDAO articleVenduDAO, CategorieDAO categorieDAO) {

		this.articleVenduDAO = articleVenduDAO;
		this.categorieDAO = categorieDAO;
	}

	@Override
	public List<ArticleVendu> consulterArticle() {

		return articleVenduDAO.findAll();
	}

	@Override
	public List<Categories> consulterCategories() {
		
		return categorieDAO.findAll();
	}

}
