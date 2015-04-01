package cz.cvut.sigmet.gsmWebUtils;


public enum EOperator {
	TMOBILE(230,01,"tmobile"),
	TMOBILE_UMTS(230,01,"tmobileumts"),
	TMOBILE_LTE(230,01,"tmobilelte"),
	
	VODAFONE(230,03,"vodafone"),
	VODAFONE_UMTS(230,03,"vodafoneumts"),
	VODAFONE_LTE(230,03,"vodafonelte"),
	
	O2(230,02,"o2"),
	O2_LTE(230,02,"o2lte"),
	O2_UMTS(230,02,"o2umts"),
	O2_CDMA(230,02,"o2cdma"),
	
	UFON(230,04,"ufon"),
	
	//NOT TESTED
	TRAVEL_TELECOMUNICATION(230,04,""),
	OSNO_TELECOMMUNICATION(230,05,""),
	ASTELNET(230,06,""),
	SPRAVA_ZELEZNIC(230,98,"");

	private int mcc;
	private int mnc;
	private String gsmWebCode;
	
	
	
	private EOperator(int mcc, int mnc, String gsmWebCode) {
		this.mnc = mnc;
		this.mcc = mcc;
		this.gsmWebCode = gsmWebCode;
	}
	
	public static EOperator getByMcc(int mnc, int mcc){
		for(EOperator e: values()){
			if(e.getMcc() == mcc && e.getMnc() == mnc){
				return e;
			}
		}
		return null;
	}
	
	public static EOperator getByGsmWebCode(String op){
		for(EOperator e: values()){
			if(e.getGsmWebCode().equals(op)){
				return e;
			}
		}
		return null;
	}
	
	public boolean isLTE(){
		return name().endsWith("LTE");
	}
	
	public boolean isUMTS(){
		return name().endsWith("UMTS");
	}
	
	
	public int getMcc() {
		return mcc;
	}
	public void setMcc(int mcc) {
		this.mcc = mcc;
	}
	public int getMnc() {
		return mnc;
	}
	public void setMnc(int mnc) {
		this.mnc = mnc;
	}
	public String getGsmWebCode() {
		return gsmWebCode;
	}
	public void setGsmWebCode(String gsmWebCode) {
		this.gsmWebCode = gsmWebCode;
	}

}
