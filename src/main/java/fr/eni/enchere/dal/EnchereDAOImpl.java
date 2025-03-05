package fr.eni.enchere.dal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import fr.eni.enchere.bo.ArticleVendu;
import fr.eni.enchere.bo.Categories;
import fr.eni.enchere.bo.Enchere;
import fr.eni.enchere.bo.Utilisateur;

@Repository
public class EnchereDAOImpl implements EnchereDAO {

	private final static String CREATE = "INSERT INTO ENCHERES (date_enchere, montant_enchere, no_utilisateur, no_article) VALUES (:date_enchere, :montant_enchere, :no_utilisateur, :no_article)";
	private final static String SELECT_BY_ID_ARTICLE = "SELECT no_enchere, date_enchere, montant_enchere, no_utilisateur, no_article FROM ENCHERES WHERE no_article = :no_article";
	private final static String SELECT_ALL_BY_UTILISATEUR = "SELECT no_enchere, date_enchere, montant_enchere, no_utilisateur, no_article FROM ENCHERES WHERE no_utilisateur = :no_utilisateur";
	private final static String DELETE = "DELETE FROM ENCHERES WHERE no_enchere = :no_enchere";
	private final static String SELECT_BY_ARTICLE = "SELECT no_enchere, date_enchere, montant_enchere, no_utilisateur, no_article FROM ENCHERES WHERE no_article = :no_article";
	
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public EnchereDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	@Override
	public void create(Enchere enchere) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("no_utilisateur", enchere.getUtilisateur().getNoUtilisateur());
		params.addValue("no_article", enchere.getArticle().getNoArticle());
		params.addValue("montant_enchere", enchere.getMontantEnchere());
		params.addValue("date_enchere", enchere.getDateEnchere());

		KeyHolder keyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update(CREATE, params, keyHolder, new String[] { "no_enchere" });

		if (keyHolder.getKey() != null) {
			enchere.setNoEnchere(keyHolder.getKey().longValue());
		}

	}

	@Override
	public List<Enchere> SelectEnchereByIdArticle(long id) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("no_article", id);

		return namedParameterJdbcTemplate.query(SELECT_BY_ID_ARTICLE, namedParameters, new EnchereRowMapper());
	}

	class EnchereRowMapper implements RowMapper<Enchere> {

		@Override
		public Enchere mapRow(ResultSet rs, int rowNum) throws SQLException {
			Enchere enchere = new Enchere();

			enchere.setNoEnchere(rs.getInt("no_enchere"));
			enchere.setDateEnchere(rs.getObject("date_enchere", LocalDate.class));
			enchere.setMontantEnchere(rs.getInt("montant_enchere"));

			ArticleVendu articleVendu = new ArticleVendu();
			articleVendu.setNoArticle(rs.getLong("no_article"));
			enchere.setArticle(articleVendu);

			Utilisateur utilisateur = new Utilisateur();
			utilisateur.setNoUtilisateur(rs.getInt("no_utilisateur"));
			enchere.setUtilisateur(utilisateur);

			return enchere;
		}
	}

	@Override
	public List<Enchere> findEncheresByUtilisateur(long idUtilisateur) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("no_utilisateur", idUtilisateur);

		return namedParameterJdbcTemplate.query(SELECT_ALL_BY_UTILISATEUR, namedParameters, new EnchereRowMapper());
	}

	@Override
	public void delete(long noEnchere) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("no_enchere", noEnchere);
		namedParameterJdbcTemplate.update(DELETE, params);
	}

	@Override
	public List<Enchere> findByArticle(long noArticle) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("no_article", noArticle);
		return namedParameterJdbcTemplate.query(SELECT_BY_ARTICLE, params, new EnchereRowMapper());
	}
	
}
