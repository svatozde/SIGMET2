package cz.cvut.sigmet;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Spinner;
import android.widget.TextView;

import cz.cvut.sigmet.dbUtils.SigmetDataListener;
import cz.cvut.sigmet.model.CellDTO;
import cz.cvut.sigmet.model.SignalDTO;
import cz.cvut.sigmet.model.WalkDTO;

public class GSMWalksListFragment extends ListFragment {

	private static List<WalkDTO> cells = new ArrayList<WalkDTO>();

	private EditText etSearch;

	private CellArrayAdapter cellAdapter = new CellArrayAdapter(MAPKA.ctx, R.layout.walk_list_item, new ArrayList<WalkDTO>());

	public GSMWalksListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new GetAllCells().execute((Void) null);
		setListAdapter(cellAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_walk_list, container, false);
		etSearch = (EditText) view.findViewById(R.id.walkSearch);

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
		
		
		return view;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	private class GetAllCells extends AsyncTask<Void, Void, List<WalkDTO>> {

		@Override
		protected List<WalkDTO> doInBackground(Void... params) {
			try {
				return MAPKA.dataManager.getAllWalks();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return Collections.<WalkDTO> emptyList();
		}

		@Override
		protected void onPostExecute(List<WalkDTO> result) {
			cells = result;
			cellAdapter.addAll(result);
		}
	}

	private class CellArrayAdapter extends ArrayAdapter<WalkDTO> {

		private static final String FIRST_ROW_TEMPLATE = "NAME: %s";

		private static final String SECOND_ROW_TEMPLATE = "START: %-15s STOP: %s";

		private final SimpleDateFormat sdf = new SimpleDateFormat("dd.M.yyyy hh:mm", Locale.US);

		public CellArrayAdapter(Context ctx, int textViewResourceId, List<WalkDTO> objects) {
			super(ctx, textViewResourceId, objects);

		}
		

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final WalkDTO walk = getItem(position);

			final View rowView = inflater.inflate(R.layout.walk_list_item, parent, false);

			TextView first = (TextView) rowView.findViewById(R.id.firstLine);
			first.setText(String.format(FIRST_ROW_TEMPLATE, walk.getName()));

			TextView second = (TextView) rowView.findViewById(R.id.secondLine);
			String start = sdf.format(new Date(walk.getStart()));
			String stop = sdf.format(new Date(walk.getStop()));
			second.setText(String.format(SECOND_ROW_TEMPLATE, start, stop));
			
			
						
			rowView.setOnLongClickListener(new OnLongClickListener() {
	
				@Override
				public boolean onLongClick(View v) {
					PopupMenu pop = new PopupMenu(MAPKA.ctx, rowView);
					pop.getMenuInflater().inflate(R.menu.walk_item_menu, pop.getMenu());  
					pop.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							switch (item.getItemId()) {
							case R.id.walk_show:
								FragmentManager f = getFragmentManager();
								MAPKA.setCurrent_position(0);
								f.beginTransaction().replace(R.id.container, MAPKA.map_fragment).commit();
								MAPKA.map_fragment.drawWalk(walk);
								break;
							case R.id.walk_delete:
								delete(walk);
								break;
							default:
								break;
							}
							return false;
						}
					});
					pop.show();					
					return false;
				}
			});
		
			
			
			return rowView;
		}
		
		protected void delete(final WalkDTO walk){
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	cellAdapter.remove(walk);
			        	MAPKA.dataManager.deleteWalk(walk);
			            break;
			        }
			    }
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(MAPKA.ctx);
			builder.setMessage("Do you want to delete " + walk.getName()).setPositiveButton("Yes", dialogClickListener)
			    .setNegativeButton("No", dialogClickListener).setCancelable(true).show();
		}

		@Override
		public Filter getFilter() {
			// TODO Auto-generated method stub
			return new ItemFilter();
		}

		private class ItemFilter extends Filter {
			@SuppressLint("DefaultLocale") @Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();

				if (constraint == null || constraint.length() == 0) {
					results.values = cells;
					results.count = cells.size();
					return results;
				}

				String filterString = constraint.toString().toLowerCase();

				final ArrayList<WalkDTO> nlist = new ArrayList<WalkDTO>();

				for (WalkDTO walk : cells) {
					if (walk.getName().toLowerCase().contains(filterString)) {
						nlist.add(walk);
						continue;
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
				addAll((Collection<? extends WalkDTO>) results.values);
				notifyDataSetChanged();
			}

		}

	}

}
