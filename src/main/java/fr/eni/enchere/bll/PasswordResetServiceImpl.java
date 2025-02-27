
package fr.eni.enchere.bll;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import fr.eni.enchere.bo.Utilisateur;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private ContexteService contexteService;
    
    @Override
    public void createPasswordResetTokenForUser(String email) {
        logger.debug("Demande de réinitialisation pour: {}", email);
        try {

            Utilisateur utilisateur = contexteService.charger(email);
            if (utilisateur != null) {
                String token = UUID.randomUUID().toString();
                utilisateur.setResetToken(token);
                utilisateur.setResetTokenExpiry(LocalDateTime.now().plusHours(24));
                contexteService.updateUtilisateur(utilisateur);
                
                sendPasswordResetEmail(utilisateur.getEmail(), token);
                logger.info("Email de réinitialisation envoyé à: {}", email);

            }
        } catch (Exception e) {
            logger.error("Erreur lors de la réinitialisation du mot de passe: {}", e.getMessage(), e);
            throw e;
        }
    }
    
   private void sendPasswordResetEmail(String email, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(email);
            helper.setSubject("Réinitialisation de votre mot de passe - ENI-Enchères");
            
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
                        .warning {
                            color: #dc3545;
                            font-weight: bold;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Réinitialisation de mot de passe</h1>
                        </div>
                        <div class="content">
                            <p><strong>Bonjour,</strong></p>
                            <p>Vous avez demandé la réinitialisation de votre mot de passe sur ENI-Enchères.</p>
                            <p>Pour créer un nouveau mot de passe, cliquez sur le bouton ci-dessous :</p>
                            <p style="text-align: center;">
                                <a href="http://localhost:8080/reset-password?token=%s" class="button">
                                    Réinitialiser mon mot de passe
                                </a>
                            </p>
                            <p class="warning">⚠️ Ce lien est valable pendant 24 heures.</p>
                            <p>Si vous n'avez pas demandé cette réinitialisation, vous pouvez ignorer cet email.</p>
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
                """, token);
            
            helper.setText(htmlContent, true); // true pour indiquer que c'est du HTML
            mailSender.send(mimeMessage);
            
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    public boolean resetPassword(String token, String newPassword) {
        Utilisateur utilisateur = contexteService.findByResetToken(token);
        
        if (utilisateur == null) {
            return false;
        }

        // Vérifier si le token n'a pas expiré
        if (utilisateur.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Mettre à jour le mot de passe et réinitialiser le token
        utilisateur.setMotDePasse(newPassword);
        utilisateur.setResetToken(null);
        utilisateur.setResetTokenExpiry(null);
        
        contexteService.updateUtilisateur(utilisateur);
        
        return true;
    }
}