package com.example.datasetapi.model.UserManager;

import com.example.datasetapi.enums.UserStatus;
import com.example.datasetapi.model.Dataset.DownloadToken;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "User_Information")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false,unique = true)
    private String email;

    @OneToOne(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL, fetch =  FetchType.LAZY, optional = true)
    private Consumer consumer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "token_id")
    private Token token;

    @Column(nullable = true)
    private String Author;
    @Column(nullable = true)
    private String Auth_id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private UserStatus userStatus;

    private boolean isActive=false;

    @OneToMany
    private List<DownloadToken> downloadTokens;

    public void setToken(Token token) {
        this.token = token;
        if (token != null) token.setUser(this);
    }
}
