package com.example.test1.call.bean;

import android.telecom.Call;

import java.util.ArrayList;
import java.util.List;

public class Contact {
    //姓名
    public String mName;
    //号码
    public String mNumber;
    //通话记录
    public List<Calllog> mCalllogs = new ArrayList<>();
}
