package com.example.datasetapi.util;

import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.dataset.ApiAccessToken;
import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.dataset.DownloadToken;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.model.dataset.TimeGroup;
import com.example.datasetapi.repository.ApiAccessTokenRepository;
import com.example.datasetapi.repository.DownloadTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {
    private final SecretKey key;  // Dùng để ký và verify token
    private final long expirationTime;

    @Autowired
    private DownloadTokenRepository downloadTokenRepository;
    @Autowired
    private ApiAccessTokenRepository apiAccessTokenRepository;

    @Autowired
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationTime) {
        // chú ý: secret nên có độ dài >= 32 bytes (256 bit) cho HS256
        this.key = Keys.hmacShaKeyFor(secret.getBytes()); // String -> SecretKey
        this.expirationTime = expirationTime;
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole() != null ? user.getRole().getName() : null)
                .claim("id", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // 15 phút
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("user_Id", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7)) // 7 ngày
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public DownloadToken generateDowloadToken(User consumer, Dataset dataset, long day, int useAmount, TimeGroup timeGroup) {
        DownloadToken token = new DownloadToken();
        token.setId(UUID.randomUUID());
        token.setConsumer(consumer);
        token.setUse_amount(useAmount);
        token.setExpiresAt(LocalDateTime.now().plusDays(day));
        if (dataset != null) {
            token.setDataset(dataset);
        } else {
            token.setTimeGroup(timeGroup);
        }
        return downloadTokenRepository.save(token);
    }

    // Lấy username từ token (nếu invalid => ném JwtException)
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Kiểm tra token còn hợp lệ hay không, an toàn: bắt exception và trả false nếu invalid
    public boolean validateToken(String token, String username) {
        try {
            String extractedUsername = extractUsername(token);
            return (username != null && username.equals(extractedUsername) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            // token malformed / expired / signature invalid -> return false
            // bạn có thể log e.getMessage() ở đây nếu muốn
            return false;
        }
    }

    // ================== PRIVATE METHODS ==================

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        Date exp = extractAllClaims(token).getExpiration();
        return exp != null && exp.before(new Date());
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object idObj = claims.get("id");
            if (idObj == null) return null;
            if (idObj instanceof Number) {
                return ((Number) idObj).longValue();
            } else {
                // nếu lưu dạng string trong claim
                return Long.valueOf(idObj.toString());
            }
        } catch (ExpiredJwtException e) {
            System.out.println("Token hết hạn: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("Token không hỗ trợ: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Token sai format: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println("Chữ ký token không hợp lệ: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Token rỗng hoặc null: " + e.getMessage());
        }
        return null;
    }
    public String generateApiSaleToken(User buyer, Dataset dataset, long daysValid, long useAmount) {
        // tạo jti
        String jti = UUID.randomUUID().toString();
        long nowMillis = System.currentTimeMillis();
        Date issuedAt = new Date(nowMillis);
        Date exp = new Date(nowMillis + daysValid * 24L * 60L * 60L * 1000L); // days -> ms

        Map<String, Object> claims = new HashMap<>();
        claims.put("buyerId", buyer != null ? buyer.getId() : null);
        claims.put("datasetId", dataset != null ? dataset.getId() : null);

        claims.put("useAmount", useAmount);

        String token = Jwts.builder()
                .setId(jti)
                .setSubject(buyer != null ? String.valueOf(buyer.getId()) : "anonymous")
                .setClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Lưu vào DB
        ApiAccessToken apiToken = new ApiAccessToken();
        apiToken.setId(UUID.fromString(jti));
        apiToken.setToken(token); // production: lưu hash(token) thay vì token raw
        apiToken.setBuyer(buyer);
        apiToken.setDataset(dataset);
        apiToken.setUseAmount(useAmount);
        apiToken.setUsesCount(0);
        apiToken.setRevoked(false);
        apiToken.setCreatedAt(LocalDateTime.now());
        apiToken.setExpiresAt(LocalDateTime.now().plusDays(daysValid));

        apiAccessTokenRepository.save(apiToken);

        return token;
    }
    @Transactional
    public boolean consumeApiToken(String token) {
        try {
            System.out.println(token);
            ApiAccessToken t = apiAccessTokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Token not found"));

            if (Boolean.TRUE.equals(t.getRevoked())) return false;
            if (t.getExpiresAt() != null && t.getExpiresAt().isBefore(LocalDateTime.now())) return false;
            if (t.getUseAmount() != null && t.getUseAmount() >= 0) {
                if (t.getUsesCount() >= t.getUseAmount()) return false;
                t.setUsesCount(t.getUsesCount() + 1);
            }
            apiAccessTokenRepository.save(t);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Dataset finđDatasetFromAPIToken(String token) {
        return apiAccessTokenRepository.findByToken(token).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.TOKEN_NOT_FOUND)).getDataset();
    }
}
