package com.pemc.crss.meter.upload;

public interface EndPoint {

    String OAUTH_TOKEN = "/admin/oauth/token";
    String USER_URL = "/admin/user";
    String PARTICIPANT_CATEGORY_URL = "/reg/participants/current/category";
    String MSP_LISTING_URL = "/reg/participants/category/msp";
    String UPLOAD_HEADER = "/metering/uploadheader";
    String UPLOAD_FILE = "/metering/uploadfile";
    String UPLOAD_TRAILER = "/metering/uploadtrailer";

}
