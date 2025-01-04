package com.spring.iris.controllers.dto;

public record RegisterRequestDTO(String cpf, String name, String email, String password, String role) {
}
