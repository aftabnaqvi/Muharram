package schedule.com.syed.muharram;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Paint;

import java.util.List;

public class ScheduleArrayAdapter extends ArrayAdapter<ScheduleDataModel> {

    List<ScheduleDataModel> mScheduleList;
    Context mContext;
    static Activity mActivity;
    private LayoutInflater mInflater;

    // Constructors
    public ScheduleArrayAdapter(Context context, Activity activity, List<ScheduleDataModel> objects) {
        super(context, 0, objects);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        mScheduleList = objects;
        mActivity = activity;

    }

    @Override
    public ScheduleDataModel getItem(int position) {
        return mScheduleList.get(position);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder vh;
        if (convertView == null) {
            View view = mInflater.inflate(R.layout.layout_row_view, parent, false);
            vh = ViewHolder.create((RelativeLayout) view);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        ScheduleDataModel item = getItem(position);

        vh.tvName.setText(item.getName());
        vh.tvDate.setText(item.getDate());
        vh.tvDay.setText(item.getDay());
        vh.tvAddress.setText(item.getAddress());
        vh.tvTime.setText(item.getTime());
        vh.tvPhone.setText(item.getPhone());

        //vh.tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
        vh.tvAddress.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
            }
        });


        return vh.rootView;
    }


    private static class ViewHolder {
        public final RelativeLayout rootView;

        public final TextView tvName;
        public final TextView tvDate;
        public final TextView tvDay;
        public final TextView tvTime;
        public final TextView tvPhone;
        public final TextView tvAddress;

        private ViewHolder(RelativeLayout rootView,
                           TextView tvName,
                           TextView tvDate,
                           TextView tvDay,
                           TextView tvAddress,
                           TextView tvPhone,
                           TextView tvTime) {

            this.rootView = rootView;
            this.tvName = tvName;
            this.tvAddress = tvAddress;
            this.tvDate = tvDate;
            this.tvDay = tvDay;
            this.tvTime = tvTime;
            this.tvPhone = tvPhone;

            /*SpannableString spanStr = new SpannableString(this.tvAddress.getText());
            spanStr.setSpan(new UnderlineSpan(), 0, spanStr.length(), 0);
            this.tvAddress.setText(spanStr);*/

            /*SpannableString content = new SpannableString(this.tvAddress.getText());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            this.tvAddress.setText(content);*/

            this.tvAddress.setPaintFlags(this.tvAddress.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            //this.tvAddress.setAutoLinkMask(8);
        }

        public static ViewHolder create(RelativeLayout rootView) {
            TextView tvName     = (TextView) rootView.findViewById(R.id.tvName);
            TextView tvDate     = (TextView) rootView.findViewById(R.id.tvDate);
            TextView tvDay     = (TextView) rootView.findViewById(R.id.tvDay);
            TextView tvAddress  = (TextView) rootView.findViewById(R.id.tvAddress);
            TextView tvPhone    = (TextView) rootView.findViewById(R.id.tvPhone);
            TextView tvTime     = (TextView) rootView.findViewById(R.id.tvTime);


            return new ViewHolder(rootView, tvName, tvDate, tvDay, tvAddress, tvPhone, tvTime);
        }
    }
}