package com.pemc.crss.meter.upload;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.apache.http.Consts.UTF_8;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;

@Slf4j
public class RestUtil {

    // TODO: Make URL configurable with a default value
    private static final String TOKEN_URI = "http://localhost:8080/admin/oauth/token";
    private static final String UPLOAD_HEADER = "http://localhost:8080/metering/uploadheader";
    private static final String UPLOAD_FILE = "http://localhost:8080/metering/uploadfile";
    private static final String UPLOAD_TRAILER = "http://localhost:8080/metering/uploadtrailer";

    private static final String CHAR_ENCODING = "UTF-8";
    private static final String CLIENT_ID = "crss";
    private static final String CLIENT_SECRET = "crsssecret";
    private static final String GRANT_TYPE = "password";
    private static final String CLIENT = CLIENT_ID + ":" + CLIENT_SECRET;
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    public static String login(String username, String password) throws LoginException {
        String retVal = null;

        try {
            URIBuilder builder = new URIBuilder(TOKEN_URI);

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

    public static void sendHeader(String transactionID, String username, int fileCount, String category, int mspID,
                                  String token) {

        log.debug("Transaction ID: {}", transactionID);

        // TODO: Retrieve URL from configuration
        try {
            URIBuilder builder = new URIBuilder(UPLOAD_HEADER);

            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader(AUTHORIZATION, String.format("Bearer %s", token));

            List<NameValuePair> formParams = new ArrayList<>();
            formParams.add(new BasicNameValuePair("transactionID", transactionID));
            formParams.add(new BasicNameValuePair("mspID", String.valueOf(mspID)));
            formParams.add(new BasicNameValuePair("fileCount", String.valueOf(fileCount)));
            formParams.add(new BasicNameValuePair("category", category));
            formParams.add(new BasicNameValuePair("username", username));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, UTF_8);
            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() == SC_OK) {
                String content = EntityUtils.toString(httpResponse.getEntity());

                log.debug("Response:{}", content);
            } else {
                log.error("Unauthorized:{}", httpResponse.getStatusLine().getStatusCode());
            }
        } catch (URISyntaxException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    // TODO: Optimize code. Remove duplication.
    public static void sendTrailer(String transactionID, String token) {
        log.debug("Transaction ID: {}", transactionID);

        // TODO: Retrieve URL from configuration
        try {
            URIBuilder builder = new URIBuilder(UPLOAD_TRAILER);

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
                log.error("Unauthorized:{}", httpResponse.getStatusLine().getStatusCode());
            }
        } catch (URISyntaxException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void sendFile(String transactionID, FileBean file, String token) {
        log.debug("Transaction ID: {}", transactionID);

        // TODO: Retrieve URL from configuration
        try {
            URIBuilder builder = new URIBuilder(UPLOAD_FILE);

            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader(AUTHORIZATION, String.format("Bearer %s", token));

            InputStreamBody fileContent = new InputStreamBody(Files.newInputStream(file.getPath()),
                    file.getPath().getFileName().toString());

            // TODO: Explore HttpAsyncClient:ZeroCopyPost if some performance can be gained.
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            HttpEntity entity = multipartEntityBuilder
                    .setContentType(MULTIPART_FORM_DATA)
                    .addTextBody("headerID", "1") // TODO: Send actual header_id
                    .addTextBody("transactionID", transactionID)
                    .addTextBody("fileName", file.getPath().getFileName().toString())
                    .addTextBody("fileType", "xxx")
                    .addTextBody("fileSize", String.valueOf(file.getSize()))
                    .addTextBody("checksum", file.getChecksum())
                    .addPart("file", fileContent)
                    .build();

            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() == SC_OK) {
                String content = EntityUtils.toString(httpResponse.getEntity());

                log.debug("Response:{}", content);
            } else {
                log.error("Unauthorized:{}", httpResponse.getStatusLine().getStatusCode());
            }
        } catch (URISyntaxException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
