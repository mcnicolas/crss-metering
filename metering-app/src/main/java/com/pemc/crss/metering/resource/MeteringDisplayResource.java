package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.web.dto.datatable.DataTableResponse;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.commons.web.resource.BaseListResource;
import com.pemc.crss.metering.dto.MeterDataDisplay;
import com.pemc.crss.metering.service.MeterService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@RestController
public class MeteringDisplayResource extends BaseListResource<MeterDataDisplay>  {

    @NonNull
    private final MeterService meterService;

    @Override
    public DataTableResponse<MeterDataDisplay> executeSearch(PageableRequest request) {
        Page<MeterDataDisplay> meterDataPage = meterService.getMeterData(request);

        return new DataTableResponse<MeterDataDisplay>()
                .withData(meterDataPage.getContent())
                .withRecordsTotal(meterDataPage.getTotalElements());
    }

}
