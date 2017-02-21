package com.pemc.crss.metering.dto.bcq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class ParticipantBuyerDetails {

    private long userId;
    private String name;
    private String shortName;
    private boolean bcqConfirmation;

}
