package uk.co.dalelane.kafkastreams.xboxlive.data.presence;

import java.time.Instant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import uk.co.dalelane.kafkastreams.xboxlive.data.users.XboxUserInfo;

public class EnrichedPresence {

    @SerializedName("date")
    @Expose
    private Instant date;

    @SerializedName("gamertag")
    @Expose
    private String gamertag;

    @SerializedName("realName")
    @Expose
    private String realName;

    @SerializedName("profilePicUrl")
    @Expose
    private String profilePicUrl;

    @SerializedName("state")
    @Expose
    private String state;

    @SerializedName("titleid")
    @Expose
    private String titleid;

    @SerializedName("titlename")
    @Expose
    private String titlename;



    public EnrichedPresence() {}

    public EnrichedPresence(Presence event, XboxUserInfo userinfo) {
        this.date = event.getDate();
        this.gamertag = userinfo.getGamertag();
        this.realName = userinfo.getRealName();
        this.profilePicUrl = userinfo.getProfilePicUrl();
        this.state = event.getState();
        this.titleid = event.getTitleId();
        this.titlename = event.getTitleName();
    }

    public boolean userOffline() {
        return "Offline".equals(state) || "Away".equals(state);
    }




    public Instant getDate() {
        return date;
    }

    public String getGamertag() {
        return gamertag;
    }

    public String getRealName() {
        return realName;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public String getState() {
        return state;
    }

    public String getTitleId() {
        if (titleid == null) {
            return "";
        }
        return titleid;
    }

    public String getTitleName() {
        return titlename;
    }
}
