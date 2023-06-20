package com.ankit.teaboard.dto.entitydto;

import com.ankit.teaboard.entity.AuctionBidDetail;
import com.ankit.teaboard.entity.AuctionDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuctionBidHistoryDTO {
    private Long auctionBidHistoryId;
    private AuctionDetailDTO auctionDetail;
    private AuctionBidDetailDTO auctionBidDetail;
    private Date createdOn;
    private Long createdBy;
    private BigDecimal bidAmount;
}
