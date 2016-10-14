package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.web.dto.datatable.DataTableResponse;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.commons.web.resource.BaseListResource;
import com.pemc.crss.metering.constants.UploadType;
import com.pemc.crss.metering.dto.MeterDataListWebDto;
import com.pemc.crss.metering.service.MeterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
public class MeteringResource extends BaseListResource<MeterDataListWebDto> {

    @Autowired
    private MeterService meterService;

    private List<MeterDataListWebDto> meterDataList;

    public MeteringResource() {
        long sampleSize = 25L;

        meterDataList = generateSampleMeterDataList(sampleSize);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadMeterData(MultipartHttpServletRequest request,
                                                  @RequestParam("uploadType") UploadType uploadType) throws IOException {

        meterService.saveMeterData(request.getFileMap().values(), uploadType);

        return new ResponseEntity<>("Successfully parsed", OK);
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
