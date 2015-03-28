package tyRuBa.engine.factbase.berkeley_db;

import tyRuBa.modes.BindingList;
import annotations.Feature;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.SecondaryKeyCreator;

@Feature(names="./BDB")
public abstract class RBSecondaryKeyCreator implements SecondaryKeyCreator {

	protected EntryBinding dataBinding;

	public RBSecondaryKeyCreator(BerkeleyDBFactBase env, BindingList bindings) {
		this.dataBinding = env.getDataBinding();
	}
	
}
