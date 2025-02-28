package fr.eni.enchere.bo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

public class ArticleVendu implements Serializable {

	private long noArticle;
	private String nomArticle;
	private String description;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateDebutEncheres;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateFinEncheres;
	private Float prixInitial;
	private Float prixVente;
	private String etatVente;
	private Utilisateur utilisateur;
	private List<Enchere> encheres = new ArrayList<>();
	private Categories categorie;
	private Retrait retrait;

	public ArticleVendu() {

	}

	public ArticleVendu(long noArticle, String nomArticle, String description, LocalDate dateDebutEncheres,
			LocalDate dateFinEncheres, Float miseAPrix, Float prixVente, String etatVente) {

		this.noArticle = noArticle;
		this.nomArticle = nomArticle;
		this.description = description;
		this.dateDebutEncheres = dateDebutEncheres;
		this.dateFinEncheres = dateFinEncheres;
		this.prixInitial = miseAPrix;
		this.prixVente = prixVente;
		this.etatVente = etatVente;
	}

	public long getNoArticle() {
		return noArticle;
	}

	public void setNoArticle(long noArticle) {
		this.noArticle = noArticle;
	}

	public String getNomArticle() {
		return nomArticle;
	}

	public void setNomArticle(String nomArticle) {
		this.nomArticle = nomArticle;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getDateDebutEncheres() {
		return dateDebutEncheres;
	}

	public void setDateDebutEncheres(LocalDate dateDebutEncheres) {
		this.dateDebutEncheres = dateDebutEncheres;
	}

	public LocalDate getDateFinEncheres() {
		return dateFinEncheres;
	}

	public void setDateFinEncheres(LocalDate dateFinEncheres) {
		this.dateFinEncheres = dateFinEncheres;
	}

	public Float getPrixInitial() {
		return prixInitial;
	}

	public void setPrixInitial(Float prixInitial) {
		this.prixInitial = prixInitial;
	}

	public Float getPrixVente() {
		return prixVente;
	}

	public void setPrixVente(Float prixVente) {
		this.prixVente = prixVente;
	}

	public String getEtatVente() {
		return etatVente;
	}

	public void setEtatVente(String etatVente) {
		this.etatVente = etatVente;
	}

	public Categories getCategorie() {
		return categorie;
	}

	public void setCategorie(Categories categorie) {
		this.categorie = categorie;
	}

	public Utilisateur getUtilisateur() {
		return utilisateur;
	}

	public void setUtilisateur(Utilisateur vendeur) {
		this.utilisateur = vendeur;
	}

	public Retrait getRetrait() {
		return retrait;
	}

	public void setRetrait(Retrait retrait) {
		this.retrait = retrait;
	}

	public List<Enchere> getEncheres() {
		return encheres;
	}

	public void setEncheres(List<Enchere> encheres) {
		this.encheres = encheres;
	}
	
	  public void ajouterEnchere(Enchere enchere) {
	        this.encheres.add(enchere); 
	    }

	@Override
	public String toString() {
		return "ArticleVendu [noArticle=" + noArticle + ", nomArticle=" + nomArticle + ", description=" + description
				+ ", dateDebutEncheres=" + dateDebutEncheres + ", dateFinEncheres=" + dateFinEncheres + ", prixInitial="
				+ prixInitial + ", prixVente=" + prixVente + ", etatVente=" + etatVente + ", categorie=" + categorie
				+ "]";
	}

}
