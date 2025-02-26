package fr.eni.enchere.dal;

import java.util.List;

import fr.eni.enchere.bo.Categories;



public interface CategorieDAO {

	List<Categories> findAll();
	
	Categories read(long id);

	void create(Categories categorie);

    void update(Categories categorie);

    void delete(long id);
}
