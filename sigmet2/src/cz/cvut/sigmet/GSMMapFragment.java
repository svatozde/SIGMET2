package cz.cvut.sigmet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.LocalActivityManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import cz.cvut.sigmet.dbUtils.SigmetDataListener;
import cz.cvut.sigmet.model.CellDTO;
import cz.cvut.sigmet.model.HandoverDTO;
import cz.cvut.sigmet.model.SignalDTO;
import cz.cvut.sigmet.model.WalkDTO;

public class GSMMapFragment extends MapFragment implements SigmetDataListener, ClusterManager.OnClusterItemClickListener<GSMLocationObject>,
		ClusterManager.OnClusterClickListener<GSMLocationObject> {

	private GSMLocationObject active_marker;

	private Set<CellDTO> current_markers = new HashSet<CellDTO>();
	
	private Set<Circle> current_signals = new HashSet<Circle>();

	
	private Set<CellDTO> all_markers = new HashSet<CellDTO>();
	
	private Set<CellDTO> walk_cells = new HashSet<CellDTO>();
	
	private Set<Circle> walk_signal = new HashSet<Circle>();
	
	private Set<Circle> walk_handovers = new HashSet<Circle>();

	private Set<Polyline> walk_handover_lines = new HashSet<Polyline>();
	 
	private Set<Circle> show_signal = new HashSet<Circle>();

	private Set<Circle> handovers_lines = new HashSet<Circle>();

	private Set<Polyline> handover_lines = new HashSet<Polyline>();

	private Set<CellDTO> handover_markers = new HashSet<CellDTO>();
	
	private CameraPosition cp;

	private int[] colors = { Color.rgb(255, 0, 0), Color.rgb(0, 225, 0) };

	private float[] startPoints = { 0.01f, 1f };

	private Gradient g = new Gradient(colors, startPoints);

	private TileOverlay heat_map;

	// Declare a variable for the cluster manager.
	private ClusterManager<GSMLocationObject> mClusterManager;

	private static final int CLUSTERING_DISTANCE = 40;

	private ToggleButton S;
	private ToggleButton H;
	private ToggleButton M;
	private ToggleButton A;

	public GSMMapFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FrameLayout root = (FrameLayout) super.onCreateView(inflater, container, savedInstanceState);
		inflater.inflate(R.layout.map_buttons, root);
		S = (ToggleButton) root.findViewById(R.id.show_signal);
		H = (ToggleButton) root.findViewById(R.id.show_handovers);
		M = (ToggleButton) root.findViewById(R.id.show_signal_heatmap);
		A = (ToggleButton) root.findViewById(R.id.show_all);

		S.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					drawSignal(active_marker);
				} else {
					removeNonCurrentSignal();
				}

			}

		});

		H.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					drawHandovers(active_marker);
				} else {
					removeHandovers();
				}
			}
		});

		M.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					drawSignalMap();
				} else {
					heat_map.setVisible(false);
					heat_map.clearTileCache();
					heat_map.remove();

				}

			}
		});

		A.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					drawAllCells();
				} else {
					removeNonCurentCells();
				}
			}
		});

		mClusterManager = new ClusterManager<GSMLocationObject>(MAPKA.ctx, getMap());

		getMap().setOnCameraChangeListener(mClusterManager);
		getMap().setOnMarkerClickListener(mClusterManager);

		mClusterManager.setOnClusterItemClickListener(this);

		mClusterManager.setAlgorithm(new PreCachingAlgorithmDecorator<GSMLocationObject>(new NonHierarchicalDistanceBasedAlgorithm<GSMLocationObject>()));

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getMap().setMyLocationEnabled(true);
		getMap().getUiSettings().setZoomControlsEnabled(true);
		getMap().setInfoWindowAdapter(new TooltiAdaper());
	}

	@Override
	public void onPause() {
		super.onPause();
		cp = getMap().getCameraPosition();	
	}

	@Override
	public void onResume() {
		super.onResume();
		if (cp != null) {
			getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cp));
		}
	}

	@Override
	public void onCellChange(CellDTO dto) {
		if (!current_markers.contains(dto)) {
			if (!GSMLocationObject.contains(dto)) {
				GSMLocationObject gO = GSMLocationObject.add(dto);
				if (gO != null) {
					mClusterManager.addItem(gO);
					mClusterManager.cluster();
					recalculateCameraPosision();
				}
			}
			current_markers.add(dto);
			all_markers.remove(dto);
		}
	}

	@Override
	public void onLocationCange(SignalDTO dto) {
		Circle c = drawSingleSignal(dto);
		current_signals.add(c);
	};

	private CircleOptions getCircleOptions(SignalDTO dto) {
		int color = getColorBaseOnSignal(dto.getValue());
		CircleOptions circleOptions = new CircleOptions().center(new LatLng(dto.getLatitude(), dto.getLongtitude())).radius(30).fillColor(color)
				.strokeColor(color); //
		return circleOptions;
	}

	private Circle drawSingleSignal(SignalDTO dto) {
		Circle circle = getMap().addCircle(getCircleOptions(dto));
		return circle;
	}
	
	private void drawHandover(HandoverDTO dto, Set<CellDTO> markers, Set<Circle> circles, Set<Polyline> lines) {
		LatLng handover = new LatLng(dto.getLatitude(), dto.getLongtitude());
		LatLng toCell = new LatLng(dto.getTo().getLatitude(), dto.getTo().getLongtitude());
		LatLng fromCell = new LatLng(dto.getFrom().getLatitude(), dto.getFrom().getLongtitude());

		CircleOptions circleOptions = new CircleOptions().center(handover).radius(10).fillColor(Color.WHITE).strokeColor(Color.BLACK);

		Circle circle = getMap().addCircle(circleOptions);

		circles.add(circle);

		GSMLocationObject fromMarker = GSMLocationObject.add(dto.getFrom());
		if (fromMarker != null) {
			mClusterManager.addItem(fromMarker);
			markers.add(dto.getFrom());
		}

		

		GSMLocationObject toMarker = GSMLocationObject.add(dto.getTo());
		if (toMarker != null) {
			mClusterManager.addItem(toMarker);
			markers.add(dto.getTo());
		}

		

		if (!fromCell.equals(toCell)) {
			PolylineOptions toLineOptions = new PolylineOptions().add(handover).add(toCell).color(Color.BLUE).width(2.0f);
			lines.add(getMap().addPolyline(toLineOptions));
			PolylineOptions fromLineOptions = new PolylineOptions().add(handover).add(fromCell).color(Color.RED).width(2.0f);
			lines.add(getMap().addPolyline(fromLineOptions));
		} else {
			PolylineOptions toLineOptions = new PolylineOptions().add(handover).add(toCell).color(Color.MAGENTA).width(2.0f);
			lines.add(getMap().addPolyline(toLineOptions));
		}

	}

	private static final int opacity = 128;
	private static final int MAX_NEGATIVE_DBM = 120;
	
	public int getColorBaseOnSignal(double i) {
		if (i > 0) {
			return Color.argb(opacity, 0, 255, 0);
		} else if (i > -30) {
			return Color.argb(opacity, 102, 255, 102);
		} else if (i > -60) {
			return Color.argb(opacity, 204, 255, 153);
		} else if (i > -75) {
			return Color.argb(opacity,255, 255, 0);
		} else if (i > -90) {
			return Color.argb(opacity, 255, 204, 0);
		} else if (i > -100) {
			return Color.argb(opacity, 255, 102, 0);
		} else {
			return Color.argb(opacity, 255, 0, 0);
		}

	}

	@SuppressLint("InflateParams")
	private class TooltiAdaper implements InfoWindowAdapter {

		@Override
		public View getInfoContents(Marker marker) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public View getInfoWindow(Marker marker) {
			if (active_marker == null) {
				return null;
			}

			CellDTO[] cells = (CellDTO[]) active_marker.getCells().toArray(new CellDTO[active_marker.getCells().size()]);
			StringBuilder sb;
			// Getting view from the layout file info_window_layout
			LayoutInflater inflater = LayoutInflater.from(getActivity());

			View v = inflater.inflate(R.layout.tooltip_layout, null);
			TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);

			tvLat.setText("Latitude: " + active_marker.getPosition().latitude);

			TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);
			tvLng.setText("Longitude: " + active_marker.getPosition().longitude);

			TextView tvDesc = (TextView) v.findViewById(R.id.tv_desc);
			tvDesc.setText(cells[0].getAddres());

			// /////// LAC /////////
			TextView tvLac = (TextView) v.findViewById(R.id.tv_lac);
			tvLac.setText("LAC: " + cells[0].getLac());

			// /// cid //////
			sb = new StringBuilder();
			for (CellDTO c : active_marker.getCells()) {
				sb.append(c.getCId()).append(" | ");
			}
			// remove last delimiter
			String s = sb.toString();
			s = s.substring(0, s.length() - 3);

			TextView tvCid = (TextView) v.findViewById(R.id.tv_cid);
			tvCid.setText("CID: " + s);

			// Returning the view containing InfoWindow contents
			return v;
		}

	}

	private void drawAllCells() {
		AsyncTask<Void, Void, List<CellDTO>> getAll = new AsyncTask<Void, Void, List<CellDTO>>() {
			@Override
			protected List<CellDTO> doInBackground(Void... params) {
				try {
					return MAPKA.dataManager.getAllCells();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return Collections.emptyList();
			}

			@Override
			protected void onPostExecute(List<CellDTO> result) {
				result.removeAll(current_markers);
				result.removeAll(walk_cells);
				all_markers.addAll(result);
				Collection<GSMLocationObject> newLycCreated = GSMLocationObject.addAll(result);
				mClusterManager.addItems(newLycCreated);
				mClusterManager.cluster();

			}
		};
		getAll.execute(null, null);
	}
	
	
	public void drawWalk(final WalkDTO walk){
		
		getMapAsync(new OnMapReadyCallback() {
			
			@Override
			public void onMapReady(GoogleMap googleMap) {
				removeNonCurentCells();
				removeNonCurrentSignal();
				removeHandovers();
				
				for(HandoverDTO h : walk.getHandovers()){
					drawHandover(h, walk_cells, walk_handovers, walk_handover_lines);
				}
				
				for(SignalDTO s : walk.getSignals()){
					walk_signal.add(drawSingleSignal(s));
				}
				
				recalculateCameraPosision();
				
			}
		});
		
	}
	
	private void recalculateCameraPosision(){
		if(mClusterManager.getMarkerCollection().getMarkers().isEmpty()){
			 return;
		 }
		LatLngBounds.Builder builder = new LatLngBounds.Builder();		 
		for (Marker marker : mClusterManager.getMarkerCollection().getMarkers()) {
		    builder.include(marker.getPosition());
		}
		LatLngBounds bounds = builder.build();
		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
		getMap().animateCamera(cu);
	}

	private void removeNonCurentCells() {
		active_marker = null;		
		for(GSMLocationObject item : GSMLocationObject.removeAll(all_markers)){
			mClusterManager.removeItem(item);
		};
		mClusterManager.cluster();
	}

	private void drawSignalMap() {
		DrawAllSignal action = new DrawAllSignal();
		action.execute((Void) null);
	}

	private void drawSignal(GSMLocationObject cell) {
		if (cell == null) {
			return;
		}
		DrawSingleCellSignal signalsTask = new DrawSingleCellSignal();
		signalsTask.execute(cell.getPosition());
	}

	private void removeNonCurrentSignal() {
		removeCircle(show_signal);
	}

	private void removeHandovers() {
		removeCircle(handovers_lines);
		removeLine(handover_lines);
		if (all_markers.isEmpty()) {
			handover_markers.removeAll(current_markers);
			GSMLocationObject.removeAll(handover_markers);
			mClusterManager.clearItems();
			mClusterManager.addItems(GSMLocationObject.getAll());
			mClusterManager.cluster();
		}

	}

	private void removeCircle(Collection<Circle> in) {
		for (Circle c : in) {
			c.setVisible(false);
			c.remove();
		}
		in.clear();
	}

	private void removeLine(Collection<Polyline> in) {
		for (Polyline p : in) {
			p.setVisible(false);
			p.remove();
		}
	}

	private class DrawSingleCellSignal extends AsyncTask<LatLng, Void, List<CircleOptions>> {

		@Override
		protected List<CircleOptions> doInBackground(LatLng... params) {
			try {
				List<SignalDTO> ss = MAPKA.dataManager.getSignalMeasures(params[0]);
				List<SignalAVG> clustered = new ArrayList<SignalAVG>();
				// TODO USE QUAD TREE OR OTHER CLEVER STRUCTURE IN SIGNAL DATA
				// MANAGER VIT COMBINATION OF CURSOR
				while (!ss.isEmpty()) {
					SignalDTO curr = ss.remove(0);
					boolean add = true;
					for (SignalAVG sAvg : clustered) {
						float[] result = new float[1];
						Location.distanceBetween(curr.getLatitude(), curr.getLongtitude(), sAvg.getLatitude(), sAvg.getLongtitude(), result);
						if (MAPKA.MIN_DISTANCE > result[0]) {
							sAvg.addValue(curr.getValue());
							add = false;
						}
					}
					if (add) {
						SignalAVG newAvg = new SignalAVG(curr.getLatitude(), curr.getLongtitude());
						newAvg.addValue(curr.getValue());
						clustered.add(newAvg);
					}
				}

				List<CircleOptions> circles = new ArrayList<CircleOptions>();
				for (SignalAVG s : clustered) {
					LatLng l = new LatLng(s.latitude, s.longtitude);
					int color = getColorBaseOnSignal(s.avg());
					CircleOptions circleOptions = new CircleOptions().center(l).radius(30).fillColor(color).strokeColor(color);
					circles.add(circleOptions);
				}
				return circles;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return Collections.emptyList();
			// DefaultClusterRenderer a;
		}

		@Override
		protected void onPostExecute(List<CircleOptions> result) {
			for (CircleOptions c : result) {
				Circle circle = getMap().addCircle(c);
				show_signal.add(circle);
			}
		}

	};

	private class DrawAllSignal extends AsyncTask<Void, Void, List<WeightedLatLng>> {
		@Override
		protected List<WeightedLatLng> doInBackground(Void... params) {
			try {

				// this will be long loooooooooooong
				List<SignalDTO> ss = MAPKA.dataManager.getAllSignal();
				List<WeightedLatLng> data = new LinkedList<WeightedLatLng>();
				List<SignalAVG> clustered = new ArrayList<SignalAVG>();
				while (!ss.isEmpty()) {
					SignalDTO curr = ss.remove(0);
					boolean add = true;
					for (SignalAVG sAvg : clustered) {
						float[] result = new float[1];
						Location.distanceBetween(curr.getLatitude(), curr.getLongtitude(), sAvg.getLatitude(), sAvg.getLongtitude(), result);
						if (CLUSTERING_DISTANCE > result[0]) {
							sAvg.addValue(curr.getValue());
							add = false;
						}
					}
					if (add) {
						SignalAVG newAvg = new SignalAVG(curr.getLatitude(), curr.getLongtitude());
						newAvg.addValue(curr.getValue());
						clustered.add(newAvg);
					}
				}

				for (SignalAVG s : clustered) {
					LatLng ll = new LatLng(s.getLatitude(), s.getLongtitude());
					data.add(new WeightedLatLng(ll, s.avg() + MAX_NEGATIVE_DBM));
				}

				return data;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return Collections.emptyList();
		}

		@Override
		protected void onPostExecute(List<WeightedLatLng> result) {
			if (result.isEmpty()) {
				return;
			}
			HeatmapTileProvider provider = new HeatmapTileProvider.Builder().weightedData(result).gradient(g).radius(50).build();
			TileOverlayOptions tO = new TileOverlayOptions().tileProvider(provider);
			heat_map = getMap().addTileOverlay(tO);
		}

	}

	public void drawHandovers(GSMLocationObject dto) {
		if (dto == null) {
			return;
		}
		DrawHandovers in = new DrawHandovers();
		in.execute(dto.getPosition());

	}

	private class DrawHandovers extends AsyncTask<LatLng, Void, List<HandoverDTO>> {

		public DrawHandovers() {

		}

		@Override
		protected List<HandoverDTO> doInBackground(LatLng... params) {
			try {
				return MAPKA.dataManager.getHandovers(params[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return Collections.emptyList();
		}

		@Override
		protected void onPostExecute(List<HandoverDTO> result) {
			for (HandoverDTO h : result) {
				drawHandover(h,handover_markers,handovers_lines,handover_lines);
			}
		}

	}

	/**
	 * Helper class for averaging the signal values
	 * 
	 * @author Zdenek
	 * 
	 */
	private class SignalAVG {
		double latitude;
		double longtitude;
		List<Double> value = new ArrayList<Double>();

		public SignalAVG(double latitude, double longtitude) {
			this.latitude = latitude;
			this.longtitude = longtitude;
		}

		public double getLatitude() {
			return latitude;
		}

		public double getLongtitude() {
			return longtitude;
		}

		public void addValue(double value) {
			this.value.add(value);
		}

		public double avg() {
			double sum = 0;
			for (Double d : value) {
				sum += d;
			}
			return sum / value.size();
		}

	}

	@Override
	public boolean onClusterItemClick(GSMLocationObject item) {
		boolean consume = false;
		if (S.isChecked()) {
			if (!item.equals(active_marker)) {
				removeNonCurrentSignal();
				drawSignal(item);
			}
			consume = true;
		}

		if (H.isChecked()) {
			if (!item.equals(active_marker)) {
				removeHandovers();
				drawHandovers(item);
			}
			consume = true;
		}
		active_marker = item;
		return consume;
	}

	@Override
	public boolean onClusterClick(Cluster<GSMLocationObject> cluster) {
		return true;
	}

	public void startWalk() {
		GSMLocationObject.clear();
		removeCircle(current_signals);
		removeCircle(show_signal);
		onCellChange(MAPKA.dataManager.getActual());
		removeHandovers();

	}

}
