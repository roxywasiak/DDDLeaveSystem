package com.example.leave.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginCommand {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String ipAddress;

    public LoginCommand(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public LoginCommand(String email, String password, String ipAddress) {
        this.email = email;
        this.password = password;
        this.ipAddress = ipAddress;
    }
}
