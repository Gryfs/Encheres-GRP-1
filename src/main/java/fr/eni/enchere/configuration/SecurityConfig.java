package fr.eni.enchere.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.GenericFilterBean;
import fr.eni.enchere.security.CustomUserDetails;

import fr.eni.enchere.bll.ContexteService;
import fr.eni.enchere.bo.Utilisateur;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final ContexteService contexteService;
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(ContexteService contexteService) {
        this.contexteService = contexteService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin/categories/**").hasRole("ADMIN")
                .requestMatchers("/*").permitAll()
                .requestMatchers("/css/*").permitAll()
                .requestMatchers("/img/*").permitAll()
                .requestMatchers("/encheres").permitAll()
                .requestMatchers("/detail/*").permitAll()
                .requestMatchers("/article/edit").authenticated()
                .requestMatchers("/encherir/*").authenticated()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/logout").permitAll()
                .requestMatchers("/register").permitAll()
                .requestMatchers("/session").authenticated()
                .requestMatchers("/creer").authenticated()
                .requestMatchers("/profile").authenticated()
                .requestMatchers("/profile/edit").authenticated()
                .requestMatchers("/profile/delete").authenticated()
                .requestMatchers(HttpMethod.GET, "/detail/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/encherir/*").authenticated()
                .requestMatchers(HttpMethod.GET, "/creer").authenticated()
                .requestMatchers(HttpMethod.POST, "/creer").authenticated()
                .requestMatchers(HttpMethod.GET, "/article/edit/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/artcile/edit/*").authenticated()
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
            )
            .sessionManagement(session -> session
                .invalidSessionUrl("/login") // Redirection vers login en cas de session invalide
                .maximumSessions(1) // Limite à une session active par utilisateur
                .expiredUrl("/login?expired=true") // Redirection en cas d'expiration
                .and()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().migrateSession()
                .invalidSessionUrl("/login")
                .maximumSessions(1)
                .and()
                .sessionAuthenticationErrorUrl("/login")
                .sessionFixation().migrateSession()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            )
            .addFilterBefore(new SessionTimeoutFilter(), UsernamePasswordAuthenticationFilter.class);
            

        return http.build();
    }

    class SessionTimeoutFilter extends GenericFilterBean {
        private static final long MAX_INACTIVE_INTERVAL = 500; // 5 minutes en secondes

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            logger.debug(String.format("Vérification timeout session pour: %s", httpRequest.getRequestURI()));

            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpSession session = httpRequest.getSession(false);

            if (session != null) {
                long lastAccessedTime = session.getLastAccessedTime();
                long currentTime = System.currentTimeMillis();
                long elapsedTime = (currentTime - lastAccessedTime) / 1000; // Conversion en secondes

                if (elapsedTime > MAX_INACTIVE_INTERVAL) {
                    session.invalidate();
                    logger.info(String.format("Session expirée pour: %s", httpRequest.getRequestURI()));
                    ((HttpServletResponse) response).sendRedirect("/login?timeout=true");
                    return;
                }
            }

            chain.doFilter(request, response);
        }
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            try {
                Utilisateur utilisateur = contexteService.charger(username);
                if (utilisateur == null) {
                    throw new UsernameNotFoundException("Utilisateur non trouvé");
                }
    
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                if (utilisateur.isAdmin()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }
    
                return new CustomUserDetails(
                    utilisateur.getEmail(),
                    "{noop}" + utilisateur.getMotDePasse(),
                    authorities,
                    utilisateur.getNom(),
                    utilisateur.getPrenom()
                );
    
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        };
    }


    @Bean
    UserDetailsManager users(DataSource dataSource) {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);
    
        jdbcUserDetailsManager.setUsersByUsernameQuery(
            "select email, mot_de_passe, 'true' as enabled from UTILISATEURS where email = ?"
        );
        
        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery(
            "SELECT email, CASE WHEN administrateur = 1 THEN 'ROLE_ADMIN' ELSE 'ROLE_USER' END as role " +
            "FROM UTILISATEURS WHERE email = ?"
        );

        return jdbcUserDetailsManager;
    }


}

