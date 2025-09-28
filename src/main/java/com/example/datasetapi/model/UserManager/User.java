package com.example.datasetapi.model.UserManager;

import com.example.datasetapi.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "User_Information")
@Data
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


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;


    @OneToOne(mappedBy = "user",orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private Token token;

    @Column(nullable = true)
    private String provider;
    @Column(nullable = true)
    private String provider_id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private UserStatus userStatus;

    public void setToken(Token token) {
        this.token = token;
        if (token != null) token.setUser(this);
    }
}
