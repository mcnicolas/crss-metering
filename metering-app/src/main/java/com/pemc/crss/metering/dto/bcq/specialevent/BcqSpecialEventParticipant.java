package com.pemc.crss.metering.dto.bcq.specialevent;

import lombok.Data;

import java.util.Objects;

@Data
public class BcqSpecialEventParticipant {

    private String shortName;
    private String participantName;

    @Override
    public int hashCode() {
        return shortName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BcqSpecialEventParticipant that = (BcqSpecialEventParticipant) o;
        return Objects.equals(shortName.toUpperCase(), that.shortName.toUpperCase())
                && Objects.equals(participantName.toUpperCase(), that.participantName.toUpperCase());
    }

}
