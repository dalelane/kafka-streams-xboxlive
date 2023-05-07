package uk.co.dalelane.kafkastreams.xboxlive.data.users;

import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a subset of the response from the
 *  https://xbl.io/api/v2/friends API
 *
 * See https://xbl.io/console for documentation on the API.
 */
public class XboxUsers {

    @SerializedName("people")
    @Expose
    private List<XboxUserInfo> people;

    public List<XboxUserInfo> getPeople() {
        if (people == null) {
            return Collections.emptyList();
        }
        return people;
    }
}