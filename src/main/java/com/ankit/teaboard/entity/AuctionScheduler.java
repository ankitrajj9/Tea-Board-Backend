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
@Table(name = "auctionScheduler")
public class AuctionScheduler {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auctionSchedulerId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="auctionDetailId")
    private AuctionDetail auctionDetail;
    private String schedulerName;
    private Date scheduledTime;
    private Date endTime;
    private Date createdOn;
    private Long createdBy;
    private int isActive;
    private int cstatus;

}
