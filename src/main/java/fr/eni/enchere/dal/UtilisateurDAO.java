package fr.eni.enchere.dal;

import java.util.List;

import fr.eni.enchere.bo.Utilisateur;

public interface UtilisateurDAO {

	Utilisateur read(long id);

	Utilisateur read(String email);

	void create(Utilisateur utilisateur);

	int countByEmail(String email);

	void update(Utilisateur utilisateur);

	void delete(long id);

	List<Utilisateur> findAll();

	Utilisateur findById(Integer id);

	Utilisateur findByResetToken(String token);

	void updateCredit(Utilisateur utilisateur);

}
