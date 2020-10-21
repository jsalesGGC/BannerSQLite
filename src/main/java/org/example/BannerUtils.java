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

    // API Calls
    public static final String URL_API_GET_TERMS = URL_API + "/classSearch/getTerms?offset=1&max=500&searchTerm=";
    public static final String URL_API_POST_TERM_COOKIE = URL_API + "/term/search?mode=search";

    // API Call Builder
    public static String createSearchResultsApiUrl(int term, int pageOffset) {
        return URL_API + "/searchResults/searchResults?txt_subject=&txt_courseNumber=&txt_term=" + term
                + "&startDatepicker=&endDatepicker=&pageOffset=" + pageOffset
                + "&pageMaxSize=" + 500 + "&sortColumn=subjectDescription&sortDirection=asc";
    }

    public static final String DIR_JSON = "json";
    // File Names
    public static final String FILE_TERMS = DIR_JSON + "/" + "terms.json";


    // Private fields
    private ObjectMapper objectMapper;

    // Properties
    private int[] terms;

    public BannerUtils() throws IOException {
        objectMapper = new ObjectMapper();
        initTermsFromFile(FILE_TERMS);
    }

    public void testFileLocator() {
        if (new File(FILE_TERMS).exists()) {
            System.out.println("Found it");
        }
    }

    public void init() throws IOException {
        for (int term : terms) {
            int pageOffset = 0; // the index to start from
            String fileName = term + "-" + pageOffset + ".json";
            int totalCount = 1000;
            boolean setTotalCountOnce = true;

            do {
                if (!new File(fileName).exists()) {
                    String searchResultsApiUrl = createSearchResultsApiUrl(term, pageOffset);
                    String termCookie = fetchTermCookie(URL_API_POST_TERM_COOKIE, term);
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
                        String termCookie = fetchTermCookie(URL_API_POST_TERM_COOKIE, term);
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
            } while (totalCount > 0);
        }
    }

    public void prune() throws IOException {
        for (int term : terms) {
            int pageOffset = 0; // the index to start from
            String fileName = term + "-" + pageOffset + ".json";
            int totalCount = 4000;
            boolean setTotalCountOnce = true;
            do {
                if (!new File(fileName).exists()) {
                } else {
                    JsonNode fileData = objectMapper.readTree(new File(fileName));
                    if (fileData.get("data").size() == 0) {
                        System.out.println("Deleted: " + fileName);
                        new File(fileName).delete();
                    }
                }
                totalCount -= 500;
                pageOffset += 500;
                fileName = term + "-" + pageOffset + ".json";
            } while (totalCount > 0);
        }
    }

    public void read() throws IOException {
        for (int term : terms) {
            int pageOffset = 0; // the index to start from
            String fileName = DIR_JSON + "/" + term + "-" + pageOffset + ".json";
            int totalCount = 4000;
            boolean setTotalCountOnce = true;
            do {
                if (!new File(fileName).exists()) {
                } else {
                    JsonNode fileData = objectMapper.readTree(new File(fileName));
                    JsonNode termPageData = fileData.get("data");
                    for (int i = 0; i < termPageData.size(); i++) {
                        JsonNode faculty = termPageData.get(i).get("faculty");
                        for (int j = 0; j < faculty.size(); j++) {
                            if (faculty.get(j).get("displayName").asText().contains("Tacksoo")) {
                                System.out.print("Term:" + termPageData.get(i).get("termDesc").asText() + " ");
                                System.out.print("ID:" + termPageData.get(i).get("courseReferenceNumber").asText() + " ");
                                System.out.print("Subject:" + termPageData.get(i).get("subjectDescription").asText() + " ");
                                System.out.print("Title:" + termPageData.get(i).get("courseTitle").asText() + " ");
                                System.out.print("CourseNumber:" + termPageData.get(i).get("courseNumber").asText() + " ");
                                System.out.print("Credit Hours:" + termPageData.get(i).get("creditHourLow").asText() + " ");
                                System.out.print("Number of Students" + termPageData.get(i).get("enrollment").asText() + " ");
                                System.out.println(faculty.get(j).get("displayName").asText());
                            }
                        }
                    }
                }
                totalCount -= 500;
                pageOffset += 500;
                fileName = DIR_JSON + "/" + term + "-" + pageOffset + ".json";
            } while (totalCount > 0);
        }
    }

    public void buildFileFromJsonString(String fileName, String courseDataOfTerm) throws IOException {
        JsonNode data = objectMapper.readTree(courseDataOfTerm);
        objectMapper.writeValue(new File(fileName), data);
    }

    public void initTermsFromFile(String fileName) throws IOException {
        if (!new File(fileName).exists()) {
            String jsonTerms = IOUtils.toString(new URL(URL_API_GET_TERMS), "UTF-8");
            buildFileFromJsonString(fileName, jsonTerms);
        }
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
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(5000);
            // Add headers to the request
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpURLConnection.setRequestProperty("user-agent", "intellij");
            httpURLConnection.setRequestProperty("accept", "*/*");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
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
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(5000);
            // Add headers to the request
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpURLConnection.setRequestProperty("Cookie", cookie);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
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
