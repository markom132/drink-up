package com.party_up.network.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for handling success login requests. Includes User information, jwt token created, and token expiration date
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginSuccessResponseDTO {

    private Long id;

    private String username;

    private String firstName;

    private String lastName;

    private String email;

    private String token;

    private String expiresAt;

}
