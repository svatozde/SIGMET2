package cz.cvut.sigmet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Spinner;
import android.widget.TextView;
import cz.cvut.sigmet.dbUtils.SigmetDataListener;
import cz.cvut.sigmet.model.CellDTO;
import cz.cvut.sigmet.model.SignalDTO;

public class GSMCellListFragment extends ListFragment implements SigmetDataListener {

	private static List<CellDTO> cells = new ArrayList<CellDTO>();

	private EditText etSearch;

	private Spinner searchSpinner;

	private CellArrayAdapter cellAdapter = new CellArrayAdapter(MAPKA.ctx, R.layout.cell_list_item, new ArrayList<CellDTO>());

	private int search_type = 0;

	public GSMCellListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new GetAllCells().execute((Void) null);
		setListAdapter(cellAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_cell_list, container, false);
		etSearch = (EditText) view.findViewById(R.id.cellSearch);

		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				cellAdapter.getFilter().filter(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		searchSpinner = (Spinner) view.findViewById(R.id.search_spinner);

		searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				search_type = position;
				
				switch (search_type){
				case 0:
					etSearch.setInputType(InputType.TYPE_CLASS_TEXT);
					break;
				case 1:
					etSearch.setInputType(InputType.TYPE_CLASS_NUMBER);
					break;
				case 2:
					etSearch.setInputType(InputType.TYPE_CLASS_NUMBER);
					break;
				case 3:
					etSearch.setInputType(InputType.TYPE_CLASS_TEXT);
					break;
				}
				
				
				cellAdapter.getFilter().filter(etSearch.getText());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		return view;
	}

	@Override
	public void onCellChange(CellDTO dto) {
		if (getListAdapter() != null) {
			((CellArrayAdapter) getListAdapter()).add(dto);
			cells.add(dto);
		}

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
			cells = result;
			cellAdapter.addAll(result);
		}
	}

	private class CellArrayAdapter extends ArrayAdapter<CellDTO> {

		private static final String FIRST_ROW_TEMPLATE = "CID: %-30s LAC: %s";

		private static final String SECOND_ROW_TEMPLATE = "ADDED: %s";

		private static final String THIRD_ROW_TEMPLATE = "ADDRESS: %s";

		public CellArrayAdapter(Context ctx, int textViewResourceId, List<CellDTO> objects) {
			super(ctx, textViewResourceId, objects);

		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			CellDTO cell = getItem(position);

			View rowView = inflater.inflate(R.layout.cell_list_item, parent, false);

			TextView first = (TextView) rowView.findViewById(R.id.firstLine);
			first.setText(String.format(FIRST_ROW_TEMPLATE, cell.getCId(), cell.getLac()));

			TextView second = (TextView) rowView.findViewById(R.id.secondLine);
			second.setText(String.format(SECOND_ROW_TEMPLATE, cell.getDate()));

			TextView third = (TextView) rowView.findViewById(R.id.thirdline);
			third.setText(String.format(THIRD_ROW_TEMPLATE, cell.getAddres()));

			return rowView;
		}

		@Override
		public Filter getFilter() {
			// TODO Auto-generated method stub
			return new ItemFilter();
		}

		private class ItemFilter extends Filter {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();

				if (constraint == null || constraint.length() == 0) {
					results.values = cells;
					results.count = cells.size();
					return results;
				}

				String filterString = constraint.toString().toLowerCase();

				final ArrayList<CellDTO> nlist = new ArrayList<CellDTO>();

				for (CellDTO cell : cells) {
					if (search_type == 0 || search_type == 1) {
						if (cell.getCId().contains(filterString)) {
							nlist.add(cell);
							continue;
						}
					}
					
					if (search_type == 0 || search_type == 2) {
						if (cell.getLac().contains(filterString)) {
							nlist.add(cell);
							continue;
						}
					}

					if (search_type == 0 || search_type == 3) {
						if (cell.getAddres().toLowerCase().contains(filterString)) {
							nlist.add(cell);
							continue;
						}
					}
					

				}

				results.values = nlist;
				results.count = nlist.size();

				return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				clear();
				addAll((Collection<? extends CellDTO>) results.values);
				notifyDataSetChanged();
			}

		}

	}
	
	
	////////////////// UNUSED METHODS //////////////////////////
	
	@Override
	public void onLocationCange(SignalDTO dto) {
		// TODO Auto-generated method stub

	}


}
