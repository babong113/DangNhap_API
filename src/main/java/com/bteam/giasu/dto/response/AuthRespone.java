package com.bteam.giasu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRespone {

    private String token;
    private String userID;
    private String email;
    private String fullName;
    private String role;
}
