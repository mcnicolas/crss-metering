package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.web.dto.datatable.DataTableResponse;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.commons.web.resource.BaseListResource;
import com.pemc.crss.metering.dto.MeterData;
import com.pemc.crss.metering.dto.MeterData2;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

// TODO: Move searching to a separate controller
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@RestController
public class MeteringResource extends BaseListResource<MeterData2> {

    public static final String ROUTING_KEY = "meter.quantity";

    @NonNull
    private final MeterService meterService;

    @NonNull
    private final RabbitTemplate rabbitTemplate;

    @NonNull
    private final ApplicationEventPublisher eventPublisher;

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

    @Deprecated // Change in impl
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

    @Deprecated // Change in impl
    private void header(int noOfUploadedFiles) {
        log.debug("HEADER - Number of uploaded files: " + noOfUploadedFiles);
    }

    @Deprecated // Change in impl
    private void trailer(Map<String, Object> messagePayload) {
        eventPublisher.publishEvent(new MeterUploadEvent(messagePayload));
    }

    @Override
    public DataTableResponse<MeterData2> executeSearch(PageableRequest request) {
        List<MeterData2> meterDataList = generateSampleMeterDataList(25L);

        int pageNo = request.getPageNo();
        int pageSize = request.getPageSize();

        int remainingElement = (meterDataList.size() > pageSize * (pageNo + 1)) ?
                meterDataList.size() :
                pageSize * (pageNo + 1) - meterDataList.size();

        Page<MeterData2> page = new PageImpl<>(
                getMeterDataListByPage(meterDataList, pageNo, pageSize),
                request.getPageable(), remainingElement);

        log.debug("Upload type: {}, Date: {}",
                request.getMapParams().get("uploadType"),
                request.getMapParams().get("date"));

        return new DataTableResponse<MeterData2>()
                .withData(page.getContent())
                .withRecordsTotal(page.getTotalElements());
    }


    /*****************************************
     * Sample meter data list generator
     *****************************************/

    private List<MeterData2> getMeterDataListByPage(List<MeterData2> meterDataList, int pageNo, int pageSize) {
        int toIndex = (meterDataList.size() < pageSize * (pageNo + 1)) ? meterDataList.size() : pageSize * (pageNo + 1);

        meterDataList = meterDataList.subList((pageSize * pageNo), toIndex);

        return meterDataList;
    }

    // TODO: Temporary data
    private List<MeterData2> generateSampleMeterDataList(Long total) {
        List<MeterData2> meterDataList = new ArrayList<>();

        for (long i = 1; i <= total; i++) {
            MeterData2 meterData = new MeterData2();
            meterData.setMeterDataID(i);
            meterData.setSein("R3MEXCEDC01TNSC0" + i);
            meterData.setReadingDateTime(new Date());
            meterData.setKwd(Double.valueOf("7860.00000"));
            meterData.setKwhd(Double.valueOf("7860.00000"));
            meterData.setKwr(Double.valueOf("7860.00000"));
            meterData.setKwhr(Double.valueOf("7860.00000"));
            meterData.setKvarhd(Double.valueOf("7860.00000"));
            meterData.setKvarhr(Double.valueOf("7860.00000"));
            meterData.setVan(Double.valueOf("7860.00000"));
            meterData.setVbn(Double.valueOf("7860.00000"));
            meterData.setVcn(Double.valueOf("7860.00000"));
            meterData.setIan(Double.valueOf("7860.00000"));
            meterData.setIbn(Double.valueOf("7860.00000"));
            meterData.setIcn(Double.valueOf("7860.00000"));
            meterData.setPf(Double.valueOf("7860.00000"));

            meterDataList.add(meterData);
        }

        return meterDataList;
    }

}
