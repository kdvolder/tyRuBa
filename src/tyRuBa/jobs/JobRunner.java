package tyRuBa.jobs;

public class JobRunner {
	
	public void run(Job job) {
		job.run(new NullProgressMonitor());
	}

}
