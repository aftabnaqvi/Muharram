package schedule.com.syed.muharram;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Syed Aftab Naqvi
 * @create December, 2014
 * @version 1.0
 */
public class MuharramApplication extends Application {
	private static Context mContext;
	private String TAG = MuharramApplication.class.toString();
	/**
	 * The Analytics singleton. The field is set in onCreate method override when the application
	 * class is initially created.
	 */
	private static GoogleAnalytics mAnalytics;

	/**
	 * The default app tracker. The field is from onCreate callback when the application is
	 * initially created.
	 */
	private static Tracker mTracker;

	/**
	 * Access to the global Analytics singleton. If this method returns null you forgot to either
	 * set android:name="&lt;this.class.name&gt;" attribute on your application element in
	 * AndroidManifest.xml or you are not setting this.analytics field in onCreate method override.
	 */
	public static GoogleAnalytics analytics() {
		return mAnalytics;
	}

	/**
	 * The default app tracker. If this method returns null you forgot to either set
	 * android:name="&lt;this.class.name&gt;" attribute on your application element in
	 * AndroidManifest.xml or you are not setting this.tracker field in onCreate method override.
	 */
	public static Tracker tracker() {
		return mTracker;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		MuharramApplication.mContext = this;

		getSabaClient().getHijriDate("hijriDate", new ServerResponseListener() {
			@Override
			public void processJsonObject(String programName, JSONObject response) {
				if(response == null){
					Log.d(TAG, "json object is null for :" + programName);
					return;
				}

				if(programName.compareToIgnoreCase("hijriDate") == 0){
					try{
						String hijriDate = response.getString("hijridate");
						if(hijriDate != null){
							Log.d(TAG, "HijriDate: " + hijriDate);
							getSabaClient().saveHijriDate(hijriDate);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void processJsonObject(String programName, JSONArray response) {

			}
		});
		initializeGoogleAnalytics();
	}

	@Override
    public void onTerminate () {
        super.onTerminate ();
    }
	
	public static MuharramClient getSabaClient() {
		return MuharramClient.getInstance(MuharramApplication.mContext);
	}

	private void initializeGoogleAnalytics(){
		//mAnalytics = GoogleAnalytics.getInstance(this);
		//mAnalytics.setLocalDispatchPeriod(120);

		//https://www.google.com/analytics/web/
		//mTracker = mAnalytics.newTracker("UA-65121409-6");
	}

	// this function sends tracking events for Analytics.
	public static void sendAnalyticsEvent(String screenName, String eventCategory, String eventAction, String eventLabel){
		//mTracker.setScreenName(screenName);
		//mTracker.send(new HitBuilders.EventBuilder()
		//		.setCategory(eventCategory)
		//		.setAction(eventAction)
		//		.setLabel(eventLabel)
		//		.build());
	}

	public static void sendAnalyticsScreenName(String screenName){
		// Set screen name.
		//mTracker.setScreenName(screenName);
		// Send a screen view.
		//mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	}
}