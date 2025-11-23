package com.example.datasetapi.model.dataset;

import com.example.datasetapi.model.userManager.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_access_tokens")
public class ApiAccessToken {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id; // dùng giống jti

    @Column(name = "token", length = 2000) // lưu token JWT (hoặc hash)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;


    @Column(name = "use_amount")
    private Long useAmount; // tổng lượt phép dùng (-1 = unlimited)

    @Column(name = "uses_count")
    private Integer usesCount = 0; // đã dùng bao nhiêu lần

    @Column(name = "revoked")
    private Boolean revoked = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // --- constructors, getters, setters ---

    public ApiAccessToken() { }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public Dataset getDataset() { return dataset; }
    public void setDataset(Dataset dataset) { this.dataset = dataset; }


    public Long getUseAmount() { return useAmount; }
    public void setUseAmount(Long useAmount) { this.useAmount = useAmount; }

    public Integer getUsesCount() { return usesCount; }
    public void setUsesCount(Integer usesCount) { this.usesCount = usesCount; }

    public Boolean getRevoked() { return revoked; }
    public void setRevoked(Boolean revoked) { this.revoked = revoked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
