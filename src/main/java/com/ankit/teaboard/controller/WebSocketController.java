package com.ankit.teaboard.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@CrossOrigin(origins = "localhost:4200")
public class WebSocketController {

    private final SimpMessagingTemplate template;

    @Autowired
    WebSocketController(SimpMessagingTemplate template){
        this.template=template;
    }
    @MessageMapping("/send/message")
    public void onReceivedMessage(String message) {
        System.out.println(message);
        byte[] decodedBytes = Base64.decodeBase64(message);
        String decodedAuctionDetailId = new String(decodedBytes);
        this.template.convertAndSend("/broadcast/biddingdashboard/"+decodedAuctionDetailId,new SimpleDateFormat("HH:mm:ss").format(new Date())+"-"+message);
    }

}