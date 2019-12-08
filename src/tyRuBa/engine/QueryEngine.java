package tyRuBa.engine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;

import tyRuBa.engine.factbase.NamePersistenceManager;
import tyRuBa.engine.factbase.PersistenceStrategy;
import tyRuBa.engine.factbase.ValidatorManager;
import tyRuBa.modes.CompositeType;
import tyRuBa.modes.ConstructorType;
import tyRuBa.modes.Factory;
import tyRuBa.modes.PredInfo;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeConstructor;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeMapping;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.parser.TyRuBaParser;
import tyRuBa.tdbc.PreparedInsert;
import tyRuBa.tdbc.PreparedQuery;
import tyRuBa.util.Action;
import tyRuBa.util.ElementSource;
import tyRuBa.util.NullQueryLogger;
import tyRuBa.util.QueryLogger;

/**
 * @author kdvolder
 */
public abstract class QueryEngine {

	/** Every query engine has a logger. The default logger does nothing. */
	private QueryLogger logger = NullQueryLogger.the;
	
	public void setLogger(QueryLogger logger) {
		if (logger==null) 
			logger = NullQueryLogger.the;
		synchronized (frontend()) {
			this.logger.close();
			this.logger = logger;
		}
	}
	
	public void insert(RBComponent r) throws TypeModeError {
		frontend().updateCounter++;
		rulebase().insert(r);
	}

	public void insertPredInfo(PredInfo p) throws TypeModeError {
		rulebase().insert(p);
	}

	public void insert(RBPredicateExpression exp) throws TypeModeError {
		insert(new RBFact(exp));
	}

	/** Every QueryEngine must have a frontend */
	public abstract FrontEnd frontend();

	public abstract ModedRuleBaseIndex rulebase();

	public PrintStream output() {
		return frontend().os;
	}

	/** 
	 * Gets the storage path for this query engine (will be where
	 * the factbase is stored
	 */
	public abstract String getStoragePath();
	
	public abstract PersistenceStrategy getPersistenceStrategy();

	public abstract String getIdentifier();

	public long getFrontEndCacheSize() {
		return frontend().getCacheSize();
	}

	public ValidatorManager getFrontEndValidatorManager() {
		return frontend().getValidatorManager();
	}

	public NamePersistenceManager getFrontendNamePersistenceManager() {
		return frontend().getNamePersistenceManager();
	}

	public long getFrontEndLastBackupTime() {
		return frontend().getLastBackupTime();
	}

	/** Load a file from the default library directory (stored somewhere
	 in the classpath */
	public void loadLibrary(String fileName) throws ParseException,
			IOException, TypeModeError {
		URL initfile = ClassLoader.getSystemClassLoader().getResource(
				"lib/" + fileName);
		load(initfile);
	}

	/** Parse a file into this database */
	public void load(String fileName) throws ParseException, IOException,
			TypeModeError {
		System.err.println("** loading : " + fileName);
		TyRuBaParser.parse(this, fileName, output());
	}

	/** Parse a file into this database */
	public void load(InputStream input) throws IOException, ParseException,
			TypeModeError {
		TyRuBaParser.parse(this, input, output());
	}

	/** Parse from a URL */
	public void load(URL url) throws ParseException, IOException, TypeModeError {
		TyRuBaParser.parse(this, url, output());
	}

	/** Parse from a string and execute/insert in this rulebase */
	public void parse(String input) throws ParseException, TypeModeError {
		parse(input, System.err);
	}

	/** Parse and interpret from a string */
	public void parse(String input, PrintStream output) throws ParseException,
			TypeModeError {
		TyRuBaParser.parse(this, new ByteArrayInputStream(input.getBytes()),
				output);
	}

	/** Solve a query and return an enumeration of frames with 
	 bindings for the values in the query */
	public ElementSource frameQuery(RBExpression e) throws TypeModeError,
			ParseException {
		final PreparedQuery runable = e.prepareForRunning(this,true);
		//		System.out.println("--- query result structure dump ----");
		//		System.out.println(result);
		//		System.out.println("------------------------------------");
		return runable.start();
	}

	/** Solve a query and return an enumeration of frames with 
	 bindings for the values in the query */
	public ElementSource frameQuery(String q) throws ParseException,
			TypeModeError {
		RBExpression exp = TyRuBaParser.parseExpression(
				new ByteArrayInputStream(q.getBytes()), System.err, this);
		return frameQuery(exp);
	}

	/** Solve a query and return the value it finds for a given variable */
	public ElementSource varQuery(RBExpression e, final RBVariable v)
			throws TypeModeError, ParseException {
		return frameQuery(e).map(new Action() {
			public Object compute(Object f) {
				return ((Frame) f).get(v);
			}
		});
	}

	/** Shortcut to get "property" of an object stored as
	 *  a fact. Returns null, if no such property for the object
	 *  is in the db.
	 * 
	 *  properties are retrieved like this:
	 * 
	 *  propertyName(?Object,?PropVal). 
	 */
	public RBTerm getProperty(Object obj, String propertyName) {
		try {
			RBVariable propval = FrontEnd.makeVar("?propval");
			RBTerm objTerm = obj instanceof RBTerm ? (RBTerm) obj
					: makeJavaObject(obj);
			RBExpression query = FrontEnd.makePredicateExpression(propertyName,
					new RBTerm[] { objTerm, propval });
			ElementSource result = varQuery(query, propval);
			if (!result.hasMoreElements())
				return null;
			else {
				RBTerm val = (RBTerm) result.nextElement();
				result.release();
				return val;
			}
		} catch (TypeModeError e) {
			e.printStackTrace();
			throw new Error("Problem calling predicate: " + e.getMessage());
		} catch (ParseException e) {
			e.printStackTrace();
			throw new Error("Problem calling predicate: " + e.getMessage());
		}
	}

	/**
	 * like getProperty, but convert result to a String
	 */
	public String getStringProperty(Object obj, String propertyName) {
		RBTerm res = getProperty(obj, propertyName);
		if (res==null)
			return "null";
		else {
			Object upped = res.up();
			return upped.toString();
		}
	}

	/**
	 * like getProperty, but convert result to an int (must be RBJavaObject with
	 * an Integer inside or will throw some nasty execeptions)
	 */
	public int getIntProperty(Object obj, String propertyName) {
		RBTerm prop = getProperty(obj, propertyName);
		if (prop == null)
			return 0;
		else
			return prop.intValue();
	}

	/** Solve a query and return the value it finds for a given variable */
	public ElementSource varQuery(String qs, String vs) throws ParseException,
			TypeModeError {
		RBVariable v = RBVariable.make(vs);
		RBExpression q = TyRuBaParser.parseExpression(new ByteArrayInputStream(
				qs.getBytes()), System.err, this);
		return varQuery(q, v);
	}

	public void dumpFacts(PrintStream out) {
		rulebase().dumpFacts(out);
	}

	/** Parse and interpret from a string */
	public RBExpression makeExpression(String expression)
			throws ParseException, TypeModeError {
		TyRuBaParser parser = new TyRuBaParser(new ByteArrayInputStream(
				expression.getBytes()), System.err, null);
		return parser.Expression(this);
	}

	private RBPredicateExpression makePredExpression(String expression)
			throws ParseException, TypeModeError {
		TyRuBaParser parser = new TyRuBaParser(new ByteArrayInputStream(
				expression.getBytes()), System.err, null);
		return parser.SimplePredicate(this);
	}

//	/** Parse and interpret from a string **/
//	//moved from FrontEnd to make available for buckets)
//	public static RBTerm makeTerm(String term) throws ParseException,
//			TypeModeError {
//		//TODO: This method probably should not be used... if used it can not
//		// parse userDefined type objects because those can only be parsed
//		// in the context of a query engine that know about their definitions.
//		TyRuBaParser parser = new TyRuBaParser(new ByteArrayInputStream(term
//				.getBytes()), System.err);
//		return parser.Term(new FrontEnd(false));
//	}

	public TypeConstructor findTypeConst(String typeName) throws TypeModeError {
		TypeConstructor result = rulebase().findTypeConst(typeName);
		if (result == null) {
			throw new TypeModeError("Unknown type: " + typeName);
		} else {
			return result;
		}
	}

	public TypeConstructor findTypeConst(String typeName, int arity)
			throws TypeModeError {
		TypeConstructor typeConst = rulebase().findTypeConst(typeName, arity);
		if (typeConst == null) {
			throw new TypeModeError("Unknown composite type: " + typeName
					+ " with arity " + arity);
		} else {
			return typeConst;
		}
	}

	public Type findType(String name) throws TypeModeError {
		TypeConstructor tc = findTypeConst(name);
		return tc.apply(Factory.makeTupleType(), false);
	}

	public ConstructorType findConstructorType(FunctorIdentifier id)
			throws TypeModeError {
		ConstructorType typeConst = rulebase().findConstructorType(id);
		if (typeConst == null) {
			throw new TypeModeError("Unknown functor: " + id);
		} else {
			return typeConst;
		}
	}
	
	public void addTypePredicate(TypeConstructor TypeConstructor,
			ArrayList subTypes) {
		rulebase().addTypePredicate(TypeConstructor, subTypes);
	}

	//	public TypeConstructor addNewType(String typeName) throws TypeModeError {
	//		TypeConstructor result = new UserDefinedTypeConstructor(typeName,0);
	//		rulebase().addType(result);
	//		return result;
	//	}

	public CompositeType addNewType(CompositeType type) throws TypeModeError {
		return rulebase().addType(type);
	}

	public void addFunctorConst(Type repAs, CompositeType type) {
		rulebase().addFunctorConst(repAs, type);
	}

	public PreparedQuery prepareForRunning(RBExpression e, boolean typeCheck) throws TypeModeError, ParseException {
		return e.prepareForRunning(this, typeCheck);
	}

	public PreparedQuery prepareForRunning(String queryTemplate, boolean typeCheck)
			throws ParseException, TypeModeError {
		RBExpression exp = TyRuBaParser.parseExpression(
				new ByteArrayInputStream(queryTemplate.getBytes()), System.err,
				this);
		return prepareForRunning(exp, typeCheck);
	}

	public PreparedInsert prepareForInsertion(String factStr)
			throws ParseException, TypeModeError {
		RBPredicateExpression fact = makePredExpression(factStr);
		TypeEnv tEnv = fact.typecheck(rulebase(), new TypeEnv());
		return new PreparedInsert(this, fact, tEnv);
	}

	//	/** This method cannot be static because it needs to know the
	//		 * evaluation context (rulebase) for the expression */
	//	public RBExpression makeTermExpression(RBTerm t) {
	//		return new PredicateExpression(t, this);
	//	}

	public RBTerm makeTermFromString(String term) throws ParseException,
			TypeModeError {
		TyRuBaParser parser = new TyRuBaParser(new ByteArrayInputStream(term
				.getBytes()), System.err, null);
		return parser.Term(this);
	}

	public void addTypeMapping(FunctorIdentifier id, TypeMapping mapping)
			throws TypeModeError {
		rulebase().addTypeMapping(mapping, id);
	}

	public TypeMapping findTypeMapping(Class forWhat) {
		return rulebase().findTypeMapping(forWhat);
	}

	public RBTerm makeJavaObject(Object _o) throws TypeModeError {
		TypeMapping mapping = findTypeMapping(_o.getClass());
		if (mapping == null) {
			return RBCompoundTerm.makeJava(_o);
		}
		else {
			Object obj_rep = mapping.toTyRuBa(_o);
			RBTerm term_rep = null;
			if (obj_rep instanceof Object[]) {
				Object[] obj_arr = (Object[])obj_rep;
				RBTerm[] term_arr = new RBTerm[obj_arr.length];
				for (int i = 0; i < term_arr.length; i++) {
					term_arr[i] = makeJavaObject(obj_arr[i]);
				}

				ConstructorType consType = mapping.getFunctor();
				Type repType = consType.getTypeConst().getRepresentation();
				if (repType instanceof TupleType) {
					term_rep = FrontEnd.makeTuple(term_arr);
				}
				else
					throw new Error("Cannot convert java object "+term_rep+" to "+ repType);
			}
			else {
				term_rep = makeJavaObject(obj_rep);
			}

			return mapping.getFunctor().apply(term_rep);
		}
	}	
	
	public RBTerm makeTypeCast(TypeConstructor toType, Object value) throws TypeModeError {
		return toType.getConstructorType()
				.apply(RBCompoundTerm.makeJava(value));
	}

//	/**	
//	 * @codegroup metadata
//	 */
//	public abstract void enableMetaData();

}