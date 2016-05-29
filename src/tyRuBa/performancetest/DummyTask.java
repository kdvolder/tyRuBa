//package tyRuBa.performancetest;
//import ca.ubc.performancetest.Task;
//
//
//public class DummyTask extends Task {
//
//	private int result;
//	private long waitTime;
//
//	public DummyTask(long waitTime,String name) {
//		super(name);
//		this.waitTime = waitTime;
//	}
//	
//	@Override
//	public synchronized void run() {
//		try {
//			wait(waitTime);
//		} catch (InterruptedException e) {
//		}
//	}
//
//}
