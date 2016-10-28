package com.pemc.crss.meter.upload;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.apache.http.Consts.UTF_8;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpStatus.SC_OK;

public class RestUtil {

    // TODO: Make URL configurable with a default value
    private static final String TOKEN_URI = "http://localhost:8080/admin/oauth/token";
    private static final String SAMPLE_URI = "http://localhost:8080/metering/uploadheader";

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

            if(statusLine.getStatusCode() == SC_OK) {
                String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
                JSONObject obj = new JSONObject(content);

                retVal = obj.get("access_token").toString();
            }
        } catch (URISyntaxException | IOException e) {
            throw new LoginException(e.getMessage(), e);
        }

        return retVal;
    }

    public static void sendHeader(String token) {
        String transactionID = UUID.randomUUID().toString();
        System.out.println("Transaction ID:" + transactionID);

        // TODO: Retrieve URL from configuration
        try {
            URIBuilder builder = new URIBuilder(SAMPLE_URI);

            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader(AUTHORIZATION, String.format("Bearer %s", token));

            List<NameValuePair> formParams = new ArrayList<>();
            formParams.add(new BasicNameValuePair("transactionID", transactionID));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, UTF_8);
            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if(httpResponse.getStatusLine().getStatusCode() == SC_OK) {
                String content = EntityUtils.toString(httpResponse.getEntity());
                System.out.println("Response:" + content);
            } else {
                System.out.println("Unauthorized:" + httpResponse.getStatusLine().getStatusCode());
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

}
