/* A simple test program to test the BoundedStack: a conusmer and a producer
  getting and putting elements from/to the stack. */
  
class Consumer extends Thread {
  public void run() {
    Object hold;
    for (int i=1;i<100;i++) {
      synchronized(Test.st) {
        hold = Test.st.pop();
        System.out.print("C: ");
        System.out.println(hold);
      }
    }
  }
}

class Producer extends Thread {
  public void run() {
    for (int i=1;i<100;i++) {
      synchronized(Test.st) {
        Test.st.push(""+i);
        System.out.println("P: "+i);
      }
    }
  }
}

public class Test {

  static BoundedStack st = new BoundedStack();

  static Thread consumer = new Consumer();
  static Thread producer = new Producer();

  public static void main(String[] args) {
    producer.start();
    consumer.start();
  }

}
