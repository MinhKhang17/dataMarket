package com.example.datasetapi.model.userManager;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProviderRegisReviewHistory {
    @Id
    @GeneratedValue
    private Long id;

    private long providerRegistrationId;

    @ManyToOne
    @JoinColumn(name = "admin_id", referencedColumnName = "id")
    private User admin;
        //nếu như accept
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "provider_id", referencedColumnName = "id")
    private Provider provider;
    private boolean isAccept =false;

    private String reason;

    public ProviderRegisReviewHistory(User admin, Provider provider, boolean isAccept) {
    this.admin=admin;
    this.provider=provider;
    this.isAccept=isAccept;
    }

    public ProviderRegisReviewHistory(User admin, String reason, boolean isAccept) {
        this.admin=admin;
        this.isAccept=isAccept;
        this.reason = reason;
    }
}
