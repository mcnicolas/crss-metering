package com.pemc.crss.meter.upload;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;

import static org.apache.http.HttpStatus.SC_OK;

public class LoginUtil {

    // TODO: Make URL configurable with a default value
    private static final String TOKEN_URI = "http://localhost:8080/admin/oauth/token";

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

}
