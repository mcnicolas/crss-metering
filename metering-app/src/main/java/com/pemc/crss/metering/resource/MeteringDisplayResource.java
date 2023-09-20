package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.web.dto.datatable.DataTableResponse;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.dto.VersionData;
import com.pemc.crss.metering.service.MeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

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
    public ResponseEntity<DataTableResponse<MeterDataDisplay>> executeSearch(@RequestBody PageableRequest request) {

        if (validateParams(request)) {

            Page<MeterDataDisplay> meterDataPage = meterService.getMeterData(request);

            DataTableResponse<MeterDataDisplay> response = new DataTableResponse<MeterDataDisplay>()
                    .withData(meterDataPage.getContent())
                    .withRecordsTotal(meterDataPage.getTotalElements());

            return ok(response);
        } else {
            return ok(new DataTableResponse<MeterDataDisplay>());
        }
    }

    @PostMapping(value = "/version")
    @PreAuthorize("hasAuthority('MQ_VIEW_METERING_QUANTITY')")
    public ResponseEntity<List<VersionData>> getVersionData(@RequestBody Map<String, String> request) {
        List<VersionData> versionData = meterService.getVersionedData(request);

        return ok(versionData);
    }

    private boolean validateParams(PageableRequest request) {
        Map<String, String> params = request.getMapParams();

        String readingDateFrom = StringUtils.trimAllWhitespace(params.get("readingDateFrom"));
        String transactionID = StringUtils.trimAllWhitespace(params.get("transactionID"));

        return (!StringUtils.isEmpty(readingDateFrom) || !StringUtils.isEmpty(transactionID));
    }

}
