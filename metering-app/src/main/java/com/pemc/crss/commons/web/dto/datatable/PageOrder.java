package com.pemc.crss.commons.web.dto.datatable;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import static org.springframework.data.domain.Sort.NullHandling.NULLS_LAST;

@Data
@AllArgsConstructor
public class PageOrder {

    private String sortColumn;
    private Direction sortDirection;

    public Order getOrder() {
        return new Order(sortDirection, sortColumn, NULLS_LAST);
    }

}
