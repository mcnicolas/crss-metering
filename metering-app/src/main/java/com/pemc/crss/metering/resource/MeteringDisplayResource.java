package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.web.dto.datatable.DataTableResponse;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.service.MeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/mq")
public class MeteringDisplayResource {

    private final MeterService meterService;

    @Autowired
    public MeteringDisplayResource(MeterService meterService) {
        this.meterService = meterService;
    }

    @PostMapping(value = "/list")
    @PreAuthorize("hasAuthority('MQ_VIEW_METERING_QUANTITY')")
    public ResponseEntity<DataTableResponse<MeterDataDisplay>> executeSearch(PageableRequest request) {
        Page<MeterDataDisplay> meterDataPage = meterService.getMeterData(request);

        DataTableResponse<MeterDataDisplay> response = new DataTableResponse<MeterDataDisplay>()
                .withData(meterDataPage.getContent())
                .withRecordsTotal(meterDataPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

}
