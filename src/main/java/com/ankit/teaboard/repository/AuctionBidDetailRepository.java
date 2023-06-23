package com.ankit.teaboard.repository;

import com.ankit.teaboard.entity.AuctionBidDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

public interface AuctionBidDetailRepository extends JpaRepository<AuctionBidDetail,Long> {
    @Query("SELECT auctionBidDetail FROM AuctionBidDetail auctionBidDetail WHERE auctionBidDetail.auctionDetail.auctionDetailId=:auctionDetailId AND auctionBidDetail.userLogin.userLoginId=:userLoginId ")
    public List<AuctionBidDetail> getPreBidDetailsByAuctionDetailIdAndUserLoginId(@Param("auctionDetailId") Long auctionDetailId,@Param("userLoginId") Long userLoginId);
    @Query("SELECT auctionBidDetail FROM AuctionBidDetail auctionBidDetail WHERE auctionBidDetail.auctionItemDetail.auctionItemDetailId=:auctionItemDetailId AND auctionBidDetail.userLogin.userLoginId=:userLoginId AND auctionBidDetail.isActive=1 ")
    public AuctionBidDetail getPreBidDetailsByAuctionItemDetailIdAndUserLoginId(@Param("auctionItemDetailId") Long auctionItemDetailId,@Param("userLoginId") Long userLoginId);

    @Query("SELECT DISTINCT(auctionBidDetail.userLogin.userLoginId) FROM AuctionBidDetail auctionBidDetail WHERE auctionBidDetail.auctionDetail.auctionDetailId=:auctionDetailId")
    public List<Long> getEligibleBidderIds(@Param("auctionDetailId") Long auctionDetailId);

    @Query(value = "select item.auctionItemDetailId,bidDtl.bidderId,bidDtl.auctionBidDetailId,bidDtl.maxBid,item.schedulerCount,item.basePrice,item.increment,item.auctionDetailId,item.currentPrice,bidDtl.cstatus,item.reservePrice from auctionDetail auction inner join auctionItemDetail item on item.auctionDetailId=auction.auctionDetailId inner join auctionBidDetail bidDtl on item.auctionItemDetailId=bidDtl.auctionItemDetailId where item.isactive=1 and auction.auctionDetailId=:auctionDetailId and bidDtl.isActive=1 and bidDtl.cstatus=0 and item.cstatus=0 order by auctionItemDetailId",nativeQuery = true)
    public List<Object[]> getLiveBidData(@Param("auctionDetailId") Long auctionDetailId);

    @Query(value = "select item.auctionItemDetailId,bidDtl.bidderId,bidDtl.auctionBidDetailId,bidDtl.maxBid,item.schedulerCount,item.basePrice,item.increment,item.auctionDetailId,item.currentPrice,bidDtl.cstatus,item.reservePrice from auctionDetail auction inner join auctionItemDetail item on item.auctionDetailId=auction.auctionDetailId inner join auctionBidDetail bidDtl on item.auctionItemDetailId=bidDtl.auctionItemDetailId where item.isactive=1 and auction.auctionDetailId=:auctionDetailId and bidDtl.isActive=1 and bidDtl.bidderId=:userLoginId ORDER BY item.auctionItemDetailId",nativeQuery = true)
    public List<Object[]> getBidderWiseLiveBidData(@Param("auctionDetailId") Long auctionDetailId,@Param("userLoginId") Long userLoginId);

    @Query(value = "select item.auctionItemDetailId,count(bidDtl.bidderId) as totalEligibleBidderCount from auctionDetail auction inner join auctionItemDetail item on item.auctionDetailId=auction.auctionDetailId inner join auctionBidDetail bidDtl on item.auctionItemDetailId=bidDtl.auctionItemDetailId where item.isactive=1 and auction.auctionDetailId=:auctionDetailId  and bidDtl.isActive=1 and bidDtl.cstatus = 0 group by item.auctionItemDetailId ",nativeQuery = true)
    public List<Object[]> getItemWiseEligibleBiddersCount(@Param("auctionDetailId") Long auctionDetailId);

    @Query(value = "select distinct itemWiseEligibleBidderCount.itemId,itemWiseEligibleBidderCount.totalEligibleBidderCount ,CASE WHEN itemWiseEligibleBidderCount.totalEligibleBidderCount = 1 THEN bidDtl.bidderId else 0 end as eligibleBidderId ,isNull (lastBid.finalBid,0) as finalBid from auctionDetail auction inner join auctionItemDetail item on item.auctionDetailId=auction.auctionDetailId inner join auctionBidDetail bidDtl on item.auctionItemDetailId=bidDtl.auctionItemDetailId INNER JOIN (select itemIn.auctionItemDetailId as itemId,count(bidDtlIn.bidderId) as totalEligibleBidderCount from auctionDetail auctionIn inner join auctionItemDetail itemIn on itemIn.auctionDetailId=auctionIn.auctionDetailId inner join auctionBidDetail bidDtlIn on itemIn.auctionItemDetailId=bidDtlIn.auctionItemDetailId where itemIn.isactive=1 and auctionIn.auctionDetailId=:auctionDetailId  and bidDtlIn.isActive=1 and bidDtlIn.cstatus = 0 group by itemIn.auctionItemDetailId) itemWiseEligibleBidderCount ON itemWiseEligibleBidderCount.itemId=item.auctionItemDetailId LEFT JOIN (select aid.auctionItemDetailId,(max(abd.maxBid)+aid.increment) as finalBid from auctionBidDetail abd inner join auctionItemDetail aid on aid.auctionItemDetailId=abd.auctionItemDetailId where abd.cstatus in (2,4) group by aid.auctionItemDetailId,aid.increment) lastBid on lastBid.auctionItemDetailId=item.auctionItemDetailId where item.isactive=1 and bidDtl.isActive=1 and bidDtl.cstatus = 0 and auction.auctionDetailId=:auctionDetailId ",nativeQuery = true)
    public List<Object[]> getL1Details(@Param("auctionDetailId") Long auctionDetailId);

    @Query("SELECT auctionBidDetail FROM AuctionBidDetail auctionBidDetail WHERE auctionBidDetail.auctionItemDetail.auctionItemDetailId IN(:auctionItemDetailIds) AND auctionBidDetail.isActive=1 ")
    public List<AuctionBidDetail> getAuctionBidDetailsByAuctionItemDetailId(@Param("auctionItemDetailIds") List<Long> auctionItemDetailIds);

    @Query(value ="select distinct aid.auctionItemDetailId,(max(abd.maxBid)+aid.increment) as finalBid  from auctionBidDetail abd  inner join auctionItemDetail aid on aid.auctionItemDetailId=abd.auctionItemDetailId  where abd.cstatus in (2,4) and aid.auctionItemDetailId=:auctionItemDetailId group by aid.auctionItemDetailId,aid.increment",nativeQuery = true)
    public List<Object[]> findBestBidBasedUpon2ndHighestBid(@Param("auctionItemDetailId") Long auctionItemDetailId);

    @Query(value = "select distinct aid.auctionItemDetailId,(max(aid.currentPrice)+aid.increment) as finalBid  from auctionBidDetail abd  inner join auctionItemDetail aid on aid.auctionItemDetailId=abd.auctionItemDetailId  where abd.cstatus in (2,4) and aid.auctionItemDetailId=:auctionItemDetailId group by aid.auctionItemDetailId,aid.increment",nativeQuery = true)
    public List<Object[]> findBestBidBasedUponItemCurrentPrice(@Param("auctionItemDetailId") Long auctionItemDetailId);

    @Query(value = "select abd.auctionBidDetailId,max(abd.cpAtExit) as lastExit from auctionBidDetail abd where auctionItemDetailId=:auctionItemDetailId and abd.cstatus in (2,4) group by abd.auctionBidDetailId order by lastExit desc",nativeQuery = true)
    public List<Object[]> getLastExitBidWithBidDetailId(@Param("auctionItemDetailId") Long auctionItemDetailId);

    @Query(value = "select ul.loginId,abd.cpAtExit as lastExit,abd.maxBid as maxBid ,abd.cstatus from auctionBidDetail abd inner join userLogin ul on ul.userLoginId=abd.bidderId where abd.auctionItemDetailId=:auctionItemDetailId and abd.cstatus in (2,4) and abd.isActive=1 order by lastExit desc",nativeQuery = true)
    public List<Object[]> getExitDetails(@Param("auctionItemDetailId") Long auctionItemDetailId);

    @Query(value = "select item.auctionItemDetailId,count(bidDtl.bidderId) as totalEligibleBidderCount from auctionDetail auction inner join auctionItemDetail item on item.auctionDetailId=auction.auctionDetailId inner join auctionBidDetail bidDtl on item.auctionItemDetailId=bidDtl.auctionItemDetailId where item.isactive=1 and item.auctionItemDetailId=:auctionItemDetailId and auction.auctionDetailId=:auctionDetailId  and bidDtl.isActive=1 and bidDtl.cstatus = 0 group by item.auctionItemDetailId ",nativeQuery = true)
    public List<Object[]> getItemWiseEligibleBiddersCountByAuctionItemDetailId(@Param("auctionDetailId") Long auctionDetailId,@Param("auctionItemDetailId") Long auctionItemDetailId);

    @Query(value = "select item.auctionItemDetailId,bidDtl.bidderId,bidDtl.auctionBidDetailId,bidDtl.maxBid,item.schedulerCount,item.basePrice,item.increment,item.auctionDetailId,item.currentPrice,bidDtl.cstatus,item.reservePrice from auctionDetail auction inner join auctionItemDetail item on item.auctionDetailId=auction.auctionDetailId inner join auctionBidDetail bidDtl on item.auctionItemDetailId=bidDtl.auctionItemDetailId where item.isactive=1 and auction.auctionDetailId=:auctionDetailId and bidDtl.isActive=1 and bidDtl.cstatus=0 and item.auctionItemDetailId=:auctionItemDetailId order by item.auctionItemDetailId",nativeQuery = true)
    public List<Object[]> getLiveBidDataByAuctionItemDetailId(@Param("auctionDetailId") Long auctionDetailId,@Param("auctionItemDetailId") Long auctionItemDetailId);

    @Query(value = "select count(bidDtl.bidderId) as totalEligibleBidderCount  from auctionDetail auction  inner join auctionItemDetail item on item.auctionDetailId=auction.auctionDetailId  inner join auctionBidDetail bidDtl on item.auctionItemDetailId=bidDtl.auctionItemDetailId  where item.isactive=1 and item.auctionItemDetailId=:auctionItemDetailId  and bidDtl.isActive=1 and bidDtl.cstatus = 0  group by item.auctionItemDetailId",nativeQuery = true)
    public int getItemWiseEligibleBiddersCountByAuctionItemDetailId(@Param("auctionItemDetailId") Long auctionItemDetailId);

    @Query(value = "select count (distinct(bidDtl.maxBid)) as maxBid  from auctionDetail auction  inner join auctionItemDetail item on item.auctionDetailId=auction.auctionDetailId  inner join auctionBidDetail bidDtl on item.auctionItemDetailId=bidDtl.auctionItemDetailId  where item.isactive=1 and item.auctionItemDetailId=:auctionItemDetailId  and bidDtl.isActive=1 and bidDtl.cstatus = 0",nativeQuery = true)
    public int getCountdistinctMaxBid(@Param("auctionItemDetailId") Long auctionItemDetailId);

    @Query(value = "select bidDtl.bidderId from auctionDetail auction  inner join auctionItemDetail item on item.auctionDetailId=auction.auctionDetailId  inner join auctionBidDetail bidDtl on item.auctionItemDetailId=bidDtl.auctionItemDetailId  where item.isactive=1 and item.auctionItemDetailId=:auctionItemDetailId  and bidDtl.isActive=1 and bidDtl.cstatus = 0  order by bidDtl.createdOn",nativeQuery = true)
    public List<Long> getBiddersBasedUponTimeCriteria(@Param("auctionItemDetailId") Long auctionItemDetailId);


    @Query("SELECT MAX(auctionBidDetail.maxBid) FROM AuctionBidDetail auctionBidDetail WHERE auctionBidDetail.auctionItemDetail.auctionItemDetailId=:auctionItemDetailId AND auctionBidDetail.isActive=1 AND auctionBidDetail.cstatus=0 ")
    public BigDecimal findMaxBidForItem(@Param("auctionItemDetailId") Long auctionItemDetailId);
    @Query(" SELECT auctionBidDetail FROM AuctionBidDetail auctionBidDetail WHERE auctionBidDetail.auctionItemDetail.auctionItemDetailId=:auctionItemDetailId AND auctionBidDetail.userLogin.userLoginId IN (:bidderIds) ")
    List<AuctionBidDetail> findTimeWiseRejectList(@Param("bidderIds") List<Long> bidderIds,@Param("auctionItemDetailId") Long auctionItemDetailId);
}
