package tyRuBa.util;

/**
 * Ultra simple list class designed for minimal overhead.
 * 
 * @author cburns
 */
public final class SList {
	
	private Object object;
	private SList next;
	
	public final Object object() {
		return object;
	}
	
	public final SList next() {
		return next;
	}
	
	public SList(Object object, SList next) {
		this.object = object;
		this.next = next;
	}
}
