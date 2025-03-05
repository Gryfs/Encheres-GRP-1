package fr.eni.enchere.dal;

import java.util.List;

import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Enchere;

public interface EnchereDAO {

	void create(Enchere enchere);
	
	List<Enchere> SelectEnchereByIdArticle(long id);
	
	List<Enchere> findEncheresByUtilisateur(long idUtilisateur);


}
