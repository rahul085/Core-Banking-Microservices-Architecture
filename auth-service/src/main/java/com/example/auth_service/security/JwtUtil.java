package com.example.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;

import java.time.Instant;
import java.time.Period;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
@Component
public class JwtUtil {
    private final Key secretKey;
    public JwtUtil(@Value("${application.jwt.secret}")String secretKey){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        if(keyBytes.length<32){
            throw new IllegalArgumentException("The length of the key must be at least 32 bits");
        }
       this.secretKey= Keys.hmacShaKeyFor(keyBytes);
    }

    @Value("${application.jwt.expirationMs}")
    private long expiration;

    public String generateActiveToken(UserDetails userDetails,String email,Long userId){
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        Map<String,Object> claims=new HashMap<>();
        claims.put("roles",authorities);
        claims.put("tokenType","ActiveToken");
        claims.put("email",email);
        claims.put("userId",userId);
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(Date.from(Instant.now().plusMillis(expiration)))
                .setSubject(userDetails.getUsername())
                .setIssuedAt(Date.from(Instant.now()))
                .signWith(secretKey)
                .compact();

    }

    public String generateRefreshToken(UserDetails userDetails,String email,Long userId){
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        Map<String,Object> claims=new HashMap<>();
        claims.put("roles",authorities);
        claims.put("tokenType","RefreshToken");
        claims.put("email",email);
        claims.put("userId",userId);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setExpiration(Date.from(Instant.now().plus(Period.ofDays(7))))
                .setIssuedAt(Date.from(Instant.now()))
                .signWith(secretKey)
                .compact();
    }


    public String extractUsername(String token){
        return extractClaims(token,Claims::getSubject);
    }

    public Date extractExpiration(String token){
        return extractClaims(token,Claims::getExpiration);
    }

    public String getTokenType(String token){
        return extractClaims(token,claims -> claims.get("tokenType",String.class));
    }

    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(Date.from(Instant.now()));
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = userDetails.getUsername();
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }




    private <T> T extractClaims(String token, Function<Claims,T> claimsResolver){
        Claims claims=parseClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims parseClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
