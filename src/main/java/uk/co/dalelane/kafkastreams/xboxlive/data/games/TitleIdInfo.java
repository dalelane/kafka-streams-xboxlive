package uk.co.dalelane.kafkastreams.xboxlive.data.games;

public class TitleIdInfo {

    public static final String GAME         = "GAME";
    public static final String MEDIA_PLAYER = "MEDIA";
    public static final String APP          = "APP";



    public static String getTitleType(String titleid) {
        if (titleid == null) {
            return null;
        }

        switch (titleid) {
            // Xbox iOS app
            case "328178078":
            // Xbox game store
            case "1864271209":
            // Xbox dashboard
            case "750323071":
                return APP;

            // Amazon Prime Video
            case "1509068581":
            // All-4
            case "1749265960":
            // BBC iPlayer
            case "1824269336":
            // itvX
            case "1715639942":
            // Plex
            case "1826276761":
            // AppleTV
            case "1943777318":
            // YouTube
            case "122001257":
            // Netflix
            case "327370029":
                return MEDIA_PLAYER;


            default:
                return GAME;
        }
    }
}
