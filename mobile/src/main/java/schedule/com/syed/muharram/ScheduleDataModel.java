package schedule.com.syed.muharram;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ScheduleDataModel {

    private String name;

    private String country;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }


    public static ScheduleDataModel fromJSON(JSONObject json){
        ScheduleDataModel data = new ScheduleDataModel();

        try {
            data.setName(json.getString("name"));
            data.setCountry(json.getString("country"));
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