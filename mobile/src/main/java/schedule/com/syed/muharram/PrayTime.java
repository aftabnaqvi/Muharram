package schedule.com.syed.muharram;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PrayTime {
	private String mName;
	private String mTime;
	
	public PrayTime(){
		
	}
	
	public PrayTime(String name, String time){
		mName = name;
		mTime = time;
	}

	public String getName() {
		return mName;
	}
	public String getTime() {
		return mTime;
	}

	public static List<PrayTime> fromJSON(JSONObject response) {
		if(response == null)
			return null;
		
		//{"Fajr":"05:59","Isha":"18:18","Asr":"14:43","Dhuhr":"12:11","Sunset":"17:01","Sunrise":"07:21","Maghrib":"17:19","Imsaak":"05:48"}
		List<PrayTime> prayTimes = new ArrayList<PrayTime>();
		
		try {
			if(response.getString("Imsaak") != null){
				PrayTime time = new PrayTime("Imsaak", response.getString("Imsaak"));
				time.mTime = get12HrFormatTime(time.mTime);
				prayTimes.add(time);
			}
		
			if(response.getString("Fajr") != null){
				PrayTime time = new PrayTime("Fajr", response.getString("Fajr"));
				time.mTime = get12HrFormatTime(time.mTime);
				prayTimes.add(time);
			}
			
			if(response.getString("Sunrise") != null){
				PrayTime time = new PrayTime("Sunrise", response.getString("Sunrise"));
				time.mTime = get12HrFormatTime(time.mTime);
				prayTimes.add(time);
			}
			
			if(response.getString("Dhuhr") != null){
				PrayTime time = new PrayTime("Dhuhr", response.getString("Dhuhr"));
				time.mTime = get12HrFormatTime(time.mTime);
				prayTimes.add(time);
			}
			
			if(response.getString("Asr") != null){
				PrayTime time = new PrayTime("Asr", response.getString("Asr"));
				time.mTime = get12HrFormatTime(time.mTime);
				prayTimes.add(time);
			}
			
			if(response.getString("Sunset") != null){
				PrayTime time = new PrayTime("Sunset", response.getString("Sunset"));
				time.mTime = get12HrFormatTime(time.mTime);
				prayTimes.add(time);
			}
			
			if(response.getString("Maghrib") != null){
				PrayTime time = new PrayTime("Maghrib", response.getString("Maghrib"));
				time.mTime = get12HrFormatTime(time.mTime);
				prayTimes.add(time);
			}
			
			if(response.getString("Isha") != null){
				PrayTime time = new PrayTime("Isha", response.getString("Isha"));
				get12HrFormatTime(time.mTime);
				time.mTime = get12HrFormatTime(time.mTime);
				prayTimes.add(time);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return prayTimes;
	}

	public static List<PrayTime> fromSabaPraytimesJSON(JSONObject response) {
		if(response == null)
			return null;

		//{"Fajr":"05:59","Isha":"18:18","Asr":"14:43","Dhuhr":"12:11","Sunset":"17:01","Sunrise":"07:21","Maghrib":"17:19","Imsaak":"05:48"}
		List<PrayTime> prayTimes = new ArrayList<PrayTime>();

		try {
			if(response.getString("imsak") != null){
				PrayTime time = new PrayTime("Imsaak", response.getString("imsak"));
				time.mTime = time.mTime;
				prayTimes.add(time);
			}

			if(response.getString("fajar") != null){
				PrayTime time = new PrayTime("Fajr", response.getString("fajar"));
				time.mTime = time.mTime;
				prayTimes.add(time);
			}

			if(response.getString("sunrise") != null){
				PrayTime time = new PrayTime("Sunrise", response.getString("sunrise"));
				time.mTime = time.mTime;
				prayTimes.add(time);
			}

			if(response.getString("zuhur") != null){
				PrayTime time = new PrayTime("Zuhr", response.getString("zuhur"));
				time.mTime = time.mTime;
				prayTimes.add(time);
			}

			if(response.getString("sunset") != null){
				PrayTime time = new PrayTime("Sunset", response.getString("sunset"));
				time.mTime = time.mTime;
				prayTimes.add(time);
			}

			if(response.getString("maghrib") != null){
				PrayTime time = new PrayTime("Maghrib", response.getString("maghrib"));
				time.mTime = time.mTime;
				prayTimes.add(time);
			}

			if(response.getString("isha") != null){
				PrayTime time = new PrayTime("Isha", response.getString("isha"));
				time.mTime = time.mTime;
				prayTimes.add(time);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return prayTimes;
	}

	public static String get12HrFormatTime(String time){
		if("-----".equals(time)== true)
			return time;

		Date date24Hours = null;
		SimpleDateFormat simpleDateFormat12Hours = null;
		try {
			SimpleDateFormat simpleDateFormat24Hours = new SimpleDateFormat("HH:mm");
			simpleDateFormat12Hours = new SimpleDateFormat("hh:mm a");
			date24Hours = simpleDateFormat24Hours.parse(time);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return simpleDateFormat12Hours.format(date24Hours);
	}
}

