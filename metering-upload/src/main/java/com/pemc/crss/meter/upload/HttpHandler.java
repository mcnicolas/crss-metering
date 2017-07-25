package com.pemc.crss.meter.upload;

import com.pemc.crss.meter.upload.http.FileStatus;
import com.pemc.crss.meter.upload.http.HeaderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultClientConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static com.pemc.crss.meter.upload.EndPoint.CHECK_STATUS;
import static com.pemc.crss.meter.upload.EndPoint.GET_HEADER;
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
import static org.apache.http.conn.ssl.NoopHostnameVerifier.INSTANCE;
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;
import static org.apache.http.util.EntityUtils.consume;

@Slf4j
public class HttpHandler {

    private static final String CHAR_ENCODING = "UTF-8";
    private static final String CLIENT_ID = "crss";
    private static final String CLIENT_SECRET = "crsssecret";
    private static final String GRANT_TYPE = "password";
    private static final String CLIENT = CLIENT_ID + ":" + CLIENT_SECRET;

    private static final String PEMC_USERTYPE = "PEMC";

    private static final String METERING_DEPARTMENT = "METERING";
    public static final String MARKET_OPERATOR = "MO";
    public static final String SYSTEM_OPERATOR = "SO";

    private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager connectionManager;

    private String oAuthToken;
    private String encodedClient;
    private String protocol;
    private String hostname;
    private int port;

    private String deviceId;

    public HttpHandler() {
        encodedClient = new String(Base64.getEncoder().encode(CLIENT.getBytes()));
        initializeDeviceId();
    }


    public void initialize(String urlString) {
        log.debug("Initializing HTTP Connection for {}", urlString);

        try {
            if (httpClient != null) {
                httpClient.close();
            }

            URL url = new URL(urlString);
            protocol = url.getProtocol();
            hostname = url.getHost();
            port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();

            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                    .build();
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, INSTANCE);

            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", new PlainConnectionSocketFactory())
                    .register("https", sslSocketFactory)
                    .build();

            connectionManager = new PoolingHttpClientConnectionManager(registry);
            connectionManager.setMaxTotal(300);

            connectionManager.setDefaultMaxPerRoute(50);
            HttpHost httpHost = new HttpHost(hostname, port);
            connectionManager.setMaxPerRoute(new HttpRoute(httpHost), 300);

            httpClient = HttpClients
                    .custom()
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .addInterceptorLast(
                            (HttpRequestInterceptor) (request, context) -> {
                                if (!request.containsHeader("Accept-Encoding")) {
                                    request.addHeader("Accept-Encoding", "gzip");
                                }
                            }
                    )
                    .setConnectionReuseStrategy(new DefaultClientConnectionReuseStrategy())
                    .setConnectionManager(connectionManager)
                    .build();
            log.debug("HTTP Connection initialized");
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void login(String username, String password) throws LoginException, HttpConnectionException, HttpResponseException {
        log.debug("Logging in user:{}", username);

        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme(protocol).setHost(hostname).setPort(port).setPath(OAUTH_TOKEN)
                    .addParameter("username", username)
                    .addParameter("password", password)
                    .addParameter("device_id", deviceId)
                    .addParameter("client_id", CLIENT_ID)
                    .addParameter("client_secret", CLIENT_SECRET)
                    .addParameter("grant_type", GRANT_TYPE);

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader("Authorization", "Basic " + encodedClient);
            httpPost.setHeader("Content-type", APPLICATION_FORM_URLENCODED.getMimeType());

            HttpResponse httpResponse = httpClient.execute(httpPost);
            StatusLine statusLine = httpResponse.getStatusLine();

            String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
            consume(httpResponse.getEntity());

            if (statusLine.getStatusCode() == SC_OK) {
                JSONObject obj = new JSONObject(content);

                oAuthToken = obj.get("access_token").toString();

                if (isBlank(oAuthToken)) {
                    throw new LoginException("Unable to authenticate: " + username);
                }
            } else {
                throw new HttpResponseException("Message:Login error"
                        + "\nStatus Code:" + statusLine.getStatusCode()
                        + "\nReason:" + statusLine.getReasonPhrase());
            }
        } catch (URISyntaxException | IOException e) {
            throw new HttpConnectionException(e.getMessage(), e);
        }
    }

    public List<String> getUserType() throws HttpConnectionException, HttpResponseException, AuthorizationException {
        log.debug("Retrieving user type");

        List<String> retVal = new ArrayList<>();

        boolean isPemcUser;

        try {
            // TODO:
            // 1. Use a stripped down endpoint to avoid data exposure
            // 2. Validate user role. Should have MQ upload privilege
            URIBuilder builder = new URIBuilder()
                    .setScheme(protocol).setHost(hostname).setPort(port).setPath(USER_URL);

            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.setHeader(AUTHORIZATION, String.format("Bearer %s", oAuthToken));
            httpGet.setHeader("Content-type", APPLICATION_FORM_URLENCODED.getMimeType());

            HttpResponse httpResponse = httpClient.execute(httpGet);

            StatusLine statusLine = httpResponse.getStatusLine();

            String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
            consume(httpResponse.getEntity());

            if (statusLine.getStatusCode() == SC_OK) {
                JSONObject obj = new JSONObject(content);

                boolean found = false;
                JSONArray authorities = (JSONArray) obj.get("authorities");
                for (Object objAuthority : authorities) {
                    JSONObject authority = (JSONObject) objAuthority;

                    if (equalsIgnoreCase((String) authority.get("authority"), "MQ_UPLOAD_METER_DATA")) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    throw new AuthorizationException("User is not authorized to access the MQ Uploader");
                }

                JSONObject principal = obj.getJSONObject("principal");

                retVal.add(principal.getString("fullName"));

                String userType = principal.getString("userType");
                if (equalsIgnoreCase(userType, MARKET_OPERATOR)) {
                    String department = principal.getString("department");
                    if (equalsIgnoreCase(department, METERING_DEPARTMENT)) {
                        retVal.add(PEMC_USERTYPE);
                    } else {
                        retVal.add("");
                    }
                } else if (equalsIgnoreCase(userType, SYSTEM_OPERATOR)) {
                    retVal.add(getParticipant().getRegistrationCategory());
                }
            } else {
                throw new HttpResponseException("Connection error"
                        + " statusCode:" + statusLine.getStatusCode()
                        + " reason:" + statusLine.getReasonPhrase());
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
                    .setScheme(protocol).setHost(hostname).setPort(port).setPath(PARTICIPANT_CATEGORY_URL);

            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.setHeader(AUTHORIZATION, String.format("Bearer %s", oAuthToken));
            httpGet.setHeader("Content-type", APPLICATION_FORM_URLENCODED.getMimeType());

            HttpResponse httpResponse = httpClient.execute(httpGet);
            StatusLine statusLine = httpResponse.getStatusLine();

            String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
            consume(httpResponse.getEntity());

            if (statusLine.getStatusCode() == SC_OK) {
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
        } catch (URISyntaxException | IOException e) {
            throw new HttpConnectionException(e.getMessage(), e);
        }

        return retVal;
    }

    public long sendHeader(int fileCount, String category, String mspShortName) throws HttpConnectionException, HttpResponseException {
        log.debug("Sending Header Record. fileCount:{} category:{}", fileCount, category);

        long retVal = -1;

        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme(protocol).setHost(hostname).setPort(port).setPath(UPLOAD_HEADER);

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader(AUTHORIZATION, String.format("Bearer %s", oAuthToken));
            httpPost.setHeader("Content-type", APPLICATION_JSON.getMimeType());

            String param = new JSONStringer()
                    .object()
                    .key("fileCount").value(fileCount)
                    .key("category").value(category)
                    .key("mspShortName").value(mspShortName)
                    .endObject()
                    .toString();

            StringEntity entity = new StringEntity(param);
            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            StatusLine statusLine = httpResponse.getStatusLine();

            String content = EntityUtils.toString(httpResponse.getEntity());
            consume(httpResponse.getEntity());

            if (statusLine.getStatusCode() == SC_OK) {
                if (NumberUtils.isParsable(content)) {
                    retVal = Long.valueOf(content);
                }

                log.debug("Response:{}", content);
            } else {
                throw new HttpResponseException("Send Header Error"
                        + " statusCode:" + statusLine.getStatusCode()
                        + " reason:" + statusLine.getReasonPhrase());
            }
        } catch (URISyntaxException | IOException e) {
            throw new HttpConnectionException(e.getMessage(), e);
        }

        return retVal;
    }

    public void sendFiles(long headerID, List<FileBean> fileList)
            throws HttpResponseException, HttpConnectionException {

        log.debug("Sending file. headerID:{} file count:{}", headerID);

        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme(protocol).setHost(hostname).setPort(port).setPath(UPLOAD_FILE);

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader(AUTHORIZATION, String.format("Bearer %s", oAuthToken));

            // TODO: Explore HttpAsyncClient:ZeroCopyPost if some performance can be gained.
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder
                    .create()
                    .setContentType(MULTIPART_FORM_DATA)
                    .setLaxMode()
                    .setCharset(UTF_8)
                    .addTextBody("headerID", String.valueOf(headerID));

            String lastFile = "";
            for (FileBean fileBean : fileList) {
                lastFile = fileBean.getPath().getFileName().toString();

                InputStreamBody fileContent = new InputStreamBody(Files.newInputStream(fileBean.getPath()),
                        fileBean.getPath().getFileName().toString());
                multipartEntityBuilder.addPart("file", fileContent);

                log.debug("Uploading file:{}", fileBean.getPath().getFileName().toString());
            }

            multipartEntityBuilder.addTextBody("fileType", getFileType(lastFile));

            HttpEntity httpEntity = multipartEntityBuilder.build();
            log.debug("--------- Content size:{}, isChunked:{}", httpEntity.getContentLength(), httpEntity.isChunked());

            httpPost.setEntity(httpEntity);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            StatusLine statusLine = httpResponse.getStatusLine();

            String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
            PoolStats stats = connectionManager.getTotalStats();
            log.debug("Stats[L:{}, P:{}, A:{}, M:{}] Ack file list:{}", stats.getLeased(), stats.getPending(),
                    stats.getAvailable(), stats.getMax(), content);

            consume(httpResponse.getEntity());

            if (statusLine.getStatusCode() != SC_OK) {
                throw new HttpResponseException("Message:Send File Error"
                        + "\nStatus Code:" + statusLine.getStatusCode()
                        + "\nReason:" + statusLine.getReasonPhrase());
            }
        } catch (URISyntaxException | IOException e) {
            throw new HttpConnectionException(e.getMessage(), e);
        }
    }

    // TODO: Optimize code. Remove duplication.
    public String sendTrailer(long headerID) throws HttpConnectionException, HttpResponseException {
        String retVal;

        log.debug("Sending Trailer Record. headerID:{}", headerID);

        // TODO: Retrieve URL from configuration
        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme(protocol).setHost(hostname).setPort(port).setPath(UPLOAD_TRAILER);

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setHeader(AUTHORIZATION, String.format("Bearer %s", oAuthToken));
            httpPost.setHeader("Content-type", APPLICATION_JSON.getMimeType());

            String param = new JSONStringer()
                    .object()
                    .key("headerID").value(headerID)
                    .endObject()
                    .toString();

            StringEntity entity = new StringEntity(param);

            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            StatusLine statusLine = httpResponse.getStatusLine();

            String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
            JSONObject jsonData = new JSONObject(content);

            consume(httpResponse.getEntity());

            if (statusLine.getStatusCode() == SC_OK) {
                retVal = (String) jsonData.get("transactionID");

                log.debug("Response:{}", retVal);
            } else {
                throw new HttpResponseException("Message:Send Header Error"
                        + "\nStatus Code:" + jsonData.get("status")
                        + "\nReason:" + jsonData.get("error"));
            }
        } catch (URISyntaxException | IOException e) {
            throw new HttpConnectionException(e.getMessage(), e);
        }

        return retVal;
    }

    public List<ComboBoxItem> getMSPListing() throws HttpConnectionException, HttpResponseException {
        log.debug("Loading MSP Listing");

        List<ComboBoxItem> retVal = new ArrayList<>();

        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme(protocol).setHost(hostname).setPort(port).setPath(MSP_LISTING_URL);

            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.setHeader(AUTHORIZATION, String.format("Bearer %s", oAuthToken));
            httpGet.setHeader("Content-type", APPLICATION_FORM_URLENCODED.getMimeType());

            HttpResponse httpResponse = httpClient.execute(httpGet);
            StatusLine statusLine = httpResponse.getStatusLine();

            String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
            consume(httpResponse.getEntity());

            if (statusLine.getStatusCode() == SC_OK) {
                JSONArray jsonArray = new JSONArray(content);

                for (Object aJsonArray : jsonArray) {
                    JSONObject object = (JSONObject) aJsonArray;

                    String shortName = object.getString("shortName");
                    String participantName = object.getString("participantName") + "(" + shortName + ")";

                    retVal.add(new ComboBoxItem(shortName, participantName));
                }
            } else {
                throw new HttpResponseException("Message:Get MSP Listing Error"
                        + "\nStatus Code:" + statusLine.getStatusCode()
                        + "\nReason:" + statusLine.getReasonPhrase());
            }
        } catch (URISyntaxException | IOException e) {
            throw new HttpConnectionException(e.getMessage(), e);
        }

        return retVal;
    }

    public List<FileStatus> checkStatus(Long headerID) {
        log.debug("Check status headerID: {}", headerID);

        List<FileStatus> retVal = new ArrayList<>();

        try {
            String path = CHECK_STATUS + "/" + headerID;
            URIBuilder builder = new URIBuilder()
                    .setScheme(protocol).setHost(hostname).setPort(port).setPath(path);

            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.setHeader(AUTHORIZATION, String.format("Bearer %s", oAuthToken));

            try {
                HttpResponse httpResponse = httpClient.execute(httpGet);

                StatusLine statusLine = httpResponse.getStatusLine();

                if (statusLine.getStatusCode() == SC_OK) {
                    String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);

                    JSONArray jsonData = new JSONArray(content);

                    for (int i = 0; i < jsonData.length(); i++) {
                        FileStatus fileStatus = new FileStatus();
                        // TODO: Set log level to INFO
                        BeanUtils.populate(fileStatus, jsonData.getJSONObject(i).toMap());
                        retVal.add(fileStatus);
                    }
                }

                consume(httpResponse.getEntity());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return retVal;
    }

    public HeaderStatus getHeader(Long headerID) {
        log.debug("Check status:{}", headerID);

        HeaderStatus retVal = null;

        try {
            String path = GET_HEADER + "/" + headerID;
            URIBuilder builder = new URIBuilder()
                    .setScheme(protocol).setHost(hostname).setPort(port).setPath(path);

            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.setHeader(AUTHORIZATION, String.format("Bearer %s", oAuthToken));

            HttpResponse httpResponse = httpClient.execute(httpGet);

            StatusLine statusLine = httpResponse.getStatusLine();

            String content = EntityUtils.toString(httpResponse.getEntity(), CHAR_ENCODING);
            log.debug("content:{}", content);

            consume(httpResponse.getEntity());

            if (statusLine.getStatusCode() == SC_OK) {
                JSONObject jsonData = new JSONObject(content);
                retVal = new HeaderStatus();

                // TODO: Set log level to INFO
                BeanUtils.populate(retVal, jsonData.toMap());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return retVal;
    }

    public void shutdown() throws IOException {
        log.debug("Shutting down HTTP connection");

        httpClient.close();
    }

    private String getFileType(String filename) {
        String retVal = null;

        String fileExt = FilenameUtils.getExtension(filename);

        if (equalsIgnoreCase(fileExt, "XLS") || equalsIgnoreCase(fileExt, "XLSX")) {
            retVal = "XLS";
        } else if (equalsIgnoreCase(fileExt, "MDE") || equalsIgnoreCase(fileExt, "MDEF")) {
            retVal = "MDEF";
        } else if (equalsIgnoreCase(fileExt, "CSV")) {
            retVal = "CSV";
        }

        return retVal;
    }


    private void initializeDeviceId() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();

            if (mac != null) {
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }

                deviceId = generateKey(sb.toString()); //sb.toString() will look like 12-34-56-78-9A-BC
            } else {
                deviceId = UUID.randomUUID().toString();
            }
        } catch (UnknownHostException | SocketException | NullPointerException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            deviceId = UUID.randomUUID().toString();
        }
    }

    private String generateKey(String values) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(values.getBytes("UTF-8"));
            return String.format("%032x", new BigInteger(1, bytes));
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).", nsae);
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).", uee);
        }
    }

}
