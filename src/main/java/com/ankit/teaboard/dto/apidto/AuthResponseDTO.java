package com.ankit.teaboard.dto.apidto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private Long userLoginId;
    private int userTypeId;
    private String token;
    private int responseCode;
    private String message;
}
