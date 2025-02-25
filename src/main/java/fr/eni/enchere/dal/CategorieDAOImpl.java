package fr.eni.enchere.dal;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fr.eni.enchere.bo.Categories;

@Repository
public class CategorieDAOImpl implements CategorieDAO {

	
	private final static String SELECT_ALL = "SELECT no_categorie, libelle FROM categories";
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public CategorieDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	@Override
	public List<Categories> findAll() {
		return namedParameterJdbcTemplate.query(SELECT_ALL, new BeanPropertyRowMapper<Categories>(Categories.class));
	}

}
