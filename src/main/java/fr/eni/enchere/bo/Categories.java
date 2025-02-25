package fr.eni.enchere.bo;

import java.util.ArrayList;
import java.util.List;

public class Categories {

	private int id;
	private String libelle;
	private List<ArticleVendu> articles = new ArrayList<>();

	public Categories() {
	}

	public Categories(int id, String titre) {
		this.id = id;
		this.libelle = titre;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public List<ArticleVendu> getArticles() {
		return articles;
	}

	public void setArticles(List<ArticleVendu> articles) {
		this.articles = articles;
	}

	@Override
	public String toString() {
		return "Categories [noCategorie=" + id + ", libelle=" + libelle + "]";
	}

}
