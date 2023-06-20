package com.ankit.teaboard.repository;

import com.ankit.teaboard.entity.AuctionItemDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuctionItemDetailRepository extends JpaRepository<AuctionItemDetail,Long> {
    @Query("SELECT auctionItemDetail FROM AuctionItemDetail auctionItemDetail WHERE auctionItemDetail.auctionDetail.auctionDetailId=:auctionDetailId")
    List<AuctionItemDetail> getAuctionItemsByAuctionDetailId(@Param("auctionDetailId") Long auctionDetailId);

    @Query("SELECT auctionItemDetail FROM AuctionItemDetail auctionItemDetail WHERE auctionItemDetail.auctionDetail.auctionDetailId=:auctionDetailId AND auctionItemDetail.isActive=1 AND auctionItemDetail.cstatus=0 ")
    List<AuctionItemDetail> getCurrentActiveItems(@Param("auctionDetailId") Long auctionDetailId);

    @Query(value="SELECT TOP 20 * FROM AuctionItemDetail auctionItemDetail WHERE auctionItemDetail.auctionDetailId=:auctionDetailId AND auctionItemDetail.isActive=0 AND auctionItemDetail.cstatus=0 ",nativeQuery = true)
    List<AuctionItemDetail> getTop3Items(@Param("auctionDetailId") Long auctionDetailId);

}
