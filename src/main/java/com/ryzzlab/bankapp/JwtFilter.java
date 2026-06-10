package com.ryzzlab.bankapp;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component // same as service it makes a bean
public class JwtFilter extends OncePerRequestFilter { // allows only on run per request
    @Autowired // autowired finds a bean(service)
    private JwtService jwtService; //it gets the jwt service
    @Override // it helps catch mistakes so cause its extended by OncePerRequestFilter you can make your own type of this method instead of using the one from OncePerRequestFilter
    protected void doFilterInternal(HttpServletRequest request, //represents the request coming in for example {"token":
                                    HttpServletResponse response, //represents the response of this server
                                    FilterChain filterChain) //passes the request to the next filter or controller
                                    throws ServletException, IOException { // if something happens because of this trowh in error in the terminal
        String authHeader = request.getHeader("Authorization"); // take the auth header
        if(authHeader == null || !authHeader.startsWith("Bearer ")){ //check if it is empty
            filterChain.doFilter(request,response); //do filter
            return;// stop
        }
        try {
            String token = authHeader.substring(7); //the token from request
            String userId = jwtService.extractUserId(token); //extracts the userId
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of());
            //creates a object for the authentication with user id and no credinials cause we will handle it with bcrypt
            SecurityContextHolder.getContext().setAuthentication((authentication)); //puts it in the security holder of spring boot
            filterChain.doFilter(request, response); // pass to controller
        }
        catch(Exception e){
            filterChain.doFilter(request,response);
        }
    }
}
