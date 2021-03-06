package tk.blackwolf12333.grieflog.utils.filters;

import tk.blackwolf12333.grieflog.data.BaseData;

public class EventFilter implements Filter {

	String event;
	
	public EventFilter(String event) {
		this.event = event;
	}
	
	@Override
	public boolean doFilter(BaseData data) {
		return data.getEvent().equals(event);
	}

}
