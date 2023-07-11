package com.ankit.teaboard.dto.entitydto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuctionItemDetailDescriptionDTO {
    private Long auctionItemDetailDescriptionId;
    @JsonIgnore
    private AuctionItemDetailDTO auctionItemDetail;
    private String origin;
    private String type;
    private String subType;
    private String mark;
    private String status;
    private String netWeight;
    private String grossWeight;
    private String totalNetWeight;
    private int manufacturePercentage;
    private String packageComments;
    private String lotType;
    private String gpNo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/Kolkata",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gpDate;
    private String invoiceNo;
    private String lspSp;
    private String pl;
    private String packageNo;
    private String warehouseName;
    private String garden;
    private String special;
    private String quality;
    private String color;
    private int age;
    private String brewer;
    private String bidder;
    private int noOfBidders;
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/Kolkata",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date activeSince;
}
