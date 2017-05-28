package com.simoncherry.findface.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Simon on 2017/5/28.
 */

public class SkipBean extends RealmObject {
    @PrimaryKey
    private long id;
    private String path;
    private String name;
    private long date;

    public SkipBean() {
    }

    public SkipBean(long id, String path, String name, long date) {
        this.id = id;
        this.path = path;
        this.name = name;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "SkipBean{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", date=" + date +
                '}';
    }
}
