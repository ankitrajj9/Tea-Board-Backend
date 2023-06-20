package com.ankit.teaboard.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JWTService {


    public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";


    public boolean validateToken(final String token) {
        try{
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            System.out.println("Token verified");
            return true;
        }
        catch(Exception e){
            System.out.println("Token not verified");

            return false;
        }
        /*System.out.println("Inside Validate token service 2");
       return Mono.just(Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token))
               .flatMap(s -> Mono.just(true))
               .onErrorReturn(false).flatMap(b -> Mono.just(false));*/
    }


    public String generateToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userName);
    }

    private String createToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 600 * 300))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getLoginIdFromToken(String token) {
        final Claims claims = Jwts.parser().setSigningKey(getSignKey()).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
}