package fr.eni.enchere.bo;

import java.util.ArrayList;
import java.util.List;

public class Categories {

	private int numCategorie;
	private String titre;
	private List<ArticleVendu> articles = new ArrayList<>();

	public Categories() {
	}

	public Categories(int numCategorie, String titre) {
		this.numCategorie = numCategorie;
		this.titre = titre;
	}

	public int getNumCategorie() {
		return numCategorie;
	}

	public void setNumCategorie(int numCategorie) {
		this.numCategorie = numCategorie;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public List<ArticleVendu> getArticles() {
		return articles;
	}

	public void setArticles(List<ArticleVendu> articles) {
		this.articles = articles;
	}

	@Override
	public String toString() {
		return "Categories [numCategorie=" + numCategorie + ", titre=" + titre + "]";
	}

}
