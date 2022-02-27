package akvelon.testcase;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;



/**
 * Class-solution for akvelon hackerrank test case.
 * The main problem I have faced is I was unable to deserialize server response from json to readable object without third-library dependendcy.
 * So I had to do some workaround.
 * */
public class AkvelonTestCase {

    public static List<User> users = new ArrayList<>();

    public static void main(String[] args) {


        // get total pages count
        String url = "https://jsonmock.hackerrank.com/api/article_users";
        StringBuffer response = getRequest(url);
        Map contents = getContentFromResponse(response);
        int total_value = (int) contents.get("total_pages");
//        System.out.println(total_pages);


        // iterate through the pages to gather info
        for (int i = 1; i <= total_value; i++) {
            url = "https://jsonmock.hackerrank.com/api/article_users?page=" + i;
            processEachPage(url);
        }


        // return the list of users which has submission_count > 10
        System.out.println(
                users.stream()
                        .map(User::getName)
                        .collect(toList())
        );

    }

    private static void processEachPage(String GET_URL) {

        StringBuffer response = getRequest(GET_URL);
        Map contents = getContentFromResponse(response);

        // take all entities from response
        List datas = (List) contents.get("data");

        for (Object person : datas) {
            // iterate through every entity from response
            Map<Object, Object> personDetails = (Map) person;

            String username = null;
            int submissionCount = -1;

            User user = new User(); // use only one reference for all user objects to reduce memory consuming

            for (Map.Entry<Object, Object> entry : personDetails.entrySet()) {
                if (entry.getKey().equals("username")) {
                    user.setName((String) entry.getValue());
                }
                if (entry.getKey().equals("submission_count")) {
                    user.setSubmissionCount((int) entry.getValue());
                }
            }

            // filter for interesting users
            if (user.getSubmissionCount() > 10)
                users.add(user);
        }
    }

    private static Map getContentFromResponse(StringBuffer response) {
        ScriptEngine scriptEngine;
        ScriptEngineManager sem = new ScriptEngineManager();
        scriptEngine = sem.getEngineByName("javascript");
        String script = "Java.asJSONCompatible(" + response.toString() + ")";
        Object result = null;
        try {
            result = scriptEngine.eval(script);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        Map contents = (Map) result;
        return contents;
    }

    private static StringBuffer getRequest(String GET_URL) {
        URL obj;
        int responseCode;
        StringBuffer response = null;
        String USER_AGENT = "Mozilla/5.0";
        HttpURLConnection httpURLConnection;

        try {
            obj = new URL(GET_URL);
            httpURLConnection = (HttpURLConnection) obj.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
            responseCode = httpURLConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                String inputLine;
                response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
//            system.out.println(response);
            } else {
//            System.out.println("GET request not worked");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    static class User {
        String name;
        int submissionCount;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSubmissionCount() {
            return submissionCount;
        }

        public void setSubmissionCount(int submissionCount) {
            this.submissionCount = submissionCount;
        }

        @Override
        public String toString() {
            return "User{" +
                    "name='" + name + '\'' +
                    ", submissionCount=" + submissionCount +
                    '}';
        }
    }
}
