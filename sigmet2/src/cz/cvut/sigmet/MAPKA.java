package cz.cvut.sigmet;

import java.sql.SQLException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import android.text.InputType;

import com.google.maps.android.projection.SphericalMercatorProjection;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

import cz.cvut.sigmet.dbUtils.SigmetDataManager;
import cz.cvut.sigmet.dbUtils.SigmetSqlHelper;

public class MAPKA extends OrmLiteBaseActivity<SigmetSqlHelper> implements NavigationDrawerFragment.NavigationDrawerCallbacks {
	
	private NavigationDrawerFragment mNavigationDrawerFragment;

	private CharSequence mTitle;

	public static Context ctx;

	public static SigmetDataManager dataManager;

	public static GSMMapFragment map_fragment;

	public static GSMGRaphFragment graph_fragment;

	public static GSMCellListFragment list_fragment;

	public static GSMWalksListFragment walk_fragment;

	private static int current_position = 0;

	private static final int MIN_TIME = 2500;

	public static final int MIN_DISTANCE = 15;

	private boolean isWalkStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this has to be set up before content view
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		ctx = getApplicationContext();
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		initManager();

		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
		
		if (!isNetworkAvailable()) {
			Toast.makeText(ctx, R.string.connection_off, 2);
		}
		
		if(!isGpsOn()){
			Toast.makeText(ctx, R.string.gps_off, 2);
		}
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public boolean isGpsOn() {
		LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	private void initManager() {
		if (dataManager == null) {
			try {
				dataManager = new SigmetDataManager(getHelper());
			} catch (SQLException e) {
				Log.e("MAPKA ERROR", e.getMessage(), e);
			}
			if (map_fragment == null) {
				map_fragment = new GSMMapFragment();
			}
			// this has to be set up before content view
			if (graph_fragment == null) {
				graph_fragment = new GSMGRaphFragment();

			}
			if (list_fragment == null) {
				list_fragment = new GSMCellListFragment();
			}
			if (walk_fragment == null) {
				walk_fragment = new GSMWalksListFragment();
			}

		}

		dataManager.addDataListener(graph_fragment);
		dataManager.addDataListener(map_fragment);
		dataManager.addDataListener(list_fragment);

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, dataManager);
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		tm.listen(dataManager, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_LOCATION);
		tm.listen(graph_fragment.listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		// initial call
		dataManager.onCellLocationChanged(tm.getCellLocation());
		String provider = locationManager.getBestProvider(new Criteria(), true);
		if(isGpsOn()){
			dataManager.setActual_location(locationManager.getLastKnownLocation(provider));
		}

	}
	
	@Override
	protected void onStop() {
		dataManager.stopWalk();
		super.onStop();
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		current_position = position;
		switch (position) {
		case 0:
			fragmentManager.beginTransaction().replace(R.id.container, map_fragment).commit();
			break;
		case 1:
			fragmentManager.beginTransaction().replace(R.id.container, graph_fragment).commit();
			break;
		case 2:
			fragmentManager.beginTransaction().replace(R.id.container, list_fragment).commit();
			break;
		case 3:
			fragmentManager.beginTransaction().replace(R.id.container, walk_fragment).commit();
			break;
		case 4:
			fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(4)).commit();
			break;
		}

	}
	
	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setTitle("");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (selectMenu(menu)) {
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	private boolean selectMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			switch (current_position) {
			case 0:
				getMenuInflater().inflate(R.menu.map_menu, menu);
				setupMapMenu(menu);
				break;
			case 1:
				getMenuInflater().inflate(R.menu.empty_menu, menu);
				break;
			case 2:
				getMenuInflater().inflate(R.menu.empty_menu, menu);
				break;
			case 3:
				getMenuInflater().inflate(R.menu.empty_menu, menu);
				break;
			}

			restoreActionBar();
			return true;
		}
		return false;
	}

	private void setupMapMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.start_walk);
		if (isWalkStarted) {
			item.setChecked(true);
			item.setTitle(R.string.stop_walk);
		} else {
			item.setChecked(false);
			item.setTitle(R.string.start_walk);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.start_walk:
			if (item.isChecked()) {
				item.setChecked(false);
				item.setTitle(R.string.start_walk);
				stopWalk();
			} else {
				item.setChecked(true);
				item.setTitle(R.string.stop_walk);
				startWalk(item);
			}
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}

	}

	private void startWalk(final MenuItem item) {

		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(input);
		builder.setPositiveButton("START", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					String name = input.getText().toString();
					isWalkStarted = true;
					dataManager.startWalk(name);
					map_fragment.startWalk();
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				item.setChecked(false);
				item.setTitle(R.string.start_walk);
			}
		});

		builder.setCancelable(true);

		AlertDialog dialog = builder.create();
		dialog.show();

	}

	private void stopWalk() {
		MAPKA.dataManager.stopWalk();
		isWalkStarted = false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}

	}

	public static int getCurrent_position() {
		return current_position;
	}

	public static void setCurrent_position(int current_position) {
		MAPKA.current_position = current_position;
	}

}
