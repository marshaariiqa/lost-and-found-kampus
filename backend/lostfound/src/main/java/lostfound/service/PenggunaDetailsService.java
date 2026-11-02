package lostfound.service;

import lostfound.model.Pengguna;
import lostfound.repository.PenggunaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PenggunaDetailsService implements UserDetailsService {

    @Autowired
    private PenggunaRepository penggunaRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Pengguna pengguna = penggunaRepository.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException("Email tidak ditemukan")
            );

        if (pengguna.getKataSandi() == null) {
            throw new UsernameNotFoundException(
                "Akun ini menggunakan login Google"
            );
        }

        return User.builder()
            .username(pengguna.getEmail())
            .password(pengguna.getKataSandi())
            .roles(pengguna.getPeran().name())
            .build();
    }
}