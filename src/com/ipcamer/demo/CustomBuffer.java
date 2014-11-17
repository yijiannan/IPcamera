package com.ipcamer.demo;

import java.util.LinkedList;
import java.util.List;

public class CustomBuffer{
	private List<CustomBufferData> DataBuffer = new LinkedList<CustomBufferData>();	
	
	public boolean addData(CustomBufferData data){
		synchronized (this) {
			DataBuffer.add(data);
			return true;
		}		
	}
	
	public CustomBufferData RemoveData(){
		synchronized (this) {
			if (DataBuffer.isEmpty()) {
				return null;
			}			
			return DataBuffer.remove(0);
		}		
	}
	
	public void ClearAll(){
		synchronized (this) {
			DataBuffer.clear();
		}
		
	}
}