package fr.eni.enchere.dal;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import fr.eni.enchere.bo.Utilisateur;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Repository
public class UtilisateurDAOImpl implements UtilisateurDAO {
	private final static String SELECT_BY_ID = "SELECT no_utilisateur, pseudo, nom, prenom, email, telephone, rue, code_postal as codePostal, ville, mot_de_passe as motDePasse, credit, administrateur as admin, actif FROM UTILISATEURS WHERE no_utilisateur=:id";
	private final static String SELECT_BY_EMAIL = "SELECT no_utilisateur, pseudo, nom, prenom, email, telephone, rue, code_postal as codePostal, ville, mot_de_passe as motDePasse, credit, administrateur as admin, actif FROM UTILISATEURS WHERE email=:email";
	private final static String CREATE = "INSERT INTO UTILISATEURS (pseudo, nom, prenom, email, telephone, rue, code_postal, ville, mot_de_passe, credit, administrateur) VALUES (:pseudo, :nom, :prenom, :email, :telephone, :rue, :codePostal, :ville, :motDePasse, :credit, :administrateur)";
	private final static String COUNT_BY_EMAIL = "SELECT count(*) FROM UTILISATEURS WHERE email = :email";
	private final static String UPDATE = "UPDATE UTILISATEURS SET pseudo=:pseudo, nom=:nom, prenom=:prenom, email=:email, telephone=:telephone, rue=:rue, code_postal=:codePostal, ville=:ville, mot_de_passe=:motDePasse, actif=CAST(:actif AS BIT), reset_token=:resetToken, reset_token_expiry=:resetTokenExpiry WHERE no_utilisateur=:noUtilisateur";
    private final static String DELETE = "DELETE FROM UTILISATEURS WHERE no_utilisateur = :id";
	private static final String FIND_BY_RESET_TOKEN = "SELECT no_utilisateur, pseudo, nom, prenom, email, telephone, rue, code_postal, ville, mot_de_passe, credit, administrateur, actif, reset_token, reset_token_expiry FROM UTILISATEURS WHERE reset_token = :token";
    private static final String SOUSTRAIRE_CREDIT = "UPDATE UTILISATEURS SET credit = credit - :montant WHERE no_utilisateur = :id";
    private static final String AJOUTER_CREDIT = "UPDATE UTILISATEURS SET credit = credit + :montant WHERE no_utilisateur = :id";
	
	private static final Logger logger = LoggerFactory.getLogger(UtilisateurDAOImpl.class);

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
		logger.debug("Début création utilisateur en DB: {}", utilisateur.getEmail());

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

		try  {
			KeyHolder keyHolder = new GeneratedKeyHolder();
			namedParameterJdbcTemplate.update(CREATE, namedParameters, keyHolder);
			logger.info("Utilisateur créé en DB avec succès: {}", utilisateur.getEmail());

			if (keyHolder.getKeys() != null) {
				utilisateur.setNoUtilisateur(((Number) keyHolder.getKeys().get("no_utilisateur")).longValue());
			}
		} catch (Exception e) {
			logger.error("Erreur lors de la création de l'utilisateur: {}", e.getMessage(), e);
			throw e;
		}


	}
	
	@Override
	public int countByEmail(String email) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("email", email);

		return namedParameterJdbcTemplate.queryForObject(COUNT_BY_EMAIL, namedParameters, Integer.class);
	}

	@Override
    public void update(Utilisateur utilisateur) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("noUtilisateur", utilisateur.getNoUtilisateur());
        namedParameters.addValue("pseudo", utilisateur.getPseudo());
        namedParameters.addValue("nom", utilisateur.getNom());
        namedParameters.addValue("prenom", utilisateur.getPrenom());
        namedParameters.addValue("email", utilisateur.getEmail());
        namedParameters.addValue("telephone", utilisateur.getTelephone());
        namedParameters.addValue("rue", utilisateur.getRue());
        namedParameters.addValue("codePostal", utilisateur.getCodePostal());
        namedParameters.addValue("ville", utilisateur.getVille());
        namedParameters.addValue("motDePasse", utilisateur.getMotDePasse());
		namedParameters.addValue("actif", utilisateur.isActif() ? 1 : 0);
        namedParameters.addValue("resetToken", utilisateur.getResetToken());
		namedParameters.addValue("resetTokenExpiry", utilisateur.getResetTokenExpiry());

        namedParameterJdbcTemplate.update(UPDATE, namedParameters);
    }

	@Override
    public void delete(long id) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("id", id);
        namedParameterJdbcTemplate.update(DELETE, namedParameters);
    }

	@Override
	public Utilisateur findById(Integer id) {
		if (id == null) {
			return null;
		}
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue("id", id);

		try {
			return namedParameterJdbcTemplate.queryForObject(SELECT_BY_ID, namedParameters,
					new BeanPropertyRowMapper<>(Utilisateur.class));
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<Utilisateur> findAll() {
		final String SELECT_ALL = "SELECT no_utilisateur, pseudo, nom, prenom, email, telephone, rue, code_postal as codePostal, ville, mot_de_passe as motDePasse, credit, administrateur as admin, actif FROM UTILISATEURS";
		
		return namedParameterJdbcTemplate.query(SELECT_ALL, 
				new BeanPropertyRowMapper<>(Utilisateur.class));
	}

	@Override
    public Utilisateur findByResetToken(String token) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("token", token);
            
            return namedParameterJdbcTemplate.queryForObject(
                FIND_BY_RESET_TOKEN,
                namedParameters,
                new BeanPropertyRowMapper<>(Utilisateur.class)
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
	
	@Override
	public void retirerCredit(Utilisateur utilisateur, Float montant) {

	    MapSqlParameterSource namedParameters = new MapSqlParameterSource();
	    namedParameters.addValue("montant", montant);
	    namedParameters.addValue("id", utilisateur.getNoUtilisateur());

	    namedParameterJdbcTemplate.update(SOUSTRAIRE_CREDIT, namedParameters);


	    utilisateur.setCredit(utilisateur.getCredit() - montant);
	}

	@Override
	public void ajouterCredit(Utilisateur utilisateur, Float montant) {


	    MapSqlParameterSource namedParameters = new MapSqlParameterSource();
	    namedParameters.addValue("montant", montant);
	    namedParameters.addValue("id", utilisateur.getNoUtilisateur());

	    namedParameterJdbcTemplate.update(AJOUTER_CREDIT, namedParameters);


	    utilisateur.setCredit(utilisateur.getCredit() + montant);
		
	}


}
