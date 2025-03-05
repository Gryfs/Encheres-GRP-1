package fr.eni.enchere.dal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Categories;
import fr.eni.enchere.bo.Enchere;
import fr.eni.enchere.bo.Utilisateur;

@Repository
public class ArticleVenduDAOImpl implements ArticleVenduDAO {

	private final static String SELECT_ALL = "SELECT no_article, nom_article, description, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente, no_categorie, no_utilisateur, image, etat_vente FROM articles_vendus";
	private final static String CREATE = "INSERT INTO articles_vendus (nom_article, description, no_categorie, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente, no_utilisateur, image) VALUES (:nom_article, :description, :no_categorie, :date_debut_encheres, :date_fin_encheres, :prix_initial, :prix_vente, :no_utilisateur, :image)";
	private final static String SELECT_ALL_BY_CATEGORIE = "SELECT no_article, nom_article, description, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente, no_categorie, no_utilisateur, image, etat_vente FROM articles_vendus WHERE no_categorie = :no_categorie";
	private final static String SELECT_ALL_BY_CATEGORIE_UTILISATEUR = "SELECT no_article, nom_article, description, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente, no_categorie, no_utilisateur, image, etat_vente FROM articles_vendus WHERE no_categorie = :no_categorie AND no_utilisateur = :no_utilisateur";
	private final static String SELECT_BY_NOM = "SELECT no_article, nom_article, description, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente, no_categorie, no_utilisateur, image, etat_vente FROM articles_vendus WHERE LOWER(nom_article) LIKE LOWER(:search)";
	private final static String SELECT_BY_NOM_UTILISATEUR = "SELECT no_article, nom_article, description, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente, no_categorie, no_utilisateur, image, etat_vente FROM articles_vendus WHERE LOWER(nom_article) LIKE LOWER(:search) AND no_utilisateur = :no_utilisateur";
	private final static String SELECT_BY_ID = "SELECT no_article, nom_article, description, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente, no_categorie, no_utilisateur, image, etat_vente FROM articles_vendus WHERE no_article=:no_article";
	private final static String UPDATE_ARTICLE = "UPDATE articles_vendus SET nom_article = :nom_article, description = :description, no_categorie = :no_categorie, date_debut_encheres = :date_debut_encheres, date_fin_encheres = :date_fin_encheres, prix_initial = :prix_initial, image = :image WHERE no_article = :no_article";
	private final static String UPDATE_PRIX = "UPDATE articles_vendus SET prix_vente = :nouveau_prix WHERE no_article = :no_article";
	private final static String SELECT_ALL_BY_ID_UTILISATEUR = "SELECT no_article, nom_article, description, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente, no_categorie, no_utilisateur, image, etat_vente FROM articles_vendus WHERE no_utilisateur=:no_utilisateur";
	private final static String DELETE_ARTICLE = "DELETE FROM articles_vendus WHERE no_article = :no_article";
	private final static String DELETE_RETRAIT = "DELETE FROM retraits WHERE no_article = :no_article";
	private final static String DELETE_ENCHERES = "DELETE FROM encheres WHERE no_article = :no_article";
	private final static String SELECT_EXPIRED_AUCTIONS = "SELECT av.no_article, av.nom_article, av.description, av.date_debut_encheres, av.date_fin_encheres, av.prix_initial, av.prix_vente, av.no_categorie, av.no_utilisateur, av.image, av.etat_vente, e.no_enchere, e.date_enchere, e.montant_enchere, e.no_utilisateur AS enchere_utilisateur FROM articles_vendus av LEFT JOIN encheres e ON av.no_article = e.no_article WHERE av.date_fin_encheres <= :today";
	private final static String CLOSE_VENTE = "UPDATE articles_vendus SET etat_vente = 'CLOSE' WHERE no_article = :no_article";
	private final static String SELECT_BY_RECHERCHE_AND_USER = "SELECT DISTINCT av.* FROM articles_vendus av LEFT JOIN encheres e ON av.no_article = e.no_article WHERE e.no_utilisateur = :idUtilisateur OR LOWER(av.nom_article) LIKE LOWER(:nomRecherche)";
	private final static String SELECT_ALL_BY_IDS = "SELECT * FROM articles_vendus WHERE no_article IN (:articleIds)";
	private final static String SELECT_ALL_BY_IDS_SEARCH = "SELECT * FROM articles_vendus WHERE no_article IN (:articleIds) AND LOWER(nom_article) LIKE LOWER(:nomRecherche)";
	private final static String SELECT_ARTICLE_GAGNE = "SELECT DISTINCT av.* FROM articles_vendus av JOIN encheres e ON av.no_article = e.no_article WHERE av.etat_vente = 'CLOSE' AND e.no_utilisateur = :idUtilisateur AND av.date_fin_encheres <= :today AND e.date_enchere = (SELECT MAX(e2.date_enchere) FROM encheres e2 WHERE e2.no_article = av.no_article)";
	private final static String SELECT_ARTICLE_GAGNE_SEARCH = "SELECT DISTINCT av.* " + "FROM articles_vendus av "
			+ "JOIN encheres e ON av.no_article = e.no_article " + "WHERE av.etat_vente = 'CLOSE' "
			+ "AND e.no_utilisateur = :idUtilisateur " + "AND av.date_fin_encheres <= :today "
			+ "AND e.date_enchere = (" + "   SELECT MAX(e2.date_enchere) " + "   FROM encheres e2 "
			+ "   WHERE e2.no_article = av.no_article) " + "AND LOWER(av.nom_article) LIKE LOWER(:nomRecherche)";

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
	public List<ArticleVendu> findArticlesByIds(Set<Long> articleIds) {

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("articleIds", articleIds);

		return namedParameterJdbcTemplate.query(SELECT_ALL_BY_IDS, params, new ArticleRowMapper());
	}

	@Override
	public List<ArticleVendu> findArticlesByIdsAndNom(Set<Long> articleIds, String nomRecherche) {

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("articleIds", articleIds);
		params.addValue("nomRecherche", "%" + nomRecherche + "%");

		return namedParameterJdbcTemplate.query(SELECT_ALL_BY_IDS_SEARCH, params, new ArticleRowMapper());
	}

	@Override
	public List<ArticleVendu> findAllByCategorie(Integer id) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("no_categorie", id);

		return namedParameterJdbcTemplate.query(SELECT_ALL_BY_CATEGORIE, namedParameters, new ArticleRowMapper());
	}

	@Override
	public List<ArticleVendu> findAllByCategorieUtilisateur(Integer id, Utilisateur utilisateur) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("no_categorie", id);
		namedParameters.addValue("no_utilisateur", utilisateur.getNoUtilisateur());

		return namedParameterJdbcTemplate.query(SELECT_ALL_BY_CATEGORIE_UTILISATEUR, namedParameters,
				new ArticleRowMapper());
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

	public interface ArticleVenduDAO {
		List<ArticleVendu> findArticlesByEncheresAndNom(long idUtilisateur, String nomRecherche);
	}

	@Override
	public List<ArticleVendu> findArticlesByEncheresAndNom(long idUtilisateur, String nomRecherche) {

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("idUtilisateur", idUtilisateur);
		params.addValue("nomRecherche", "%" + nomRecherche + "%");

		return namedParameterJdbcTemplate.query(SELECT_BY_RECHERCHE_AND_USER, params, new ArticleRowMapper());
	}

	@Override
	public List<ArticleVendu> rechercherArticlesParNomEtUtilisateur(String search, Utilisateur utilisateur) {

		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("search", "%" + search + "%"); // Recherche partielle avec LIKE
		namedParameters.addValue("no_utilisateur", utilisateur.getNoUtilisateur());

		return namedParameterJdbcTemplate.query(SELECT_BY_NOM_UTILISATEUR, namedParameters, new ArticleRowMapper());
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
		namedParameters.addValue("image", article.getImage());

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
		namedParameters.addValue("image", article.getImage());

		namedParameterJdbcTemplate.update(UPDATE_ARTICLE, namedParameters);
	}

	@Override
	public void deleteArticle(ArticleVendu article) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("no_article", article.getNoArticle());

		// Supprimer d'abord les dépendances
		namedParameterJdbcTemplate.update(DELETE_RETRAIT, namedParameters);
		namedParameterJdbcTemplate.update(DELETE_ENCHERES, namedParameters);

		// Puis supprimer l'article
		namedParameterJdbcTemplate.update(DELETE_ARTICLE, namedParameters);
	}

	@Override
	public List<ArticleVendu> findExpiredAuctions(LocalDate today) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("today", today);

		// Exécuter la requête et mapper les résultats avec ExpiredAuctionRowMapper
		return namedParameterJdbcTemplate.query(SELECT_EXPIRED_AUCTIONS, params, new ExpiredAuctionRowMapper());
	}

	@Override
	public List<ArticleVendu> findArticlesGagnesByUtilisateur(long idUtilisateur, LocalDate today) {

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("idUtilisateur", idUtilisateur);
		params.addValue("today", today);

		return namedParameterJdbcTemplate.query(SELECT_ARTICLE_GAGNE, params, new ArticleRowMapper());
	}

	@Override
	public List<ArticleVendu> findArticlesGagnesByUtilisateurAndNom(long idUtilisateur, LocalDate today,
			String nomRecherche) {

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("idUtilisateur", idUtilisateur);
		params.addValue("today", today);
		params.addValue("nomRecherche", "%" + nomRecherche + "%");

		return namedParameterJdbcTemplate.query(SELECT_ARTICLE_GAGNE_SEARCH, params, new ArticleRowMapper());
	}

	@Override
	public void updateEtatVente(long idArticle) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("no_article", idArticle);

		namedParameterJdbcTemplate.update(CLOSE_VENTE, namedParameters);
	}

}

class ArticleRowMapper implements RowMapper<ArticleVendu> {

	private final Map<Integer, ArticleVendu> articlesMap = new HashMap<>();

	@Override
	public ArticleVendu mapRow(ResultSet rs, int rowNum) throws SQLException {
		int articleId = rs.getInt("no_article");

		// Vérifier si l'article existe déjà dans la map
		ArticleVendu article = articlesMap.get(articleId);
		if (article == null) {
			article = new ArticleVendu();
			article.setNoArticle(articleId);
			article.setNomArticle(rs.getString("nom_article"));
			article.setDescription(rs.getString("description"));
			article.setDateDebutEncheres(rs.getObject("date_debut_encheres", LocalDate.class));
			article.setDateFinEncheres(rs.getObject("date_fin_encheres", LocalDate.class));
			article.setPrixInitial(rs.getFloat("prix_initial"));
			article.setPrixVente(rs.getFloat("prix_vente"));
			article.setEtatVente(rs.getString("etat_vente"));

			Categories categorie = new Categories();
			categorie.setId(rs.getInt("no_categorie"));
			article.setCategorie(categorie);

			Utilisateur utilisateur = new Utilisateur();
			utilisateur.setNoUtilisateur(rs.getInt("no_utilisateur"));
			article.setUtilisateur(utilisateur);
			article.setImage(rs.getString("image"));

			// Initialisation de la liste d'enchères
			article.setEncheres(new ArrayList<>());

			// Ajouter l'article à la map
			articlesMap.put(articleId, article);
		}

		return article;
	}
}

class ExpiredAuctionRowMapper implements RowMapper<ArticleVendu> {

	private final Map<Integer, ArticleVendu> articlesMap = new HashMap<>();

	@Override
	public ArticleVendu mapRow(ResultSet rs, int rowNum) throws SQLException {
		int articleId = rs.getInt("no_article");

		// Vérifier si l'article existe déjà dans la map
		ArticleVendu article = articlesMap.get(articleId);
		if (article == null) {
			article = new ArticleVendu();
			article.setNoArticle(articleId);
			article.setNomArticle(rs.getString("nom_article"));
			article.setDescription(rs.getString("description"));
			article.setDateDebutEncheres(rs.getObject("date_debut_encheres", LocalDate.class));
			article.setDateFinEncheres(rs.getObject("date_fin_encheres", LocalDate.class));
			article.setPrixInitial(rs.getFloat("prix_initial"));
			article.setPrixVente(rs.getFloat("prix_vente"));
			article.setEtatVente(rs.getString("etat_vente"));

			Categories categorie = new Categories();
			categorie.setId(rs.getInt("no_categorie"));
			article.setCategorie(categorie);

			Utilisateur utilisateur = new Utilisateur();
			utilisateur.setNoUtilisateur(rs.getInt("no_utilisateur"));
			article.setUtilisateur(utilisateur);
			article.setImage(rs.getString("image"));

			// Initialisation de la liste d'enchères
			article.setEncheres(new ArrayList<>());

			// Ajouter l'article à la map
			articlesMap.put(articleId, article);
		}

		// Vérifier si une enchère est associée à cet article
		int noEnchere = rs.getInt("no_enchere");
		if (noEnchere > 0) { // Vérifier qu'il y a bien une enchère associée
			Enchere enchere = new Enchere();
			enchere.setNoEnchere(noEnchere);
			enchere.setDateEnchere(rs.getObject("date_enchere", LocalDate.class));
			enchere.setMontantEnchere(rs.getFloat("montant_enchere"));

			Utilisateur enchereUtilisateur = new Utilisateur();
			enchereUtilisateur.setNoUtilisateur(rs.getInt("enchere_utilisateur"));
			enchere.setUtilisateur(enchereUtilisateur);

			// Ajouter l'enchère à la liste des enchères de l'article
			article.getEncheres().add(enchere);
		}

		return article;
	}
}
