package com.ankit.teaboard.repository;

import com.ankit.teaboard.entity.AuctionBidHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionBidHistoryRepository extends JpaRepository<AuctionBidHistory,Long> {
}
