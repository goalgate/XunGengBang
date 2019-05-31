package com.xungengbang.Bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.lang.reflect.Method;
import org.greenrobot.greendao.annotation.Generated;


@Entity
public class ReUploadBean {

    @Id(autoincrement = true)
    private Long id;

    private String Method;

    private String content;

    @Generated(hash = 1826826771)
    public ReUploadBean(Long id, String Method, String content) {
        this.id = id;
        this.Method = Method;
        this.content = content;
    }

    @Generated(hash = 1965321528)
    public ReUploadBean() {
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


}
