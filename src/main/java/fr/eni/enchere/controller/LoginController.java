package fr.eni.enchere.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import fr.eni.enchere.bll.ContexteService;
import fr.eni.enchere.bll.PasswordResetService;
import fr.eni.enchere.bo.Utilisateur;
import jakarta.validation.Valid;

@Controller
@SessionAttributes({"utilisateurSession"})
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
    private PasswordResetService passwordResetService;
	
	@Autowired
	private ContexteService contexteService;	

	@GetMapping("/login")
	public String login(@RequestParam(value = "error", required = false) String error, Model model) {
		if (error != null) {
            try {
                Object exception = SecurityContextHolder.getContext().getAuthentication().getDetails();
                if (exception instanceof UsernameNotFoundException && 
                    ((UsernameNotFoundException) exception).getMessage().contains("désactivé")) {
                    model.addAttribute("error", "Compte désactivé. Veuillez contacter l'administrateur.");
                } else {
                    // Vérifier si l'utilisateur est désactivé directement
                    String username = SecurityContextHolder.getContext().getAuthentication().getName();
                    if (username != null && !username.equals("anonymousUser")) {
                        Utilisateur utilisateur = contexteService.charger(username);
                        if (utilisateur != null && !utilisateur.isActif()) {
                            model.addAttribute("error", "Compte désactivé. Veuillez contacter l'administrateur.");
                        } else {
                            model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect.");
                        }
                    } else {
                        model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect.");
                    }
                }
            } catch (Exception e) {
                model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect.");
            }
		}
		// Ajout de l'objet utilisateur au modèle
		if (!model.containsAttribute("utilisateur")) {
			model.addAttribute("utilisateur", new Utilisateur());
		}
		return "login";
	}
	
	@GetMapping("/register")
	public String register(Model model) {
		if (!model.containsAttribute("utilisateur")) {
			model.addAttribute("utilisateur", new Utilisateur());
		}
		return "register";
	}
		
	@PostMapping("/register")
	public String registerAccount(@Valid @ModelAttribute("utilisateur") Utilisateur utilisateur, BindingResult bindingResult, Model model) {
		logger.debug("Tentative d'inscription: {}", utilisateur.getEmail());
		if (bindingResult.hasErrors()) {
			return "register";
		}
		
		try {
			// Tentative d'inscription
			contexteService.creerUtilisateur(utilisateur);
			logger.info("Nouvel utilisateur créé: {}", utilisateur.getEmail());
			return "redirect:/login?success=true";
		} catch (RuntimeException e) {
			model.addAttribute("error", e.getMessage());
			// On garde l'utilisateur dans le modèle pour conserver les données saisies
			model.addAttribute("utilisateur", utilisateur);
			return "register";
		}
	}
	
	@GetMapping("/session")
    public String connecterUtilisateur(Principal principal, Model model) {
        String email = principal.getName();
        Utilisateur utilisateurConnecte = contexteService.charger(email);
        model.addAttribute("utilisateurSession", utilisateurConnecte);
        return "redirect:/";
    }

	@ModelAttribute("utilisateurSession")
	public Utilisateur chargerUtilisateurSession() {
		return new Utilisateur();
	}

	@GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }
    
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        passwordResetService.createPasswordResetTokenForUser(email);
        model.addAttribute("message", "Si un compte existe avec cet email, vous recevrez un lien de réinitialisation.");
        return "forgot-password";
    }
    
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }
    
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                     @RequestParam("password") String password,
                                     Model model) {
        // Vérifier et mettre à jour le mot de passe
        boolean result = passwordResetService.resetPassword(token, password);
        if (result) {
            return "redirect:/login?reset=success";
        }
        model.addAttribute("error", "Le lien de réinitialisation est invalide ou a expiré.");
        return "reset-password";
    }
}
