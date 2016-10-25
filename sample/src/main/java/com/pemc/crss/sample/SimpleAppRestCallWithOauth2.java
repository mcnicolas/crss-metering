package com.pemc.crss.sample;

import com.pemc.crss.sample.service.LoginService;
import com.pemc.crss.sample.service.impl.LoginServiceImpl;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;

public class SimpleAppRestCallWithOauth2 {

    private static final String SAMPLE_USERNAME = "msp";
    private static final String SAMPLE_PASSWORD = "msp";

    private static final String SAMPLE_URI = "http://localhost:8080/metering/metering/sample";

    public static void main(String... args) throws IOException, URISyntaxException {
        LoginService loginService = new LoginServiceImpl();
        String token = loginService.login(SAMPLE_USERNAME, SAMPLE_PASSWORD);

        URIBuilder builder = new URIBuilder(SAMPLE_URI);

        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet httpGet = new HttpGet(builder.build());
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token));

        HttpResponse httpResponse = httpClient.execute(httpGet);

        if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String content = EntityUtils.toString(httpResponse.getEntity());
            System.out.println(content);
        }
        else {
            System.out.println("Unauthorized");
        }
    }

}
