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
@Table(name = "auctionBidHistory")
public class AuctionBidHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auctionBidHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="auctionDetailId")
    private AuctionDetail auctionDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="auctionBidDetailId")
    private AuctionBidDetail auctionBidDetail;

    private BigDecimal bidAmount;

    private Date createdOn;
    private Long createdBy;

    public AuctionBidHistory(Long auctionBidHistoryId){
        this.auctionBidHistoryId=auctionBidHistoryId;
    }
}
