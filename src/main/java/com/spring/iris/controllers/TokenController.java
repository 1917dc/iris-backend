package com.spring.iris.controllers;

import com.spring.iris.controllers.dto.LoginRequestDTO;
import com.spring.iris.controllers.dto.LoginResponseDTO;
import com.spring.iris.entities.Role;
import com.spring.iris.entities.User;
import com.spring.iris.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class TokenController {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtEncoder jwtEncoder;

    public TokenController(BCryptPasswordEncoder passwordEncoder, UserRepository userRepository, JwtEncoder jwtEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO login) {
        Optional<User> user = userRepository.findByCpf(login.cpf());

        if (user.isEmpty() || !user.get().isLoginCorrect(login, passwordEncoder)) {
            throw new BadCredentialsException("CPF ou senha incorretos.");
        }

        Instant now = Instant.now();
        long expiresIn = 300L;

        var scopes = user.get().getRoles()
                .stream()   // desestruturação de roles *separadas por espaço*
                .map(Role::getName)
                .collect(Collectors.joining(" "));
        var claims = JwtClaimsSet.builder()
                .issuer("iris")
                .subject(user.get().getUserId().toString())
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("scope", scopes)
                .build();

        String jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return ResponseEntity.ok(new LoginResponseDTO(jwt, expiresIn));
    }
}
