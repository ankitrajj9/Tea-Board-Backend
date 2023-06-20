package com.ankit.teaboard.dto.entitydto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuctionDetailDTO {
    private Long auctionDetailId;
    private String auctionBrief;
    private Date createdOn;
    private Long createdBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/Kolkata",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/Kolkata",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endDate;

    private int cstatus;
}
