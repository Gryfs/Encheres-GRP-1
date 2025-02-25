package fr.eni.enchere.dal;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fr.eni.enchere.bo.ArticleVendu;

@Repository
public class ArticleVenduDAOImpl implements ArticleVenduDAO {

	private final static String SELECT_ALL = "SELECT no_article, nom_article, description, date_debut_encheres, date_fin_encheres, prix_initial, prix_vente FROM articles_vendus";
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public ArticleVenduDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	@Override
	public List<ArticleVendu> findAll() {

		return namedParameterJdbcTemplate.query(SELECT_ALL,
				new BeanPropertyRowMapper<ArticleVendu>(ArticleVendu.class));
	}

}
