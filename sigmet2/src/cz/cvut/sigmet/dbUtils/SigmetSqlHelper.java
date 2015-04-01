package cz.cvut.sigmet.dbUtils;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import cz.cvut.sigmet.MAPKA;
import cz.cvut.sigmet.model.CellDTO;
import cz.cvut.sigmet.model.HandoverDTO;
import cz.cvut.sigmet.model.MeasureDTO;
import cz.cvut.sigmet.model.SignalDTO;
import cz.cvut.sigmet.model.WalkDTO;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class SigmetSqlHelper extends OrmLiteSqliteOpenHelper  {

	private static final String DATABASE_NAME = "sigmet.db";
	
	private static final int DATABASE_VERSION = 1;
	
	private RuntimeExceptionDao<CellDTO, Integer> cellDao = null;

	private RuntimeExceptionDao<SignalDTO, Integer> signalDao = null;

	private RuntimeExceptionDao<HandoverDTO, Integer> handoverDao = null;

	public SigmetSqlHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	@Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2, int arg3) {
		try {
			Log.i(SigmetSqlHelper.class.getName(), "*** DATABASE UPGRADE ***");
			TableUtils.dropTable(connectionSource, CellDTO.class, true);
			TableUtils.dropTable(connectionSource, SignalDTO.class, true);
			TableUtils.dropTable(connectionSource,  HandoverDTO.class, true);
			TableUtils.dropTable(connectionSource,  WalkDTO.class, true);
			TableUtils.dropTable(connectionSource,  MeasureDTO.class, true);
			
			TableUtils.createTable(connectionSource, CellDTO.class);
			TableUtils.createTable(connectionSource, SignalDTO.class);
			TableUtils.createTable(connectionSource, HandoverDTO.class);
			TableUtils.createTable(connectionSource,  WalkDTO.class);
			TableUtils.createTable(connectionSource,  MeasureDTO.class);
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
			TableUtils.dropTable(connectionSource, CellDTO.class, true);
			TableUtils.dropTable(connectionSource, SignalDTO.class, true);
			TableUtils.dropTable(connectionSource,  HandoverDTO.class, true);
			TableUtils.dropTable(connectionSource,  WalkDTO.class, true);
			TableUtils.dropTable(connectionSource,  MeasureDTO.class, true);
			
			TableUtils.createTable(connectionSource, CellDTO.class);
			TableUtils.createTable(connectionSource, SignalDTO.class);
			TableUtils.createTable(connectionSource, HandoverDTO.class);
			TableUtils.createTable(connectionSource,  WalkDTO.class);
			TableUtils.createTable(connectionSource,  MeasureDTO.class);
		} catch (SQLException e) {
			Log.e(SigmetSqlHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		} catch (java.sql.SQLException e) {
			Log.e(SigmetSqlHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		super.close();
		cellDao = null;
		signalDao = null;
		handoverDao = null;
		
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		cellDao = null;
		signalDao = null;
		handoverDao = null;
	}
	
	public RuntimeExceptionDao<CellDTO, Integer> getCellDao(){
		if(cellDao == null){
			return  getRuntimeExceptionDao(CellDTO.class);
		}
		return cellDao;
	}
	
	public RuntimeExceptionDao<SignalDTO, Integer> getSignalDao(){
		if(signalDao == null){
			return  getRuntimeExceptionDao(SignalDTO.class);
		}
		return signalDao;
	}
	
	public RuntimeExceptionDao<HandoverDTO, Integer> getHandoverDao(){
		if(handoverDao == null){
			return  getRuntimeExceptionDao(HandoverDTO.class);
		}
		return handoverDao;
	}
	
	
	


}
