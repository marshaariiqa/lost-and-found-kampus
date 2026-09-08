package lostfound.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Boleh 'nama' di JSON/Form, di Java memakai standar camelCase 'name'

    @Column(nullable = false, unique = true)
    private String email;

    private String password; // Nullable untuk login Google

    private String noHp; // Ditambahkan sesuai ERD

    @Enumerated(EnumType.STRING)
    private Role role; // ROLE_ADMIN / ROLE_USER (ROLE_USER = Mahasiswa)

    @Enumerated(EnumType.STRING)
    private AuthProvider provider; // LOCAL / GOOGLE

    private String googleId;
    private String avatarUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum AuthProvider { LOCAL, GOOGLE }
    public enum Role { ROLE_ADMIN, ROLE_USER }

    public User() {}

    public User(String name, String email, String password, String noHp, Role role, AuthProvider provider) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.noHp = noHp;
        this.role = role;
        this.provider = provider;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public AuthProvider getProvider() { return provider; }
    public void setProvider(AuthProvider provider) { this.provider = provider; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}