package com.ankit.teaboard.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "auctionItemL1Detail")
public class AuctionItemL1Detail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auctionItemL1DetailId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="auctionDetailId")
    private AuctionDetail auctionDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="auctionItemDetailId")
    private AuctionItemDetail auctionItemDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="bidderId")
    private UserLogin userLogin;

    private int cstatus;

    private BigDecimal amount;
}
