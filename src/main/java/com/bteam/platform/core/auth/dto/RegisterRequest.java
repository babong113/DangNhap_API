package com.bteam.platform.core.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message="Email khong duoc de trong")
    @Email(message = "Email khong hop le")
    private String email;

    @NotBlank(message="Mat khau khong duoc de trong")
    @Size(min = 6,message = "Mat khau phai co it nhat 6 ky tu")
    private String password;

    @NotBlank(message="So dien thoai khong duoc de trong")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "So dien thoai khong hop le")
    private String phoneNumber;

    @NotBlank(message = "Ho ten khong duoc de trong")
    private String fullName;

    private String role;
}
