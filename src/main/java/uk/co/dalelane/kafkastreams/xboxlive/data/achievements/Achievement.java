package uk.co.dalelane.kafkastreams.xboxlive.data.achievements;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Achievement {

    @SerializedName("date")
    @Expose
    private String date;

    @SerializedName("gamertag")
    @Expose
    private String gamertag;

    @SerializedName("gamername")
    @Expose
    private String gamername;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("description")
    @Expose
    private String description;

    @SerializedName("icon")
    @Expose
    private String icon;

    @SerializedName("contentname")
    @Expose
    private String contentname;

    @SerializedName("contentimage")
    @Expose
    private String contentimage;

    @SerializedName("platform")
    @Expose
    private String platform;

    @SerializedName("gamerscore")
    @Expose
    private Integer gamerscore;

    @SerializedName("rarityscore")
    @Expose
    private Integer rarityscore;

    @SerializedName("raritycategory")
    @Expose
    private String raritycategory;




    public String getGamertag() {
        return gamertag;
    }
    public Integer getGamerScore() {
        return gamerscore;
    }
}
