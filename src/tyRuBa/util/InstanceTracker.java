package tyRuBa.util;

import java.util.ArrayList;
import java.util.Iterator;

/** Utility for debugging. Use it store a collection of Objects you want
 * to keep track of.
 */
public class InstanceTracker {
	
	private ArrayList instances;
	private String info;

	public InstanceTracker(String info) {
		this.info = info;
		instances = new ArrayList();
	}
	
	public synchronized void add(Object o) {
		instances.add(o);
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("Instances of "+info+"(\n");
		for (Iterator iter = instances.iterator(); iter.hasNext();) {
			Object element = iter.next();
			result.append(element+",\n");
		}
		result.append(")");
		return result.toString();
	}

}
