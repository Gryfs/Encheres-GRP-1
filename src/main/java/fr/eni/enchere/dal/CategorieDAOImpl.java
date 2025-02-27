package fr.eni.enchere.dal;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fr.eni.enchere.bo.Categories;

@Repository
public class CategorieDAOImpl implements CategorieDAO {

	private final static String SELECT_ALL = "SELECT id, libelle FROM categories";
	private final static String SELECT_BY_ID = "SELECT id, libelle FROM categories WHERE id=:id";
	private final static String INSERT = "INSERT INTO categories (libelle) VALUES (:libelle)";
    private final static String UPDATE = "UPDATE categories SET libelle=:libelle WHERE id=:id";
    private final static String DELETE = "DELETE FROM categories WHERE id=:id";
	

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public CategorieDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	@Override
	public List<Categories> findAll() {
		List<Categories> categories = namedParameterJdbcTemplate.query(SELECT_ALL,
				new BeanPropertyRowMapper<Categories>(Categories.class));
		return categories;
	}

	@Override
	public Categories read(long id) {

		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("id", id);

		return namedParameterJdbcTemplate.queryForObject(SELECT_BY_ID, namedParameters,
				new BeanPropertyRowMapper<Categories>(Categories.class));
	}

	@Override
    public void create(Categories categorie) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("libelle", categorie.getLibelle());
        
        namedParameterJdbcTemplate.update(INSERT, namedParameters);
    }

    @Override
    public void update(Categories categorie) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("id", categorie.getId());
        namedParameters.addValue("libelle", categorie.getLibelle());
        
        namedParameterJdbcTemplate.update(UPDATE, namedParameters);
    }

    @Override
    public void delete(long id) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("id", id);
        
        namedParameterJdbcTemplate.update(DELETE, namedParameters);
    }
}
