package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.web.dto.datatable.DataTableResponse;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.commons.web.resource.BaseListResource;
import com.pemc.crss.metering.dto.MeterDataListWebDto;
import com.pemc.crss.metering.event.MeterUploadEvent;
import com.pemc.crss.metering.service.MeterService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@RestController
public class MeteringResource extends BaseListResource<MeterDataListWebDto> {

    public static final String ROUTING_KEY = "meter.quantity";

    @NonNull
    private final MeterService meterService;

    @NonNull
    private final RabbitTemplate rabbitTemplate;

    @NonNull
    private final ApplicationEventPublisher eventPublisher;

    private List<MeterDataListWebDto> meterDataList;

//    public MeteringResource() {
//        long sampleSize = 25L;
//
//        meterDataList = generateSampleMeterDataList(sampleSize);
//    }

    @Deprecated
    @GetMapping("/sample")
    public String sample() {
        return "Success";
    }

    @PostMapping("/uploadheader")
    public void sendHeader(@RequestParam("transactionID") String transactionID,
                           @RequestParam("mspID") long mspID,
                           @RequestParam("fileCount") int fileCount,
                           @RequestParam("category") String category,
                           @RequestParam("username") String username) {
        // TODO: Should the file manifest be initialized?

        log.debug("Transaction ID:{}", transactionID);

        // TODO: Return headerID
        long headerID = meterService.saveHeader(transactionID, mspID, fileCount, category, username);
    }

    @PostMapping("/uploadfile")
    public void sendFile(MultipartHttpServletRequest request,
                         @RequestParam("headerID") int headerID,
                         @RequestParam("transactionID") String transactionID,
                         @RequestParam("fileName") String fileName,
                         @RequestParam("fileType") String fileType,
                         @RequestParam("fileSize") long fileSize,
                         @RequestParam("checksum") String checksum,
                         @RequestParam("category") String category) throws IOException {

        MultipartFile file = request.getFile("file");

        Message message = MessageBuilder.withBody(file.getBytes())
                .setHeader("headerID", headerID)
                .setHeader("transactionID", transactionID)
                .setHeader("fileName", fileName)
                .setHeader("fileType", fileType)
                .setHeader("fileSize", fileSize)
                .setHeader("checksum", checksum)
                .setHeader("category", category)
                .setHeaderIfAbsent("Content type", file.getContentType())
                .build();
        rabbitTemplate.send(ROUTING_KEY, message);
    }

    @PostMapping("/uploadtrailer")
    public void sendTrailer(@RequestParam("transactionID") String transactionID) {
        log.debug("Transaction ID:{}", transactionID);

        meterService.saveTrailer(transactionID);

        // TODO: Trigger notification event to list down accepted and rejected files based on validation
//        eventPublisher.publishEvent(new MeterUploadEvent(messagePayload));
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadMeterData(MultipartHttpServletRequest request,
                                                  @RequestParam("uploadType") String uploadType,
                                                  @RequestParam("noOfUploadedFiles") int noOfUploadedFiles) throws IOException {

        Map<String, Object> messagePayload = new HashMap<>();
        List<String> uploadedFiles = new ArrayList<>();

        messagePayload.put("noOfUploadedFiles", noOfUploadedFiles);

        header(noOfUploadedFiles);

        Map<String, MultipartFile> fileMap = request.getFileMap();

        for (MultipartFile file : fileMap.values()) {
            log.debug("RABBIT - Send file: " + file.getOriginalFilename());

            Message message = MessageBuilder.withBody(file.getBytes())
                    .setHeader("File name", file.getOriginalFilename())
                    .setHeader("Content type", file.getContentType()).build();

            rabbitTemplate.send(message);

            uploadedFiles.add(file.getOriginalFilename());
        }

        messagePayload.put("uploadedFiles", uploadedFiles);

        trailer(messagePayload);

        return new ResponseEntity<>("Successfully parsed", OK);
    }

    private void header(int noOfUploadedFiles) {
        log.debug("HEADER - Number of uploaded files: " + noOfUploadedFiles);
    }

    private void trailer(Map<String, Object> messagePayload) {
        eventPublisher.publishEvent(new MeterUploadEvent(messagePayload));
    }

    @Override
    public DataTableResponse<MeterDataListWebDto> executeSearch(PageableRequest request) {
        int pageNo = request.getPageNo();
        int pageSize = request.getPageSize();
        int remainingElement = (meterDataList.size() > pageSize * (pageNo + 1)) ?
                meterDataList.size() : pageSize * (pageNo + 1) - meterDataList.size();

        Page<MeterDataListWebDto> page = new PageImpl<>(
                getMeterDataListByPage(meterDataList, request.getPageNo(), request.getPageSize()),
                request.getPageable(), remainingElement);

        return new DataTableResponse<MeterDataListWebDto>()
                .withData(page.getContent())
                .withRecordsTotal(page.getTotalElements());
    }

    private List<MeterDataListWebDto> getMeterDataListByPage(List<MeterDataListWebDto> meterDataList, int pageNo, int pageSize) {
        int toIndex = (meterDataList.size() < pageSize * (pageNo + 1)) ? meterDataList.size() : pageSize * (pageNo + 1);

        meterDataList = meterDataList.subList((pageSize * pageNo), toIndex);

        return meterDataList;
    }

    private List<MeterDataListWebDto> generateSampleMeterDataList(Long total) {
        List<MeterDataListWebDto> meterDataList = new ArrayList<>();

        for (long i = 1; i <= total; i++) {
            MeterDataListWebDto meterData = new MeterDataListWebDto();
            meterData.setId(i);
            meterData.setSein("sein" + i);
            meterData.setReadingDate(LocalDate.now().plusDays(i).toString());
            meterData.setKwD("100");
            meterData.setKwhD("100");
            meterData.setKwR("100");
            meterData.setKwhR("100");
            meterData.setKvarhD("1000");
            meterData.setKvarhR("1000");
            meterData.setVan("10");
            meterData.setVbn("20");
            meterData.setVcn("30");
            meterData.setIa("40");
            meterData.setIb("50");
            meterData.setIc("60");
            meterData.setPf("70");

            meterDataList.add(meterData);
        }

        return meterDataList;
    }

}
