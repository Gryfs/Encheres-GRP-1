package fr.eni.enchere.bll;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;

@Service
public class PaiementService {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @Value("${app.url}")
    private String appUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = this.secretKey;
    }


    public String createCheckoutSession(int amount) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(appUrl + "/credits/success")
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
}