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
public class PreBidDetailDTO {
    private Long bidderId;
    private Long auctionItemDetailId;
    private BigDecimal maxBid;
}
