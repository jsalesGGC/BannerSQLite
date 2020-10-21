package org.example;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class BannerUtils {
    // URI for Banner 9 API, built like a URL
    public static final String PROTOCOL = "https://";
    public static final String DOMAIN = "ggc.gabest.usg.edu";
    public static final String PATH = "/StudentRegistrationSsb/ssb";
    public static final String URL_API = PROTOCOL + DOMAIN + PATH;
    public static final String URL_API_SELECT_TERM = URL_API + "/term/search?mode=search";

    public static final String FILE_TERMS = "terms.json";

    public int[] terms;

    private ObjectMapper objectMapper;

    public BannerUtils() {
        objectMapper = new ObjectMapper();
    }

    public void init() throws IOException {
        if (!new File(FILE_TERMS).exists()) {
            String jsonTerms = IOUtils.toString(new URL(createTermsApiUrl()), "UTF-8");
            buildFileFromJsonString(FILE_TERMS, jsonTerms);
        }
        initTermsFromFile(FILE_TERMS);

//        for (int term : terms) {
        int term = 200608;
        int pageOffset = 0; // the index to start from
        String fileName = term + "-" + pageOffset + ".json";
        int totalCount = 1000;
        boolean setTotalCountOnce = true;
        boolean overWrite = true;

        do {
            if (!new File(fileName).exists() || overWrite) {
                String searchResultsApiUrl = createSearchResultsApiUrl(term, pageOffset);
                String termCookie = fetchTermCookie(URL_API_SELECT_TERM, term);
                String courseDataOfTerm = fetchCourseDataOfTerm(searchResultsApiUrl, termCookie);
                JsonNode data = objectMapper.readTree(courseDataOfTerm);
                if (setTotalCountOnce) {
                    totalCount = Integer.parseInt(data.get("totalCount").asText());
                    setTotalCountOnce = false;
                }
                buildFileFromJsonString(fileName, courseDataOfTerm);
                objectMapper.writeValue(new File(fileName), data);
            }
            totalCount -= 500;
            pageOffset += 500;
            fileName = term + "-" + pageOffset + ".json";
        } while (totalCount > 0);
//        }

    }

    public void review() throws IOException {
        if (!new File(FILE_TERMS).exists()) {
            String jsonTerms = IOUtils.toString(new URL(createTermsApiUrl()), "UTF-8");
            buildFileFromJsonString(FILE_TERMS, jsonTerms);
        }
        initTermsFromFile(FILE_TERMS);

        for (int term : terms) {
            int pageOffset = 0; // the index to start from
            String fileName = term + "-" + pageOffset + ".json";
            int totalCount = 1000;
            boolean setTotalCountOnce = true;
            boolean overWrite = false;

            do {
                if (!new File(fileName).exists()) {
                    String searchResultsApiUrl = createSearchResultsApiUrl(term, pageOffset);
                    String termCookie = fetchTermCookie(URL_API_SELECT_TERM, term);
                    String courseDataOfTerm = fetchCourseDataOfTerm(searchResultsApiUrl, termCookie);
                    JsonNode data = objectMapper.readTree(courseDataOfTerm);
                    if (setTotalCountOnce) {
                        totalCount = Integer.parseInt(data.get("totalCount").asText());
                        setTotalCountOnce = false;
                    }
                    buildFileFromJsonString(fileName, courseDataOfTerm);
                    objectMapper.writeValue(new File(fileName), data);
                    System.out.println("Term:" + term + "-" + pageOffset + " Created");
                } else {
                    JsonNode fileData = objectMapper.readTree(new File(fileName));
                    if (setTotalCountOnce) {
                        totalCount = Integer.parseInt(fileData.get("totalCount").asText());
                        setTotalCountOnce = false;
                    }
                    if (Integer.parseInt(fileData.get("totalCount").asText()) == 0) {
                        String searchResultsApiUrl = createSearchResultsApiUrl(term, pageOffset);
                        String termCookie = fetchTermCookie(URL_API_SELECT_TERM, term);
                        String courseDataOfTerm = fetchCourseDataOfTerm(searchResultsApiUrl, termCookie);
                        JsonNode data = objectMapper.readTree(courseDataOfTerm);
                        buildFileFromJsonString(fileName, courseDataOfTerm);
                        objectMapper.writeValue(new File(fileName), data);
                        System.out.println("Term:" + term + "-" + pageOffset + " OverWritten");
                    }
                }
                totalCount -= 500;
                pageOffset += 500;
                fileName = term + "-" + pageOffset + ".json";
                overWrite = false;
            } while (totalCount > 0);
        }
    }

    public void prune() throws IOException {
        if (!new File(FILE_TERMS).exists()) {
            String jsonTerms = IOUtils.toString(new URL(createTermsApiUrl()), "UTF-8");
            buildFileFromJsonString(FILE_TERMS, jsonTerms);
        }
        initTermsFromFile(FILE_TERMS);
        int[] termss = {201802};

        for (int term : terms) {
            int pageOffset = 0; // the index to start from
            String fileName = term + "-" + pageOffset + ".json";
            int totalCount = 4000;
            boolean setTotalCountOnce = true;
            boolean overWrite = false;

            do {
                if (!new File(fileName).exists()) {
                } else {
                    JsonNode fileData = objectMapper.readTree(new File(fileName));
                    if (setTotalCountOnce) {
//                        totalCount = Integer.parseInt(fileData.get("totalCount").asText());
//                        System.out.println("settotalcount" + totalCount);
                        setTotalCountOnce = false;
                    }
                    if (fileData.get("data").size() == 0) {
                        System.out.println("Deleted: " + fileName);
                        new File(fileName).delete();
                    }
                }
                totalCount -= 500;
                pageOffset += 500;
                fileName = term + "-" + pageOffset + ".json";
                overWrite = false;
            } while (totalCount > 0);
        }
    }

    public String createTermsApiUrl() {
        return URL_API + "/classSearch/getTerms?offset=1&max=500&searchTerm=";
    }

    public String createSearchResultsApiUrl(int term, int pageOffset) {
        return URL_API + "/searchResults/searchResults?txt_subject=&txt_courseNumber=&txt_term=" + term
                + "&startDatepicker=&endDatepicker=&pageOffset=" + pageOffset
                + "&pageMaxSize=" + 500 + "&sortColumn=subjectDescription&sortDirection=asc";
    }

    public void buildFileFromJsonString(String fileName, String courseDataOfTerm) throws IOException {
        JsonNode data = objectMapper.readTree(courseDataOfTerm);
        objectMapper.writeValue(new File(fileName), data);
    }

    public void initTermsFromFile(String fileName) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(new File(fileName));
        int[] terms = new int[jsonNode.size()];
        for (int i = 0; i < terms.length; i++) {
            terms[i] = Integer.parseInt(jsonNode.get(i).get("code").asText());
        }
        this.terms = terms;
    }

    public String fetchTermCookie(String urlParam, int termParam) {
//        String jsonResponse = "";
        String nuBannerCookie = "";
        String jSessionId = "";
        try {
            // Set up the request
            URL url = new URL(urlParam);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(5000);
            // Add headers to the request
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpURLConnection.setRequestProperty("user-agent", "intellij");
            httpURLConnection.setRequestProperty("accept", "*/*");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("POST");
            //Send the request
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(("term=" + termParam).getBytes("UTF-8"));
            outputStream.close();

            // Read the response
            InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            // Read response headers
            nuBannerCookie = httpURLConnection.getHeaderField("Set-Cookie");
            jSessionId = httpURLConnection.getHeaderField(7);
            // Extract body of response
//            jsonResponse = IOUtils.toString(inputStream, "UTF-8");
            inputStream.close();
            httpURLConnection.disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
//        System.out.println("Set-Cookie:" + jSessionId);
//        System.out.println("Set-Cookie:" + nuBannerCookie);
        System.out.println("Term Within Cookie:" + termParam);
        return jSessionId + ";" + nuBannerCookie;
    }

    public String fetchCourseDataOfTerm(String urlParam, String cookie) {
        String jsonResponse = "";
        try {
            // Set up request
            URL url = new URL(urlParam);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(5000);
            // Add headers to the request
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpURLConnection.setRequestProperty("Cookie", cookie);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("GET");
            //Send the request
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.close();

            // Read the response
            InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            // Extract body of response
            jsonResponse = IOUtils.toString(inputStream, "UTF-8");
            inputStream.close();
            httpURLConnection.disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return jsonResponse;
    }

    public int[] getTerms() {
        return terms;
    }
}
