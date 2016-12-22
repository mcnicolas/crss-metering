package com.pemc.crss.metering.dto.bcq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class ParticipantSellerDetails {

    private long userId;
    private String name;
    private String shortName;

}
