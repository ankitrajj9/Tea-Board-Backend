package com.ankit.teaboard.repository;

import com.ankit.teaboard.entity.AuctionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface AuctionDetailRepository extends JpaRepository<AuctionDetail,Long> {
    @Query("SELECT auctionDetail FROM AuctionDetail auctionDetail WHERE auctionDetail.cstatus=1 AND :currentDate BETWEEN auctionDetail.startDate AND auctionDetail.endDate ORDER BY auctionDetail.auctionDetailId DESC ")
    List<AuctionDetail> getLiveAuctions(@Param("currentDate") Date currentDate);

    @Query("SELECT auctionDetail FROM AuctionDetail auctionDetail WHERE auctionDetail.cstatus=0 ORDER BY auctionDetail.auctionDetailId DESC ")
    List<AuctionDetail> getPendingAuctions();

    @Query("SELECT auctionDetail FROM AuctionDetail auctionDetail ORDER BY auctionDetail.auctionDetailId DESC ")
    List<AuctionDetail> getAllAuctions();

    @Query("SELECT auctionDetail FROM AuctionDetail auctionDetail WHERE auctionDetail.auctionBrief like %:param% or auctionDetail.auctionDetailId like %:param% ORDER BY auctionDetail.auctionDetailId DESC")
    public List<AuctionDetail> searchAuctions(@Param("param") String param);
}
