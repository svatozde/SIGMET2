package cz.cvut.sigmet.dbUtils;

import static cz.cvut.sigmet.dbUtils.SigmetSignalUtils.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.gsm.GsmCellLocation;

import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import cz.cvut.sigmet.gsmWebUtils.GsmWebManager;
import cz.cvut.sigmet.gsmWebUtils.GsmWebManagerImpl;
import cz.cvut.sigmet.model.CellDTO;
import cz.cvut.sigmet.model.HandoverDTO;
import cz.cvut.sigmet.model.SignalDTO;
import cz.cvut.sigmet.model.WalkDTO;

public class SigmetDataManager extends PhoneStateListener implements LocationListener {

	private final static  String  GSM_MESSAGE= "Connect to c: %s l: %s from web.";
	private final static  String  WALK_START_MESSAGE= "Walk %s started";
	private final static  String  WALK_STOP_MESSAGE= "Walk %s stopped";
    private static  String DB_MESSAGE = "Connect to c: %s l: %s from DB.";
	private GsmWebManager webMgr = new GsmWebManagerImpl();

	protected Set<SigmetDataListener> listenres = new HashSet<SigmetDataListener>();

	private CellDTO actual;
	private Location actual_location;

	private SignalStrength actual_signalStrength;

	private SigmetSqlHelper helper = null;

	private WalkDTO actual_walk;

	public SigmetDataManager(SigmetSqlHelper helper) throws SQLException {
		this.helper = helper;
	}

	public List<CellDTO> getAllCells() throws SQLException {
		// getInconsistence();
		QueryBuilder<CellDTO, Integer> qb = helper.getCellDao().queryBuilder();
		qb.orderBy("addres", true);
		List<CellDTO> cells = helper.getCellDao().query(qb.prepare());
		return cells;
	}

	public List<SignalDTO> getAllSignal() throws SQLException {
		QueryBuilder<SignalDTO, Integer> qb = helper.getSignalDao().queryBuilder();
		qb.where().isNotNull("walk_id");
		List<SignalDTO> signals = helper.getSignalDao().query(qb.prepare());
		return signals;
	}

	public synchronized CellDTO getGsmInfo(String cid, String lac) throws Exception {
		QueryBuilder<CellDTO, Integer> qb = helper.getCellDao().queryBuilder();
		qb.where().eq("cid", cid).and().eq("lac", lac);
		List<CellDTO> cells = helper.getCellDao().query(qb.prepare());

	
		CellDTO ret = null;
		if (cells == null || cells.isEmpty()) {
			ret = webMgr.getGsmInfo(cid, lac);
			helper.getCellDao().create(ret);
			SigmetLogger.info(GSM_MESSAGE, cid, lac);
		} else {
			ret = cells.get(0);
			SigmetLogger.info(DB_MESSAGE, cid, lac);
		}
		

		if (actual != null && !actual.equals(ret) && actual_location != null) {
			HandoverDTO handover = new HandoverDTO(actual, ret, actual_location.getLatitude(), actual_location.getLongitude(), System.currentTimeMillis());
			handover.setWalk(actual_walk);
			helper.getHandoverDao().create(handover);
			actual = ret;
			return ret;
		} else if (actual == null) {
			actual = ret;
			return ret;
		}

		return null;

	}

	
	public SignalDTO addSignalStrength(int strength) throws Exception {
		if (actual_location != null && helper.getSignalDao() != null) {
			SignalDTO s = new SignalDTO(actual, actual_location.getLatitude(), actual_location.getLongitude(), strength, System.currentTimeMillis());
			s.setWalk(actual_walk);
			helper.getSignalDao().create(s);
			return s;
		}
		return null;
	}

	/**
	 * Note there is no
	 * 
	 * @param cell
	 * @return
	 * @throws Exception
	 */
	public List<SignalDTO> getSignalMeasures(LatLng cell) throws Exception {
		QueryBuilder<CellDTO, Integer> qbCell = helper.getCellDao().queryBuilder();
		qbCell.where().eq("latitude", cell.latitude).and().eq("longtitude", cell.longitude);
		List<CellDTO> cells = helper.getCellDao().query(qbCell.prepare());

		List<Integer> cid = new ArrayList<Integer>();
		for (CellDTO c : cells) {
			cid.add(c.getId());
		}

		QueryBuilder<SignalDTO, Integer> qb = helper.getSignalDao().queryBuilder();
		qb.where().in("cell_id", cid);
		qb.orderBy("timestamp", true);
		return helper.getSignalDao().query(qb.prepare());
	}
	
	

	public List<HandoverDTO> getHandovers(LatLng cell) throws Exception {
		Cursor dbCursor = helper.getReadableDatabase().query("handover", null, null, null, null, null, null);
		String[] columnNames = dbCursor.getColumnNames();
		System.out.println(columnNames);

		QueryBuilder<CellDTO, Integer> qbCell = helper.getCellDao().queryBuilder();
		qbCell.where().eq("latitude", cell.latitude).and().eq("longtitude", cell.longitude);
		List<CellDTO> cells = helper.getCellDao().query(qbCell.prepare());

		List<Integer> cid = new ArrayList<Integer>();
		for (CellDTO c : cells) {
			cid.add(c.getId());
		}

		QueryBuilder<HandoverDTO, Integer> qb = helper.getHandoverDao().queryBuilder();
		qb.where().in("from_id", cid).or().in("to_id", cid);

		return helper.getHandoverDao().query(qb.prepare());
	}

	public List<HandoverDTO> getHandoversIn(CellDTO cell) throws Exception {
		QueryBuilder<HandoverDTO, Integer> qb = helper.getHandoverDao().queryBuilder();
		qb.where().eq("to", cell);
		return helper.getHandoverDao().query(qb.prepare());
	}
	
	public List<HandoverDTO> getHandoversForWalk(WalkDTO walk) throws Exception {
		QueryBuilder<HandoverDTO, Integer> qb = helper.getHandoverDao().queryBuilder();
		qb.where().eq("walk_id", walk.getId());
		return helper.getHandoverDao().query(qb.prepare());
	}
	
	public List<SignalDTO> getSignalsForWalk(WalkDTO walk) throws Exception {
		QueryBuilder<SignalDTO, Integer> qb = helper.getSignalDao().queryBuilder();
		qb.where().eq("walk_id", walk.getId());
		return helper.getSignalDao().query(qb.prepare());
	}

	@Override
	public void onLocationChanged(Location location) {
		actual_location = location;
		SignalDTO s;
		if (actual_signalStrength == null) {
			return;
		}

		try {
			s = addSignalStrength(getSignalByReflection(actual_signalStrength));
			if (s != null) {
				for (SigmetDataListener l : listenres) {
					l.onLocationCange(s);
				}
			}
		} catch (Exception e) {
			SigmetLogger.error(e.getMessage());
		}
	}

	@Override
	public void onCellLocationChanged(CellLocation location) {
		GsmCellLocation gsmLoc = (GsmCellLocation) location;
		AsyncTask<GsmCellLocation, Void, CellDTO> a = new AsyncTask<GsmCellLocation, Void, CellDTO>() {

			@Override
			protected CellDTO doInBackground(GsmCellLocation... params) {
				try {
					return getGsmInfo(Integer.toString(params[0].getCid()), Integer.toString(params[0].getLac()));
				} catch (Exception e) {
					return null;
				}
			}

			@Override
			protected void onPostExecute(CellDTO result) {
				if (result != null) {
					for (SigmetDataListener l : listenres) {
						l.onCellChange(result);
					}
				}
			}
		};

		a.execute(gsmLoc);
	};

	public List<WalkDTO> getAllWalks() throws SQLException {
		QueryBuilder<WalkDTO, Integer> qb = helper.getWalkDao().queryBuilder();
		qb.orderBy("start", true);
		List<WalkDTO> walks = helper.getWalkDao().query(qb.prepare());
		return walks;
	}

	public void getInconsistence() throws SQLException {
		Cursor dbCursor = helper.getReadableDatabase().query("signal", null, null, null, null, null, null);
		String[] columnNames = dbCursor.getColumnNames();
		System.out.println(columnNames);

		GenericRawResults<String[]> qb2 = helper.getSignalDao().queryRaw("select * from signal where cell_id = null");
		System.out.println(qb2);

		GenericRawResults<String[]> qb3 = helper.getCellDao().queryRaw("select cid, count(*) from cells group by cid");
		System.out.println(qb3);

		GenericRawResults<String[]> qb4 = helper.getCellDao().queryRaw("select min(value) from signal");
		System.out.println(qb4);

		GenericRawResults<String[]> qb5 = helper.getCellDao().queryRaw("select max(value) from signal");
		System.out.println(qb5);
	}

	public void startWalk(String name) throws SQLException {
		WalkDTO dto = new WalkDTO();
		dto.setName(name);
		dto.setStart(new Date().getTime());
		helper.getWalkDao().create(dto);
		actual_walk = dto;
		SigmetLogger.info(WALK_START_MESSAGE, name);
	}

	public void stopWalk() {
		if (actual_walk != null) {
			actual_walk.setStop(new Date().getTime());
			helper.getWalkDao().update(actual_walk);
			SigmetLogger.info(WALK_STOP_MESSAGE, actual_walk.getName());
			actual_walk = null;
			
		}
	}
	
	public void deleteWalk(WalkDTO walk) throws SQLException{
		UpdateBuilder<SignalDTO, Integer> sUpdate = helper.getSignalDao().updateBuilder();
		sUpdate.where().eq("walk_id", walk);
		sUpdate.updateColumnValue("walk_id", null);
		sUpdate.update();
		
		UpdateBuilder<HandoverDTO, Integer> hUpdate = helper.getHandoverDao().updateBuilder();
		hUpdate.where().eq("walk_id", walk);
		hUpdate.updateColumnValue("walk_id", null);
		hUpdate.update();
		
		helper.getWalkDao().deleteById(walk.getId());
	}

	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
		actual_signalStrength = signalStrength;
	}

	public void addDataListener(SigmetDataListener l) {
		listenres.add(l);
	}

	public void setActual_location(Location actual_location) {
		this.actual_location = actual_location;
	}

	public SignalStrength getActual_signalStrength() {
		return actual_signalStrength;
	}

	public void setActual_signalStrength(SignalStrength actual_signalStrength) {
		this.actual_signalStrength = actual_signalStrength;
	}

	public void recallWithActulCell() {
		for (SigmetDataListener l : listenres) {
			l.onCellChange(actual);
		}
	}
	
	public CellDTO getActual() {
		return actual;
	}

	// //////////////////////////////////////////////////////////
	// //////// UNUSED METHODS ////////////
	// //////////////////////////////////////////////////////////

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

}
