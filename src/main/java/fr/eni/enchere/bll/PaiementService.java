package fr.eni.enchere.bll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import fr.eni.enchere.bo.Utilisateur;
import fr.eni.enchere.dal.UtilisateurDAOImpl; // Ajout de l'import
import jakarta.annotation.PostConstruct;

@Service
public class PaiementService {
    private static final Logger logger = LoggerFactory.getLogger(PaiementService.class);

    private final UtilisateurDAOImpl utilisateurDAO; 
    
    public PaiementService(UtilisateurDAOImpl utilisateurDAO) {
        this.utilisateurDAO = utilisateurDAO;
    }

	@Value("${stripe.secret.key}")
	private String secretKey;

	@Value("${app.url}")
	private String appUrl;

	@PostConstruct
	public void init() {
		 //Stripe.apiKey = this.secretKey;
	}

    public String createCheckoutSession(int amount) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(appUrl + "/credits/success?session_id={CHECKOUT_SESSION_ID}") // Ajout du session_id
                .setCancelUrl(appUrl + "/credits/cancel")
                .addLineItem(SessionCreateParams.LineItem.builder()
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("eur")
                        .setUnitAmount((long) amount * 100L)
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName("Crédits ENI-Enchères")
                            .setDescription("Achat de " + amount + " crédits")
                            .build())
                        .build())
                    .setQuantity(1L)
                    .build())
                .build();

            Session session = Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création de la session de paiement", e);
        }
    }

    
    public void handleSuccessfulPayment(String sessionId, Utilisateur utilisateur) {
        try {
            logger.info("Traitement du paiement pour la session {} et l'utilisateur {}", sessionId, utilisateur.getPseudo());
            
            Session session = Session.retrieve(sessionId);
            logger.info("Statut du paiement : {}", session.getPaymentStatus());
            
            if ("paid".equals(session.getPaymentStatus())) {
                long amountPaid = session.getAmountTotal();
                int creditsToAdd = (int) (amountPaid / 100);
                
                logger.info("Ajout de {} crédits pour l'utilisateur {}", creditsToAdd, utilisateur.getPseudo());
                
                float newCredit = utilisateur.getCredit() + creditsToAdd;
                utilisateur.setCredit(newCredit);
                
                utilisateurDAO.updateCredit(utilisateur); // Modification ici
                logger.info("Crédits mis à jour avec succès. Nouveau solde : {}", newCredit);
            } else {
                logger.warn("Le statut du paiement n'est pas 'paid' : {}", session.getPaymentStatus());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour des crédits", e);
            throw new RuntimeException("Erreur lors de la mise à jour des crédits", e);
        }
    }
}