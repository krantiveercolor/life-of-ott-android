package com.life.android.models;

import com.life.android.utils.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GenreModel {
    String name, id;
    @SerializedName("genere_size")
    String viewType;


    List<CommonModels> list = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CommonModels> getList() {
        return list;
    }

    public void setList(List<CommonModels> list) {
        this.list = list;
    }

    public int getViewType() {
        switch (viewType) {
            case "VERTICAL_SMALL":
                return Constants.GenreSizeTypes.VERTICAL_SMALL;
            case "HORIZONTAL_SMALL":
                return Constants.GenreSizeTypes.HORIZONTAL_SMALL;
            case "HORIZONTAL_LARGE":
                return Constants.GenreSizeTypes.HORIZONTAL_LARGE;
            case "CIRCLE_SMALL":
                return Constants.GenreSizeTypes.CIRCLE_SMALL;
            default:
                return Constants.GenreSizeTypes.VERTICAL_LARGE;
        }

    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }
}
