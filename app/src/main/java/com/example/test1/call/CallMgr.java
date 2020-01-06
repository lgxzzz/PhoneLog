package com.example.test1.call;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.test1.call.bean.Calllog;
import com.example.test1.call.bean.Contact;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.icu.text.DateTimePatternGenerator.DAY;

public class CallMgr {
    public static final String TAG = "CallMgr";
    public Context mContext;
    //所有联系人记录
    List<Contact> mContacts = new ArrayList<>();

    private ContentResolver mResolver;
    private Uri mCallUri = CallLog.Calls.CONTENT_URI;
    private String[] mColumns = {
            CallLog.Calls.CACHED_NAME// 通话记录的联系人
            , CallLog.Calls.NUMBER// 通话记录的电话号码
            , CallLog.Calls.DATE// 通话记录的日期
            , CallLog.Calls.DURATION// 通话时长
            , CallLog.Calls.TYPE};// 通话类型

    public CallMgr(Context mContext){
        this.mContext = mContext;
    }

    //初始化数据
    public void init(){
        loadAllContacts();
        loadAllLogs();
    }

    //获取所有联系人
    public void loadAllContacts(){
        try{
            mResolver = mContext.getContentResolver();
            Cursor cursor = mResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[] { "display_name", "sort_key", "contact_id","data1" }, null, null, null);
            Log.e(TAG,"cursor connect count:" + cursor.getCount());
            while (cursor.moveToNext()) {
                //读取通讯录的姓名
                String name = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                //读取通讯录的号码
                String number = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                Log.i(TAG,"获取的通讯录是： " + name + "\n"
                        +  " number : " + number);

                Contact contact = new Contact();
                contact.mName = name;
                contact.mNumber = number;
                mContacts.add(contact);
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //获取所有联系人的通话记录
    public void loadAllLogs(){
        try{
            mResolver = mContext.getContentResolver();

            Cursor cursor = mResolver.query(
                    mCallUri, // 查询通话记录的URI
                    mColumns
                    , null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
            );
            Log.e(TAG,"cursor log count:" + cursor.getCount());
            while (cursor.moveToNext()){
                String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateLong));
                String time = new SimpleDateFormat("HH:mm").format(new Date(dateLong));
                int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));
                int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                String dayCurrent = new SimpleDateFormat("dd").format(new Date());
                String dayRecord = new SimpleDateFormat("dd").format(new Date(dateLong));
                String typeString = "";
                switch (type) {
                    case CallLog.Calls.INCOMING_TYPE:
                        //"打入"
                        typeString = "打入";
                        break;
                    case CallLog.Calls.OUTGOING_TYPE:
                        //"打出"
                        typeString = "打出";
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        //"未接"
                        typeString = "未接";
                        break;
                    default:
                        break;
                }

                Calllog mCalllog = new Calllog();
                mCalllog.mName = name;
                mCalllog.mNumber = number;
                mCalllog.mDateLong = dateLong;
                mCalllog.mDate = date;
                mCalllog.mTime = time;
                mCalllog.mDuration = duration;
                mCalllog.mTypeString = typeString;
                mCalllog.mDayCurrent = dayCurrent;
                mCalllog.mDayRecord = dayRecord;

                Contact contact = getContactByName(name);
                if (contact!=null){
                    contact.mCalllogs.add(mCalllog);
                }

                String dayString = "";
                if ((Integer.parseInt(dayCurrent)) == (Integer.parseInt(dayRecord))) {
                    //今天
                    dayString = "今天";
                } else if ((Integer.parseInt(dayCurrent) - 1) == (Integer.parseInt(dayRecord))) {
                    //昨天
                    dayString = "昨天";
                } else {
                    //前天
                    dayString = "更久之前";
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //根据名字获取联系人
    public Contact getContactByName(String name){
        Contact contact = null;
        for(int i=0;i<mContacts.size();i++){
            Contact temp = mContacts.get(i);
            if (temp.mName.equals(name)){
                contact = temp;
                break;
            }
        }
        return contact;
    }

    /**
     * 获得两个日期间距多少天
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long getTimeDistance(Date beginDate, Date endDate) {
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(beginDate);
        fromCalendar.set(Calendar.HOUR_OF_DAY, fromCalendar.getMinimum(Calendar.HOUR_OF_DAY));
        fromCalendar.set(Calendar.MINUTE, fromCalendar.getMinimum(Calendar.MINUTE));
        fromCalendar.set(Calendar.SECOND, fromCalendar.getMinimum(Calendar.SECOND));
        fromCalendar.set(Calendar.MILLISECOND, fromCalendar.getMinimum(Calendar.MILLISECOND));
        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(endDate);
        toCalendar.set(Calendar.HOUR_OF_DAY, fromCalendar.getMinimum(Calendar.HOUR_OF_DAY));
        toCalendar.set(Calendar.MINUTE, fromCalendar.getMinimum(Calendar.MINUTE));
        toCalendar.set(Calendar.SECOND, fromCalendar.getMinimum(Calendar.SECOND));
        toCalendar.set(Calendar.MILLISECOND, fromCalendar.getMinimum(Calendar.MILLISECOND));

        long dayDistance = (toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / DAY;
        dayDistance = Math.abs(dayDistance);

        return dayDistance;
    }
}
