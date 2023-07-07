package com.ankit.teaboard.service;

import com.ankit.teaboard.dto.apidto.LiveBidDetailDTO;
import com.ankit.teaboard.entity.*;
import com.ankit.teaboard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuctionService {

    @Autowired
    private AuctionBidDetailRepository auctionBidDetailRepository;

    @Autowired
    private AuctionDetailRepository auctionDetailRepository;

    @Autowired
    private AuctionItemDetailRepository auctionItemDetailRepository;

    @Autowired
    private AuctionBidHistoryRepository auctionBidHistoryRepository;

    @Autowired
    private AuctionItemL1DetailRepository auctionItemL1DetailRepository;

    @Autowired
    private SimpMessagingTemplate template;
    public List<LiveBidDetailDTO> getLiveBidDetails(Long auctionDetailId,Long bidderId){
        List<LiveBidDetailDTO> liveBids = new ArrayList<>();

        return liveBids;
    }

    public boolean saveBidPeriodically(Long auctionDetailId){
        boolean vbool = true;
        boolean dynUpdate = false;
        AuctionDetail auctionDetail = auctionDetailRepository.findById(auctionDetailId).get();
        /*List<Long> bidders= auctionBidDetailRepository.getEligibleBidderIds(auctionDetailId);
        List<AuctionItemDetail> currentActiveItems = auctionItemDetailRepository.getCurrentActiveItems(auctionDetailId);
        for(int bidderLoop=0;bidderLoop<bidders.size();bidderLoop++){
            for(int itemLoop=0;itemLoop<currentActiveItems.size();itemLoop++){
                AuctionBidHistory auctionBidHistory = new AuctionBidHistory();

            }
        }*/

        //AUCTION LIVE CHECK
        if(auctionDetail.getCstatus() == 1 && auctionDetail.getEndDate().compareTo(new Date()) > 0){}
        List<AuctionItemDetail> currentActiveItems = auctionItemDetailRepository.getCurrentActiveItems(auctionDetailId);
        List<Object[]> liveBidData = auctionBidDetailRepository.getLiveBidData(auctionDetailId);
        List<AuctionBidHistory> auctionBids = null;
        List<AuctionBidDetail> auctionBidDetails = null;
        List<AuctionItemL1Detail> l1Details = null;
        List<Long> unsoldItems = null;
        final Set<Long> itemWiseSchedulerCount = new HashSet<>();
        final Set<Long> soldItems = new HashSet<>();
        final Set<Long> timeWisesoldItems = new HashSet<>();
        if(liveBidData != null && !liveBidData.isEmpty()) {
            //auctionBids = new ArrayList<>();
            auctionBidDetails = new ArrayList<>();
            l1Details = new ArrayList<>();
            unsoldItems = new ArrayList<>();
            Map<Long,Integer> itemWiseEligibleBidders = this.getItemWiseEligibleBiddersCount(auctionDetailId);
            for (Object[] obj : liveBidData) {
                Long itemDetailId = Long.parseLong(obj[0].toString());
                if (!timeWisesoldItems.contains(itemDetailId)) {
                    int eligibleBidders=0;
                    List<Object[]> ls = auctionItemDetailRepository.getItemWiseEligibleBidderCount(itemDetailId);
                    if(ls != null && !ls.isEmpty()){
                        eligibleBidders = Integer.parseInt(ls.get(0)[1].toString());
                    }
                    BigDecimal maxQuotedBid = auctionBidDetailRepository.findMaxBidForItem(itemDetailId);
                    int scheduledCount = Integer.parseInt(obj[4].toString()) + 1;
                    BigDecimal basePrice = new BigDecimal(obj[5].toString());
                    int increment = Integer.parseInt(obj[6].toString());
                    int currentIteration = scheduledCount * increment;
                    BigDecimal currentPrice = new BigDecimal(obj[5].toString()).add(new BigDecimal(String.valueOf(currentIteration)));
                    BigDecimal reservePrice = new BigDecimal(obj[10].toString());
                    //CHECK IF ITEM IS ACTIVE OR NOT (SOLD OR NOT SOLD)
                    if (auctionItemL1DetailRepository.getItemStatus(itemDetailId) == null) {
                        itemWiseSchedulerCount.add(itemDetailId);
                        //CHECK IF NOT A SINGLE VENDOR AVAILABLE FOR ITEM
                        if (eligibleBidders == 0 && auctionItemL1DetailRepository.getItemStatus(itemDetailId) == null) {
                            Map<Long, String> l1Map = this.getL1DetailsMap(auctionDetailId);
                            String l1Data = l1Map.get(itemDetailId);
                            Long bidderId = Long.parseLong(l1Data.split("_")[0]);
                            BigDecimal finalBid = new BigDecimal(l1Data.split("_")[1]);
                            if(!unsoldItems.contains(itemDetailId)) {
                                AuctionItemL1Detail auctionItemL1Detail = new AuctionItemL1Detail();
                                auctionItemL1Detail.setCstatus(3);
                                auctionItemL1Detail.setAmount(finalBid);
                                auctionItemL1Detail.setAuctionItemDetail(new AuctionItemDetail(itemDetailId));
                                auctionItemL1Detail.setUserLogin(null);
                                auctionItemL1Detail.setAuctionDetail(auctionDetail);
                                l1Details.add(auctionItemL1Detail);
                                unsoldItems.add(itemDetailId);
                                dynUpdate = true;
                            }
                        } else {
                            //CHECK IF L1 IS REACHED FOR CURRENT ITEM OR NOT
                            if (itemWiseEligibleBidders.get(itemDetailId) == 1) {
                                Map<Long, String> l1Map = this.getL1DetailsMap(auctionDetailId);
                                String l1Data = l1Map.get(itemDetailId);
                                Long bidderId = Long.parseLong(l1Data.split("_")[0]);
                                BigDecimal finalBid = new BigDecimal(l1Data.split("_")[1]);
                                BigDecimal maxPrice = new BigDecimal(obj[3].toString());
                                AuctionBidDetail auctionBidDetail = auctionBidDetailRepository.getPreBidDetailsByAuctionItemDetailIdAndUserLoginId(itemDetailId, bidderId);
                                //CHECK IF CURRENT PRICE IS GREATER THAN ITEM'S RESERVE PRICE

                                //FINAL BID IS BASED UPON 2ND HIGHEST MAX BID
                                //SO IF SOMEONE PUT HIGHER BID AND EXITED EARLIER THE L1 BIDDER WOULD GET LOSS
                                //SO FIND BEST POSSIBLE BID
                                BigDecimal bestBid = this.findBestBidFromAllCriteria(itemDetailId);
                                if ((reservePrice.compareTo(bestBid) == -1 || reservePrice.compareTo(bestBid) == 0) && (maxPrice.compareTo(reservePrice) == 1 || maxPrice.compareTo(reservePrice) == 0)) {
                                    auctionBidDetail.setCstatus(1);
                                    auctionBidDetails.add(auctionBidDetail);
                                    soldItems.add(itemDetailId);
                            /*AuctionBidHistory auctionBidHistory = new AuctionBidHistory();
                            auctionBidHistory.setBidAmount(finalBid);
                            auctionBidHistory.setAuctionBidDetail(auctionBidDetail);
                            auctionBidHistory.setAuctionDetail(auctionDetail);
                            auctionBidHistory.setCreatedOn(new Date());
                            auctionBidHistory.setCreatedBy(Long.valueOf(obj[1].toString()));
                            auctionBids.add(auctionBidHistory);*/
                                    AuctionItemL1Detail auctionItemL1Detail = new AuctionItemL1Detail();
                                    auctionItemL1Detail.setCstatus(1);
                                    auctionItemL1Detail.setAmount(bestBid);
                                    auctionItemL1Detail.setAuctionItemDetail(new AuctionItemDetail(itemDetailId));
                                    auctionItemL1Detail.setUserLogin(new UserLogin(bidderId));
                                    auctionItemL1Detail.setAuctionDetail(auctionDetail);
                                    l1Details.add(auctionItemL1Detail);
                                    dynUpdate = true;
                                } else {
                                    if(!unsoldItems.contains(itemDetailId)) {
                                        AuctionItemL1Detail auctionItemL1Detail = new AuctionItemL1Detail();
                                        auctionItemL1Detail.setCstatus(3);
                                        auctionItemL1Detail.setAmount(bestBid);
                                        auctionItemL1Detail.setAuctionItemDetail(new AuctionItemDetail(itemDetailId));
                                        auctionItemL1Detail.setUserLogin(null);
                                        auctionItemL1Detail.setAuctionDetail(auctionDetail);
                                        l1Details.add(auctionItemL1Detail);
                                        unsoldItems.add(itemDetailId);
                                        dynUpdate = true;
                                    }
                                }
                            }
                            //check item has reached max price and no of vendors with that price
                            //allocate item based upon time criteria
                            else if ((currentPrice.compareTo(maxQuotedBid) == 0 || currentPrice.compareTo(maxQuotedBid) == 1) && checkDuplicateMaxBidForFinalVendors(itemDetailId)) {
                                List<AuctionBidDetail> timeWiseList = new ArrayList<>();
                                if (currentPrice.compareTo(reservePrice) != -1) {
                                    if (checkDuplicateMaxBidForFinalVendors(itemDetailId)) {
                                        List<Long> timeCriteriaList = auctionBidDetailRepository.getBiddersBasedUponTimeCriteria(itemDetailId);
                                        if (timeCriteriaList != null && !timeCriteriaList.isEmpty()) {
                                            Long finalVendorBasedUponTime = timeCriteriaList.get(0);
                                            List<Long> timeWiseRejectedVendorList = new ArrayList<>();
                                            timeCriteriaList.stream().forEach(
                                                    bidderId -> {
                                                        if (!bidderId.equals(finalVendorBasedUponTime)) {
                                                            timeWiseRejectedVendorList.add(bidderId);
                                                        }
                                                    }
                                            );
                                            Map<Long, String> l1Map = this.getL1DetailsMap(auctionDetailId);
                                            String l1Data = l1Map.get(itemDetailId);
                                            Long bidderId = Long.parseLong(l1Data.split("_")[0]);
                                            BigDecimal finalBid = new BigDecimal(l1Data.split("_")[1]);
                                            BigDecimal maxPrice = new BigDecimal(obj[3].toString());
                                            AuctionBidDetail auctionBidDetail = auctionBidDetailRepository.getPreBidDetailsByAuctionItemDetailIdAndUserLoginId(itemDetailId, finalVendorBasedUponTime);
                                            //CHECK IF CURRENT PRICE IS GREATER THAN ITEM'S RESERVE PRICE

                                            //FINAL BID IS BASED UPON 2ND HIGHEST MAX BID
                                            //SO IF SOMEONE PUT HIGHER BID AND EXITED EARLIER THE L1 BIDDER WOULD GET LOSS
                                            //SO FIND BEST POSSIBLE BID
                                            BigDecimal bestBid = new BigDecimal(currentPrice.toString());
                                            if ((reservePrice.compareTo(bestBid) == -1 || reservePrice.compareTo(bestBid) == 0) && (maxPrice.compareTo(reservePrice) == 1 || maxPrice.compareTo(reservePrice) == 0)) {
                                                auctionBidDetail.setCstatus(1);
                                                auctionBidDetails.add(auctionBidDetail);
                                                soldItems.add(itemDetailId);
                                                timeWisesoldItems.add(itemDetailId);

                                                List<AuctionBidDetail> timeWiseRejectedVendors = auctionBidDetailRepository.findTimeWiseRejectList(timeWiseRejectedVendorList, itemDetailId);
                                                for (AuctionBidDetail bidDtl : timeWiseRejectedVendors) {
                                                    bidDtl.setCstatus(2);
                                                    bidDtl.setCpAtExit(bestBid);
                                                    auctionBidDetails.add(bidDtl);
                                                }
                            /*AuctionBidHistory auctionBidHistory = new AuctionBidHistory();
                            auctionBidHistory.setBidAmount(finalBid);
                            auctionBidHistory.setAuctionBidDetail(auctionBidDetail);
                            auctionBidHistory.setAuctionDetail(auctionDetail);
                            auctionBidHistory.setCreatedOn(new Date());
                            auctionBidHistory.setCreatedBy(Long.valueOf(obj[1].toString()));
                            auctionBids.add(auctionBidHistory);*/
                                                AuctionItemL1Detail auctionItemL1Detail = new AuctionItemL1Detail();
                                                auctionItemL1Detail.setCstatus(1);
                                                auctionItemL1Detail.setAmount(bestBid);
                                                auctionItemL1Detail.setAuctionItemDetail(new AuctionItemDetail(itemDetailId));
                                                auctionItemL1Detail.setUserLogin(new UserLogin(finalVendorBasedUponTime));
                                                auctionItemL1Detail.setAuctionDetail(auctionDetail);
                                                l1Details.add(auctionItemL1Detail);
                                                dynUpdate = true;
                                            } else {
                                                if(!unsoldItems.contains(itemDetailId)) {
                                                    AuctionItemL1Detail auctionItemL1Detail = new AuctionItemL1Detail();
                                                    auctionItemL1Detail.setCstatus(3);
                                                    auctionItemL1Detail.setAmount(bestBid);
                                                    auctionItemL1Detail.setAuctionItemDetail(new AuctionItemDetail(itemDetailId));
                                                    auctionItemL1Detail.setUserLogin(null);
                                                    auctionItemL1Detail.setAuctionDetail(auctionDetail);
                                                    l1Details.add(auctionItemL1Detail);
                                                    unsoldItems.add(itemDetailId);
                                                    dynUpdate = true;
                                                }
                                            }
                                        }
                                    }

                                }
                                else{
                                    if(!unsoldItems.contains(itemDetailId)) {
                                        AuctionItemL1Detail auctionItemL1Detail = new AuctionItemL1Detail();
                                        auctionItemL1Detail.setCstatus(3);
                                        auctionItemL1Detail.setAmount(currentPrice);
                                        auctionItemL1Detail.setAuctionItemDetail(new AuctionItemDetail(itemDetailId));
                                        auctionItemL1Detail.setUserLogin(null);
                                        auctionItemL1Detail.setAuctionDetail(auctionDetail);
                                        l1Details.add(auctionItemL1Detail);
                                        unsoldItems.add(itemDetailId);
                                        dynUpdate = true;
                                    }
                                }

                            } else {
                                //CHECK IF BIDDER IS IN LEAGUE FOR CURRENT ITEM OR NOT
                                if (Integer.parseInt(obj[9].toString()) == 0) {

                                    BigDecimal maxPrice = new BigDecimal(obj[3].toString());
                                    //CHECKING IF CURRENT PRICE IS NOT EXCEEDING BIDDER'S MAX BID
                                    if (currentPrice.compareTo(maxPrice) != 1) {
                                /*AuctionBidHistory auctionBidHistory = new AuctionBidHistory();
                                auctionBidHistory.setBidAmount(new BigDecimal(obj[5].toString()).add(new BigDecimal(String.valueOf(currentIteration))));
                                auctionBidHistory.setAuctionBidDetail(new AuctionBidDetail(Long.valueOf(obj[2].toString())));
                                auctionBidHistory.setAuctionDetail(auctionDetail);
                                auctionBidHistory.setCreatedOn(new Date());
                                auctionBidHistory.setCreatedBy(Long.valueOf(obj[1].toString()));
                                auctionBids.add(auctionBidHistory);*/
                                    } else {
                                        //SET IF CURRENT PRICE IS GREATER THAN MAX BID (OUT OF ITEM)
                                        dynUpdate = true;
                                        AuctionBidDetail auctionBidDetail = auctionBidDetailRepository.findById(Long.parseLong(obj[2].toString())).get();
                                        if (auctionBidDetail.getCstatus() != 3) {
                                            auctionBidDetail.setCstatus(2);
                                            //ADDED AT LAST TO SEE AT WHICH RATE BIDDER GOT OUT BY SYSTEM
                                            auctionBidDetail.setCpAtExit(currentPrice);
                                        }
                                        auctionBidDetails.add(auctionBidDetail);
                                    }
                                }
                            }
                        }
                    }
            }
            }
            if(auctionBidDetails != null && !auctionBidDetails.isEmpty()) {
                auctionBidDetailRepository.saveAll(auctionBidDetails);
            }
            //auctionBidHistoryRepository.saveAll(auctionBids);
            if(l1Details != null && !l1Details.isEmpty()) {
                auctionItemL1DetailRepository.saveAll(l1Details);
            }
            for(AuctionItemDetail auctionItemDetail:currentActiveItems) {
                int eligibleBidders=0;
                List<Object[]> ls = auctionItemDetailRepository.getItemWiseEligibleBidderCount(auctionItemDetail.getAuctionItemDetailId());
                if(ls != null && !ls.isEmpty()){
                    eligibleBidders = Integer.parseInt(ls.get(0)[1].toString());
                }
                if (eligibleBidders == 0 && auctionItemL1DetailRepository.getItemStatus(auctionItemDetail.getAuctionItemDetailId()) == null) {
                    AuctionItemL1Detail auctionItemL1Detail = new AuctionItemL1Detail();
                    auctionItemL1Detail.setCstatus(3);
                    auctionItemL1Detail.setAmount(auctionItemDetail.getCurrentPrice());
                    auctionItemL1Detail.setAuctionItemDetail(auctionItemDetail);
                    auctionItemL1Detail.setUserLogin(null);
                    auctionItemL1Detail.setAuctionDetail(auctionDetail);
                    l1Details.add(auctionItemL1Detail);
                    unsoldItems.add(auctionItemDetail.getAuctionItemDetailId());
                    dynUpdate = true;
                }
            }
            if(unsoldItems != null && !unsoldItems.isEmpty()){
                List<AuctionBidDetail> items = auctionBidDetailRepository.getAuctionBidDetailsByAuctionItemDetailId(unsoldItems);
                if(items != null && !items.isEmpty()){
                    items.stream().forEach(
                            item -> item.setCstatus(3)
                    );
                    auctionBidDetailRepository.saveAll(items);
                    System.out.println("ITEMS UNSOLD FLAG SET");
                }
            }
            currentActiveItems.stream().forEach(
                    item -> {
                        if(itemWiseSchedulerCount.contains(item.getAuctionItemDetailId())) {
                            item.setCurrentPrice(item.getCurrentPrice().add(new BigDecimal(Integer.valueOf(item.getIncrement()))));
                            item.setSchedulerCount(item.getSchedulerCount() + 1);
                        }
                        if(soldItems.contains(item.getAuctionItemDetailId())) {
                            item.setCstatus(1);
                        }
                    }
            );
            auctionItemDetailRepository.saveAll(currentActiveItems);
        }

        if(dynUpdate){
            //DYNAMIC UPDATE SERVER CALL
            this.template.convertAndSend("/broadcast/biddingdashboarddyn/" + auctionDetailId, new SimpleDateFormat("HH:mm:ss").format(new Date()) + "-" + auctionDetailId);
        }
        else {
            //STATIC UPDATE CALL
            this.template.convertAndSend("/broadcast/biddingdashboard/" + auctionDetailId, new SimpleDateFormat("HH:mm:ss").format(new Date()) + "-" + auctionDetailId);
        }
        return vbool;
    }

    public Map<Long,Integer> getItemWiseEligibleBiddersCount(Long auctionDetailId){
        Map<Long,Integer> eligibleVendorMap = null;
        List<Object[]> eligibleVendorsCount = auctionBidDetailRepository.getItemWiseEligibleBiddersCount(auctionDetailId);
        if(eligibleVendorsCount != null && !eligibleVendorsCount.isEmpty()){
            eligibleVendorMap = new HashMap<>();
            for(Object[] obj:eligibleVendorsCount){
                eligibleVendorMap.put(Long.parseLong(obj[0].toString()),Integer.parseInt(obj[1].toString()));
            }
        }
        return eligibleVendorMap;
    }
    public Map<Long,String> getL1DetailsMap(Long auctionDetailId){
        Map<Long,String> l1Map = null;
        List<Object[]> l1Data = auctionBidDetailRepository.getL1Details(auctionDetailId);
        if(l1Data != null && !l1Data.isEmpty()){
            l1Map = new HashMap<>();
            for(Object[] obj:l1Data){
                if(!new BigDecimal(obj[3].toString()).equals(new BigDecimal(0))) {
                    l1Map.put(Long.parseLong(obj[0].toString()), Long.parseLong(obj[2].toString()) + "_" + new BigDecimal(obj[3].toString()));
                }
            }
        }
        return l1Map;
    }

    public BigDecimal findBestBid(Long auctionItemDetailId){
        List<Object[]> lstBidBasedUponBidder = auctionBidDetailRepository.findBestBidBasedUpon2ndHighestBid(auctionItemDetailId);
        List<Object[]> lstBidBasedUponItemCurrentPrice = auctionBidDetailRepository.findBestBidBasedUponItemCurrentPrice(auctionItemDetailId);
        BigDecimal bidBasedUponBidder = new BigDecimal( lstBidBasedUponBidder.get(0)[1].toString());
        BigDecimal bidBasedUponItemCurrentPrice = new BigDecimal( lstBidBasedUponItemCurrentPrice.get(0)[1].toString());
        BigDecimal lowestBid = bidBasedUponBidder.compareTo(bidBasedUponItemCurrentPrice) == 0 ? bidBasedUponBidder : bidBasedUponBidder.compareTo(bidBasedUponItemCurrentPrice) == -1 ?  bidBasedUponBidder : bidBasedUponItemCurrentPrice;
        AuctionItemDetail auctionItemDetail = auctionItemDetailRepository.findById(auctionItemDetailId).get();
        return lowestBid.compareTo(auctionItemDetail.getReservePrice()) == -1 ? auctionItemDetail.getReservePrice() : lowestBid;
    }

    public void getNextItems(Long auctionDetailId){
        List<AuctionItemDetail> currentActiveItems = auctionItemDetailRepository.getCurrentActiveItems(auctionDetailId);
        if(currentActiveItems != null && !currentActiveItems.isEmpty()){

        }
    }

    public BigDecimal findBestBidFromAllCriteria(Long auctionItemDetailId){
        AuctionItemDetail auctionItemDetail = auctionItemDetailRepository.findById(auctionItemDetailId).get();
        BigDecimal reservePrice = auctionItemDetail.getReservePrice();
        BigDecimal currentPrice = auctionItemDetail.getCurrentPrice();
        BigDecimal increment = new BigDecimal(auctionItemDetail.getIncrement());
        List<Object[]> lastExitBidList = auctionBidDetailRepository.getLastExitBidWithBidDetailId(auctionItemDetailId);
        if(lastExitBidList != null && !lastExitBidList.isEmpty()){
            List<Object[]> map = new ArrayList<>();
            BigDecimal maxNum = new BigDecimal(0);
            //FIND MAX BID AND IT'S BIDDETAILID
            for(Object[] obj:lastExitBidList){
                if(obj[1] != null){
                    BigDecimal currentNumber = new BigDecimal(obj[1].toString());
                    if(currentNumber.compareTo(maxNum) == 1){
                        map.clear();
                        Object[] objs = new Object[2];
                        objs[0] = Long.parseLong(obj[0].toString());
                        objs[1] = currentNumber;
                        map.add(objs);
                        maxNum = currentNumber;
                    }
                    else if(currentNumber.compareTo(maxNum) == 0){
                        Object[] objs = new Object[2];
                        objs[0] = Long.parseLong(obj[0].toString());
                        objs[1] = currentNumber;
                        map.add(objs);
                        maxNum = currentNumber;
                    }
                }
            }
            if(map != null && !map.isEmpty()){
                int mapSize = map.size();
                if(mapSize == 1){
                    Long bidDetailId = null;
                    BigDecimal finalBid =  null;
                    for (Object[] obj:map) {
                        bidDetailId = Long.parseLong(obj[0].toString());
                    }
                    BigDecimal maxBidOfLastExitedVendor = auctionBidDetailRepository.findById(bidDetailId).get().getMaxBid();
                    BigDecimal cpAtExitOfLastExitedVendor = auctionBidDetailRepository.findById(bidDetailId).get().getCpAtExit();

                    if(maxBidOfLastExitedVendor.compareTo(reservePrice) != -1){
                        if(ifPriceHasReached(auctionItemDetail,maxBidOfLastExitedVendor)) {
                            finalBid = maxBidOfLastExitedVendor.add(increment);
                        }else{
                            finalBid = cpAtExitOfLastExitedVendor.compareTo(reservePrice) != -1 ? cpAtExitOfLastExitedVendor.add(increment) : reservePrice.add(increment);
                        }
                    }
                    else{
                        finalBid = reservePrice.add(increment);
                    }
                    return finalBid.compareTo(reservePrice) == -1 ? reservePrice.add(increment) : finalBid;
                }
                else{
                    //if last few rejected bids are same then write here
                    Long bidDetailId = null;
                    BigDecimal lastExitBid =  null;
                    BigDecimal finalBid = null;
                    for (Object[] obj:map) {
                        bidDetailId = Long.parseLong(obj[0].toString());
                        lastExitBid = new BigDecimal(obj[1].toString());
                        BigDecimal maxBidOfLastExitedVendor = auctionBidDetailRepository.findById(bidDetailId).get().getMaxBid();
                        if(ifPriceHasReached(auctionItemDetail,maxBidOfLastExitedVendor)){
                            finalBid = maxBidOfLastExitedVendor.add(increment);
                            break;
                        }else{
                            finalBid = lastExitBid.add(increment);
                            break;
                        }
                    }
                    return finalBid.compareTo(reservePrice) == -1 ? reservePrice.add(increment) : finalBid;
                }
            }
        }
        //WHEN ONLY ONE BIDDER IS THERE WITH MBP>RP
        else{
            BigDecimal finalBid = new BigDecimal(reservePrice.toString());
            return finalBid;

        }
        return null;
    }

    public boolean ifPriceHasReached(AuctionItemDetail auctionItemDetail,BigDecimal maxPrice){
        return auctionItemDetail.getCurrentPrice().compareTo(maxPrice) == 1 ? true:false;
    }

    public Map<Long,Integer> getItemWiseEligibleBiddersCountByItemDetailId(Long auctionDetailId,Long auctionItemDetailId){
        Map<Long,Integer> eligibleVendorMap = null;
        List<Object[]> eligibleVendorsCount = auctionBidDetailRepository.getItemWiseEligibleBiddersCountByAuctionItemDetailId(auctionDetailId,auctionItemDetailId);
        if(eligibleVendorsCount != null && !eligibleVendorsCount.isEmpty()){
            eligibleVendorMap = new HashMap<>();
            for(Object[] obj:eligibleVendorsCount){
                eligibleVendorMap.put(Long.parseLong(obj[0].toString()),Integer.parseInt(obj[1].toString()));
            }
        }
        return eligibleVendorMap;
    }

    public void checkAndUpdateLastBidderCriteria(Long auctionDetailId,Long auctionItemDetailId){
        List<Object[]> liveBidDataForItem = auctionBidDetailRepository.getLiveBidDataByAuctionItemDetailId(auctionDetailId,auctionItemDetailId);
        if(liveBidDataForItem != null && !liveBidDataForItem.isEmpty()) {
            for (Object[] obj : liveBidDataForItem) {
                    Map<Long, String> l1Map = this.getL1DetailsMap(auctionDetailId);
                    String l1Data = l1Map.get(auctionItemDetailId);
                    Long bidderId = Long.parseLong(l1Data.split("_")[0]);
                    BigDecimal finalBid = new BigDecimal(l1Data.split("_")[1]);
                    BigDecimal reservePrice = new BigDecimal(obj[10].toString());
                    BigDecimal maxPrice = new BigDecimal(obj[3].toString());
                    AuctionBidDetail auctionBidDetail = auctionBidDetailRepository.getPreBidDetailsByAuctionItemDetailIdAndUserLoginId(auctionItemDetailId, bidderId);
                    BigDecimal bestBid = this.findBestBidFromAllCriteria(auctionItemDetailId);
                    if ((reservePrice.compareTo(bestBid) == -1 || reservePrice.compareTo(bestBid) == 0) && maxPrice.compareTo(reservePrice) == 1) {
                        auctionBidDetail.setCstatus(1);
                        auctionBidDetailRepository.save(auctionBidDetail);
                        AuctionItemL1Detail auctionItemL1Detail = new AuctionItemL1Detail();
                        auctionItemL1Detail.setCstatus(1);
                        auctionItemL1Detail.setAmount(bestBid);
                        auctionItemL1Detail.setAuctionItemDetail(new AuctionItemDetail(auctionItemDetailId));
                        auctionItemL1Detail.setUserLogin(new UserLogin(bidderId));
                        auctionItemL1Detail.setAuctionDetail(new AuctionDetail(auctionDetailId));
                        auctionItemL1DetailRepository.save(auctionItemL1Detail);
                    }
                    else {
                        if (auctionBidDetail.getMaxBid().compareTo(reservePrice) == 0) {
                            auctionBidDetail.setCstatus(1);
                            auctionBidDetailRepository.save(auctionBidDetail);
                            AuctionItemL1Detail auctionItemL1Detail = new AuctionItemL1Detail();
                            auctionItemL1Detail.setCstatus(1);
                            auctionItemL1Detail.setAmount(auctionBidDetail.getMaxBid());
                            auctionItemL1Detail.setAuctionItemDetail(new AuctionItemDetail(auctionItemDetailId));
                            auctionItemL1Detail.setUserLogin(new UserLogin(bidderId));
                            auctionItemL1Detail.setAuctionDetail(new AuctionDetail(auctionDetailId));
                            auctionItemL1DetailRepository.save(auctionItemL1Detail);
                        } else{
                            AuctionItemL1Detail auctionItemL1Detail = new AuctionItemL1Detail();
                        auctionItemL1Detail.setCstatus(3);
                        auctionItemL1Detail.setAmount(bestBid);
                        auctionItemL1Detail.setAuctionItemDetail(new AuctionItemDetail(auctionItemDetailId));
                        auctionItemL1Detail.setUserLogin(null);
                        auctionItemL1Detail.setAuctionDetail(new AuctionDetail(auctionDetailId));
                        auctionItemL1DetailRepository.save(auctionItemL1Detail);
                        List<AuctionBidDetail> items = auctionBidDetailRepository.getAuctionBidDetailsByAuctionItemDetailId(List.of(auctionItemDetailId));
                        if (items != null && !items.isEmpty()) {
                            items.stream().forEach(
                                    item -> item.setCstatus(3)
                            );
                            auctionBidDetailRepository.saveAll(items);
                            System.out.println("ITEMS UNSOLD FLAG SET");
                        }
                    }
                    }
            }
        }
    }

    public boolean checkDuplicateMaxBidForFinalVendors(Long auctionItemDetailId){
        boolean vbool = false;
        int eligibleVendors=auctionBidDetailRepository.getItemWiseEligibleBiddersCountByAuctionItemDetailId(auctionItemDetailId);
        if(eligibleVendors > 1){
            int distinctMaxBidCount = auctionBidDetailRepository.getCountdistinctMaxBid(auctionItemDetailId);
            if(distinctMaxBidCount == 1){
                vbool = true;
            }
        }
        return vbool;
    }

    public void allotItemBasedUponTimeCriteria(Long auctionItemDetailId,BigDecimal finalBid){
        List<Long> timeCriteriaList = auctionBidDetailRepository.getBiddersBasedUponTimeCriteria(auctionItemDetailId);
        if(timeCriteriaList != null && !timeCriteriaList.isEmpty()) {
            Long finalVendorBasedUponTime = timeCriteriaList.get(0);

        }
    }

    public boolean copyAuction(Long auctionDetailId){
        //Copy Auction Data
        AuctionDetail existingAuctionDetail = auctionDetailRepository.findById(auctionDetailId).get();
        AuctionDetail newAuctionDetail = new AuctionDetail();
        newAuctionDetail.setAuctionBrief(existingAuctionDetail.getAuctionBrief());
        newAuctionDetail.setCreatedBy(existingAuctionDetail.getCreatedBy());
        newAuctionDetail.setStartDate(existingAuctionDetail.getStartDate());
        newAuctionDetail.setEndDate(existingAuctionDetail.getEndDate());
        newAuctionDetail.setCstatus(0);
        newAuctionDetail.setCreatedOn(new Date());
        auctionDetailRepository.save(newAuctionDetail);
        List<AuctionItemDetail> existingAuctionItems = auctionItemDetailRepository.getAuctionItemsByAuctionDetailId(auctionDetailId);
        List<AuctionItemDetail> newAuctionItems = new ArrayList<>();
        AuctionItemDetail auctionItemDetail = null;
        for(AuctionItemDetail existingAuctionItemDetail:existingAuctionItems){
            auctionItemDetail = new AuctionItemDetail();
            auctionItemDetail.setAuctionDetail(newAuctionDetail);
            auctionItemDetail.setCreatedBy(existingAuctionDetail.getCreatedBy());
            auctionItemDetail.setCreatedOn(new Date());
            auctionItemDetail.setCstatus(0);
            auctionItemDetail.setIsActive(0);
            auctionItemDetail.setSerialNo(existingAuctionItemDetail.getSerialNo());
            auctionItemDetail.setLotNo(existingAuctionItemDetail.getLotNo());
            auctionItemDetail.setCategory(existingAuctionItemDetail.getCategory());
            auctionItemDetail.setGrade(existingAuctionItemDetail.getGrade());
            auctionItemDetail.setItemPackage(existingAuctionItemDetail.getItemPackage());
            auctionItemDetail.setBasePrice(existingAuctionItemDetail.getBasePrice());
            auctionItemDetail.setCurrentPrice(existingAuctionItemDetail.getBasePrice());
            auctionItemDetail.setReservePrice(existingAuctionItemDetail.getReservePrice());
            auctionItemDetail.setIncrement(existingAuctionItemDetail.getIncrement());
            auctionItemDetailRepository.save(auctionItemDetail);
            List<AuctionBidDetail> existingAuctionBidDetails = auctionBidDetailRepository.getAuctionBidDetailByAuctionItemDetailId(existingAuctionItemDetail.getAuctionItemDetailId());
            List<AuctionBidDetail> toBeAdded = new ArrayList<>();
                for(AuctionBidDetail auctionBidDetail:existingAuctionBidDetails){
                    AuctionBidDetail newAbd =  new AuctionBidDetail();
                    newAbd.setAuctionDetail(newAuctionDetail);
                    newAbd.setAuctionItemDetail(auctionItemDetail);
                    newAbd.setUserLogin(auctionBidDetail.getUserLogin());
                    newAbd.setMaxBid(auctionBidDetail.getMaxBid());
                    newAbd.setIsActive(1);
                    newAbd.setCstatus(0);
                    newAbd.setCreatedBy(auctionBidDetail.getCreatedBy());
                    newAbd.setCreatedOn(new Date());
                    toBeAdded.add(newAbd);
                }
            auctionBidDetailRepository.saveAll(toBeAdded);
        }
        return true;
    }
}
