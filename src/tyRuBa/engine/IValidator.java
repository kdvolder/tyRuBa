package tyRuBa.engine;

public interface IValidator {

	public abstract boolean isValid();
	
	public abstract boolean isOutdated();
	
	public abstract long handle();
	
}