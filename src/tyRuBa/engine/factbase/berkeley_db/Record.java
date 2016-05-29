package tyRuBa.engine.factbase.berkeley_db;

import tyRuBa.engine.RBTuple;

public class Record {
	
	public RBTuple data;
	public long handle;

	public Record(RBTuple data,long validatorHandle) {
		this.data = data;
		this.handle = validatorHandle;
	}
	
	@Override
	public String toString() {
		return "RECORD("+data+", "+handle+")";
	}

}
