package fr.eni.enchere.bll;

import java.util.List;

import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Categories;

public interface EnchereService {

	List<ArticleVendu> consulterArticle();

	List<Categories> consulterCategories();

}
