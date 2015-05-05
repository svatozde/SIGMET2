package cz.cvut.sigmet.dbUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.telephony.SignalStrength;

public class SigmetSignalUtils {
	
	public static int getSignalByReflection(SignalStrength s){
		try {
			Class<?> c = Class.forName("android.telephony.SignalStrength");
			Method m = c.getMethod("getDbm");
			Integer o = (Integer) m.invoke(s);
			return o;
		} catch (NoSuchMethodException e) {
			SigmetLogger.error(e.getMessage());
		} catch (ClassNotFoundException e) {
			SigmetLogger.error(e.getMessage());
		} catch (IllegalArgumentException e) {
			SigmetLogger.error(e.getMessage());
		} catch (IllegalAccessException e) {
			SigmetLogger.error(e.getMessage());
		} catch (InvocationTargetException e) {
			SigmetLogger.error(e.getMessage());
		}
		
		
		return 0;
		
	}

}
