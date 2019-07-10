package com.pemc.crss.meter.upload;

public interface EndPoint {

    String OAUTH_TOKEN = "/uaa/oauth/token";
    String USER_URL = "/uaa/user";
    String PARTICIPANT_CATEGORY_URL = "/reg/participants/current/category";
    String MSP_LISTING_URL = "/reg/participants/category/msp";
    String UPLOAD_HEADER = "/metering/uploadHeader";
    String UPLOAD_FILE = "/metering/uploadFile";
    String UPLOAD_TRAILER = "/metering/uploadTrailer";
    String CHECK_STATUS = "/metering/checkStatus";
    String GET_HEADER = "/metering/getHeader";
    String GET_INTERVAL = "/admin/admin/config";

}
