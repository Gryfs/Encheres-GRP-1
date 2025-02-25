package fr.eni.enchere.dal;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import fr.eni.enchere.bo.Utilisateur;

@Repository
public class UtilisateurDAOImpl implements UtilisateurDAO {
	private final static String SELECT_BY_ID = "SELECT no_utilisateur, pseudo, nom, prenom, email, telephone, rue, code_postal as codePostal, ville, mot_de_passe as motDePasse, credit, administrateur as admin FROM UTILISATEURS WHERE no_utilisateur=:id";
	private final static String SELECT_BY_EMAIL = "SELECT no_utilisateur, pseudo, nom, prenom, email, telephone, rue, code_postal as codePostal, ville, mot_de_passe as motDePasse, credit, administrateur as admin FROM UTILISATEURS WHERE email=:email";
	private final static String CREATE = "INSERT INTO UTILISATEURS (pseudo, nom, prenom, email, telephone, rue, code_postal, ville, mot_de_passe, credit, administrateur) VALUES (:pseudo, :nom, :prenom, :email, :telephone, :rue, :codePostal, :ville, :motDePasse, :credit, :administrateur)";
	private final static String COUNT_BY_EMAIL = "SELECT count(*) FROM UTILISATEURS WHERE email = :email";


	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	
	public UtilisateurDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	@Override
	public Utilisateur read(long id) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("id", id);

		return namedParameterJdbcTemplate.queryForObject(SELECT_BY_ID, namedParameters,
				new BeanPropertyRowMapper<Utilisateur>(Utilisateur.class));
	}

	@Override
	public Utilisateur read(String email) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("email", email);

		return namedParameterJdbcTemplate.queryForObject(SELECT_BY_EMAIL, namedParameters,
				new BeanPropertyRowMapper<Utilisateur>(Utilisateur.class));
	}

	@Override
public void create(Utilisateur utilisateur) {
    MapSqlParameterSource namedParameters = new MapSqlParameterSource();
    namedParameters.addValue("pseudo", utilisateur.getPseudo());
    namedParameters.addValue("nom", utilisateur.getNom());
    namedParameters.addValue("prenom", utilisateur.getPrenom());
    namedParameters.addValue("email", utilisateur.getEmail());
    namedParameters.addValue("telephone", utilisateur.getTelephone());
    namedParameters.addValue("rue", utilisateur.getRue());
    namedParameters.addValue("codePostal", utilisateur.getCodePostal());
    namedParameters.addValue("ville", utilisateur.getVille());
    namedParameters.addValue("motDePasse", utilisateur.getMotDePasse());
    namedParameters.addValue("credit", 0);
    namedParameters.addValue("administrateur", false);

    KeyHolder keyHolder = new GeneratedKeyHolder();
    namedParameterJdbcTemplate.update(CREATE, namedParameters, keyHolder);

    // Extraire la clé générée de manière appropriée
    if (keyHolder.getKeys() != null) {
        utilisateur.setNoUtilisateur(((Number) keyHolder.getKeys().get("no_utilisateur")).longValue());
    }
}
	
	@Override
	public int countByEmail(String email) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("email", email);

		return namedParameterJdbcTemplate.queryForObject(COUNT_BY_EMAIL, namedParameters, Integer.class);
	}

}
