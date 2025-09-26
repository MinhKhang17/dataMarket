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


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "token", referencedColumnName = "id")
    private Token token;

    @Column(nullable = true)
    private String Provider;
    @Column(nullable = true)
    private String Provider_id;

    @Column(nullable = true)
    private UserStatus userStatus;
}
