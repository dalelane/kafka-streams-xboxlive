package uk.co.dalelane.kafkastreams.xboxlive.data.users;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a subset of the data about a user as returned
 *  by the API https://xbl.io/api/v2/friends
 *
 * See https://xbl.io/console for documentation on the API.
 */
public class XboxUserInfo {

    @SerializedName("xuid")
    @Expose
    private String userid;

    @SerializedName("displayName")
    @Expose
    private String displayName;

    @SerializedName("realName")
    @Expose
    private String realName;

    @SerializedName("displayPicRaw")
    @Expose
    private String profilePicUrl;

    @SerializedName("gamertag")
    @Expose
    private String gamertag;


    public void setUserId(String userid) {
        this.userid = userid;
    }
    public String getUserId() {
        return userid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRealName() {
        return realName;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public String getGamertag() {
        return gamertag;
    }
}