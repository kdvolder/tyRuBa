package tyRuBa.engine.factbase.berkeley_db;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.SecondaryKeyCreator;

import tyRuBa.modes.BindingList;

public abstract class RBSecondaryKeyCreator implements SecondaryKeyCreator {

	protected EntryBinding dataBinding;

	public RBSecondaryKeyCreator(BerkeleyDBFactBase env, BindingList bindings) {
		this.dataBinding = env.getDataBinding();
	}
	
}
