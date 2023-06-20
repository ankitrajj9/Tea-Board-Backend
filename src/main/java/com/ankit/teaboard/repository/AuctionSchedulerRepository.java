package com.ankit.teaboard.repository;

import com.ankit.teaboard.entity.AuctionScheduler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuctionSchedulerRepository extends JpaRepository<AuctionScheduler,Long> {
    @Query("SELECT auctionScheduler FROM AuctionScheduler auctionScheduler WHERE auctionScheduler.auctionDetail.auctionDetailId=:auctionDetailId ")
    public List<AuctionScheduler> getAuctionSchedulerByAuctionDetailId(@Param("auctionDetailId") Long auctionDetailId);
}
