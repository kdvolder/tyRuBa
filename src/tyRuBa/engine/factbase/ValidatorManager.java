/*
 * Created on Jun 18, 2004
 */
package tyRuBa.engine.factbase;

import tyRuBa.engine.IValidator;

/**
 * A ValidatorManager persists the Validators that indicate whether facts for a
 * bucket are valid or not. In a FactBase the validators are referenced by their
 * handles, so mappings are implemented in the ValidatorManager that allow
 * retrieval of a validator by giving its handle.
 */
public interface ValidatorManager {
	
	public IValidator newValidator(String identifier);
	
    /**
     * Retrieves a validator by its handle.
     * @param validatorHandle validator to retrieve.
     * @return the validator or null if Validator is unknown.
     */
    public IValidator get(long validatorHandle);

    /**
     * Retrieves a validator by its identifier.
     * @param identifier Identifier of validator to retrieve.
     * @return the validator or null if Validator is unknown.
     */
    public IValidator get(String identifier);

    /**
     * Prints out all the validators.
     */
    public void printOutValidators();

    /**
     * Persists the validators.
     */
    public void backup();

    /**
     * Returns the last time a validator was invalidated.
     */
    public long getLastInvalidatedTime();

    /**
     * Invalidate a given validator and remove it from the manager.
     * 
     * @param validator
     */
	public void invalidate(IValidator validator);

	/**
	 * Set a validator's outdated status flag.
	 * 
	 * @param validator
	 * @param isOutdated
	 */
	public void setOutdated(IValidator validator, boolean isOutdate);

}