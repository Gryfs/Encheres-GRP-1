package fr.eni.enchere.dal;

import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import fr.eni.enchere.bo.ArticleVendu;


public class ArticleVenduDAOImpl implements ArticleVenduDAO {
	
	private final static String SELECT_ALL = "SELECT no_article, nom_article, annee, duree, synopsis, id_realisateur, id_genre FROM FILM";
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public ArticleVenduDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}
	
	@Override
	public List<ArticleVendu> findAll() {
		return null;
		//return namedParameterJdbcTemplate.query(SELECT_ALL, new FilmRowMapper());
	}



}
