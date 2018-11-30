package com.example.yuminkim.childhold.network.model;

public class AbsentStatusResponse {
    public int absent;
    public String name;
    public boolean isAbsent(){
        return absent == 1 ? true : false;
    }
}
