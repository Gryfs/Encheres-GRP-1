package fr.eni.enchere.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import fr.eni.enchere.bll.ContexteService;
import fr.eni.enchere.bll.PaiementService;
import fr.eni.enchere.bo.PaymentRequest;
import fr.eni.enchere.bo.Utilisateur;

@Controller
@RequestMapping("/credits")
public class PaiementController {

    private final PaiementService paiementService;
    private final ContexteService contexteService;

    public PaiementController(PaiementService paiementService, ContexteService contexteService) {
        this.paiementService = paiementService;
        this.contexteService = contexteService;
    }

    @PostMapping("/checkout")
    @ResponseBody
    public String createCheckoutSession(@RequestBody PaymentRequest request) {
        return paiementService.createCheckoutSession(request.getAmount());
    }

    @GetMapping("/success")
    public String handleSuccess(@SessionAttribute("utilisateurSession") Utilisateur utilisateur,
                              @RequestParam("session_id") String sessionId) {
        return "redirect:/profile?payment=success";
    }

    @GetMapping("/cancel")
    public String handleCancel() {
        return "redirect:/profile?payment=canceled";
    }
}