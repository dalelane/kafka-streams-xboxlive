package uk.co.dalelane.kafkastreams.xboxlive.data.users;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class XboxUsersFactory {

    private static Logger log = LoggerFactory.getLogger(XboxUsersFactory.class);


    /**
     * Fetches a list of known Xbox users from the xbl API.
     */
    public static XboxUsers getXboxUsers(String apiKey) {
        log.debug("Getting known Xbox users from the xbl.io API");

        try {
            // URL to fetch the users info from
            URL urlObj = URI.create("https://xbl.io/api/v2/friends").toURL();

            // add request header with the API key required by xbl.io
            URLConnection conn = urlObj.openConnection();
            conn.setRequestProperty("x-authorization", apiKey);

            // create reader for getting API data
            Reader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);

            // prepare parser for reading the JSON response payload
            Gson parser = new Gson();

            // return API response
            return parser.fromJson(reader, XboxUsers.class);
        }
        catch (IOException e) {
            log.error("Unable to get known Xbox users from xbl.io API", e);
            return new XboxUsers();
        }
    }
}
