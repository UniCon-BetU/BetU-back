package org.example.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserLogInRequestDto {
    private String userName;
    private String userPassword;
}
