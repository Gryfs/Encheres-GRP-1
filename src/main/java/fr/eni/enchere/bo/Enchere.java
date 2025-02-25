package fr.eni.enchere.bo;

import java.util.Date;

public class Enchere {

	private Date dateEnchere;
	private float montantEnchere;
	private Utilisateur utilisateur;
	private ArticleVendu article;

	public Enchere() {

	}

	public Enchere(Date dateEnchere, float montantEnchere, Utilisateur utilisateur, ArticleVendu article) {

		this.dateEnchere = dateEnchere;
		this.montantEnchere = montantEnchere;
		this.utilisateur = utilisateur;
		this.article = article;
	}

	public Date getDateEnchere() {
		return dateEnchere;
	}

	public void setDateEnchere(Date dateEnchere) {
		this.dateEnchere = dateEnchere;
	}

	public float getMontantEnchere() {
		return montantEnchere;
	}

	public void setMontantEnchere(float montantEnchere) {
		this.montantEnchere = montantEnchere;
	}

	public Utilisateur getUtilisateur() {
		return utilisateur;
	}

	public void setUtilisateur(Utilisateur utilisateur) {
		this.utilisateur = utilisateur;
	}

	public ArticleVendu getArticle() {
		return article;
	}

	public void setArticle(ArticleVendu article) {
		this.article = article;
	}

	@Override
	public String toString() {
		return "Enchere [dateEnchere=" + dateEnchere + ", montantEnchere=" + montantEnchere + ", utilisateur="
				+ utilisateur + ", article=" + article + "]";
	}

}
