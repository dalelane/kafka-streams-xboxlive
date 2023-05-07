package uk.co.dalelane.kafkastreams.xboxlive.data.counts;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlayerGames {
    @SerializedName("gamertag")
    @Expose
    private String gamertag;

    @SerializedName("realname")
    @Expose
    private String realname;

    @SerializedName("games")
    @Expose
    private Map<String, PlayerGame> gamesIndexedById;

    public PlayerGames() {
        gamesIndexedById = new HashMap<>();
    }

    public String getGamertag() {
        return gamertag;
    }
    public String getRealname() {
        return realname;
    }
    public Map<String, PlayerGame> getGamesIndexedById() {
        return gamesIndexedById;
    }

    public void setGamertag(String gamertag) {
        this.gamertag = gamertag;
    }
    public void setRealname(String realname) {
        this.realname = realname;
    }
}
