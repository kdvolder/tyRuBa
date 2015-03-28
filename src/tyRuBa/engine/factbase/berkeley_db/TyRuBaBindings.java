package tyRuBa.engine.factbase.berkeley_db;

import java.util.HashMap;
import java.util.Map;

import com.sleepycat.bind.EntityBinding;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import annotations.Feature;
import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTuple;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeModeError;
import tyRuBa.tests.TyrubaTest;

/**
 * An instance of this class exists for every BDBBasedPersistence strategy object.
 * <P>
 * It is responsible for managing and creating BDB EntryBindings for TyRuBa Types
 * so that TyRuBa terms can be stored in BDB databases.
 *  
 * @author kdvolder
 */
@Feature(names="./BDB")
public class TyRuBaBindings {
	
	Map<Type,TupleBinding> bindings = new HashMap<Type, TupleBinding>(); 
	
	Map<Type,TupleBinding> recordBindings = new HashMap<Type, TupleBinding>(); 
	
	private Type stringType, intType;

	private BerkeleyDBBasedPersistence env;

	protected FrontEnd frontend;

	public TyRuBaBindings(BerkeleyDBBasedPersistence env) {
		this.env = env;
		this.frontend = env.getTyRuBaFrontend();
		try {
			stringType = frontend.findType("String");
			intType = frontend.findType("Integer");
			bindings.put(stringType,new TyRuBaStringBinding());
			bindings.put(intType,new TyRuBaIntegerBinding());
		} catch (TypeModeError e) {
			throw new Error(e);
		}
		
	}
	
	public TupleBinding get(Type type) {
		TupleBinding b = bindings.get(type);
		if (b==null) {
			b = makeEntryBinding(type);
			bindings.put (type, b);
		}
		return b;
	}

	protected TupleBinding makeEntryBinding(Type type) {
		System.err.println("Warning: Inefficient serial binding for "
				+type);
		return new TyRuBaSerialBinding(env.getClassCat(),RBTerm.class);
	}

	public EntryBinding getRecordBinding(Type type) {
		TupleBinding b = recordBindings.get(type);
		if (b==null) {
			b = makeRecordBinding(type);
			recordBindings.put (type, b);
		}
		return b;
	}

	private TupleBinding makeRecordBinding(Type type) {
		final TupleBinding termBinding = get(type); 
		return new TupleBinding() {

			@Override
			public Object entryToObject(TupleInput input) {
				RBTuple data = (RBTuple) termBinding.entryToObject(input);
				long handle = input.readLong();
				return new Record(data,handle);
			}

			@Override
			public void objectToEntry(Object object, TupleOutput output) {
				Record r = (Record) object;
				termBinding.objectToEntry(r.data, output);
				output.writeLong(r.handle);
			}
			
		};
	}

}
