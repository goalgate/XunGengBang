package com.xungengbang.Bean;


import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ReUploadComplexBean {

    @Id(autoincrement = true)
    private Long id;

    @Property
    private String Method;

    @Property
    private String content;

    @Property
    private int bmpcount;

    @Property
    @Convert(converter = ListConverter.class, columnType = String.class)
    private List<String> fjUrls;

    @Property
    @Convert(converter = ListConverter.class, columnType = String.class)
    private List<String> bmpFileNames;

    @Generated(hash = 767148292)
    public ReUploadComplexBean(Long id, String Method, String content, int bmpcount,
            List<String> fjUrls, List<String> bmpFileNames) {
        this.id = id;
        this.Method = Method;
        this.content = content;
        this.bmpcount = bmpcount;
        this.fjUrls = fjUrls;
        this.bmpFileNames = bmpFileNames;
    }

    @Generated(hash = 636346521)
    public ReUploadComplexBean() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMethod() {
        return this.Method;
    }

    public void setMethod(String Method) {
        this.Method = Method;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getFjUrls() {
        return this.fjUrls;
    }

    public void setFjUrls(List<String> fjUrls) {
        this.fjUrls = fjUrls;
    }

    public List<String> getBmpFileNames() {
        return this.bmpFileNames;
    }

    public void setBmpFileNames(List<String> bmpFileNames) {
        this.bmpFileNames = bmpFileNames;
    }

    public int getBmpcount() {
        return this.bmpcount;
    }

    public void setBmpcount(int bmpcount) {
        this.bmpcount = bmpcount;
    }

    public static class ListConverter implements PropertyConverter<List<String>, String> {
        @Override
        public List<String> convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return null;
            } else {
                List<String> list = Arrays.asList(databaseValue.split(","));
                return list;
            }
        }

        @Override
        public String convertToDatabaseValue(List<String> entityProperty) {
            if (entityProperty == null) {
                return null;
            } else {
                StringBuilder sb = new StringBuilder();
                if (entityProperty.size() != 0) {
                    for (String link : entityProperty) {
                        sb.append(link);
                        sb.append(",");
                    }
                }

                return sb.toString();
            }
        }
    }

}
