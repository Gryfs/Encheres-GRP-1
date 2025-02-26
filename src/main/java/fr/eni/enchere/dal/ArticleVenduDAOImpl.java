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

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public ArticleVenduDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	@Override
	public List<ArticleVendu> findAll() {

		return namedParameterJdbcTemplate.query(SELECT_ALL, new ArticleRowMapper());
	}

	@Override
	public List<ArticleVendu> findAllByCategorie(Integer id) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("no_categorie", id);

		return namedParameterJdbcTemplate.query(SELECT_ALL_BY_CATEGORIE, namedParameters, new ArticleRowMapper());
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
		namedParameters.addValue("no_article", article.getNoArticle());
		namedParameters.addValue("nom_article", article.getNomArticle());
		namedParameters.addValue("description", article.getDescription());
		namedParameters.addValue("no_categorie", article.getCategorie().getId());
		namedParameters.addValue("date_debut_encheres", article.getDateDebutEncheres());
		namedParameters.addValue("date_fin_encheres", article.getDateFinEncheres());
		namedParameters.addValue("prix_initial", article.getPrixInitial());
		namedParameters.addValue("prix_vente", article.getPrixInitial());
		namedParameters.addValue("no_utilisateur", article.getUtilisateur().getNoUtilisateur());

		KeyHolder keyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update(CREATE, namedParameters);

		if (keyHolder != null && keyHolder.getKey() != null) {
			article.setNoArticle(keyHolder.getKey().longValue());
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

}
