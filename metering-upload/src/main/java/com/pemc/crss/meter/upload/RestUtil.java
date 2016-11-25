package com.pemc.crss.meter.upload;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.http.Consts.UTF_8;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;

// TODO: Refactor class. Consider reusing HttpClient.
@Slf4j
public class RestUtil {

    private static String BASE_URL = "";
    private static final String TOKEN_URL = "/admin/oauth/token";
    private static final String USER_URL = "/admin/user";
    private static final String PARTICIPANT_CATEGORY_URL = "/reg/participants/current/category";
    private static final String MSP_LISTING_URL = "/reg/participants/category/msp";
    private static final String UPLOAD_HEADER = "/metering/uploadheader";
    private static final String UPLOAD_FILE = "/metering/uploadfile";
    private static final String UPLOAD_TRAILER = "/metering/uploadtrailer";

    private static final String CHAR_ENCODING = "UTF-8";
    private static final String CLIENT_ID = "crss";
    private static final String CLIENT_SECRET = "crsssecret";
    private static final String GRANT_TYPE = "password";
    private static final String CLIENT = CLIENT_ID + ":" + CLIENT_SECRET;
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    private static final String PEMC_USERTYPE = "PEMC";
    private static final String MSP_USERTYPE = "MSP";

    private static final String METERING_DEPARTMENT = "METERING";
    private static final String MSP_CATEGORY = "MSP";

    public static void setBaseURL(String baseURL) {
        BASE_URL = baseURL;
    }

    public static String login(String username, String password) throws LoginException {
        String retVal = null;

        try {
            URIBuilder builder = new URIBuilder(BASE_URL + TOKEN_URL);

            String encodedClient = new String(Base64.getEncoder().encode(CLIENT.getBytes()));

            builder.addParameter("username", username);
            builder.addParameter("password", password);
            builder.addParameter("client_id", CLIENT_ID);
            builder.addParameter("client_secret", CLIENT_SECRET);
            builder.addParameter("grant_type", GRANT_TYPE);

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader("Authorization", "Basic " + encodedClient);
            httpPost.setHeader("Content-type", CONTENT_TYPE);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            StatusLine statusLine = httpResponse.getStatusLine();

            if (statusLine.getStatusCode() == SC_OK) {
                String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
                JSONObject obj = new JSONObject(content);

                retVal = obj.get("access_token").toString();
            }
        } catch (URISyntaxException | IOException e) {
            throw new LoginException(e.getMessage(), e);
        }

        return retVal;
    }

    public static List<String> getUserType(String token) {
        List<String> retVal = new ArrayList<>();

        boolean isPemcUser;

        try {
            // TODO:
            // 1. Use a stripped down endpoint to avoid data exposure
            // 2. Validate user role. Should have MQ upload privilege
            URIBuilder builder = new URIBuilder(BASE_URL + USER_URL);

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.setHeader(AUTHORIZATION, String.format("Bearer %s", token));

            HttpResponse httpResponse = httpClient.execute(httpGet);
            StatusLine statusLine = httpResponse.getStatusLine();

            if (statusLine.getStatusCode() == SC_OK) {
                String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
                JSONObject obj = new JSONObject(content);

                retVal.add(obj.getJSONObject("principal").getString("fullName"));

                String userType = obj.getJSONObject("principal").get("department").toString();
                isPemcUser = Boolean.valueOf(obj.getJSONObject("principal").get("pemcUser").toString());

                if (isPemcUser) {
                    if (equalsIgnoreCase(userType, METERING_DEPARTMENT)) {
                        retVal.add(PEMC_USERTYPE);
                    } else {
                        retVal.add("");
                    }
                } else {
                    retVal.add(getParticipant(token).getRegistrationCategory());
                }
            }
        } catch (URISyntaxException | IOException e) {
            log.error(e.getMessage(), e);
        }

        return retVal;
    }

    public static long sendHeader(String transactionID, String username, int fileCount, String category, String token)
            throws ConnectionException {

        long retVal = -1;

        log.debug("Transaction ID: {}", transactionID);

        try {
            URIBuilder builder = new URIBuilder(BASE_URL + UPLOAD_HEADER);

            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader(AUTHORIZATION, String.format("Bearer %s", token));

            List<NameValuePair> formParams = new ArrayList<>();
            formParams.add(new BasicNameValuePair("transactionID", transactionID));
            formParams.add(new BasicNameValuePair("fileCount", String.valueOf(fileCount)));
            formParams.add(new BasicNameValuePair("category", category));
            formParams.add(new BasicNameValuePair("username", username));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, UTF_8);
            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() == SC_OK) {
                String content = EntityUtils.toString(httpResponse.getEntity());

                if (NumberUtils.isParsable(content)) {
                    retVal = Long.valueOf(content);
                }

                log.debug("Response:{}", content);
            } else {
                throw new ConnectionException("Connection error"
                        + " [statusCode:" + httpResponse.getStatusLine().getStatusCode()
                        + " reason:" + httpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (URISyntaxException | IOException e) {
            log.error(e.getMessage(), e);
        }

        return retVal;
    }

    // TODO: Optimize code. Remove duplication.
    public static void sendTrailer(String transactionID, String token) throws ConnectionException {
        log.debug("Transaction ID: {}", transactionID);

        // TODO: Retrieve URL from configuration
        try {
            URIBuilder builder = new URIBuilder(BASE_URL + UPLOAD_TRAILER);

            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader(AUTHORIZATION, String.format("Bearer %s", token));

            List<NameValuePair> formParams = new ArrayList<>();
            formParams.add(new BasicNameValuePair("transactionID", transactionID));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, UTF_8);
            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() == SC_OK) {
                String content = EntityUtils.toString(httpResponse.getEntity());

                log.debug("Response:{}", content);
            } else {
                throw new ConnectionException("Connection error"
                        + " [statusCode:" + httpResponse.getStatusLine().getStatusCode()
                        + " reason:" + httpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (URISyntaxException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void sendFile(long headerID, String transactionID, FileBean file, String category, String mspShortName, String token)
            throws ConnectionException {

        log.debug("Transaction ID: {}", transactionID);

        try {
            URIBuilder builder = new URIBuilder(BASE_URL + UPLOAD_FILE);

            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader(AUTHORIZATION, String.format("Bearer %s", token));

            InputStreamBody fileContent = new InputStreamBody(Files.newInputStream(file.getPath()),
                    file.getPath().getFileName().toString());

            // TODO: Explore HttpAsyncClient:ZeroCopyPost if some performance can be gained.
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            HttpEntity entity = multipartEntityBuilder
                    .setContentType(MULTIPART_FORM_DATA)
                    .addTextBody("headerID", String.valueOf(headerID))
                    .addTextBody("transactionID", transactionID)
                    .addTextBody("fileName", file.getPath().getFileName().toString())
                    .addTextBody("fileType", getFileType(file.getPath()))
                    .addTextBody("fileSize", String.valueOf(file.getSize()))
                    .addTextBody("checksum", file.getChecksum())
                    .addTextBody("mspShortName", mspShortName)
                    .addTextBody("category", category)
                    .addPart("file", fileContent)
                    .build();

            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() == SC_OK) {
                String content = EntityUtils.toString(httpResponse.getEntity());

                log.debug("Response:{}", content);
            } else {
                throw new ConnectionException("Connection error"
                        + " [statusCode:" + httpResponse.getStatusLine().getStatusCode()
                        + " reason:" + httpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (URISyntaxException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static List<ComboBoxItem> getMSPListing(String token) {
        List<ComboBoxItem> retVal = new ArrayList<>();

        try {
            URIBuilder builder = new URIBuilder(BASE_URL + MSP_LISTING_URL);

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.setHeader(AUTHORIZATION, String.format("Bearer %s", token));

            HttpResponse httpResponse = httpClient.execute(httpGet);
            StatusLine statusLine = httpResponse.getStatusLine();

            if (statusLine.getStatusCode() == SC_OK) {
                String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
                JSONArray jsonArray = new JSONArray(content);

                Iterator<Object> iterator = jsonArray.iterator();
                while (iterator.hasNext()) {
                    JSONObject object = (JSONObject) iterator.next();

                    String shortName = object.getString("shortName");
                    String participantName = object.getString("participantName") + "(" + shortName + ")";

                    retVal.add(new ComboBoxItem(shortName, participantName));
                }
            }
        } catch (URISyntaxException | IOException e) {
            log.error(e.getMessage(), e);
        }

        return retVal;
    }

    public static ParticipantName getParticipant(String token) {
        ParticipantName retVal = null;

        try {
            URIBuilder builder = new URIBuilder(BASE_URL + PARTICIPANT_CATEGORY_URL);

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.setHeader(AUTHORIZATION, String.format("Bearer %s", token));

            HttpResponse httpResponse = httpClient.execute(httpGet);
            StatusLine statusLine = httpResponse.getStatusLine();

            if (statusLine.getStatusCode() == SC_OK) {
                String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
                JSONObject obj = new JSONObject(content);

                // TODO: Improve json deserialization
                retVal = new ParticipantName();
                retVal.setId(obj.getLong("id"));
                retVal.setShortName(obj.getString("shortName"));
                retVal.setParticipantName(obj.getString("participantName"));
                retVal.setRegistrationCategory(obj.getString("registrationCategory"));
            }
        } catch (URISyntaxException | IOException e) {
            log.error(e.getMessage(), e);
        }

        return retVal;
    }

    private static String getFileType(Path path) {
        String retVal = "";

        String fileExt = FilenameUtils.getExtension(path.getFileName().toString());

        if (equalsIgnoreCase(fileExt, "XLS") || equalsIgnoreCase(fileExt, "XLSX")) {
            retVal = "XLS";
        } else if (equalsIgnoreCase(fileExt, "MDE") || equalsIgnoreCase(fileExt, "MDEF")) {
            retVal = "MDEF";
        } else if (equalsIgnoreCase(fileExt, "CSV")) {
            retVal = "CSV";
        }

        return retVal;
    }

}
