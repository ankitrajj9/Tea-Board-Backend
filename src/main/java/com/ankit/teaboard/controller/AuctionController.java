package com.ankit.teaboard.controller;

import com.ankit.teaboard.dto.apidto.*;
import com.ankit.teaboard.dto.entitydto.AuctionBidDetailDTO;
import com.ankit.teaboard.dto.entitydto.AuctionDetailDTO;
import com.ankit.teaboard.dto.entitydto.AuctionItemDetailDTO;
import com.ankit.teaboard.dto.entitydto.UserLoginDTO;
import com.ankit.teaboard.entity.*;
import com.ankit.teaboard.repository.*;
import com.ankit.teaboard.service.AuctionSchedulerService;
import com.ankit.teaboard.service.AuctionService;
import com.ankit.teaboard.service.JWTService;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("http://teaboard.co.in:4200")
public class AuctionController {

    @Autowired
    private UserLoginRepository userLoginRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuctionDetailRepository auctionDetailRepository;
    @Autowired
    private AuctionItemDetailRepository auctionItemDetailRepository;

    @Autowired
    private AuctionBidDetailRepository auctionBidDetailRepository;

    @Autowired
    private AuctionBidHistoryRepository auctionBidHistoryRepository;
    @Autowired
    private AuctionSchedulerService auctionSchedulerService;

    @Autowired
    private AuctionSchedulerRepository auctionSchedulerRepository;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private AuctionItemL1DetailRepository auctionItemL1DetailRepository;

    @GetMapping("/test")
    public String test(){
        return "test works";
    }
    @PostMapping("/saveuser")
    public ResponseEntity<ResponseDTO> saveUser(@RequestBody UserLoginDTO userLoginDTO){
        UserLogin userLogin = modelMapper.map(userLoginDTO,UserLogin.class);
        userLogin.setPassword(bCryptPasswordEncoder.encode(userLoginDTO.getPassword()));
        userLoginRepository.save(userLogin);
        return ResponseEntity.ok(new ResponseDTO("User Created Successfully",200));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> getToken(@RequestBody UserLoginDTO userLoginDTO){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLoginDTO.getLoginId(),userLoginDTO.getPassword()));
        if(authentication.isAuthenticated()){
            UserLogin userLogin = userLoginRepository.getUserLoginByLoginId(userLoginDTO.getLoginId());
            String token = jwtService.generateToken(userLogin.getLoginId());
            return ResponseEntity.ok(new AuthResponseDTO(userLogin.getUserLoginId(),userLogin.getUserTypeId(),token,200,"Success"));
        }
        else{
            return ResponseEntity.ok(new AuthResponseDTO(0L,0,null,401,"Bad Credentials"));
        }
    }

    @GetMapping("/getallusers")
    public List<UserLoginDTO> getAllUsers(){
        List<UserLoginDTO> users = userLoginRepository.findAll().stream().map(
                userLogin -> {
                    UserLoginDTO userLoginDTO = modelMapper.map(userLogin,UserLoginDTO.class);
                    return userLoginDTO;
                }
        ).collect(Collectors.toList());
        return users;
    }

    @GetMapping("/getauctionitems/{auctionDetailId}")
    public List<AuctionItemDetailDTO> getAllUsers(@PathVariable("auctionDetailId")Long auctionDetailId){
        List<AuctionItemDetailDTO> auctionItems = auctionItemDetailRepository.getAuctionItemsByAuctionDetailId(auctionDetailId)
                .stream().map(
                        item ->{
                            AuctionItemDetailDTO auctionItemDetailDTO = modelMapper.map(item,AuctionItemDetailDTO.class);
                            return auctionItemDetailDTO;
                        }
                ).collect(Collectors.toList());
        return auctionItems;
    }

    @GetMapping("/getuserbyuserloginid/{userLoginId}")
    public UserLoginDTO getUserLoginByUserLoginId(@PathVariable Long userLoginId){
        UserLoginDTO user = modelMapper.map(userLoginRepository.findById(userLoginId).get(),UserLoginDTO.class);
        return user;
    }

    @GetMapping("/getauctionbyauctiondetailid/{auctionDetailId}")
    public AuctionDetailDTO getAuctionByauctionDetailId(@PathVariable Long auctionDetailId){
        AuctionDetailDTO auction = modelMapper.map(auctionDetailRepository.findById(auctionDetailId).get(),AuctionDetailDTO.class);
        return auction;
    }
    @PostMapping("/addauction")
    public ResponseEntity<ResponseDTO> addAuction(@RequestBody AuctionDetailDTO auctionDetailDTO){
        AuctionDetail auctionDetail = modelMapper.map(auctionDetailDTO,AuctionDetail.class);
        auctionDetail.setCreatedOn(new Date());
        auctionDetailRepository.save(auctionDetail);
        return ResponseEntity.ok(new ResponseDTO("Auction Created Successfully",200));
    }

    @GetMapping("/getallauctions")
    public List<AuctionDetailDTO> getAllAuctions(){
        List<AuctionDetailDTO> auctions = auctionDetailRepository.getAllAuctions().stream().map(
                auctionDetail -> {
                    AuctionDetailDTO auctionDetailDTO = modelMapper.map(auctionDetail,AuctionDetailDTO.class);
                    return auctionDetailDTO;
                }
        ).collect(Collectors.toList());
        return auctions;
    }
    @GetMapping("/getallliveauctions")
    public List<AuctionDetailDTO> getAllLiveAuctions(){
        List<AuctionDetailDTO> auctions = auctionDetailRepository.getLiveAuctions(new Date()).stream().map(
                auctionDetail -> {
                    AuctionDetailDTO auctionDetailDTO = modelMapper.map(auctionDetail,AuctionDetailDTO.class);
                    return auctionDetailDTO;
                }
        ).collect(Collectors.toList());
        return auctions;
    }

    @GetMapping("/getallpendingauctions")
    public List<AuctionDetailDTO> getAllPendingAuctions(){
        List<AuctionDetailDTO> auctions = auctionDetailRepository.getPendingAuctions().stream().map(
                auctionDetail -> {
                    AuctionDetailDTO auctionDetailDTO = modelMapper.map(auctionDetail,AuctionDetailDTO.class);
                    return auctionDetailDTO;
                }
        ).collect(Collectors.toList());
        return auctions;
    }

    @PostMapping("/addauctionitem")
    public void addAuction(@RequestBody AuctionItemDetailDTO auctionItemDetailDTO){
        AuctionItemDetail auctionItemDetail = modelMapper.map(auctionItemDetailDTO,AuctionItemDetail.class);
        auctionItemDetail.setCreatedOn(new Date());
        auctionItemDetailRepository.save(auctionItemDetail);
    }

    @PostMapping("/addprebiddetails")
    public void addPreBidDetails(@RequestParam("bidderId") Long bidderId,@RequestParam("auctionItemDetailId") Long auctionItemDetailId,@RequestParam("maxBid") BigDecimal maxBid){
        AuctionItemDetail auctionItemDetail = auctionItemDetailRepository.findById(auctionItemDetailId).get();
        Long auctionId = auctionItemDetail.getAuctionDetail().getAuctionDetailId();
        AuctionBidDetail auctionBidDetail = new AuctionBidDetail();
        auctionBidDetail.setAuctionDetail(new AuctionDetail(auctionId));
        auctionBidDetail.setAuctionItemDetail(auctionItemDetail);
        auctionBidDetail.setUserLogin(new UserLogin(bidderId));
        auctionBidDetail.setMaxBid(maxBid);
        auctionBidDetail.setIsActive(1);
        auctionBidDetail.setCreatedBy(bidderId);
        auctionBidDetail.setCreatedOn(new Date());
        auctionBidDetailRepository.save(auctionBidDetail);
    }

    @PostMapping("/addallprebiddetails")
    public ResponseEntity<ResponseDTO> addAllPreBidDetails(@RequestBody List<PreBidDetailDTO> preBidDetails){
        Long bidderId = preBidDetails.get(0).getBidderId();
        Long auctionItemDetailId = preBidDetails.get(0).getAuctionItemDetailId();
        AuctionBidDetail savedBidDtl = auctionBidDetailRepository.getPreBidDetailsByAuctionItemDetailIdAndUserLoginId(auctionItemDetailId,bidderId);
        if(savedBidDtl != null) {
            Long auctionDetailId = savedBidDtl.getAuctionDetail().getAuctionDetailId();
            List<AuctionBidDetail> savedBidDtlToDelete = auctionBidDetailRepository.getPreBidDetailsByAuctionDetailIdAndUserLoginId(auctionDetailId, bidderId);
            if (savedBidDtlToDelete != null && !savedBidDtlToDelete.isEmpty()) {
                auctionBidDetailRepository.deleteAll(savedBidDtlToDelete);
                System.out.println("SAVED BID DELETED");
            }
        }
        List<AuctionBidDetail> bidDetails = new ArrayList<>();
        for(PreBidDetailDTO preBid:preBidDetails) {
            AuctionItemDetail auctionItemDetail = auctionItemDetailRepository.findById(preBid.getAuctionItemDetailId()).get();
            Long auctionId = auctionItemDetail.getAuctionDetail().getAuctionDetailId();
            AuctionBidDetail auctionBidDetail = new AuctionBidDetail();
            auctionBidDetail.setAuctionDetail(new AuctionDetail(auctionId));
            auctionBidDetail.setAuctionItemDetail(auctionItemDetail);
            auctionBidDetail.setUserLogin(new UserLogin(preBid.getBidderId()));
            auctionBidDetail.setMaxBid(preBid.getMaxBid());
            auctionBidDetail.setIsActive(1);
            auctionBidDetail.setCreatedBy(preBid.getBidderId());
            auctionBidDetail.setCreatedOn(new Date());
            bidDetails.add(auctionBidDetail);
        }
        auctionBidDetailRepository.saveAll(bidDetails);
        System.out.println("SAVED BID DETAILS");
        return ResponseEntity.ok(new ResponseDTO("Pre Bid Details Added Successfully",200));
    }

    @PostMapping("/aproveauction")
    public ResponseEntity<ResponseDTO> approveAuction(@RequestBody AuctionDetailDTO auctionDetailDTO){
        AuctionDetail auctionDetail = modelMapper.map(auctionDetailDTO,AuctionDetail.class);
        auctionDetail.setCstatus(1);
        auctionDetailRepository.save(auctionDetail);
        List<AuctionScheduler> existingAuctionScheduler =auctionSchedulerRepository.getAuctionSchedulerByAuctionDetailId(auctionDetail.getAuctionDetailId());
        if(existingAuctionScheduler != null && !existingAuctionScheduler.isEmpty()){
            existingAuctionScheduler.stream().forEach(
                    scheduler ->{
                            scheduler.setIsActive(0);
                            auctionSchedulerRepository.save(scheduler);
                    }
            );
        }
        AuctionScheduler auctionScheduler = new AuctionScheduler();
        auctionScheduler.setSchedulerName("AUCTION_ID_"+auctionDetail.getAuctionDetailId());
        auctionScheduler.setAuctionDetail(auctionDetail);
        auctionScheduler.setScheduledTime(auctionDetail.getStartDate());
        auctionScheduler.setEndTime(auctionDetail.getEndDate());
        auctionScheduler.setCreatedOn(new Date());
        auctionScheduler.setCreatedBy(auctionDetail.getCreatedBy());
        auctionScheduler.setIsActive(1);
        auctionScheduler.setCstatus(0);
        auctionSchedulerRepository.save(auctionScheduler);
        List<AuctionItemDetail> first3Items = auctionItemDetailRepository.getTop3Items(auctionDetail.getAuctionDetailId());
        first3Items.stream().forEach(
                item -> item.setIsActive(1)
        );
        auctionItemDetailRepository.saveAll(first3Items);
        /*auctionSchedulerService.taskScheduler().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                System.out.println("Scheduler executed : "+auctionScheduler.getSchedulerName() +" AT "+ new Date());
                auctionService.saveBidPeriodically(auctionDetail.getAuctionDetailId());
            }
        }, auctionScheduler.getScheduledTime(),5000l);*/
        auctionSchedulerService.createAuctionScheduler(auctionScheduler);
        return ResponseEntity.ok(new ResponseDTO("Auction Approved Successfully",200));
    }

    @PostMapping("/uploadauctionitemexcel")
    public ResponseEntity<ResponseDTO> uploadImage(@RequestParam("excelFile") MultipartFile excel, @RequestParam("auctionDetailId") Long auctionDetailId,@RequestParam("userLoginId") Long userLoginId)
            throws IOException {
        List<AuctionItemDetail> auctionItems = auctionItemDetailRepository.getAuctionItemsByAuctionDetailId(auctionDetailId);
        if(auctionItems != null && !auctionItems.isEmpty()) {
            auctionItemDetailRepository.deleteAll(auctionItems);
        }

        if(excel != null){
            try{
                XSSFWorkbook workbook = new XSSFWorkbook(excel.getInputStream());
                XSSFSheet sheet = workbook.getSheetAt(0);
                List<AuctionItemDetail> auctionItemsToAdd = new ArrayList<>();
                for(int i=0; i<sheet.getPhysicalNumberOfRows();i++) {
                    if (i != 0){
                        AuctionItemDetail auctionItemDetail = new AuctionItemDetail();
                    auctionItemDetail.setAuctionDetail(new AuctionDetail(auctionDetailId));
                        auctionItemDetail.setCreatedBy(userLoginId);
                        auctionItemDetail.setCreatedOn(new Date());
                        auctionItemDetail.setCstatus(0);
                        auctionItemDetail.setIsActive(0);
                    XSSFRow row = sheet.getRow(i);
                    for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                        System.out.print(row.getCell(j) + " ");

                        int index = j;
                        switch (index) {
                            case 0:
                                auctionItemDetail.setSerialNo(Integer.parseInt(row.getCell(index).toString().split("[.]", 0)[0]));
                                break;
                            case 1:
                                auctionItemDetail.setLotNo(row.getCell(index).toString());
                                break;
                            case 2:
                                auctionItemDetail.setCategory(row.getCell(index).toString());
                                break;
                            case 3:
                                auctionItemDetail.setGrade(row.getCell(index).toString());
                                break;
                            case 4:
                                auctionItemDetail.setItemPackage(Integer.parseInt(row.getCell(index).toString().split("[.]", 0)[0]));
                                break;
                            case 5:
                                auctionItemDetail.setBasePrice(new BigDecimal(row.getCell(index).toString()));
                                auctionItemDetail.setCurrentPrice(new BigDecimal(row.getCell(index).toString()));
                                break;
                            case 6:
                                auctionItemDetail.setReservePrice(new BigDecimal(row.getCell(index).toString()));
                                break;
                            case 7:
                                auctionItemDetail.setIncrement(Integer.parseInt(row.getCell(index).toString().split("[.]", 0)[0]));
                                break;
                        }
                    }
                    System.out.println("");
                    auctionItemsToAdd.add(auctionItemDetail);
                }
                }
                if(auctionItemsToAdd != null && !auctionItemsToAdd.isEmpty()){
                    System.out.println(auctionItemsToAdd);
                    auctionItemDetailRepository.saveAll(auctionItemsToAdd);
                    System.out.println("ITEMS ADDED");
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
        return ResponseEntity.ok(new ResponseDTO("User Created Successfully",200));
    }

    @GetMapping("/getprebiddetails/{auctionDetailId}/{bidderId}")
    public List<PreBidDetailDTO> getPreBidDetails(@PathVariable("auctionDetailId") Long auctionDetailId,@PathVariable("bidderId") Long bidderId){
        List<PreBidDetailDTO> preBids = auctionBidDetailRepository.getPreBidDetailsByAuctionDetailIdAndUserLoginId(auctionDetailId,bidderId)
                .stream().map(
                        bidDtl -> {
                            PreBidDetailDTO preBidDetailDTO = new PreBidDetailDTO();
                            preBidDetailDTO.setMaxBid(bidDtl.getMaxBid());
                            preBidDetailDTO.setAuctionItemDetailId(bidDtl.getAuctionItemDetail().getAuctionItemDetailId());
                            preBidDetailDTO.setBidderId(bidDtl.getUserLogin().getUserLoginId());
                            return preBidDetailDTO;
                        }
                ).collect(Collectors.toList());
        return preBids;
    }

    @GetMapping("/getlivebiddetails/{auctionDetailId}/{bidderId}")
    public List<LiveBidDetailDTO> getLiveBidDetails(@PathVariable("auctionDetailId") Long auctionDetailId, @PathVariable("bidderId") Long bidderId){
        auctionService.saveBidPeriodically(auctionDetailId);
        return auctionService.getLiveBidDetails(auctionDetailId,bidderId);
      }

    @GetMapping("/getbidderwiselivebiddetails/{auctionDetailId}/{bidderId}")
    public List<LiveBidDetailDTO> getBidderWiseLiveBidDetails(@PathVariable("auctionDetailId") Long auctionDetailId, @PathVariable("bidderId") Long bidderId){
        List<LiveBidDetailDTO> liveBids = null;
        LiveBidDetailDTO liveBidDetailDTO = null;
        List<Object[]> bids = auctionBidDetailRepository.getBidderWiseLiveBidData(auctionDetailId,bidderId);
        if(bids != null && !bids.isEmpty()){
            liveBids = new ArrayList<>();
            for(Object[] obj:bids) {
                AuctionItemDetailDTO auctionItemDetailDTO = modelMapper.map(auctionItemDetailRepository.findById(Long.parseLong(obj[0].toString())).get(),AuctionItemDetailDTO.class) ;
                liveBidDetailDTO = new LiveBidDetailDTO();
                liveBidDetailDTO.setBidderId(Long.parseLong(obj[1].toString()));
                liveBidDetailDTO.setMaxBid(new BigDecimal(obj[3].toString()));
                liveBidDetailDTO.setAuctionDetailId(Long.parseLong(obj[7].toString()));
                liveBidDetailDTO.setCurrentBid(new BigDecimal(obj[8].toString()));
                liveBidDetailDTO.setAuctionItemDetail(auctionItemDetailDTO);
                int itemCstatus = Integer.parseInt(obj[9].toString());
                liveBidDetailDTO.setItemColor(itemCstatus == 0 ? "alert-dark" : itemCstatus == 1 ? "alert-success" : itemCstatus == 2 || itemCstatus == 4 ?  "alert-danger" : "alert-warning");
                liveBidDetailDTO.setIncrement(Integer.parseInt(obj[6].toString()));
                BigDecimal l1Amount = null;
                if(itemCstatus == 1) {
                    AuctionItemL1Detail l1Detail = auctionItemL1DetailRepository.getItemStatus(auctionItemDetailDTO.getAuctionItemDetailId());
                    if (l1Detail != null) {
                        l1Amount = l1Detail.getAmount();
                    }
                }
                liveBidDetailDTO.setL1Amount(l1Amount);
                liveBids.add(liveBidDetailDTO);
            }
        }
        return liveBids;
    }

    @PostMapping("/changemaxbid")
    public ResponseEntity<ResponseDTO> changeMaxBid(@RequestParam("bidderId") Long bidderId,@RequestParam("auctionItemDetailId") Long auctionItemDetailId,@RequestParam("maxBid") BigDecimal maxBid) {
        AuctionItemDetail auctionItemDetail = auctionItemDetailRepository.findById(auctionItemDetailId).get();
        Long auctionId = auctionItemDetail.getAuctionDetail().getAuctionDetailId();
        AuctionBidDetail existingAuctionBidDetail = auctionBidDetailRepository.getPreBidDetailsByAuctionItemDetailIdAndUserLoginId(auctionItemDetailId, bidderId);
        int scheduledCount = auctionItemDetail.getSchedulerCount() + 1;
        BigDecimal basePrice = auctionItemDetail.getBasePrice();
        int increment = auctionItemDetail.getIncrement();
        int currentIteration = scheduledCount * increment;
        BigDecimal currentIterationPrice = basePrice.add(new BigDecimal(String.valueOf(currentIteration)));
        System.out.println("CURRENT ITERATION PRICE : "+currentIterationPrice);
        if (maxBid.compareTo(auctionItemDetail.getCurrentPrice()) == 1 && maxBid.compareTo(currentIterationPrice) == 1){
            if (existingAuctionBidDetail.getCstatus() == 0) {
                existingAuctionBidDetail.setIsActive(0);
                existingAuctionBidDetail.setCpAtMb(auctionItemDetail.getCurrentPrice());
                auctionBidDetailRepository.save(existingAuctionBidDetail);
                AuctionBidDetail auctionBidDetail = new AuctionBidDetail();
                auctionBidDetail.setAuctionDetail(new AuctionDetail(auctionId));
                auctionBidDetail.setAuctionItemDetail(auctionItemDetail);
                auctionBidDetail.setUserLogin(new UserLogin(bidderId));
                auctionBidDetail.setMaxBid(maxBid);
                auctionBidDetail.setIsActive(1);
                auctionBidDetail.setCreatedBy(bidderId) ;
                auctionBidDetail.setCreatedOn(new Date());
                auctionBidDetail.setCstatus(0);
                auctionBidDetail.setCpAtMb(auctionItemDetail.getCurrentPrice());
                auctionBidDetailRepository.save(auctionBidDetail);
                this.template.convertAndSend("/broadcast/biddingdashboarddyn/" + auctionId, new SimpleDateFormat("HH:mm:ss").format(new Date()) + "-" + auctionId);
                return ResponseEntity.ok(new ResponseDTO("Max Bid changed Successfully", 200));
            } else if (existingAuctionBidDetail.getCstatus() == 1) {
                return ResponseEntity.ok(new ResponseDTO("Item is already alloted to you", 200));
            } else if (existingAuctionBidDetail.getCstatus() == 2 || existingAuctionBidDetail.getCstatus() == 4) {
                return ResponseEntity.ok(new ResponseDTO("Item is already sold", 200));
            } else {
                return ResponseEntity.ok(new ResponseDTO("Item is already went Unsold", 200));
            }
    }
        else{
            if(currentIterationPrice.compareTo(maxBid) == 1 || maxBid.compareTo(currentIterationPrice) == 0) {
                return ResponseEntity.ok(new ResponseDTO("Item's Price has reached iteration for amount: "+maxBid, 200));
        }
        else{
                return ResponseEntity.ok(new ResponseDTO("Item's Price has exceeded your max bid", 200));
        }
        }

    }

    @PostMapping("/exitfromitem")
    public ResponseEntity<ResponseDTO> exitFromItem(@RequestParam("bidderId") Long bidderId,@RequestParam("auctionItemDetailId") Long auctionItemDetailId){
        AuctionItemDetail auctionItemDetail = auctionItemDetailRepository.findById(auctionItemDetailId).get();
        Long auctionId = auctionItemDetail.getAuctionDetail().getAuctionDetailId();
        AuctionBidDetail existingAuctionBidDetail = auctionBidDetailRepository.getPreBidDetailsByAuctionItemDetailIdAndUserLoginId(auctionItemDetailId,bidderId);
        if(existingAuctionBidDetail.getCstatus() == 0){
            existingAuctionBidDetail.setIsActive(1);
            existingAuctionBidDetail.setCstatus(4);
            existingAuctionBidDetail.setCpAtExit(auctionItemDetail.getCurrentPrice());
            auctionBidDetailRepository.save(existingAuctionBidDetail);
            this.template.convertAndSend("/broadcast/biddingdashboarddyn/" + auctionId, new SimpleDateFormat("HH:mm:ss").format(new Date()) + "-" + auctionId);
            Map<Long,Integer> itemWiseEligibleBidders = auctionService.getItemWiseEligibleBiddersCountByItemDetailId(auctionId,auctionItemDetailId);
            if(itemWiseEligibleBidders.get(auctionItemDetailId) == 1){
                auctionService.checkAndUpdateLastBidderCriteria(auctionId,auctionItemDetailId);
            }
            return ResponseEntity.ok(new ResponseDTO("Exited from item Successfully",200));
        }
        else if(existingAuctionBidDetail.getCstatus() == 1){
            return ResponseEntity.ok(new ResponseDTO("Item is already alloted to you",200));
        }
        else if(existingAuctionBidDetail.getCstatus() == 2 || existingAuctionBidDetail.getCstatus() == 4){
            return ResponseEntity.ok(new ResponseDTO("Item is already sold",200));
        }
        else{
            return ResponseEntity.ok(new ResponseDTO("Item is already went Unsold",200));
        }

    }

    @GetMapping("/livebiddataverification/{auctionDetailId}")
    public ResponseEntity<ResponseDTO> testLiveBidData(@PathVariable("auctionDetailId") Long auctionDetailId){
        auctionService.saveBidPeriodically(auctionDetailId);
        return ResponseEntity.ok(new ResponseDTO("Live Bid Scheduler called!",200));
    }

    @GetMapping("/getreportdetails/{auctionDetailId}")
    public List<ReportDetailDTO> getReportDetails(@PathVariable("auctionDetailId") Long auctionDetailId){
        List<ReportDetailDTO> reportDetails = null;
        List<Object[]> reportData = auctionItemL1DetailRepository.getItemWiseL1H1Details(auctionDetailId);
        List<ItemExitDetailDTO> exitDetailsLst = null;
        if(reportData != null && !reportData.isEmpty()){

            reportDetails = new ArrayList<>();
            for(Object[] obj:reportData){
                List<Object[]> exitDetails = auctionBidDetailRepository.getExitDetails(Long.parseLong(obj[0].toString()));
                ReportDetailDTO reportDetailDTO = new ReportDetailDTO();
                reportDetailDTO.setAuctionItemDetailId(Long.parseLong(obj[0].toString()));
                reportDetailDTO.setLotNo(obj[1].toString());
                reportDetailDTO.setBasePrice(new BigDecimal(obj[2].toString()));
                reportDetailDTO.setReservePrice(new BigDecimal(obj[3].toString()));
                reportDetailDTO.setIncrement(Integer.parseInt(obj[4].toString()));
                reportDetailDTO.setBidderId(Long.parseLong(obj[5].toString()));
                reportDetailDTO.setLoginId(obj[6].toString());
                reportDetailDTO.setL1Amount(new BigDecimal(obj[7].toString()));
                if(exitDetails != null && !exitDetails.isEmpty()){
                    exitDetailsLst = new ArrayList<>();
                    for(Object[] exitDtl:exitDetails) {
                        ItemExitDetailDTO itemExitDetailDTO = new ItemExitDetailDTO();
                        itemExitDetailDTO.setLoginId(exitDtl[0].toString());
                        itemExitDetailDTO.setAmount(new BigDecimal(exitDtl[1].toString()));
                        itemExitDetailDTO.setMaxBid(new BigDecimal(exitDtl[2].toString()));
                        itemExitDetailDTO.setCstatus(Integer.parseInt(exitDtl[3].toString()));
                        exitDetailsLst.add(itemExitDetailDTO);
                    }
                }
                reportDetailDTO.setExitDetails(exitDetailsLst);
                reportDetails.add(reportDetailDTO);
            }
        }
        return reportDetails;
    }

    @GetMapping("/bestbidtesting/{auctionItemDetailId}")
    public BigDecimal bestBidTest(@PathVariable("auctionItemDetailId") Long auctionItemDetailId){
        BigDecimal tst = auctionService.findBestBidFromAllCriteria(auctionItemDetailId);
        return tst;
    }

    @PostMapping("/copyauction")
    public ResponseEntity<ResponseDTO> copyAuction(@RequestParam("auctionDetailId") Long auctionDetailId){
        boolean vbool = auctionService.copyAuction(auctionDetailId);
        return ResponseEntity.ok(new ResponseDTO("Auction copied successfully",200));
    }
}
