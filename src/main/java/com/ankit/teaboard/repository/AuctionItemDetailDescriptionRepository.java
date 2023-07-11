package com.ankit.teaboard.repository;

import com.ankit.teaboard.entity.AuctionItemDetailDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuctionItemDetailDescriptionRepository extends JpaRepository<AuctionItemDetailDescription,Long> {
    @Query("SELECT auctionItemDetailDescription FROM AuctionItemDetailDescription auctionItemDetailDescription WHERE auctionItemDetailDescription.auctionItemDetail.auctionItemDetailId IN (:auctionItemDetailIds) ")
    public List<AuctionItemDetailDescription> getAuctionItemDetailDescriptions(@Param("auctionItemDetailIds") List<Long> auctionItemDetailIds);

    @Query("SELECT auctionItemDetailDescription FROM AuctionItemDetailDescription auctionItemDetailDescription WHERE auctionItemDetailDescription.auctionItemDetail.auctionItemDetailId =:auctionItemDetailId ")
    public AuctionItemDetailDescription getAuctionItemDetailDescriptionByAuctionItemDetailId(@Param("auctionItemDetailId") Long auctionItemDetailId);
}
