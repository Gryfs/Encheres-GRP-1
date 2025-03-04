package fr.eni.enchere.bll;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Enchere;
import fr.eni.enchere.bo.Utilisateur;
import fr.eni.enchere.dal.ArticleVenduDAO;
import fr.eni.enchere.dal.EnchereDAO;
import fr.eni.enchere.dal.UtilisateurDAO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
public class ClotureEnchereBatch {

	private ArticleVenduDAO articleVenduDAO;
	private JavaMailSender mailSender;
	private UtilisateurDAO utilisateurDAO;

	public ClotureEnchereBatch(ArticleVenduDAO articleVenduDAO, JavaMailSender mailSender,
			UtilisateurDAO utilisateurDAO) {
		this.articleVenduDAO = articleVenduDAO;
		this.mailSender = mailSender;
		this.utilisateurDAO = utilisateurDAO;

	}

	@Scheduled(cron = "0 0 0 * * *") // Exécution chaque jour à minuit
	@Transactional
	public void cloturerEncheres() {
		LocalDate today = LocalDate.now();
		List<ArticleVendu> articlesExpirés = articleVenduDAO.findExpiredAuctions(today);

		for (ArticleVendu article : articlesExpirés) {
			
			if ("CLOSE".equals(article.getEtatVente())) {
	            continue; // Passe à l'article suivant sans envoyer d'email
	        }

			System.out.println(article);
			Optional<Enchere> derniereEnchere = article.getEncheres().stream()
					.max(Comparator.comparing(Enchere::getDateEnchere));
			System.out.println("Affiche : " + derniereEnchere);
			if (derniereEnchere.isPresent()) {
				Enchere gagnante = derniereEnchere.get();
				Utilisateur utilisateurGagnant = utilisateurDAO.read(gagnante.getUtilisateur().getNoUtilisateur());
				System.out.println("Voici le gagnant : " + utilisateurGagnant);
				sendAuctionWinningEmail(utilisateurGagnant, article);

			} else {
				Utilisateur vendeur = utilisateurDAO.read(article.getUtilisateur().getNoUtilisateur());
				System.out.println("Aucun acheteur");
				sendNoBidsEmail(vendeur, article);

			}
			articleVenduDAO.updateEtatVente(article.getNoArticle());
			article.setEtatVente("CLOSE");
		}
	}

	public void sendAuctionWinningEmail(Utilisateur gagnant, ArticleVendu article) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(gagnant.getEmail());
			helper.setSubject("Félicitations ! Vous avez remporté une enchère 🎉");

			String htmlContent = String.format("""
					<!DOCTYPE html>
					<html>
					<head>
					    <meta charset="UTF-8">
					    <style>
					        body {
					            font-family: Arial, sans-serif;
					            line-height: 1.6;
					            color: #333;
					            margin: 0;
					            padding: 0;
					        }
					        .container {
					            max-width: 600px;
					            margin: 0 auto;
					            padding: 20px;
					            background-color: #f8f9fa;
					        }
					        .header {
					            background: #4F46E5;
					            color: white;
					            padding: 30px;
					            text-align: center;
					            border-radius: 8px 8px 0 0;
					        }
					        .content {
					            background: white;
					            padding: 30px;
					            border-radius: 0 0 8px 8px;
					            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
					        }
					        .button {
					            display: inline-block;
					            padding: 12px 24px;
					            background: #4F46E5;
					            color: white;
					            text-decoration: none;
					            border-radius: 6px;
					            margin: 20px 0;
					            font-weight: bold;
					        }
					        .footer {
					            text-align: center;
					            margin-top: 20px;
					            padding: 20px;
					            font-size: 12px;
					            color: #666;
					        }
					    </style>
					</head>
					<body>
					    <div class="container">
					        <div class="header">
					            <h1>Félicitations, %s !</h1>
					        </div>
					        <div class="content">
					            <p><strong>Bravo !</strong> Vous avez remporté l'enchère pour :</p>
					            <p><strong>Article :</strong> %s</p>
					            <p><strong>Prix final :</strong> %.2f €</p>
					            <p><strong>Clôturé le :</strong> %s</p>
					            <p style="text-align: center;">
					                <a href="http://localhost:8080/article/%d" class="button">
					                    Voir mon article
					                </a>
					            </p>
					            <p>Un e-mail sera envoyé au vendeur afin d’organiser la remise de l’article.</p>
					        </div>
					        <div class="footer">
					            <p>Cordialement,<br><strong>L'équipe ENI-Enchères</strong></p>
					            <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
					            <p style="color: #999;">
					                Note : Ceci est un message automatique, merci de ne pas y répondre.
					            </p>
					        </div>
					    </div>
					</body>
					</html>
					""", gagnant.getPseudo(), article.getNomArticle(), article.getPrixVente(),
					article.getDateFinEncheres(), article.getNoArticle());

			helper.setText(htmlContent, true); // true = HTML format
			mailSender.send(mimeMessage);

		} catch (MessagingException e) {
			throw new RuntimeException("Erreur lors de l'envoi de l'email au gagnant", e);
		}
	}

	public void sendNoBidsEmail(Utilisateur vendeur, ArticleVendu article) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(vendeur.getEmail());
			helper.setSubject("Fin de l'enchère - Aucun acheteur");

			String htmlContent = String.format(
					"""
							<!DOCTYPE html>
							<html>
							<head>
							    <meta charset="UTF-8">
							    <style>
							        body {
							            font-family: Arial, sans-serif;
							            line-height: 1.6;
							            color: #333;
							            margin: 0;
							            padding: 0;
							        }
							        .container {
							            max-width: 600px;
							            margin: 0 auto;
							            padding: 20px;
							            background-color: #f8f9fa;
							        }
							        .header {
							            background: #dc3545;
							            color: white;
							            padding: 30px;
							            text-align: center;
							            border-radius: 8px 8px 0 0;
							        }
							        .content {
							            background: white;
							            padding: 30px;
							            border-radius: 0 0 8px 8px;
							            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
							        }
							        .footer {
							            text-align: center;
							            margin-top: 20px;
							            padding: 20px;
							            font-size: 12px;
							            color: #666;
							        }
							    </style>
							</head>
							<body>
							    <div class="container">
							        <div class="header">
							            <h1>Votre enchère est terminée</h1>
							        </div>
							        <div class="content">
							            <p><strong>Bonjour %s,</strong></p>
							            <p>Nous vous informons que votre enchère pour l'article suivant s'est terminée sans enchère :</p>
							            <p><strong>Article :</strong> %s</p>
							            <p><strong>Date de fin :</strong> %s</p>
							            <p>Malheureusement, aucun acheteur ne s'est manifesté.</p>
							            <p>Vous pouvez relancer l'enchère ou modifier votre annonce.</p>
							            <p style="text-align: center;">
							                <a href="http://localhost:8080/relancer-enchere/%d" class="button">
							                    Relancer l'enchère
							                </a>
							            </p>
							        </div>
							        <div class="footer">
							            <p>Cordialement,<br><strong>L'équipe ENI-Enchères</strong></p>
							            <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
							            <p style="color: #999;">
							                Note : Ceci est un message automatique, merci de ne pas y répondre.
							            </p>
							        </div>
							    </div>
							</body>
							</html>
							""",
					vendeur.getPseudo(), article.getNomArticle(), article.getDateFinEncheres(), article.getNoArticle());

			helper.setText(htmlContent, true); // true pour HTML
			mailSender.send(mimeMessage);

		} catch (MessagingException e) {
			throw new RuntimeException("Erreur lors de l'envoi de l'email au vendeur", e);
		}
	}

}
