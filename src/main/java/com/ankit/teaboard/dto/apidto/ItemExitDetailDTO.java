package com.ankit.teaboard.dto.apidto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemExitDetailDTO {
    private String loginId;
    private BigDecimal amount;
    private BigDecimal maxBid;
    private int cstatus;
}
