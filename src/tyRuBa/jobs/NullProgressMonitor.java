package tyRuBa.jobs;

/**
 * NullProgresMonitor is a dummy "empty" progressMonitor that
 * doesn't do anything. 
 * 
 * @author kdvolder
 */
public class NullProgressMonitor implements ProgressMonitor {

	public void beginTask(String name, int totalWork) {
	}

	public void done() {
	}

	public boolean isCanceled() {
		return false;
	}

	public void worked(int units) {
	}

}
