package com.life.android.models.home_content;

public class LanguageModel {
    private String languageId;
    private String languageName;
    private String languageKey;

    public LanguageModel(String languageId, String languageName, String languageKey) {
        this.languageId = languageId;
        this.languageName = languageName;
        this.languageKey = languageKey;
    }

    public String getLanguageId() {
        return languageId;
    }

    public String getLanguageName() {
        return languageName;
    }

    public String getLanguageKey() {
        return languageKey;
    }
}
