package com.spring.iris.controllers;

import com.spring.iris.controllers.dto.RegisterRequestDTO;
import com.spring.iris.entities.Role;
import com.spring.iris.entities.User;
import com.spring.iris.repositories.RoleRepository;
import com.spring.iris.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Transactional  // semântica que especifica que é uma operação transacional no bd
    @PostMapping("/users")
    //@PreAuthorize("hasAuthority('SCOPE_COORDENADOR')")
    public ResponseEntity<Void> register(@RequestBody RegisterRequestDTO register) {
        Optional<User> user = userRepository.findByCpf(register.cpf());
        var role = roleRepository.findByName(register.role());
        if(user.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        User newUser = new User();
        newUser.setCpf(register.cpf());
        newUser.setPassword(passwordEncoder.encode(register.password()));
        newUser.setName(register.name());
        newUser.setRoles(Set.of(role));

        userRepository.save(newUser);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('SCOPE_COORDENADOR')")
    public ResponseEntity<List<User>> getUser(String cpf) {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
