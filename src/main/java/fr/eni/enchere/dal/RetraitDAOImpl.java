package fr.eni.enchere.dal;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import fr.eni.enchere.bo.Retrait;

@Repository
public class RetraitDAOImpl implements RetraitDAO {

	private final static String SELECT_BY_ID = "SELECT rue, code_postal, ville FROM retraits WHERE no_article=:no_article";

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public RetraitDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	@Override
	public Retrait consulterRetraitParIdarticle(long id) {

		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("no_article", id);

		try {
			// Tente de récupérer le retrait existant
			return namedParameterJdbcTemplate.queryForObject(SELECT_BY_ID, namedParameters,
					new BeanPropertyRowMapper<>(Retrait.class));
		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
			// Si aucun retrait n'est trouvé, créez un nouveau retrait vide
			System.out.println("Aucun retrait trouvé pour l'article " + id + ". Création d'un nouveau retrait vide.");
			Retrait retrait = new Retrait();
			retrait.setNoArticle(id);
			return retrait;
		}
	}
}
