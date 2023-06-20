package com.ankit.teaboard.repository;

import com.ankit.teaboard.entity.AuctionItemL1Detail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuctionItemL1DetailRepository extends JpaRepository<AuctionItemL1Detail,Long> {
    @Query("SELECT auctionItemL1Detail FROM AuctionItemL1Detail auctionItemL1Detail WHERE auctionItemL1Detail.auctionItemDetail.auctionItemDetailId=:auctionItemDetailId ")
    public AuctionItemL1Detail getItemStatus(@Param("auctionItemDetailId") Long auctionItemDetailId);


    @Query(value = "select item.auctionItemDetailId,item.lotNo,item.basePrice,item.reservePrice,item.increment,l1.bidderId,ul.loginId,l1.amount as l1Amount from auctionDetail auction inner join auctionItemDetail item on item.auctionDetailId=auction.auctionDetailId inner join auctionItemL1Detail l1 on l1.auctionDetailId=auction.auctionDetailId and l1.auctionItemDetailId=item.auctionItemDetailId inner join userLogin ul on ul.userLoginId=l1.bidderId where auction.auctionDetailId=:auctionDetailId order by item.auctionItemDetailId",nativeQuery = true)
    public List<Object[]> getItemWiseL1H1Details(@Param("auctionDetailId") Long auctionDetailId);
}
