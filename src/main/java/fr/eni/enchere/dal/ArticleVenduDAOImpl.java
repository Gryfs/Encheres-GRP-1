package fr.eni.enchere.dal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Categories;
import fr.eni.enchere.bo.Utilisateur;

@Repository
public class ArticleVenduDAOImpl implements ArticleVenduDAO {

	private final static String SELECT_ALL = "SELECT no_article, nom_article, description, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente, no_categorie, no_utilisateur FROM articles_vendus";
	private final static String CREATE = "INSERT INTO articles_vendus (nom_article, description, no_categorie, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente, no_utilisateur) VALUES (:nom_article, :description, :no_categorie, :date_debut_encheres, :date_fin_encheres, :prix_initial, :prix_vente, :no_utilisateur)";
	private final static String SELECT_ALL_BY_CATEGORIE = "SELECT no_article, nom_article, description, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente, no_categorie, no_utilisateur FROM articles_vendus WHERE no_categorie = :no_categorie";
	private final static String SELECT_BY_NOM = "SELECT no_article, nom_article, description, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente, no_categorie, no_utilisateur FROM articles_vendus WHERE LOWER(nom_article) LIKE LOWER(:search)";
	private final static String SELECT_BY_ID = "SELECT no_article, nom_article, description, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente, no_categorie, no_utilisateur FROM articles_vendus WHERE no_article=:no_article";
	private final static String UPDATE_PRIX = "UPDATE articles_vendus SET prix_vente = :nouveau_prix WHERE no_article = :no_article";
	private final static String UPDATE_ARTICLE = "UPDATE articles_vendus SET nom_article = :nom_article, description = :description, no_categorie = :no_categorie, date_debut_encheres = :date_debut_encheres, date_fin_encheres = :date_fin_encheres, prix_initial = :prix_initial	WHERE no_article = :no_article";
	private final static String SELECT_ALL_BY_ID_UTILISATEUR = "SELECT no_article, nom_article, description, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente, no_categorie, no_utilisateur FROM articles_vendus WHERE no_utilisateur=:no_utilisateur";
	
	
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public ArticleVenduDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	@Override
	public List<ArticleVendu> findAll() {

		return namedParameterJdbcTemplate.query(SELECT_ALL, new ArticleRowMapper());
	}

	@Override
	public ArticleVendu rechercherArticleParId(long id) {

		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("no_article", id);

		return namedParameterJdbcTemplate.queryForObject(SELECT_BY_ID, namedParameters, new ArticleRowMapper());
	}

	@Override
	public List<ArticleVendu> findAllByCategorie(Integer id) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("no_categorie", id);

		return namedParameterJdbcTemplate.query(SELECT_ALL_BY_CATEGORIE, namedParameters, new ArticleRowMapper());
	}
	
	@Override
	public List<ArticleVendu> findAllByUser(long id) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("no_utilisateur", id);

		return namedParameterJdbcTemplate.query(SELECT_ALL_BY_ID_UTILISATEUR, namedParameters, new ArticleRowMapper());
	}

	@Override
	public List<ArticleVendu> rechercherArticlesParNom(String search) {

		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("search", "%" + search + "%"); // Recherche partielle avec LIKE

		return namedParameterJdbcTemplate.query(SELECT_BY_NOM, namedParameters, new ArticleRowMapper());
	}

	@Override
	public void create(ArticleVendu article) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("nom_article", article.getNomArticle());
		namedParameters.addValue("description", article.getDescription());
		namedParameters.addValue("no_categorie", article.getCategorie().getId());
		namedParameters.addValue("date_debut_encheres", article.getDateDebutEncheres());
		namedParameters.addValue("date_fin_encheres", article.getDateFinEncheres());
		namedParameters.addValue("prix_initial", article.getPrixInitial());
		namedParameters.addValue("prix_vente", article.getPrixInitial());
		namedParameters.addValue("no_utilisateur", article.getUtilisateur().getNoUtilisateur());

		KeyHolder keyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update(CREATE, namedParameters, keyHolder, new String[] { "no_article" });

		if (keyHolder != null && keyHolder.getKey() != null) {
			article.setNoArticle(keyHolder.getKey().longValue());

			// Insertion dans la table RETRAITS
			String insertRetrait = "INSERT INTO RETRAITS (no_article, rue, code_postal, ville) VALUES (:no_article, :rue, :code_postal, :ville)";
			MapSqlParameterSource retraitParameters = new MapSqlParameterSource();
			retraitParameters.addValue("no_article", article.getNoArticle());
			retraitParameters.addValue("rue", article.getRetrait().getRue());
			retraitParameters.addValue("code_postal", article.getRetrait().getCodePostal());
			retraitParameters.addValue("ville", article.getRetrait().getVille());

			namedParameterJdbcTemplate.update(insertRetrait, retraitParameters);

		}
	}

	@Override
	public void updatePrixVente(ArticleVendu article, Float nouveauPrix) {

		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("nouveau_prix", nouveauPrix);
		namedParameters.addValue("no_article", article.getNoArticle());

		namedParameterJdbcTemplate.update(UPDATE_PRIX, namedParameters);
	}

	@Override
	public void updateArticle(ArticleVendu article) {

		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("nom_article", article.getNomArticle());
		namedParameters.addValue("description", article.getDescription());
		namedParameters.addValue("no_categorie", article.getCategorie().getId());
		namedParameters.addValue("date_debut_encheres", article.getDateDebutEncheres());
		namedParameters.addValue("date_fin_encheres", article.getDateFinEncheres());
		namedParameters.addValue("prix_initial", article.getPrixInitial());
		namedParameters.addValue("no_article", article.getNoArticle());

		namedParameterJdbcTemplate.update(UPDATE_ARTICLE, namedParameters);
	}

}

class ArticleRowMapper implements RowMapper<ArticleVendu> {

	@Override
	public ArticleVendu mapRow(ResultSet rs, int rowNum) throws SQLException {
		ArticleVendu article = new ArticleVendu();

		article.setNoArticle(rs.getInt("no_article"));
		article.setNomArticle(rs.getString("nom_article"));
		article.setDescription(rs.getString("description"));

		// ✅ Gestion du LocalDate
		article.setDateDebutEncheres(rs.getObject("date_debut_encheres", LocalDate.class));
		article.setDateFinEncheres(rs.getObject("date_fin_encheres", LocalDate.class));

		article.setPrixInitial(rs.getFloat("prix_initial"));
		article.setPrixVente(rs.getFloat("prix_vente"));

		Categories categorie = new Categories();
		categorie.setId(rs.getInt("no_categorie"));
		article.setCategorie(categorie);

		Utilisateur utilisateur = new Utilisateur();
		utilisateur.setNoUtilisateur(rs.getInt("no_utilisateur"));
		article.setUtilisateur(utilisateur);

		return article;
	}
}
