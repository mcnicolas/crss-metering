package com.pemc.crss.commons.web.dto;

import java.io.Serializable;

public interface WebDto<TARGET> extends Serializable {

    TARGET target();

}
