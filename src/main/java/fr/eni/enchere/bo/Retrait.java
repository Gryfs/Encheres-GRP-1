package fr.eni.enchere.bo;

public class Retrait {

	private String rue;
	private String codePostal;
	private String ville;
	private long noArticle;
	private ArticleVendu article;

	public Retrait() {

	}

	public Retrait(String rue, String codePostal, String ville, long noArticle) {

		this.rue = rue;
		this.codePostal = codePostal;
		this.ville = ville;
		this.noArticle = noArticle;
	}

	public String getRue() {
		return rue;
	}

	public void setRue(String rue) {
		this.rue = rue;
	}

	public String getCodePostal() {
		return codePostal;
	}

	public void setCodePostal(String codePostal) {
		this.codePostal = codePostal;
	}

	public String getVille() {
		return ville;
	}

	public void setVille(String ville) {
		this.ville = ville;
	}

	public long getNoArticle() {
		return noArticle;
	}

	public void setNoArticle(long noArticle) {
		this.noArticle = noArticle;
	}

	@Override
	public String toString() {
		return "Retrait [rue=" + rue + ", codePostal=" + codePostal + ", ville=" + ville + ", article=" + noArticle
				+ "]";
	}

}
