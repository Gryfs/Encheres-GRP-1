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
import fr.eni.enchere.dal.EnchereDAO;
import fr.eni.enchere.dal.ArticleVenduDAO;
import fr.eni.enchere.bo.Enchere;
import fr.eni.enchere.bo.ArticleVendu;
import java.time.LocalDate;

@Service
@Primary
public class ContexteServiceImpl implements ContexteService {
	private final UtilisateurDAO utilisateurDAO;
    private final EnchereDAO enchereDAO;
    private final ArticleVenduDAO articleVenduDAO;
    private static final Logger logger = LoggerFactory.getLogger(ContexteServiceImpl.class);

	@Autowired
	public ContexteServiceImpl(UtilisateurDAO utilisateurDAO, EnchereDAO enchereDAO, ArticleVenduDAO articleVenduDAO) {
		this.utilisateurDAO = utilisateurDAO;
        this.enchereDAO = enchereDAO;
        this.articleVenduDAO = articleVenduDAO;
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
		} else {
			logger.warn("Tentative de création avec un email existant: {}", utilisateur.getEmail());
			throw new RuntimeException("Un compte existe déjà avec cet email");
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

    @Override
    @Transactional
    public void annulerEncheresEtVentes(Utilisateur utilisateur) {
        // Annuler toutes les enchères de l'utilisateur
        for (Enchere enchere : utilisateur.getEncheres()) {
            // Rembourser les crédits de l'enchère
            utilisateur.setCredit(utilisateur.getCredit() + enchere.getMontantEnchere());
            enchereDAO.delete(enchere.getNoEnchere());
        }
        
        // Supprimer toutes les ventes de l'utilisateur
        for (ArticleVendu article : utilisateur.getArticlesVendus()) {
            // Rembourser les enchères sur cet article
            List<Enchere> encheres = enchereDAO.findByArticle(article.getNoArticle());
            for (Enchere enchere : encheres) {
                Utilisateur encherisseur = enchere.getUtilisateur();
                encherisseur.setCredit(encherisseur.getCredit() + enchere.getMontantEnchere());
                utilisateurDAO.updateCredit(encherisseur);
                enchereDAO.delete(enchere.getNoEnchere());
            }
            // Supprimer l'article
            articleVenduDAO.delete(article.getNoArticle());
        }
        
        // Mettre à jour les crédits de l'utilisateur
        utilisateurDAO.updateCredit(utilisateur);
    }
}
