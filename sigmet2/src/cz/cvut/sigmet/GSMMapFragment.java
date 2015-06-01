package cz.cvut.sigmet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import cz.cvut.sigmet.dbUtils.SigmetDataListener;
import cz.cvut.sigmet.dbUtils.SigmetLogger;
import cz.cvut.sigmet.model.CellDTO;
import cz.cvut.sigmet.model.HandoverDTO;
import cz.cvut.sigmet.model.LocationDTO;
import cz.cvut.sigmet.model.SignalDTO;
import cz.cvut.sigmet.model.WalkDTO;

public class GSMMapFragment extends MapFragment implements SigmetDataListener, ClusterManager.OnClusterItemClickListener<GSMLocationObject>,
		ClusterManager.OnClusterClickListener<GSMLocationObject>, GoogleMap.OnMyLocationChangeListener {

	private GSMLocationObject active_marker;

	private Set<CellDTO> current_markers = new HashSet<CellDTO>();

	private Map<SignalDTO, Circle> current_signals = new HashMap<SignalDTO, Circle>();

	private Set<CellDTO> all_markers = new HashSet<CellDTO>();

	private Set<CellDTO> walk_cells = new HashSet<CellDTO>();

	private Set<Circle> walk_signal = new HashSet<Circle>();

	private Set<Circle> walk_handovers = new HashSet<Circle>();

	private Set<Polyline> walk_handover_lines = new HashSet<Polyline>();

	private Set<Polygon> show_signal = new HashSet<Polygon>();

	private Set<Circle> handovers_lines = new HashSet<Circle>();

	private Set<Polyline> handover_lines = new HashSet<Polyline>();

	private Set<CellDTO> handover_markers = new HashSet<CellDTO>();

	private CameraPosition cp;

	private int[] colors = { Color.rgb(255, 0, 0), Color.rgb(0, 255, 0) };

	private float[] startPoints = { 0.01f, 1f };

	private Gradient g = new Gradient(colors, startPoints);

	private TileOverlay heat_map;



	// Declare a variable for the cluster manager.
	private ClusterManager<GSMLocationObject> mClusterManager;

	private static final int SINGLE_CELL_GEOHASH_PRECIZION = 32;

	private Location currentLoc;

	private ToggleButton S;
	private ToggleButton H;
	private ToggleButton M;
	private ToggleButton A;

	public GSMMapFragment() {
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

		mClusterManager = new ClusterManager<GSMLocationObject>(SigmetActivity.ctx, getMap());
		mClusterManager.setOnClusterItemClickListener(this);
		mClusterManager.setAlgorithm(new PreCachingAlgorithmDecorator<GSMLocationObject>(new NonHierarchicalDistanceBasedAlgorithm<GSMLocationObject>()));

		getMap().setOnCameraChangeListener(mClusterManager);
		getMap().setOnMarkerClickListener(mClusterManager);
		getMap().setOnMyLocationChangeListener(this);

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
		mClusterManager.clearItems();
		GSMLocationObject.clear();
		mClusterManager.addItems(GSMLocationObject.addAll(current_markers));
		if (A.isChecked()) {
			mClusterManager.addItems(GSMLocationObject.addAll(all_markers));
		}

		S.setChecked(false);
		H.setChecked(false);
		M.setChecked(false);

		mClusterManager.cluster();
		for (SignalDTO s : current_signals.keySet()) {
			current_signals.put(s, drawSingleSignal(s));
		}

	}

	public CircleOptions getCircleOptionsFromCircle(Circle c) {
		CircleOptions co = new CircleOptions();
		co.center(c.getCenter());
		co.radius(c.getRadius());
		co.fillColor(c.getFillColor());
		co.strokeWidth(0);
		return co;
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
	public void onLocationCange(final SignalDTO dto) {
		if (getMap() != null) {
			Circle c = drawSingleSignal(dto);
			current_signals.put(dto, c);
		} else {
			current_signals.put(dto, null);
		}
	};
	

	private Polygon drawLocation(LocationDTO loc){
		GeoHash g = GeoHash.fromBinaryString(loc.getGeohash());
		double maxLat = g.getBoundingBox().getMaxLat();
		double minLat = g.getBoundingBox().getMinLat();
		double maxLng = g.getBoundingBox().getMaxLon();
		double minLng = g.getBoundingBox().getMinLon();
		return drawRectangle(getMap(), maxLat, minLat, maxLng, minLng, 1, getColorBaseOnSignal(loc.getAvg()));
	}
	
	private Polygon drawRectangle(GoogleMap googleMap, double maxLat, double minLat, double maxLng, double minLng,int zindex,int color) {
		PolygonOptions po = new PolygonOptions().add(new LatLng(maxLat, maxLng)).add(new LatLng(maxLat, minLng)).add(new LatLng(minLat, minLng))
				.add(new LatLng(minLat, maxLng)).add(new LatLng(maxLat, maxLng));
		
		po.fillColor(color);
		po.strokeWidth(0);
		po.strokeColor(Color.TRANSPARENT);
		po.zIndex(zindex);

		return googleMap.addPolygon(po);
	}

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

		CircleOptions circleOptions = new CircleOptions().center(handover).radius(2).fillColor(Color.BLACK).strokeColor(Color.TRANSPARENT);

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
			PolylineOptions toLineOptions = new PolylineOptions().add(handover).add(toCell).color(Color.argb(opacity, 102, 204, 255)).width(4f);
			lines.add(getMap().addPolyline(toLineOptions));
			PolylineOptions fromLineOptions = new PolylineOptions().add(handover).add(fromCell).color(Color.argb(opacity, 255, 80, 80)).width(4f);
			lines.add(getMap().addPolyline(fromLineOptions));
		} else {
			PolylineOptions toLineOptions = new PolylineOptions().add(handover).add(toCell).color(Color.argb(opacity, 255, 153, 255)).width(4f);
			lines.add(getMap().addPolyline(toLineOptions));
		}

		mClusterManager.cluster();

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
			return Color.argb(opacity, 255, 255, 0);
		} else if (i > -90) {
			return Color.argb(opacity, 255, 204, 0);
		} else if (i > -100) {
			return Color.argb(opacity, 255, 0, 0);
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
		if (!all_markers.isEmpty()) {
			Collection<GSMLocationObject> newLycCreated = GSMLocationObject.addAll(all_markers);
			mClusterManager.addItems(newLycCreated);
			mClusterManager.cluster();
		} else {
			AsyncTask<Void, Void, List<CellDTO>> getAll = new AsyncTask<Void, Void, List<CellDTO>>() {
				@Override
				protected List<CellDTO> doInBackground(Void... params) {
					try {
						return SigmetActivity.dataManager.getAllCells();
					} catch (SQLException e) {
						SigmetLogger.error(e.getMessage());
						cancel(true);
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
			getAll.execute((Void) null);
		}
	}

	public void drawWalk(final WalkDTO walk) {

		final AsyncTask<WalkDTO, Void, List<HandoverDTO>> drawWalkHandowers = new AsyncTask<WalkDTO, Void, List<HandoverDTO>>() {

			@Override
			protected List<HandoverDTO> doInBackground(WalkDTO... params) {
				try {
					return SigmetActivity.dataManager.getHandoversForWalk(walk);
				} catch (Exception e) {
					SigmetLogger.error(e.getMessage());
					cancel(true);
				}
				return Collections.emptyList();
			}

			@Override
			protected void onPostExecute(List<HandoverDTO> result) {
				for (HandoverDTO h : result) {
					drawHandover(h, walk_cells, walk_handovers, walk_handover_lines);
				}
				recalculateCameraForWalk();
			}

		};

		final AsyncTask<WalkDTO, Void, List<SignalDTO>> drawWalkSignals = new AsyncTask<WalkDTO, Void, List<SignalDTO>>() {

			@Override
			protected List<SignalDTO> doInBackground(WalkDTO... params) {
				try {
					return SigmetActivity.dataManager.getSignalsForWalk(walk);
				} catch (Exception e) {
					SigmetLogger.error(e.getLocalizedMessage());
					cancel(true);
				}
				return Collections.emptyList();
			}

			@Override
			protected void onPostExecute(List<SignalDTO> result) {
				for (SignalDTO s : result) {
					walk_signal.add(drawSingleSignal(s));
				}
			}
		};

		getMapAsync(new OnMapReadyCallback() {

			@Override
			public void onMapReady(GoogleMap googleMap) {
				removeNonCurentCells();
				removeNonCurrentSignal();
				removeHandovers();
				H.setVisibility(View.GONE);
				M.setVisibility(View.GONE);
				S.setVisibility(View.GONE);
				A.setVisibility(View.GONE);

				drawWalkHandowers.execute(walk);
				drawWalkSignals.execute(walk);
			}
		});

	}

	private void recalculateCameraPosision() {
		if (mClusterManager.getMarkerCollection().getMarkers().isEmpty()) {
			return;
		}
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (GSMLocationObject marker : GSMLocationObject.getAll()) {
			builder.include(marker.getPosition());
		}
		if (currentLoc != null) {
			builder.include(new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude()));
		}
		LatLngBounds bounds = builder.build();
		final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 20);
		getMapAsync(new OnMapReadyCallback() {

			@Override
			public void onMapReady(GoogleMap googleMap) {
				googleMap.animateCamera(cu);
			}
		});
	}

	private void recalculateCameraForWalk() {
		getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				LatLngBounds.Builder builder = new LatLngBounds.Builder();
				for (CellDTO marker : walk_cells) {
					builder.include(new LatLng(marker.getLatitude(), marker.getLongtitude()));
				}
				LatLngBounds bounds = builder.build();
				CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 30);
				googleMap.animateCamera(cu, 700, null);
				
			}
		});
	}

	private void removeNonCurentCells() {
		active_marker = null;
		for (GSMLocationObject item : GSMLocationObject.removeAll(all_markers)) {
			mClusterManager.removeItem(item);
		}
		mClusterManager.cluster();
	}

	private void drawSignalMap() {
			M.setEnabled(false);
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
		removePolygon(show_signal);
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

	private void removePolygon(Collection<Polygon> in) {
		for (Polygon c : in) {
			c.setVisible(false);
			c.remove();
		}
		in.clear();
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

	private class DrawSingleCellSignal extends AsyncTask<LatLng, Void, Collection<LocationDTO>> {

		@Override
		protected Collection<LocationDTO> doInBackground(LatLng... params) {
			try {
				List<SignalDTO> ss = SigmetActivity.dataManager.getSignalMeasures(params[0]);
				Map<String,LocationDTO> averages = new HashMap<String,LocationDTO>();
				while (!ss.isEmpty()) {
					SignalDTO curr = ss.remove(0);
					String geohash = curr.getGeohash();
					geohash = geohash.substring(0,SINGLE_CELL_GEOHASH_PRECIZION);
					if(averages.containsKey(geohash)){
						averages.get(geohash).addSignal(curr.getValue());
					}else{
						LocationDTO loc = new LocationDTO();
						loc.setGeohash(geohash);
						loc.addSignal(curr.getValue());
						averages.put(geohash, loc);
					}
					
				}				
				return averages.values();
			} catch (Exception e) {
				SigmetLogger.error(e.getMessage());
			}
			return Collections.emptyList();
			// DefaultClusterRenderer a;
		}

		@Override
		protected void onPostExecute(Collection<LocationDTO> averages) {
			for (LocationDTO l : averages) {
				show_signal.add(drawLocation(l));
			}	
		}
	};

	private class DrawAllSignal extends AsyncTask<Void, Void, List<WeightedLatLng>> {
		@Override
		protected List<WeightedLatLng> doInBackground(Void... params) {
			try {
				List<LocationDTO> ss = SigmetActivity.dataManager.getAllLocations();
				List<WeightedLatLng> data = new LinkedList<WeightedLatLng>();
				for (LocationDTO s : ss) {
					LatLng ll = new LatLng(s.getLatitude(), s.getLongitude());
					data.add(new WeightedLatLng(ll, (s.getAvg() + MAX_NEGATIVE_DBM)));
				}

				return data;
			} catch (Exception e) {
				SigmetLogger.error(e.getMessage());
			}
			return Collections.emptyList();
		}

		@Override
		protected void onPostExecute(final List<WeightedLatLng> result) {
			if (result.isEmpty()) {
				return;
			}
			getMapAsync(new OnMapReadyCallback() {

				@Override
				public void onMapReady(GoogleMap googleMap) {
					HeatmapTileProvider provider = new HeatmapTileProvider.Builder().weightedData(result).gradient(g).radius(50).opacity(0.5).build();
					heat_map = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
					M.setEnabled(true);

				}
			});
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
				return SigmetActivity.dataManager.getHandovers(params[0]);
			} catch (Exception e) {
				SigmetLogger.error(e.getMessage());
			}
			return Collections.emptyList();
		}

		@Override
		protected void onPostExecute(List<HandoverDTO> result) {
			for (HandoverDTO h : result) {
				drawHandover(h, handover_markers, handovers_lines, handover_lines);
			}
		}

	}

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
		removeCircle(current_signals.values());
		current_signals.clear();
		removePolygon(show_signal);
		onCellChange(SigmetActivity.dataManager.getActual());
		removeHandovers();

	}

	@Override
	public void onMyLocationChange(final Location location) {
		currentLoc = location;
	}

}
