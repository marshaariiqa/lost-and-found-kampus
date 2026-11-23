package lostfound.config;

import lostfound.service.CustomOAuth2UserService;
import lostfound.service.PenggunaDetailsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private PenggunaDetailsService penggunaDetailsService;

    @Autowired
    private LoginSuccessHandler loginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(penggunaDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/register/sukses",
                    "/css/**", "/js/**", "/images/**", "/uploads/barang/**",
                    "/favicon.ico", "/error").permitAll()

                // Endpoint arsip hanya untuk admin
                .requestMatchers(
                    "/api/kehilangan/arsip/**",
                    "/api/temuan/arsip/**"
                ).hasRole("ADMIN")

                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Halaman mahasiswa
                .requestMatchers("/barang/**", "/lapor/**", "/laporan/**",
                    "/klaim/**", "/notifikasi/**").hasRole("MAHASISWA")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(loginSuccessHandler)
                .permitAll()
            )

            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(loginSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauthException
                            && "invalid_email".equals(
                                oauthException.getError().getErrorCode())) {

                        response.sendRedirect("/login?oauth2Error=invalid_email");

                    } else if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauthException2
                            && "account_disabled".equals(
                                oauthException2.getError().getErrorCode())) {

                        response.sendRedirect("/login?oauth2Error=disabled");

                    } else {
                        response.sendRedirect("/login?oauth2Error=failed");
                    }
                })
            )

            .sessionManagement(session -> session
                // Sesi habis (cookie lama tidak valid lagi) -> login dengan pesan jelas
                .invalidSessionUrl("/login?expired")
            )

            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                // Hapus cookie lama agar tidak dikira "sesi berakhir" setelah keluar
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}