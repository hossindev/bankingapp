package com.ryzzlab.bankapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController()
@RequestMapping("/api/auth/")
public class AuthController {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @PostMapping("register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body){
        String email = body.get("email");
        String password = body.get("password");
        String firstName = body.get("firstName");
        String lastName = body.get("lastName");
        if(userRepository.findByEmail(email).isPresent()){
            return ResponseEntity.badRequest().body("Email already exists");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        userRepository.save(user);
        return ResponseEntity.ok().body("User registered");
    }


    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody Map<String, String > body){
        String email = body.get("email");
        String password = body.get("password");
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if ( optionalUser.isEmpty()){
            return ResponseEntity.status(401).body("Invalid Credentials");
        }
        User user = optionalUser.get();
        if(!passwordEncoder.matches(password, user.getPassword())){
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        String token = jwtService.generateToken(user.getId().toString());
        return ResponseEntity.ok(Map.of("token",token));
    }
}
