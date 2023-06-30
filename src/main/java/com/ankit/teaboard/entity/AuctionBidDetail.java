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
@Table(name = "auctionBidDetail")
public class AuctionBidDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auctionBidDetailId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="auctionDetailId")
    private AuctionDetail auctionDetail;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="auctionItemDetailId")
    private AuctionItemDetail auctionItemDetail;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="bidderId")
    private UserLogin userLogin;
    private BigDecimal maxBid;
    private BigDecimal cpAtMb;

    private BigDecimal cpAtExit;
    private int isActive;
    private int cstatus;

    private Date createdOn;
    private Long createdBy;
    private Date updatedOn;

    public AuctionBidDetail(Long auctionBidDetailId){
        this.auctionBidDetailId=auctionBidDetailId;
    }
}
