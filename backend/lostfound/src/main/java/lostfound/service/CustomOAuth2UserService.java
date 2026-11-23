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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
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
        if (email == null ||
            !(email.toLowerCase().endsWith("@upnvj.ac.id") ||
            email.toLowerCase().endsWith("@mahasiswa.upnvj.ac.id"))) {

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

            if (!pengguna.isAktif()) {
                throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_disabled"),
                    "Akun ini dinonaktifkan oleh admin."
                );
            }

            // Nama hanya diisi dari Google bila belum ada, agar perubahan
            // nama dari halaman Pengaturan tidak tertimpa setiap login.
            if (pengguna.getNamaLengkap() == null || pengguna.getNamaLengkap().isBlank()) {
                pengguna.setNamaLengkap(name);
            }
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

        // Nama principal dibuat = email supaya Authentication.getName()
        // konsisten dengan login form (semua service memakai findByEmail).
        // Role dimasukkan agar aturan hasRole() pada SecurityConfig berlaku.
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + pengguna.getPeran().name()));

        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "email");
    }
}