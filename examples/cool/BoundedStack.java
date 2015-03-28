package examples.cool;


 class BoundedStack 
   
   
  { public BoundedStack() {}
   public void print() {
    while (true) {
      synchronized ( this ) {
        if ((COOLBUSY_pop == 0) && (COOLBUSY_push == 0) && (COOLBUSY_print == 0)) { 
           
           break; } }
      try { wait ( ) ; }
      catch ( InterruptedException COOLe ) { } }
    try {
      System.out.print("[");
      for (int i=0;i<pos;i++) {
         System.out.print(contents[i]+" ");
      }
      System.out.print("]");
   }
    finally {
      synchronized(this) {
        
        notifyAll();} }
}
   public Object peek() {
    while (true) {
      synchronized ( this ) {
        if ((COOLBUSY_pop == 0) && (COOLBUSY_push == 0)) { 
           
           break; } }
      try { wait ( ) ; }
      catch ( InterruptedException COOLe ) { } }
    try { 
     return contents[pos]; }
    finally {
      synchronized(this) {
        
        notifyAll();} }
}
   public Object pop() {
    while (true) {
      synchronized ( this ) {
        if ((!empty()) && (COOLBUSY_pop == 0) && (COOLBUSY_push == 0) && (COOLBUSY_print == 0) && (COOLBUSY_full == 0) && (COOLBUSY_empty == 0) && (COOLBUSY_peek == 0)) { 
           
           break; } }
      try { wait ( ) ; }
      catch ( InterruptedException COOLe ) { } }
    try { 
     return contents[--pos]; }
    finally {
      synchronized(this) {
        
        notifyAll();} }
}
   public void push(Object e) {
    while (true) {
      synchronized ( this ) {
        if ((!full()) && (COOLBUSY_pop == 0) && (COOLBUSY_push == 0) && (COOLBUSY_print == 0) && (COOLBUSY_full == 0) && (COOLBUSY_empty == 0) && (COOLBUSY_peek == 0)) { 
           
           break; } }
      try { wait ( ) ; }
      catch ( InterruptedException COOLe ) { } }
    try { 
     contents[pos++]=e; }
    finally {
      synchronized(this) {
        
        notifyAll();} }
}  
   public boolean empty() {
    while (true) {
      synchronized ( this ) {
        if ((COOLBUSY_pop == 0) && (COOLBUSY_push == 0)) { 
           
           break; } }
      try { wait ( ) ; }
      catch ( InterruptedException COOLe ) { } }
    try { 
      return pos==0;}
    finally {
      synchronized(this) {
        
        notifyAll();} }
}
   public boolean full() {
    while (true) {
      synchronized ( this ) {
        if ((COOLBUSY_pop == 0) && (COOLBUSY_push == 0)) { 
           
           break; } }
      try { wait ( ) ; }
      catch ( InterruptedException COOLe ) { } }
    try {
      return pos==MAX;}
    finally {
      synchronized(this) {
        
        notifyAll();} }
}
  private int COOLBUSY_print = 0; 

  private int COOLBUSY_full = 0; 

  private int COOLBUSY_empty = 0; 

  private int COOLBUSY_peek = 0; 

  private int COOLBUSY_pop = 0; 

  private int COOLBUSY_push = 0; 
int pos = 0;Object[] contents = new Object[MAX];static final int MAX = 10; }
