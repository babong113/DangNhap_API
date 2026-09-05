package com.bteam.giasu.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message="Email không để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message="mật khẩu không để trống")
    @Size(min = 6,message = "mật khẩu không hợp lệ")
    private String password;

    @NotBlank(message="Số điện thoại không để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotNull(message = "Vai trò không được để trống")
    private String role;
}
