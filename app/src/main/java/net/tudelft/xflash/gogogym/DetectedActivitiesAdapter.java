package net.tudelft.xflash.gogogym;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.tudelft.xflash.gogogym.UserLog;

import java.util.ArrayList;
import java.util.List;

//custom ArrayAdapter
class detectedActivitiesAdapter extends ArrayAdapter<UserLog> {

    private Context context;
    private List<UserLog> userLogs;

    //constructor, call on creation
    public detectedActivitiesAdapter(Context context, int resource, ArrayList<UserLog> objects) {
        super(context, resource, objects);

        this.context = context;
        this.userLogs = objects;
    }

    //called when rendering the list
    public View getView(int position, View convertView, ViewGroup parent) {

        //get the property we are displaying
        UserLog ulog = userLogs.get(position);

        //get the inflater and inflate the XML layout for each item
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.property_layout, null);

        TextView activity_name = (TextView) view.findViewById(R.id.activity);
        TextView start_time = (TextView) view.findViewById(R.id.startTime);
        TextView end_time = (TextView) view.findViewById(R.id.endTime);
        ImageView image = (ImageView) view.findViewById(R.id.act_image);

        //set activity, start end time attributes
        activity_name.setText("$" + String.valueOf(ulog.log_desc));
        start_time.setText("Start: " + String.valueOf(ulog.start_time));
        end_time.setText("End: " + String.valueOf(ulog.finish_time));

        //get the image associated with this activity
        int imageID = context.getResources().getIdentifier(ulog.getImage(), "drawable", context.getPackageName());
        image.setImageResource(imageID);

        return view;
    }
}
