package com.gmail.seinkenaiyan.models;


public class Stage {
    private String stageName;
    private StageLocation stageLocation;
    private String iconURL;
    private String rating;
    private String vicinity;

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public StageLocation getStageLocation() {
        return stageLocation;
    }

    public void setStageLocation(StageLocation stageLocation) {
        this.stageLocation = stageLocation;
    }

    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }
}
