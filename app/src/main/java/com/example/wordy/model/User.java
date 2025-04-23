package com.example.wordy.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    private String name;
    @Nullable
    private String birthday;
    private String phone;
    public User(){};

    public User(String name, @Nullable String birthday, String phone) {
        this.name = name;
        this.birthday = birthday;
        this.phone = phone;
    }

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getPhone() {return phone;}
    public void setPhone(String phone) {this.phone = phone;}

    @Nullable
    public String getBirthday() {return birthday;}
    public void setBirthday(@Nullable String birthday) {this.birthday = birthday;}
}
