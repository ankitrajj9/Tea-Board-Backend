package com.ankit.teaboard.dto.entitydto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDTO {
    private Long userLoginId;
    private int userTypeId;
    private String loginId;
    private String password;
}
