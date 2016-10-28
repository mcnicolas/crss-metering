package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.web.dto.datatable.DataTableResponse;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.commons.web.resource.BaseListResource;
import com.pemc.crss.metering.dto.MeterDataListWebDto;
import com.pemc.crss.metering.event.MeterUploadEvent;
import com.pemc.crss.metering.service.MeterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

@RestController
public class MeteringResource extends BaseListResource<MeterDataListWebDto> {

    private static final Logger LOG = LoggerFactory.getLogger(MeteringResource.class);

    @Autowired
    private MeterService meterService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private List<MeterDataListWebDto> meterDataList;

    public MeteringResource() {
        long sampleSize = 25L;

        meterDataList = generateSampleMeterDataList(sampleSize);
    }

    @GetMapping("/sample")
    public String sample() {
        return "Success";
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

        for(MultipartFile file : fileMap.values()) {
            LOG.debug("RABBIT - Send file: " + file.getOriginalFilename());

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
        LOG.debug("HEADER - Number of uploaded files: " + noOfUploadedFiles);
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

        for(long i = 1; i <= total; i ++) {
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
