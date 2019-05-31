package com.xungengbang.Bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class User {

    @Id(autoincrement = true)
    private Long id;

    @Unique
    private String username;

    @Unique
    private String password;

    @Unique
    private String gestures;

    @Unique
    private String cardId;

    private String name;

    @Generated(hash = 971287338)
    public User(Long id, String username, String password, String gestures,
            String cardId, String name) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.gestures = gestures;
        this.cardId = cardId;
        this.name = name;
    }

    @Generated(hash = 586692638)
    public User() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGestures() {
        return this.gestures;
    }

    public void setGestures(String gestures) {
        this.gestures = gestures;
    }

    public String getCardId() {
        return this.cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }




}
