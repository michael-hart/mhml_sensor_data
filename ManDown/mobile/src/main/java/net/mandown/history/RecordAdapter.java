package net.mandown.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.mandown.R;

import java.util.ArrayList;

/**
 * Adapter to easily list new intoxication records on the History screen
 */
public class RecordAdapter extends ArrayAdapter<IntoxicationRecord> {

    public RecordAdapter(Context context, ArrayList<IntoxicationRecord> records) {
        super(context, 0, records);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        IntoxicationRecord record = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_intoxication_record, parent, false);
        }

        // Lookup view for data population
        TextView tvDate = (TextView) convertView.findViewById(R.id.tvDate);
        TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);
        TextView tvLevel = (TextView) convertView.findViewById(R.id.tvLevel);

        // Populate the data into the template view using the data object
        tvDate.setText(record.date);
        tvTime.setText(record.time);
        tvLevel.setText(record.level);

        // Return the completed view to render on screen
        return convertView;
    }
}
