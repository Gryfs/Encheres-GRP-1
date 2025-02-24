package fr.eni.enchere.dal;

import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import fr.eni.enchere.bo.ArticleVendu;

public class ArticleVenduDAOImpl implements ArticleVenduDAO {

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public ArticleVenduDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	@Override
	public List<ArticleVendu> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

}
