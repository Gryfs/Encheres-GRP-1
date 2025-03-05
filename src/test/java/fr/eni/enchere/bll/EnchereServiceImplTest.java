package fr.eni.enchere.bll;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.eni.enchere.bll.EnchereServiceImpl;
import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Enchere;
import fr.eni.enchere.bo.Utilisateur;
import fr.eni.enchere.dal.ArticleVenduDAO;
import fr.eni.enchere.dal.EnchereDAO;
import fr.eni.enchere.dal.UtilisateurDAO;

public class EnchereServiceImplTest {

    @Mock
    private ArticleVenduDAO articleVenduDAO;

    @Mock
    private UtilisateurDAO utilisateurDAO;

    @Mock
    private EnchereDAO enchereDAO;

    @InjectMocks
    private EnchereServiceImpl enchereService;

    private Utilisateur utilisateur;
    private ArticleVendu article;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        utilisateur = new Utilisateur();
        utilisateur.setNoUtilisateur(1L);
        utilisateur.setCredit(100F);

        article = new ArticleVendu();
        article.setNoArticle(1L);
        article.setPrixVente(50F);
        article.setEncheres(new ArrayList<>());
    }

    @Test
    void testEncherir_Success() {
        Float nouveauPrix = 60F;
        
        when(utilisateurDAO.read(utilisateur.getNoUtilisateur())).thenReturn(utilisateur);
        
        enchereService.encherir(article, nouveauPrix, utilisateur);
        
        assertEquals(40F, utilisateur.getCredit(), "Le crédit de l'utilisateur doit être mis à jour");
        verify(utilisateurDAO).updateCredit(utilisateur);
        verify(enchereDAO).create(any(Enchere.class));
        verify(articleVenduDAO).updatePrixVente(article, nouveauPrix);
    }

    @Test
    void testEncherir_WithPreviousEnchere() {
        Utilisateur ancienEncherisseur = new Utilisateur();
        ancienEncherisseur.setNoUtilisateur(2L);
        ancienEncherisseur.setCredit(80F);
        
        Enchere derniereEnchere = new Enchere();
        derniereEnchere.setUtilisateur(ancienEncherisseur);
        derniereEnchere.setMontantEnchere(55F);
        article.getEncheres().add(derniereEnchere);
        
        when(utilisateurDAO.read(utilisateur.getNoUtilisateur())).thenReturn(utilisateur);
        when(utilisateurDAO.read(ancienEncherisseur.getNoUtilisateur())).thenReturn(ancienEncherisseur);
        
        enchereService.encherir(article, 65F, utilisateur);
        
        assertEquals(35F, utilisateur.getCredit(), "Le crédit du nouvel encherisseur doit être mis à jour");
        assertEquals(135F, ancienEncherisseur.getCredit(), "Le précédent encherisseur doit être remboursé");
        verify(utilisateurDAO).updateCredit(ancienEncherisseur);
        verify(utilisateurDAO).updateCredit(utilisateur);
        verify(enchereDAO).create(any(Enchere.class));
        verify(articleVenduDAO).updatePrixVente(article, 65F);
    }
}
