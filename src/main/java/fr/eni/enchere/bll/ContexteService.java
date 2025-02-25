package fr.eni.enchere.bll;

import fr.eni.enchere.bo.Utilisateur;

public interface ContexteService {
	
	Utilisateur charger(String email);
	
	void creerUtilisateur(Utilisateur utilisateur);

	void updateUtilisateur(Utilisateur utilisateur);

	void deleteUtilisateur(long id);

}
