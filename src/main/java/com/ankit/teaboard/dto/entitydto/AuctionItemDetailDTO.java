package com.ankit.teaboard.dto.entitydto;

import com.ankit.teaboard.dto.entitydto.AuctionDetailDTO;
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
public class AuctionItemDetailDTO {
    private Long auctionItemDetailId;
    private AuctionDetailDTO auctionDetail;
    private int serialNo;
    private String lotNo;
    private BigDecimal basePrice;
    private BigDecimal reservePrice;
    private BigDecimal currentPrice;
    private int increment;
    private Date createdOn;
    private Long createdBy;
    private String category;
    private String grade;
    private int itemPackage;
    private int isActive;
    private int cstatus;
    private int schedulerCount;


}
