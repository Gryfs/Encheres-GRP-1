package fr.eni.enchere.dal;

import fr.eni.enchere.bo.Utilisateur;

public interface UtilisateurDAO {
	
	Utilisateur read(long id);
	
	Utilisateur read(String email);
	
	void create(Utilisateur utilisateur);

	int countByEmail(String email);

	void update(Utilisateur utilisateur);

}
