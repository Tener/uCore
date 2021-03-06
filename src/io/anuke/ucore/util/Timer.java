package io.anuke.ucore.util;

import io.anuke.ucore.core.Timers;

public class Timer{
	float[] times;
	
	public Timer(int capacity){
		times = new float[capacity];
	}
	
	public boolean get(int id, float time){
		if(id >= times.length) throw new RuntimeException("Out of bounds! Max timer size is " + times.length + "!");
		if(Timers.time() - times[id] >= time){
			times[id] = Timers.time();
			return true;
		}else{
			return false;
		}
	}
}
