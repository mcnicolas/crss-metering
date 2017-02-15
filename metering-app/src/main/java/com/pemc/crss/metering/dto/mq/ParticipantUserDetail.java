package com.pemc.crss.metering.dto.mq;

import lombok.Data;

import java.util.Set;

@Data
public class ParticipantUserDetail {

    private String participantName;
    private String shortName;
    private Set<Long> associatedUserID;

}
