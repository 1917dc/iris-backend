package com.spring.iris.controllers.dto;

public record LoginResponseDTO(String token, Long expiresIn) {
}
