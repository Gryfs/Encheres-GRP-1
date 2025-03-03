package fr.eni.enchere.bll;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(ContexteServiceImpl.class);

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
		logger.debug("Vérification de l'email: {}", utilisateur.getEmail());

		if (validerEmailInexistant(utilisateur.getEmail())) {
			utilisateurDAO.create(utilisateur);
			logger.info("Utilisateur créé avec succès: {}", utilisateur.getEmail());
		} else{
			logger.warn("Tentative de création avec un email existant: {}", utilisateur.getEmail());

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

	@Override
	public void ajouterCredits(long userId, int amount) {
		Utilisateur utilisateur = chargerAvecId(userId);
		utilisateur.setCredit(utilisateur.getCredit() + amount);
		updateUtilisateur(utilisateur);
	}

}
