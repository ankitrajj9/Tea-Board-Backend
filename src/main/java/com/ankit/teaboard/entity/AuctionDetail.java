package com.ankit.teaboard.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "auctionDetail")
public class AuctionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auctionDetailId;
    private String auctionBrief;
    private Date createdOn;
    private Long createdBy;
    private int cstatus;
    private Date startDate;
    private Date endDate;

    public AuctionDetail(Long auctionDetailId){
        this.auctionDetailId=auctionDetailId;
    }

}
