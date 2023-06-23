package com.ankit.teaboard.dto.entitydto;

import com.ankit.teaboard.entity.AuctionDetail;
import com.ankit.teaboard.entity.AuctionItemDetail;
import com.ankit.teaboard.entity.UserLogin;
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
public class AuctionBidDetailDTO {
    private Long auctionBidDetailId;
    private AuctionDetailDTO auctionDetail;
    private AuctionItemDetailDTO auctionItemDetail;
    private UserLoginDTO userLogin;
    private BigDecimal maxBid;
    private BigDecimal cpAtMb;
    private int isActive;
    private int cstatus;
    private Date createdOn;
    private Long createdBy;
    private String remarks;
}
