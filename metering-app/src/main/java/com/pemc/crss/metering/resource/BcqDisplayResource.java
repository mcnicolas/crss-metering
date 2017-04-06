package com.pemc.crss.metering.resource;

import com.pemc.crss.commons.web.dto.datatable.DataTableResponse;
import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.bcq.*;
import com.pemc.crss.metering.service.BcqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.pemc.crss.metering.constants.BcqStatus.VOID;
import static com.pemc.crss.metering.constants.BcqStatus.getExcludedStatuses;
import static com.pemc.crss.metering.dao.query.ComparisonOperator.NOT_IN;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("/bcq")
public class BcqDisplayResource {

    private final BcqService bcqService;

    @Autowired
    public BcqDisplayResource(BcqService bcqService) {
        this.bcqService = bcqService;
    }

    @PostMapping(value = "/declaration/list")
    @PreAuthorize("hasAuthority('BCQ_VIEW_BILATERAL_CONTRACT_QUANTITY')")
    public ResponseEntity<DataTableResponse<BcqHeaderPageDisplay>> getHeaderPage(@RequestBody PageableRequest request) {
        Page<BcqHeaderPageDisplay> headerPage = bcqService.findAllHeaders(request);
        DataTableResponse<BcqHeaderPageDisplay> response = new DataTableResponse<BcqHeaderPageDisplay>()
                .withData(headerPage.getContent())
                .withRecordsTotal(headerPage.getTotalElements());
        return ok(response);
    }

    @GetMapping("/declaration/{headerId}/latest")
    @PreAuthorize("hasAuthority('BCQ_VIEW_BILATERAL_CONTRACT_QUANTITY')")
    public ResponseEntity<?> getLatestHeaderId(@PathVariable long headerId,
                                               @RequestParam(required = false) boolean isSettlement) {

        log.debug("Request for getting the latest header ID of the header with ID: {}", headerId);
        BcqHeader header = bcqService.findHeader(headerId);
        if (header == null) {
            log.error("No header found with ID: {}", headerId);
            return new ResponseEntity<>("No declaration found with an ID of " + headerId, INTERNAL_SERVER_ERROR);
        }

        List<BcqHeader> sameHeaders = bcqService.findSameHeaders(header, getExcludedStatuses(isSettlement), NOT_IN);
        if (sameHeaders.isEmpty()) {
            log.error("Header with ID: {} is voided and no latest version found");
            return new ResponseEntity<>("Declaration was already voided", INTERNAL_SERVER_ERROR);
        }

        Long latestHeaderId = sameHeaders.get(0).getHeaderId();
        log.debug("Found latest header ID: {}", latestHeaderId);
        return ok(latestHeaderId);
    }

    @GetMapping("/declaration/{headerId}")
    @PreAuthorize("hasAuthority('BCQ_VIEW_BILATERAL_CONTRACT_QUANTITY')")
    public ResponseEntity<?> getHeaderById(@PathVariable long headerId) {
        log.debug("Request for getting header with ID: {}", headerId);
        BcqHeader header = bcqService.findHeader(headerId);
        if (header == null) {
            log.error("No header found with ID: {}", headerId);
            return new ResponseEntity<>("No declaration found with an ID of " + headerId, INTERNAL_SERVER_ERROR);
        }

        if (header.getStatus() == VOID) {
            log.error("Header with ID: {} is voided");
            return new ResponseEntity<>("Declaration was already voided", INTERNAL_SERVER_ERROR);
        }

        log.debug("Found header: {}", header);
        return ok(new BcqHeaderDisplay(header));
    }

    @GetMapping("/declaration/{headerId}/same")
    @PreAuthorize("hasAuthority('BCQ_VIEW_BILATERAL_CONTRACT_QUANTITY')")
    public ResponseEntity<?> getSameHeaders(@PathVariable long headerId,
                                            @RequestParam(required = false) boolean isSettlement) {

        log.debug("Request for getting same headers with ID: {}", headerId);
        BcqHeader header = bcqService.findHeader(headerId);
        if (header == null) {
            log.error("No header found with ID: {}", headerId);
            return new ResponseEntity<>("No declaration found with an ID of " + headerId, INTERNAL_SERVER_ERROR);
        }

        List<BcqHeader> sameHeaders = bcqService.findSameHeaders(header, getExcludedStatuses(isSettlement), NOT_IN);
        if (sameHeaders.isEmpty()) {
            log.error("Header with ID: {} is voided and no latest version found");
            return new ResponseEntity<>("Declaration was already voided", INTERNAL_SERVER_ERROR);
        }

        log.debug("Found {} same headers with ID: {}", sameHeaders.size(), headerId);
        List<BcqHeaderDisplay> sameHeaderDisplays = sameHeaders.stream().map(BcqHeaderDisplay::new).collect(toList());
        return ok(sameHeaderDisplays);
    }

    @GetMapping("/declaration/{headerId}/data")
    @PreAuthorize("hasAuthority('BCQ_VIEW_BILATERAL_CONTRACT_QUANTITY')")
    public ResponseEntity<List<BcqDataDisplay>> getData(@PathVariable long headerId) {
        log.debug("Request for getting data of header with ID: {}", headerId);
        List<BcqDataDisplay> dataDisplays = bcqService.findDataByHeaderId(headerId).stream()
                .map(BcqDataDisplay::new).collect(toList());
        log.debug("Found {} data of header with ID: {}", dataDisplays.size(), headerId);
        return ok(dataDisplays);
    }

    @PostMapping(value = "/prohibited/list")
    @PreAuthorize("hasAuthority('BCQ_VIEW_PROHIBITED')")
    public ResponseEntity<DataTableResponse<BcqProhibitedPairPageDisplay>> getProhibitedPage(@RequestBody PageableRequest request) {
        Page<BcqProhibitedPairPageDisplay> prohibitedPage = bcqService.findAllProhibitedPairs(request);
        DataTableResponse<BcqProhibitedPairPageDisplay> response = new DataTableResponse<BcqProhibitedPairPageDisplay>()
                .withData(prohibitedPage.getContent())
                .withRecordsTotal(prohibitedPage.getTotalElements());
        return ok(response);
    }

}
