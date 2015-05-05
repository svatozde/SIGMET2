package cz.cvut.sigmet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import cz.cvut.sigmet.dbUtils.SigmetLogAppender;
import cz.cvut.sigmet.model.CellDTO;
import cz.cvut.sigmet.model.LogDTO;
import cz.cvut.sigmet.model.LogDTO.Level;
import android.annotation.SuppressLint;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class LogFragment extends ListFragment implements SigmetLogAppender {

	private static List<LogDTO> logs = new ArrayList<LogDTO>();

	private static final int ERROR_COLOR = 0xFFFAAAAA;

	private static final int WARN_COLOR = 0xFFF3FAAA;

	private static final int INFO_COLOR = 0xFFAAFAD6;
	
	private static final SimpleDateFormat DATE_FORMAT =  new SimpleDateFormat("HH:mm:ss");

	private LogArrayAdapter cellAdapter = new LogArrayAdapter(SigmetActivity.ctx, R.layout.log_list_item, new ArrayList<LogDTO>());

	public LogFragment() {
		setListAdapter(cellAdapter);
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_log_list, container, false);
		return view;
	}

	@Override
	public void append(LogDTO log) {
		((LogArrayAdapter)getListAdapter()).add(log);
	}

	private class LogArrayAdapter extends ArrayAdapter<LogDTO> {

		private static final String FIRST_ROW_TEMPLATE = " [%s][%s]: %s";

		public LogArrayAdapter(Context ctx, int textViewResourceId, List<LogDTO> objects) {
			super(ctx, textViewResourceId, objects);

		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LogDTO log = getItem(position);

			View rowView = inflater.inflate(R.layout.log_list_item, parent, false);

			TextView first = (TextView) rowView.findViewById(R.id.firstLine);
			
			String level = null;
			switch (log.getLevel()) {
			case ERROR:
				first.setBackgroundColor(ERROR_COLOR);
				level = "E";
				break;
			case WARN:
				first.setBackgroundColor(WARN_COLOR);
				level = "W";
				break;
			case INFO:
				first.setBackgroundColor(INFO_COLOR);
				level = "I";
				break;
			}
			Date d = new Date(log.getTimestamp());
			first.setText(String.format(FIRST_ROW_TEMPLATE, log.getLevel(), DATE_FORMAT.format(d), log.getMessage()));

			return rowView;
		}

	}

}
