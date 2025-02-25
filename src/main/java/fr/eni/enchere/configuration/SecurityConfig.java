package fr.eni.enchere.configuration;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import fr.eni.enchere.bll.ContexteService;
import fr.eni.enchere.bo.Utilisateur;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final ContexteService contexteService;

    public SecurityConfig(ContexteService contexteService) {
        this.contexteService = contexteService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authorize) -> authorize
            .requestMatchers("/*").permitAll()
            .requestMatchers("/css/*").permitAll()
            .requestMatchers("/img/*").permitAll()
            .requestMatchers("/articles").permitAll()
            .requestMatchers("/login").permitAll()
            .requestMatchers("/logout").permitAll()
            .requestMatchers("/register").permitAll()
            .requestMatchers("/session").authenticated()
            .requestMatchers("/creer").authenticated()
            .requestMatchers("/profile").authenticated()
            .requestMatchers("/profile/edit").authenticated()
            .requestMatchers("/profile/delete").authenticated()
            
            .requestMatchers(HttpMethod.GET, "/creer").authenticated()
            .requestMatchers(HttpMethod.POST, "/creer").authenticated()
            .anyRequest().denyAll()
            )
            .userDetailsService(userDetailsService())
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form
            .loginPage("/login")
            .usernameParameter("username")
            .passwordParameter("password")
            .defaultSuccessUrl("/session", true)
            .failureUrl("/login?error=true")
            .permitAll()
            )
            .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll()
            );

        return http.build();
    }

     @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Utilisateur utilisateur = contexteService.charger(username);
            if (utilisateur == null) {
                throw new UsernameNotFoundException("Utilisateur non trouvé");
            }
            return User.builder()
                .username(utilisateur.getEmail())
                .password("{noop}" + utilisateur.getMotDePasse()) // {noop} indique pas de chiffrement
                .roles("USER")
                .build();
        };
    }

    @Bean
    UserDetailsManager users(DataSource dataSource) {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);
    
        // Update query to use UTILISATEURS table
        jdbcUserDetailsManager.setUsersByUsernameQuery(
            "select email, mot_de_passe, 'true' as enabled from UTILISATEURS where email = ?"
        );
        
        // Update authorities query to use UTILISATEURS table
        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery(
            "SELECT email, CASE WHEN administrateur = 1 THEN 'ROLE_ADMIN' ELSE 'ROLE_USER' END as role " +
            "FROM UTILISATEURS WHERE email = ?"
        );

        return jdbcUserDetailsManager;
    }


}