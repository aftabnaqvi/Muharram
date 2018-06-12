package schedule.com.syed.muharram;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ScheduleDataModel {

    private String mName;

    private String mAddress;



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


    public static ScheduleDataModel fromJSON(JSONObject json){
        ScheduleDataModel data = new ScheduleDataModel();

        try {
            data.setName(json.getString("Name"));
            data.setAddress(json.getString("Address"));
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
                programs.add(schedule);

            } catch(JSONException e){
                e.printStackTrace();
                continue;
            }

        }

        return programs;
    }


}