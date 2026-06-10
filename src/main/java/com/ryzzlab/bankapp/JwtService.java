package com.ryzzlab.bankapp;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;

import java.util.Date;

@Service // tells spring that its a business logic,where other dependenies can inject in this service we use it so for example jwtfilter can use it to extract user ids or create some in authcontroller
public class JwtService {
    @Value("${jwt.secret}") // like python dot env it gets the value so it doesnt have to "leak" it in this code great for opensource
    private String secretKey; //the value is being stored in this variable
    public String generateToken(String userId){
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 *24))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }
    public String extractUserId(String token){
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes())) //use the key from the variable at top
                .build()
                .parseSignedClaims(token) // the full token
                .getPayload()//gets the payload so
                .getSubject(); ///get the userId
    }
}
