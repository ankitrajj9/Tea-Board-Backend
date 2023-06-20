package com.ankit.teaboard.dto.apidto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportDetailDTO {
    private Long auctionItemDetailId;
    private String lotNo;
    private BigDecimal basePrice;
    private BigDecimal reservePrice;
    private int increment;
    private Long bidderId;
    private String loginId;
    private BigDecimal l1Amount;

    private List<ItemExitDetailDTO> exitDetails;

}
