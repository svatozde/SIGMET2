package cz.cvut.sigmet.dbUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.telephony.SignalStrength;

public class SigmetSignalUtils {
	
	public static int getSignalByReflection(SignalStrength s){
		
		return 2*s.getGsmSignalStrength() - 113;
		
	}

}
