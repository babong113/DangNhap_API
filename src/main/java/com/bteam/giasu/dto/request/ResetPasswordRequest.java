package com.bteam.giasu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {
    @NotBlank(message = "To không được để trống")
    private String token;

    @NotBlank(message = "Không được để trống ")
    @Size(min =8, message = "Mật khẩu ít nhất 8 kí tự")
    private String newPassword;

    @NotBlank(message = "Không được để trống ")
    private String confirmPassword;
}
