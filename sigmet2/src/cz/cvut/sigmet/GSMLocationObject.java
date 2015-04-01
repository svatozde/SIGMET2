package cz.cvut.sigmet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import cz.cvut.sigmet.model.CellDTO;

public class GSMLocationObject implements ClusterItem{
	private static Map<LatLng,GSMLocationObject> allItems = new HashMap<LatLng,GSMLocationObject>();
	
	private LatLng location;
    private Set<CellDTO> cells = new HashSet<CellDTO>();
	
	
	private GSMLocationObject(LatLng location){
		this.location = location;
		allItems.put(location,this);
	}


	@Override
	public LatLng getPosition() {
		return location;
	}
	
	public static boolean contains(CellDTO dto){
		LatLng key = new LatLng(dto.getLatitude(), dto.getLongtitude());
		return allItems.containsKey(key);
	}
	/**
	 * Creates new location object for cell dto, if there is already one adds the cell into it.
	 * Object is returned only in case it was created and therefore it should be added into cluster manager
	 * 
	 * @param dto
	 * @return
	 */
	public static GSMLocationObject add(CellDTO dto){
		LatLng key = new LatLng(dto.getLatitude(), dto.getLongtitude());
		if(allItems.containsKey(key)){
			allItems.get(key).cells.add(dto);
			return null;
		}else{
			GSMLocationObject newLoc = new GSMLocationObject(key);
			newLoc.cells.add(dto);
			allItems.put(key, newLoc);
			return newLoc;
		}
		
	}
	
	public static GSMLocationObject remove(CellDTO dto){
		LatLng key = new LatLng(dto.getLatitude(), dto.getLongtitude());
		if(allItems.containsKey(key)){
			GSMLocationObject current = allItems.get(key);
			current.cells.remove(dto);
			if(current.cells.isEmpty()){
				return allItems.remove(key);
			}
		}
		return null;
	}
	
	public static Collection<GSMLocationObject> removeAll(Collection<CellDTO> cells){
		Set<GSMLocationObject> ret = new HashSet<GSMLocationObject>();
		for(CellDTO c : cells){
			GSMLocationObject newO = remove(c);
			if(newO != null){
				ret.add(newO);
			}
		}
		return ret;
	}
	
	public static Collection<GSMLocationObject> addAll(Collection<CellDTO> cells){
		Set<GSMLocationObject> ret = new HashSet<GSMLocationObject>();
		for(CellDTO c : cells){
			GSMLocationObject newO = add(c);
			if(newO != null){
				ret.add(newO);
			}
		}
		return ret;
	}
	
	public static Collection<GSMLocationObject> getAll(){
		return allItems.values();
	}
	
	public Set<CellDTO> getCells(){
		return cells;
	}
	
	public static void clear(){
		allItems.clear();
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GSMLocationObject other = (GSMLocationObject) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}
	
	
	
}
