package schedule.com.syed.muharram;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

//import com.loopj.android.http.H;
//import org.apache.http.Header;

/**
 * @author Syed Aftab Naqvi
 * @create December, 2014
 * @version 1.0
 */
public class MuharramClient {
	private static volatile MuharramClient 	mMuharramClient;
	private static Context mContext;
	private static Map<String, ServerResponseListener> mMap;

	private static final String BASE_URL = "https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec?id=1CZ-5EaMUymPiQ4qz81HAGVkwA0ATU5NhSngmuLyMnuk&sheet=Sheet1";
	public static final String TAG = "MuharramClient";
	private static final String KEY_USER_ID = "user_id";

	private static final int TIME_OUT = 30000;

	private MuharramClient(){

	}
	/**
	 * @param context Application Context
	 * @return MuharramClient
	 */
	public static MuharramClient getInstance(Context context) {
	   if(mMuharramClient == null) {
		   synchronized(MuharramClient.class) {
			   if(mMuharramClient == null) {
				   mMuharramClient = new MuharramClient();
				   mMuharramClient.mMap = new HashMap<String, ServerResponseListener>();
				   mMuharramClient.mContext = context;
			   }
		   }
	   }

	   return mMuharramClient;
	}

	public boolean isInProgress(){
		return mMap.size() != 0;
	}

	public void getMuharramSchedule(ServerResponseListener target){
		sendRequest("MuharramSchedule", BASE_URL, target);
	}

	public void removeTarget(String program, ServerResponseListener target){
		if(mMap.containsKey(program)){
			mMap.remove(program);
		}
	}
	
	private void sendRequest(final String programName, final String url, final ServerResponseListener target){
		mMap.put(programName, target);

		// create the network client
    	AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(TIME_OUT);

		// trigger the network request
		client.get(url, new JsonHttpResponseHandler() {

			//			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				super.onSuccess(statusCode, headers, responseString);
			}

//			@Override
//			protected Object parseResponse(byte[] responseBody) throws JSONException {
//				return super.parseResponse(responseBody);
//			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
                                  Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				throwable.printStackTrace();

				ServerResponseListener listener = mMap.get(programName);
				if(listener != null) {
					target.processJsonObject(programName, errorResponse);
					removeTarget(programName, listener);
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
								  JSONArray response) {
				super.onSuccess(statusCode, headers, response);
				ServerResponseListener listener = mMap.get(programName);
				if(listener != null) {
					listener.processJsonObject(programName, response);
					removeTarget(programName, listener);
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
								  JSONObject response) {
				super.onSuccess(statusCode, headers, response);

				ServerResponseListener listener = mMap.get(programName);
				if(listener != null) {
					target.processJsonObject(programName, response);
					removeTarget(programName, listener);
				}
			}
		});
	}

	public void getHijriDate(String hijriDate, ServerResponseListener target) {
		sendRequest(hijriDate, "http://www.saba-igc.org/prayerTimes/salatDataService/salatDataService.php", target);
	}

	public void getPrayerTimeFromSaba(String prayTimesFromSaba, ServerResponseListener target){
		sendRequest(prayTimesFromSaba, "http://www.saba-igc.org/prayerTimes/salatDataService/salatDataService.php", target);
	}

	public void getPrayTimes(String timeZoneOffsetInMinutes, double latitude, double longitude, ServerResponseListener target) {
		StringBuilder sb = new StringBuilder("http://praytime.info/getprayertimes.php?school=0&gmt=");

		sb.append(timeZoneOffsetInMinutes); // appending timeZoneOffsetInMinutes.

		// setting location
		sb.append("&lat=");
		sb.append(latitude);
		sb.append("&lon=");
		sb.append(longitude);

		// setting today's date.
		sb.append("&m=");
		sb.append(Calendar.getInstance().get(Calendar.MONTH) + 1); // month is zero based.
		sb.append("&d=");
		sb.append(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		sb.append("&y=");
		sb.append(Calendar.getInstance().get(Calendar.YEAR));

		Log.d("PrayerTime URL: ", sb.toString());

		sendRequest("Prayer Times", sb.toString(), target);
	}

// Need to find a better place for following code.
	private void savePreferences(String key, String value) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.apply();
	}

	private String getSavedPreferences(String key) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		return sharedPreferences.getString(key, "");
	}

	public String getHijriDate(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		String englishDate = dateFormat.format(calendar.getTime());
		if( englishDate.compareTo(getEnglishDate())==0){
			return getSavedPreferences("hijriDate");
		}

		return "";
	}

	public void saveHijriDate(String hijriDate){
		saveEnglishDate();
		savePreferences("hijriDate", hijriDate);
	}

	public String getEnglishDate(){
		return getSavedPreferences("englishDate");
	}

	public void saveEnglishDate(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		String englishDate = dateFormat.format(calendar.getTime());
		savePreferences("englishDate", englishDate);
	}
}