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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return 0;
		
	}

}
