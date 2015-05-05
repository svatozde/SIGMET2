package cz.cvut.sigmet.dbUtils;

import java.util.ArrayList;
import java.util.List;

import cz.cvut.sigmet.model.LogDTO;

public class SigmetLogger {

	private static List<SigmetLogAppender> appenders = new ArrayList<SigmetLogAppender>();
	
	public static void info(String message){
		append(new LogDTO(LogDTO.Level.INFO,message, System.currentTimeMillis()));
	}
	
	public static void info(String message, Object... values){
		message = String.format(message, values);
		append(new LogDTO(LogDTO.Level.INFO,message, System.currentTimeMillis()));
	}
	
	
	
	public static void warn(String message){
		append(new LogDTO(LogDTO.Level.WARN,message, System.currentTimeMillis()));
	}
	
	public static void warn(String message, Object... values){
		message = String.format(message, values);
		append(new LogDTO(LogDTO.Level.INFO,message, System.currentTimeMillis()));
	}

	public static void error(String message){
		append(new LogDTO(LogDTO.Level.ERROR,message, System.currentTimeMillis()));
	}
	
	public static void error(String message, Object... values){
		message = String.format(message, values);
		append(new LogDTO(LogDTO.Level.INFO,message, System.currentTimeMillis()));
	}
	
	public static void addAppender(SigmetLogAppender appender){
		appenders.add(appender);
	}
	
	
	
	private static void append(LogDTO log){
		for(SigmetLogAppender a : appenders){
			a.append(log);
		}
	}
	
}
