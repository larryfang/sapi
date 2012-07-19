package job;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.io.InputStream;

import java.util.List;
import java.util.LinkedList;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class SapiSearch {

    public class Response {

        public int code;
        public String message;
        public int totalResults;
        public List<Listing> results = new LinkedList<Listing>();

    }

    public class Listing {

        public String name;
        public String addressLine;

    }

    private static final String API_KEY = "frnt5jmtav2gue5x2kpu7m2u";

    private Response callEndpoint(String endpoint, String query, String location, Integer page, Integer rows)
            throws Exception {

        // build path and query component of URL
        String path =  endpoint + "?key=" + API_KEY + "&query=" + URLEncoder.encode(query, "UTF-8");

        // add location parameter if given
        if (location != null) {
            path += "&location=" + URLEncoder.encode(location, "UTF-8");
        }

        // add page parameter if given
        if (page != null) {
            path += "&page=" + page;
        }

        // add rows parameter if given
        if (rows != null) {
            path += "&rows=" + rows;
        }

        // create URL from server and path
        URL searchUrl = new URL("http://api.sensis.com.au/ob-20110511/test/" + path);

        // open connection to the server
        HttpURLConnection conn = (HttpURLConnection)searchUrl.openConnection();
        conn.connect();

        try {

            // ensure HTTP 200 (OK) response
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException(
                        "Error calling Search API (HTTP status "
                                + conn.getResponseCode() + ")");
            }

            // grab the response stream
            InputStream stream = conn.getInputStream();

            // parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readValue(stream, JsonNode.class);

            Response response = new Response();

            // grab the reponse code and message fields
            response.code = root.get("code").getIntValue();
            response.message = root.get("message").getTextValue();

            // ensure status code is success or spell-checker run
            if (response.code != 200 && response.code != 206) {
                throw new RuntimeException(
                        "API returned error: " + response.message +
                                ", code: " + response.code);
            }

            // grab the totalResults field
            response.totalResults = root.get("totalResults").getIntValue();

            // iterate over the results and add to list
            for (JsonNode result : root.get("results")) {

                Listing listing = new Listing();

                listing.name = result.path("name").getTextValue();
                listing.addressLine = result.path("primaryAddress").path("addressLine").getTextValue();

                response.results.add(listing);
            }

            stream.close();

            return response;

        } finally {
            conn.disconnect(); // ensure we always close the connection
        }

    }

    public Response search(String query, String location, Integer page, Integer rows)
            throws Exception {

        // call search endpoint with given parameters
        return callEndpoint("search", query, location, page, rows);

    }

    public Response getByListingId(int id) throws Exception {

        // call getByListingId endpoint
        return callEndpoint("getByListingId", String.valueOf(id), null, null, null);

    }

}