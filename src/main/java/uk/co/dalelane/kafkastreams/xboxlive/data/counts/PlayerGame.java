package uk.co.dalelane.kafkastreams.xboxlive.data.counts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlayerGame {

    @SerializedName("titleId")
    @Expose
    private String titleId;

    @SerializedName("titleName")
    @Expose
    private String titleName;

    public PlayerGame() {}

    public PlayerGame(String id, String name) {
        titleId = id;
        titleName = name;
    }

    public String getTitleId() {
        return titleId;
    }
    public String getTitleName() {
        return titleName;
    }
}
