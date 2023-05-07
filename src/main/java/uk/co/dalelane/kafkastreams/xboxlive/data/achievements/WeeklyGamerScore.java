package uk.co.dalelane.kafkastreams.xboxlive.data.achievements;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WeeklyGamerScore {

    @SerializedName("gamer")
    @Expose
    private String gamer;

    @SerializedName("score")
    @Expose
    private Integer score;


    public WeeklyGamerScore() { }

    public WeeklyGamerScore(String gamertag, Integer weeklyScore) {
        this.gamer = gamertag;
        this.score = weeklyScore;
    }


    public String getGamer() {
        return gamer;
    }

    public Integer getScore() {
        return score;
    }
}
