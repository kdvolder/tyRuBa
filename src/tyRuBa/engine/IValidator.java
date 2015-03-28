package tyRuBa.engine;

import annotations.Export;
import annotations.Feature;

@Feature(names={"Validator"})
public interface IValidator {

	@Export
	public abstract boolean isValid();
	
	@Export
	public abstract boolean isOutdated();
	
	public abstract long handle();
	
}