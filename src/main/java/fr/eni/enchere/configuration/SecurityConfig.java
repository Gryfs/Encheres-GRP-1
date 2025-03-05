package fr.eni.enchere.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.util.unit.DataSize;
import org.springframework.web.filter.GenericFilterBean;
import fr.eni.enchere.security.CustomUserDetails;

import fr.eni.enchere.bll.ContexteService;
import fr.eni.enchere.bo.Utilisateur;
import jakarta.servlet.FilterChain;
import jakarta.servlet.MultipartConfigElement;
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
                .requestMatchers(HttpMethod.POST, "/article/delete").authenticated()
                .requestMatchers("/credits/checkout").authenticated()
                .requestMatchers("/credits/success").authenticated()
                .requestMatchers("/credits/cancel").authenticated()                
                .requestMatchers(HttpMethod.GET, "/detail/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/encherir/*").authenticated()
                .requestMatchers(HttpMethod.GET, "/creer").authenticated()
                .requestMatchers(HttpMethod.POST, "/creer").authenticated()
                .requestMatchers(HttpMethod.GET, "/article/edit/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/artcile/edit/*").authenticated()
                .anyRequest().denyAll()
            )
            .userDetailsService(userDetailsService())
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/session", true)
                .failureHandler((request, response, exception) -> {
                    String errorMessage = "Nom d'utilisateur ou mot de passe incorrect.";
                    if (exception.getMessage().contains("désactivé")) {
                        errorMessage = "Compte désactivé. Veuillez contacter l'administrateur.";
                    }
                    request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception);
                    response.sendRedirect("/login?error=true&message=" + errorMessage);
                })
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
                .invalidSessionUrl("/login")
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

                if (!utilisateur.isActif()) {
                    throw new UsernameNotFoundException("Compte désactivé. Veuillez contacter l'administrateur.");
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
            "select email, mot_de_passe, actif from UTILISATEURS where email = ?"
        );
        
        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery(
            "SELECT email, CASE WHEN administrateur = 1 THEN 'ROLE_ADMIN' ELSE 'ROLE_USER' END as role " +
            "FROM UTILISATEURS WHERE email = ?"
        );
        
        return jdbcUserDetailsManager;
    }

    @Configuration
    public static class MultipartConfig {
        @Bean
        public MultipartConfigElement multipartConfigElement() {
            MultipartConfigFactory factory = new MultipartConfigFactory();
            factory.setMaxFileSize(DataSize.ofMegabytes(5));
            factory.setMaxRequestSize(DataSize.ofMegabytes(5));
            return factory.createMultipartConfig();
        }
    }
}
