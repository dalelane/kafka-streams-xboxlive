package uk.co.dalelane.kafkastreams.xboxlive.data.sessions;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import uk.co.dalelane.kafkastreams.xboxlive.data.presence.EnrichedPresence;

public class PlaySession {

    @SerializedName("startDate")
    @Expose
    private Instant startDate;

    @SerializedName("endDate")
    @Expose
    private Instant endDate;

    @SerializedName("duration")
    @Expose
    private Duration duration;

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



    public static PlaySession createFromStart(EnrichedPresence start) throws DateTimeParseException {
        PlaySession session = new PlaySession();
        session.startDate = start.getDate();
        session.gamertag = start.getGamertag();
        session.realName = start.getRealName();
        session.profilePicUrl = start.getProfilePicUrl();
        session.state = start.getState();
        session.titleid = start.getTitleId();
        session.titlename = start.getTitleName();
        return session;
    }

    public void setEnd(Instant end) {
        endDate = end;
        duration = Duration.between(startDate, endDate);
    }

    public boolean isComplete() {
        return endDate != null && duration != null;
    }

    public String getTitleId() {
        return titleid;
    }

    public Duration getDuration() {
        if (duration == null) {
            return Duration.ZERO;
        }
        return duration;
    }

    @Override
    public String toString() {
        return "PlaySession [startDate=" + startDate + ", endDate=" + endDate + ", duration=" + duration + ", gamertag="
                + gamertag + ", realName=" + realName + ", profilePicUrl=" + profilePicUrl + ", state=" + state
                + ", titleid=" + titleid + ", titlename=" + titlename + "]";
    }
}
