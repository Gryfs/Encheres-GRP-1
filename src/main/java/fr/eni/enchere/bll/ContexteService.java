package fr.eni.enchere.bll;

import java.util.List;

import fr.eni.enchere.bo.Utilisateur;

public interface ContexteService {
	
	Utilisateur charger(String email);
	
	void creerUtilisateur(Utilisateur utilisateur);

	void updateUtilisateur(Utilisateur utilisateur);

	void deleteUtilisateur(long id);

	List<Utilisateur> getAllUtilisateurs();

    Utilisateur chargerParId(Integer id);
}
