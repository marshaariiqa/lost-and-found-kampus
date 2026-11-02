package lostfound.service;

import lostfound.model.Pengguna;
import lostfound.model.PeranPengguna;
import lostfound.model.ProviderLogin;
import lostfound.repository.PenggunaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private PenggunaRepository penggunaRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");

        // Memastikan email berasal dari kampus
        if (email == null || !email.toLowerCase().endsWith("@upnvj.ac.id")) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_email"),
                "Login hanya diperbolehkan menggunakan email kampus UPNVJ."
            );
        }

        Optional<Pengguna> penggunaOptional =
                penggunaRepository.findByEmail(email);

        Pengguna pengguna;

        if (penggunaOptional.isPresent()) {
            // Jika akun sudah terdaftar
            pengguna = penggunaOptional.get();
            pengguna.setNamaLengkap(name);
            pengguna.setGoogleId(googleId);

        } else {
            // Jika pengguna baru login menggunakan Google
            pengguna = new Pengguna();

            pengguna.setNamaLengkap(name);
            pengguna.setEmail(email);
            pengguna.setGoogleId(googleId);
            pengguna.setPeran(PeranPengguna.MAHASISWA);
            pengguna.setProvider(ProviderLogin.GOOGLE);
        }

        penggunaRepository.save(pengguna);

        return oAuth2User;
    }
}