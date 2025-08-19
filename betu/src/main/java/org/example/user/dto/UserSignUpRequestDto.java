package org.example.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignUpRequestDto {
    private String userName;
    private String userEmail;
    private String userPassword;
}
