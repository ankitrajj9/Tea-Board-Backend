package com.ankit.teaboard.dto.apidto;

import com.ankit.teaboard.dto.entitydto.AuctionItemDetailDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LiveBidDetailDTO {
    private Long bidderId;
    private AuctionItemDetailDTO auctionItemDetail;
    private Long auctionDetailId;
    private BigDecimal currentBid;
    private BigDecimal maxBid;
    private String itemColor;
    private int increment;

    private BigDecimal l1Amount;
}
