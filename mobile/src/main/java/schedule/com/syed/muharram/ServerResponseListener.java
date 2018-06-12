package schedule.com.syed.muharram;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Syed Aftab Naqvi
 * @create June, 2018
 * @version 1.0
 */
public interface ServerResponseListener {
	public void processJsonObject(String programName, JSONObject response);
	public void processJsonObject(String programName, JSONArray response);
}
