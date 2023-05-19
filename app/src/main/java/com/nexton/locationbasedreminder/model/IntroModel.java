package com.nexton.locationbasedreminder.model;

public class IntroModel {
    String introTitle, introDesc;
    int introImg;

    public IntroModel(String introTitle, String introDesc, int introImg) {
        this.introTitle = introTitle;
        this.introDesc = introDesc;
        this.introImg = introImg;
    }

    public String getIntroTitle() {
        return introTitle;
    }

    public void setIntroTitle(String introTitle) {
        this.introTitle = introTitle;
    }

    public String getIntroDesc() {
        return introDesc;
    }

    public void setIntroDesc(String introDesc) {
        this.introDesc = introDesc;
    }

    public int getIntroImg() {
        return introImg;
    }

    public void setIntroImg(int introImg) {
        this.introImg = introImg;
    }
}
