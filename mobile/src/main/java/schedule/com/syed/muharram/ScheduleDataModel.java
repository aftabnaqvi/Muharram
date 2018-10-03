package schedule.com.syed.muharram;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ScheduleDataModel {

    private String mName;

    private String mAddress;
    private String mDate;
    private String mDay;
    private String mTime;
    private String mPhone;
    private String mMasayab;


    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getDay() {
        return mDay;
    }

    public void setDay(String day) {
        mDay = day;
    }


    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getMasayab() {
        return mMasayab;
    }

    public void setMasayab(String masayab) {
        mMasayab = masayab;
    }

    public static ScheduleDataModel fromJSON(JSONObject json){
        ScheduleDataModel data = new ScheduleDataModel();

        try {
            data.setName(json.getString("Name"));
            data.setAddress(json.getString("Address"));
            data.setPhone(json.getString("Phone"));
            data.setDate(json.getString("Date"));
            data.setDay(json.getString("Day"));
            data.setTime(json.getString("Time"));
            data.setMasayab(json.getString("Masayab"));

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return data;
    }

    public static ArrayList<ScheduleDataModel> fromJSONArray(String programName, JSONArray jsonArray){
        ArrayList<ScheduleDataModel> programs = new ArrayList<ScheduleDataModel>();

        for(int i=0; i<jsonArray.length(); i++){
            JSONObject scheduleJson = null;
            try{
                scheduleJson = jsonArray.getJSONObject(i);
                ScheduleDataModel schedule = fromJSON(scheduleJson);

                // convert in-coming date in more readable formt which includes day as well.
                Date actualDate = null;
                try{
                    actualDate = new SimpleDateFormat("MM/dd/yy").parse(schedule.getDate()); // MM/dd/yy is important ---- lower-case dd/yy is important.
                } catch(Exception e){
                    e.printStackTrace();
                    continue;
                }

                Date currDate = new Date();

                if(actualDate!=null && actualDate.getTime()<currDate.getTime()){
                    continue;
                }

                programs.add(schedule);

            } catch(JSONException e){
                e.printStackTrace();
                continue;
            }

        }

        return programs;
    }


}