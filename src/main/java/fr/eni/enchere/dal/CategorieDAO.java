package fr.eni.enchere.dal;

import java.util.List;

import fr.eni.enchere.bo.Categories;

public interface CategorieDAO {

	List<Categories> findAll();

}
