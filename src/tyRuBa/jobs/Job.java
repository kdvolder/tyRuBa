package tyRuBa.jobs;

/**
 * TyRuBa will create an instance of this class to represent
 * long running Jobs. The jobs are dispatched to a JobRunner
 * instance. Normally the JobRunner instance simply calls
 * the Job's run method in the current trhead 
 * with a NullProgressMonitor.
 * <p>
 * However, the user of the TyRuBa engine can provide their own
 * JobRunner that, for example use the ProgressMonitor to hook
 * the Job up to a UI progress monitor dialog.
 * <p>
 * @author kdvolder
 */
public abstract class Job {
	
	public abstract void run(ProgressMonitor mon);
	
	
}
