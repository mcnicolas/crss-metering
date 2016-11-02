package com.pemc.crss.commons.web.resource;

import com.pemc.crss.commons.web.dto.datatable.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

public abstract class BaseListResource<T> {

    @PostMapping(value = "/list/v2")
    @ResponseBody
    @Transactional
    public ResponseEntity listJson(@RequestBody final PageableRequest request) {
        return new ResponseEntity<>(executeSearch(request), HttpStatus.OK);

    }

    public abstract DataTableResponse<T> executeSearch(PageableRequest request);

}
