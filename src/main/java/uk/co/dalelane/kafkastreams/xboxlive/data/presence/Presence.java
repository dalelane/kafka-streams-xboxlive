package uk.co.dalelane.kafkastreams.xboxlive.data.presence;

import java.time.Instant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Represents a subset of the response from the
 *  https://xbl.io/api/v2/presence API
 *
 * See https://xbl.io/console for documentation on the API.
 */
public class Presence {

    @SerializedName("date")
    @Expose
    private Instant date;

    @SerializedName("userid")
    @Expose
    private String userid;

    @SerializedName("state")
    @Expose
    private String state;

    @SerializedName("titleid")
    @Expose
    private String titleid;

    @SerializedName("titlename")
    @Expose
    private String titlename;




    public Instant getDate() {
        return date;
    }

    public String getUserId() {
        return userid;
    }

    public String getState() {
        return state;
    }

    public String getTitleId() {
        return titleid;
    }

    public String getTitleName() {
        return titlename;
    }
}
