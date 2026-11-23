package lostfound.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Setelah login: admin ke /admin, mahasiswa ke halaman yang tadi dituju
 * (atau /dashboard). Menyimpan pesan toast "Berhasil masuk" di sesi,
 * yang ditampilkan satu kali oleh GlobalModelAdvice.
 */
@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public static final String ATTR_TOAST = "FINDUP_TOAST_SUKSES";

    public LoginSuccessHandler() {
        setDefaultTargetUrl("/dashboard");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        request.getSession().setAttribute(ATTR_TOAST, "Berhasil masuk.");

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (admin) {
            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, "/admin");
            return;
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
