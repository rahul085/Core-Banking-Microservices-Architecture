package com.example.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {
    private final Key secretKey;
    public JwtUtil(@Value("${jwt.secret}")String secret){
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey=Keys.hmacShaKeyFor(keyBytes);
    }

    // if the token passes this method without any exceptions then the token is valid.
    public void validateToken(final String token){
        Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }

 // extract the userId that we added to our jwt token.
    public String extractUserId(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return String.valueOf(claims.get("userId"));
    }

}
