    package com.example.datasetapi.util;

    import com.example.datasetapi.model.UserManager.User;
    import io.jsonwebtoken.*;
    import io.jsonwebtoken.security.Keys;
    import jakarta.servlet.http.HttpServletRequest;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Component;

    import java.security.Key;
    import java.util.Date;
    import java.util.stream.Collectors;

    @Component
    public class JwtUtil {
        private final Key key;  // Dùng để ký và verify token
        private final long expirationTime;

        // Spring sẽ inject từ application.properties
        public JwtUtil(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration}") long expirationTime) {
            this.key = Keys.hmacShaKeyFor(secret.getBytes()); // String -> Key
            this.expirationTime = expirationTime;
        }

        public String generateAccessToken(User user) {
            return Jwts.builder()
                    .setSubject(user.getUsername())
                    .claim("role", user.getRole()).claim("id", user.getId())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // 15 phút
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        }

        public String generateRefreshToken(String userId) {
            System.out.println("User id from generateRefreshToken: " + userId);
            return Jwts.builder()
                    .setSubject(userId)
                    .claim("user_Id",userId)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7)) // 7 ngày
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        }


        // Lấy username từ token
        public String extractUsername(String token) {
            return extractAllClaims(token).getSubject();
        }

        // Kiểm tra token còn hợp lệ hay không
        public boolean validateToken(String token, String username) {
            String extractedUsername = extractUsername(token);
            return (username.equals(extractedUsername) && !isTokenExpired(token));
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
            return extractAllClaims(token).getExpiration().before(new Date());
        }

        public Long getUserIdFromToken(String token) {
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // Lấy userId từ claim "id"
                return claims.get("id", Long.class);
            } catch (ExpiredJwtException e) {
                System.out.println("Token hết hạn");
            } catch (UnsupportedJwtException e) {
                System.out.println("Token không hỗ trợ");
            } catch (MalformedJwtException e) {
                System.out.println("Token sai format");
            } catch (SignatureException e) {
                System.out.println("Chữ ký token không hợp lệ");
            } catch (IllegalArgumentException e) {
                System.out.println("Token rỗng hoặc null");
            }
            return null; // ⚡ trả null thay vì Long.MIN_VALUE
        }


    }