package com.ankit.teaboard.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "auctionItemDetail")
public class AuctionItemDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auctionItemDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="auctionDetailId")
    private AuctionDetail auctionDetail;
    private int serialNo;
    private String lotNo;
    private String category;
    private String grade;
    private int itemPackage;
    private BigDecimal basePrice;
    private BigDecimal reservePrice;

    private BigDecimal currentPrice;
    private int increment;
    private Date createdOn;
    private Long createdBy;

    private int isActive;
    private int cstatus;

    private int schedulerCount;

    public AuctionItemDetail(Long auctionItemDetailId){
        this.auctionItemDetailId=auctionItemDetailId;
    }


}
