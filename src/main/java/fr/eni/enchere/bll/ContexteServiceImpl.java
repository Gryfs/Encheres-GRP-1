package fr.eni.enchere.bll;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.eni.enchere.bo.Utilisateur;
import fr.eni.enchere.dal.UtilisateurDAO;

@Service
@Primary
public class ContexteServiceImpl implements ContexteService {
	private final UtilisateurDAO utilisateurDAO;

	@Autowired
	public ContexteServiceImpl(UtilisateurDAO utilisateurDAO) {
		this.utilisateurDAO = utilisateurDAO;
	}

	@Override
	public Utilisateur charger(String email) {
		return utilisateurDAO.read(email);
	}
	
	@Override
	public Utilisateur chargerAvecId(long id) {
		
		return utilisateurDAO.read(id);
	}
	

	@Override
	@Transactional()
	public void creerUtilisateur(Utilisateur utilisateur) {
		if (validerEmailInexistant(utilisateur.getEmail())) {
			utilisateurDAO.create(utilisateur);
		}
	}

	@Override
    public void updateUtilisateur(Utilisateur utilisateur) {
        // Si le mot de passe est vide, charger l'ancien mot de passe
        if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().isEmpty()) {
            Utilisateur ancienUtilisateur = utilisateurDAO.read(utilisateur.getEmail());
            utilisateur.setMotDePasse(ancienUtilisateur.getMotDePasse());
        }
        utilisateurDAO.update(utilisateur);
    }

	private boolean validerEmailInexistant(String email) {
		int nbGenre = utilisateurDAO.countByEmail(email);
		return nbGenre != 1;
	}

	@Override
    @Transactional
    public void deleteUtilisateur(long id) {
        utilisateurDAO.delete(id);
    }


 	@Override
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurDAO.findAll();
    }

	@Override
	public Utilisateur chargerParId(Integer id) {
		Utilisateur utilisateur = utilisateurDAO.findById(id);
		if (utilisateur == null) {
			throw new RuntimeException("Utilisateur non trouvé avec l'ID : " + id);
		}
		return utilisateur;
	}

	@Override
    public Utilisateur findByResetToken(String token) {
        return utilisateurDAO.findByResetToken(token);
    }

}
