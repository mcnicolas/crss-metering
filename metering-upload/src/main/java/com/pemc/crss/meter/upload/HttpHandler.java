package com.pemc.crss.meter.upload;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.pemc.crss.meter.upload.EndPoint.MSP_LISTING_URL;
import static com.pemc.crss.meter.upload.EndPoint.OAUTH_TOKEN;
import static com.pemc.crss.meter.upload.EndPoint.PARTICIPANT_CATEGORY_URL;
import static com.pemc.crss.meter.upload.EndPoint.UPLOAD_FILE;
import static com.pemc.crss.meter.upload.EndPoint.UPLOAD_HEADER;
import static com.pemc.crss.meter.upload.EndPoint.UPLOAD_TRAILER;
import static com.pemc.crss.meter.upload.EndPoint.USER_URL;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.http.Consts.UTF_8;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;

@Slf4j
public class HttpHandler {

    private static final String CHAR_ENCODING = "UTF-8";
    private static final String CLIENT_ID = "crss";
    private static final String CLIENT_SECRET = "crsssecret";
    private static final String GRANT_TYPE = "password";
    private static final String CLIENT = CLIENT_ID + ":" + CLIENT_SECRET;

    private static final String PEMC_USERTYPE = "PEMC";
    private static final String MSP_USERTYPE = "MSP";

    private static final String METERING_DEPARTMENT = "METERING";
    private static final String MSP_CATEGORY = "MSP";

    private CloseableHttpClient httpClient;
    private String oAuthToken;
    private String encodedClient;
    private String hostname;
    private int port;

    public HttpHandler() {
        encodedClient = new String(Base64.getEncoder().encode(CLIENT.getBytes()));
    }

    public void initialize(String urlString) {
        log.debug("Initializing HTTP Connection for {}", urlString);

        try {
            if (httpClient != null) {
                httpClient.close();
            }

            URL url = new URL(urlString);
            hostname = url.getHost();
            port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();

            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(100);

            connectionManager.setDefaultMaxPerRoute(20);
            HttpHost httpHost = new HttpHost(hostname, port);
            connectionManager.setMaxPerRoute(new HttpRoute(httpHost), 50);

            httpClient = HttpClientBuilder
                    .create()
                    .setConnectionManager(connectionManager)
                    .build();

            log.debug("HTTP Connection initialized");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void login(String username, String password) throws LoginException, HttpConnectionException, HttpResponseException {
        log.debug("Logging in user:{}", username);

        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme("http").setHost(hostname).setPort(port).setPath(OAUTH_TOKEN)
                    .addParameter("username", username)
                    .addParameter("password", password)
                    .addParameter("client_id", CLIENT_ID)
                    .addParameter("client_secret", CLIENT_SECRET)
                    .addParameter("grant_type", GRANT_TYPE);

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader("Authorization", "Basic " + encodedClient);
            httpPost.setHeader("Content-type", APPLICATION_FORM_URLENCODED.getMimeType());

            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                StatusLine statusLine = httpResponse.getStatusLine();
                log.debug("HTTP Response code:{} reason:{}", statusLine.getStatusCode(), statusLine.getReasonPhrase());

                if (statusLine.getStatusCode() == SC_OK) {
                    String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
                    JSONObject obj = new JSONObject(content);

                    oAuthToken = obj.get("access_token").toString();

                    if (isBlank(oAuthToken)) {
                        throw new LoginException("Unable to authenticate: " + username);
                    }
                } else {
                    throw new HttpResponseException("Connection error"
                            + " statusCode:" + statusLine.getStatusCode()
                            + " reason:" + statusLine.getReasonPhrase());
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new HttpConnectionException(e.getMessage(), e);
        }
    }

    public List<String> getUserType() throws HttpConnectionException, HttpResponseException {
        log.debug("Retrieving user type");

        List<String> retVal = new ArrayList<>();

        boolean isPemcUser;

        try {
            // TODO:
            // 1. Use a stripped down endpoint to avoid data exposure
            // 2. Validate user role. Should have MQ upload privilege
            URIBuilder builder = new URIBuilder()
                    .setScheme("http").setHost(hostname).setPort(port).setPath(USER_URL);

            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.setHeader(AUTHORIZATION, String.format("Bearer %s", oAuthToken));
            httpGet.setHeader("Content-type", APPLICATION_FORM_URLENCODED.getMimeType());

            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
                StatusLine statusLine = httpResponse.getStatusLine();
                log.debug("HTTP Response code:{} reason:{}", statusLine.getStatusCode(), statusLine.getReasonPhrase());

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
                        retVal.add(getParticipant().getRegistrationCategory());
                    }
                } else {
                    throw new HttpResponseException("Connection error"
                            + " statusCode:" + statusLine.getStatusCode()
                            + " reason:" + statusLine.getReasonPhrase());
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new HttpConnectionException(e.getMessage(), e);
        }

        return retVal;
    }

    public ParticipantName getParticipant() throws HttpConnectionException, HttpResponseException {
        log.debug("Retrieving participant information");

        ParticipantName retVal = new ParticipantName();

        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme("http").setHost(hostname).setPort(port).setPath(PARTICIPANT_CATEGORY_URL);

            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.setHeader(AUTHORIZATION, String.format("Bearer %s", oAuthToken));
            httpGet.setHeader("Content-type", APPLICATION_FORM_URLENCODED.getMimeType());

            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
                StatusLine statusLine = httpResponse.getStatusLine();

                if (statusLine.getStatusCode() == SC_OK) {
                    String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
                    JSONObject obj = new JSONObject(content);

                    // TODO: Improve json deserialization
                    retVal.setId(obj.getLong("id"));
                    retVal.setShortName(obj.getString("shortName"));
                    retVal.setParticipantName(obj.getString("participantName"));
                    retVal.setRegistrationCategory(obj.getString("registrationCategory"));
                } else {
                    throw new HttpResponseException("Connection error"
                            + " statusCode:" + statusLine.getStatusCode()
                            + " reason:" + statusLine.getReasonPhrase());
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new HttpConnectionException(e.getMessage(), e);
        }

        return retVal;
    }

    public long sendHeader(String transactionID, String username, int fileCount, String category)
            throws HttpConnectionException, HttpResponseException {

        log.debug("Sending Header Record. txnID:{} fileCount:{} category:{}", transactionID, fileCount, category);

        long retVal = -1;

        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme("http").setHost(hostname).setPort(port).setPath(UPLOAD_HEADER);

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader(AUTHORIZATION, String.format("Bearer %s", oAuthToken));
            httpPost.setHeader("Content-type", APPLICATION_FORM_URLENCODED.getMimeType());

            List<NameValuePair> formParams = new ArrayList<>();
            formParams.add(new BasicNameValuePair("transactionID", transactionID));
            formParams.add(new BasicNameValuePair("fileCount", String.valueOf(fileCount)));
            formParams.add(new BasicNameValuePair("category", category));
            formParams.add(new BasicNameValuePair("username", username));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, UTF_8);
            httpPost.setEntity(entity);

            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                StatusLine statusLine = httpResponse.getStatusLine();
                log.debug("HTTP Response code:{} reason:{}", statusLine.getStatusCode(), statusLine.getReasonPhrase());

                if (statusLine.getStatusCode() == SC_OK) {
                    String content = EntityUtils.toString(httpResponse.getEntity());

                    if (NumberUtils.isParsable(content)) {
                        retVal = Long.valueOf(content);
                    }

                    log.debug("Response:{}", content);
                } else {
                    throw new HttpResponseException("Connection error"
                            + " statusCode:" + statusLine.getStatusCode()
                            + " reason:" + statusLine.getReasonPhrase());
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new HttpConnectionException(e.getMessage(), e);
        }

        return retVal;
    }

    public void sendFile(long headerID, String transactionID, FileBean file, String category, String mspShortName)
            throws HttpConnectionException, HttpResponseException {

        log.debug("Sending file. txnID:{} fileName:{} category:{} mspShortName:{}", transactionID, file.getPath().getFileName(),
                category, mspShortName);

        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme("http").setHost(hostname).setPort(port).setPath(UPLOAD_FILE);

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader(AUTHORIZATION, String.format("Bearer %s", oAuthToken));

            InputStreamBody fileContent = new InputStreamBody(Files.newInputStream(file.getPath()),
                    file.getPath().getFileName().toString());

            // TODO: Explore HttpAsyncClient:ZeroCopyPost if some performance can be gained.
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            HttpEntity entity = multipartEntityBuilder
                    .setContentType(MULTIPART_FORM_DATA)
                    .setCharset(UTF_8)
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

            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                StatusLine statusLine = httpResponse.getStatusLine();
                log.debug("HTTP Response code:{} reason:{}", statusLine.getStatusCode(), statusLine.getReasonPhrase());

                if (statusLine.getStatusCode() != SC_OK) {
                    throw new HttpResponseException("Connection error"
                            + " statusCode:" + statusLine.getStatusCode()
                            + " reason:" + statusLine.getReasonPhrase());
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new HttpConnectionException(e.getMessage(), e);
        }
    }

    // TODO: Optimize code. Remove duplication.
    public void sendTrailer(String transactionID) throws HttpConnectionException, HttpResponseException {

        log.debug("Sending Trailer Record. txnID:{}", transactionID);

        // TODO: Retrieve URL from configuration
        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme("http").setHost(hostname).setPort(port).setPath(UPLOAD_TRAILER);

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader(AUTHORIZATION, String.format("Bearer %s", oAuthToken));
            httpPost.setHeader("Content-type", APPLICATION_FORM_URLENCODED.getMimeType());

            List<NameValuePair> formParams = new ArrayList<>();
            formParams.add(new BasicNameValuePair("transactionID", transactionID));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, UTF_8);
            httpPost.setEntity(entity);

            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost);) {
                StatusLine statusLine = httpResponse.getStatusLine();
                log.debug("HTTP Response code:{} reason:{}", statusLine.getStatusCode(), statusLine.getReasonPhrase());

                if (statusLine.getStatusCode() != SC_OK) {
                    throw new HttpResponseException("Connection error"
                            + " statusCode:" + statusLine.getStatusCode()
                            + " reason:" + statusLine.getReasonPhrase());
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new HttpConnectionException(e.getMessage(), e);
        }
    }

    public List<ComboBoxItem> getMSPListing() throws HttpConnectionException, HttpResponseException {
        log.debug("Loading MSP Listing");

        List<ComboBoxItem> retVal = new ArrayList<>();

        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme("http").setHost(hostname).setPort(port).setPath(MSP_LISTING_URL);

            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.setHeader(AUTHORIZATION, String.format("Bearer %s", oAuthToken));
            httpGet.setHeader("Content-type", APPLICATION_FORM_URLENCODED.getMimeType());

            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet);) {
                StatusLine statusLine = httpResponse.getStatusLine();
                log.debug("HTTP Response code:{} reason:{}", statusLine.getStatusCode(), statusLine.getReasonPhrase());

                if (statusLine.getStatusCode() == SC_OK) {
                    String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
                    JSONArray jsonArray = new JSONArray(content);

                    for (Object aJsonArray : jsonArray) {
                        JSONObject object = (JSONObject) aJsonArray;

                        String shortName = object.getString("shortName");
                        String participantName = object.getString("participantName") + "(" + shortName + ")";

                        retVal.add(new ComboBoxItem(shortName, participantName));
                    }
                } else {
                    throw new HttpResponseException("Connection error"
                            + " statusCode:" + statusLine.getStatusCode()
                            + " reason:" + statusLine.getReasonPhrase());
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new HttpConnectionException(e.getMessage(), e);
        }

        return retVal;
    }

    public void shutdown() throws IOException {
        log.debug("Shutting down HTTP connection");

        httpClient.close();
    }

    private String getFileType(Path path) {
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
