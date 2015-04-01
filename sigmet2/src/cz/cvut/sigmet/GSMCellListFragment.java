package cz.cvut.sigmet;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cz.cvut.sigmet.dbUtils.SigmetDataListener;
import cz.cvut.sigmet.model.CellDTO;
import cz.cvut.sigmet.model.SignalDTO;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GSMCellListFragment extends ListFragment implements SigmetDataListener{
	
	
	public GSMCellListFragment()  {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new GetAllCells().execute((Void)null);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		  View view = inflater.inflate(R.layout.fragment_cell_list, container);
          return view;
	}
	
	@Override
	public void onCellChange(CellDTO dto) {
		if(getListAdapter() != null){
			((CellArrayAdapter)getListAdapter()).add(dto);
		}
	
	}

	@Override
	public void onLocationCange(SignalDTO dto) {
		// TODO Auto-generated method stub
		
	}

	private class GetAllCells extends AsyncTask<Void, Void, List<CellDTO>> {

		@Override
		protected List<CellDTO> doInBackground(Void... params) {
			try {
				return MAPKA.dataManager.getAllCells();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return Collections.<CellDTO> emptyList();
		}

		@Override
		protected void onPostExecute(List<CellDTO> result) {
			setListAdapter( new CellArrayAdapter(MAPKA.ctx ,R.layout.cell_list_item, result));
		}
	}

	private class CellArrayAdapter extends ArrayAdapter<CellDTO> {

		private static final String FIRST_ROW_TEMPLATE = "CID: %s/tLAC: %s";
		
		private static final String SECOND_ROW_TEMPLATE = "ADDED: %s";
		
		private static final String THIRD_ROW_TEMPLATE = "ADDRESS: %s";
		
		public CellArrayAdapter(Context ctx, int textViewResourceId, List<CellDTO> objects) {
			super(ctx, textViewResourceId, objects);

		}
		
		@SuppressLint("ViewHolder") @Override
		public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				CellDTO cell = getItem(position);
				
				View rowView = inflater.inflate(R.layout.cell_list_item, parent, false);
			    
			    TextView first = (TextView) rowView.findViewById(R.id.firstLine);
			    first.setText(String.format(FIRST_ROW_TEMPLATE,cell.getCId(),cell.getLac()));
			    
			    TextView second = (TextView) rowView.findViewById(R.id.secondLine);
			    second.setText(String.format(SECOND_ROW_TEMPLATE,cell.getDate()));
			    
			    TextView third = (TextView) rowView.findViewById(R.id.thirdline);
			    third.setText(String.format(THIRD_ROW_TEMPLATE,cell.getAddres()));
			    
			   
			    return rowView;
		}

	}

}
