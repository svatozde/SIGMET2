package cz.cvut.sigmet.dbUtils;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import cz.cvut.sigmet.model.CellDTO;
import cz.cvut.sigmet.model.HandoverDTO;
import cz.cvut.sigmet.model.LocationDTO;
import cz.cvut.sigmet.model.SignalDTO;
import cz.cvut.sigmet.model.WalkDTO;

public class SigmetSqlHelper extends OrmLiteSqliteOpenHelper  {

	private static final String DATABASE_NAME = "sigmet.db";
	
	private static final int DATABASE_VERSION = 1;
	
	private RuntimeExceptionDao<CellDTO, Integer> cellDao = null;

	private RuntimeExceptionDao<SignalDTO, Long> signalDao = null;

	private RuntimeExceptionDao<HandoverDTO, Integer> handoverDao = null;
	
	private RuntimeExceptionDao<WalkDTO, Integer> walkDao = null;
	
	private RuntimeExceptionDao<LocationDTO, String> locationDao = null;
	
	public SigmetSqlHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	@Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2, int arg3) {
		try {
			Log.i(SigmetSqlHelper.class.getName(), "*** DATABASE UPGRADE ***");		
			createTables();
		} catch (SQLException e) {
			Log.e(SigmetSqlHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		} catch (java.sql.SQLException e) {
			Log.e(SigmetSqlHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}
	
	
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource cs) {

		try {
			Log.i(SigmetSqlHelper.class.getName(), "*** DATABASE CREATE ***");	
			createTables();
		} catch (SQLException e) {
			Log.e(SigmetSqlHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		} catch (java.sql.SQLException e) {
			Log.e(SigmetSqlHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}
	
	
	public void createTables() throws java.sql.SQLException{
		TableUtils.createTable(connectionSource, LocationDTO.class);
		TableUtils.createTable(connectionSource, CellDTO.class);
		TableUtils.createTable(connectionSource, SignalDTO.class);
		TableUtils.createTable(connectionSource, HandoverDTO.class);
		TableUtils.createTable(connectionSource,  WalkDTO.class);		
	}

	@Override
	public void close() {
		super.close();
		cellDao = null;
		signalDao = null;
		handoverDao = null;
		walkDao = null;
		locationDao = null;
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		cellDao = null;
		signalDao = null;
		handoverDao = null;
		walkDao = null;	
		locationDao = null;
	}
	
	
	public RuntimeExceptionDao<CellDTO, Integer> getCellDao(){
		
		if(cellDao == null){
			cellDao =  getRuntimeExceptionDao(CellDTO.class);
		}
		return cellDao;
	}
	
	public RuntimeExceptionDao<SignalDTO, Long> getSignalDao(){
		if(signalDao == null){
			return  getRuntimeExceptionDao(SignalDTO.class);
		}
		return signalDao;
	}
	
	public RuntimeExceptionDao<HandoverDTO, Integer> getHandoverDao(){
		if(handoverDao == null){
			handoverDao =  getRuntimeExceptionDao(HandoverDTO.class);
		}
		return handoverDao;
	}
	
	
	public RuntimeExceptionDao<WalkDTO, Integer> getWalkDao(){
		if(walkDao == null){
			walkDao =  getRuntimeExceptionDao(WalkDTO.class);
		}
		return walkDao;
	}
	
	public RuntimeExceptionDao<LocationDTO, String> getLocationDao(){
		if(locationDao == null){
			locationDao =  getRuntimeExceptionDao(LocationDTO.class);
		}
		return locationDao;
	}
	
}
