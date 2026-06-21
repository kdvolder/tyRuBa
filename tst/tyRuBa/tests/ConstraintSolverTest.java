package tyRuBa.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import tyRuBa.engine.RBVariable;
import tyRuBa.modes.Factory;
import tyRuBa.modes.ListType;
import tyRuBa.modes.TVar;
import tyRuBa.modes.Type;
import tyRuBa.modes.ConstraintSolver;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;

/**
 * Tests for the ConstraintSolver component.
 * The ConstraintSolver is a replacement for TypeEnv that handles
 * type constraints (lower bounds, upper bounds, equalities)
 * as a unified constraint system.
 */
public class ConstraintSolverTest {

	@Test public void emptySolverSolvesToEmptyTypeEnv() throws TypeModeError {
		TypeEnv env = Factory.makeConstraintSolver().solve();
		assertTrue(env.isEmpty(), "Empty solver should produce empty TypeEnv");
	}
	
	@Test public void singleTypeConstraintSolvesToBoundType() throws TypeModeError {
		RBVariable var = RBVariable.make("?a");
		Type intType = Type.integer;
		
		TypeEnv env = Factory.makeConstraintSolver()
			.assertSubtypeOf(var, intType)
			.solve();
		
		Type solvedType = env.get(var);
		assertEquals(intType, solvedType, "Subtype constraint should narrow to the bound type");
	}
	
	@Test public void twoSubtypeConstraintsNarrowsToMostSpecific() throws TypeModeError {
		RBVariable var = RBVariable.make("?a");
		Type intType = Type.integer;
		Type numType = Factory.makeJavaType(Number.class);
		
		TypeEnv env = Factory.makeConstraintSolver()
			.assertSubtypeOf(var, numType)
			.assertSubtypeOf(var, intType)
			.solve();
		
		Type solvedType = env.get(var);
		assertEquals(intType, solvedType, "Two subtype constraints should narrow to the most specific (Integer)");
	}
	
	@Test public void twoSupertypeConstraintsNarrowsToMostGeneral() throws TypeModeError {
		RBVariable var = RBVariable.make("?a");
		Type intType = Type.integer;
		Type numType = Factory.makeJavaType(Number.class);
		
		TypeEnv env = Factory.makeConstraintSolver()
			.assertSuperTypeOf(var, numType)
			.assertSuperTypeOf(var, intType)
			.solve();
		
		Type solvedType = env.get(var);
		assertEquals(numType, solvedType, "Two supertype constraints should narrow to the most general (Number)");
	}
	
	@Test public void incompatibleSubtypeConstraintsThrowTypeModeError() {
		RBVariable var = RBVariable.make("?a");
		Type intType = Type.integer;
		Type strType = Factory.makeJavaType(String.class);
		
		TypeModeError error = assertThrows(TypeModeError.class, () ->
			Factory.makeConstraintSolver()
				.assertSubtypeOf(var, intType)
				.assertSubtypeOf(var, strType)
				.solve()
		);
		
		assertEquals("Incompatible types: Integer<>, String<>", error.getMessage());
	}
	
	@Test public void supertypeOfNumberPlusSubtypeOfIntegerIsIncompatible() {
		RBVariable var = RBVariable.make("?a");
		Type numType = Factory.makeJavaType(Number.class);
		Type intType = Type.integer;
		
		TypeModeError error = assertThrows(TypeModeError.class, () ->
			Factory.makeConstraintSolver()
				.assertSuperTypeOf(var, numType)
				.assertSubtypeOf(var, intType)
				.solve()
		);
		
		assertEquals("Incompatible constraints: Number<> <: Integer<>", error.getMessage());
	}
	
	@Test public void intersectingListsWithTypeVarDoesNotMutateTypeVar() throws TypeModeError {
		// Scenario: ?x is a list whose element type is unknown (T).
		// Then we learn ?x is also a list of strings.
		// The solver should narrow ?x to [*]<String> and record T <: String.
		TVar tVar = Factory.makeTVar("T");
		RBVariable x = RBVariable.make("?x");
		
		ConstraintSolver solver = Factory.makeConstraintSolver();
		solver.assertSubtypeOf(x, ListType.make(tVar));
		solver.assertSubtypeOf(x, ListType.make(Type.string));
		
		// Before solve: the constraint should be recorded in the solver's store
		assertTrue(solver.isConstrained(tVar), "Solver should record constraint on T before solve");
		
		TypeEnv env = solver.solve();
		
		// ?x should be narrowed to [*]<String>
		Type solvedX = env.get(x);
		assertEquals(solvedX, ListType.make(Type.string), "?x should be narrowed to list of strings");
		
		// After solve: the solver still knows T's constraint
		assertTrue(solver.isConstrained(tVar), "Solver should still record constraint on T after solve");
	}
	
	@Test public void intersectingNestedListsWithTypeVarNarrowsInnerArg() throws TypeModeError {
		// Scenario: ?x is a list of lists whose inner element type is unknown (T).
		// Then we learn ?x is also a list of lists of strings.
		// The solver should narrow ?x to [*]<[*]<String>> and record T <: String.
		TVar tVar = Factory.makeTVar("T");
		Type innerList1 = ListType.make(tVar);
		Type outerList1 = ListType.make(innerList1);
		
		Type innerList2 = ListType.make(Type.string);
		Type outerList2 = ListType.make(innerList2);
		
		RBVariable x = RBVariable.make("?x");
		ConstraintSolver solver = Factory.makeConstraintSolver();
		solver.assertSubtypeOf(x, outerList1);
		solver.assertSubtypeOf(x, outerList2);
		
		// Before solve: the constraint should be recorded in the solver's store
		assertTrue(solver.isConstrained(tVar), "Solver should record constraint on T before solve");
		
		TypeEnv env = solver.solve();
		
		// ?x should be narrowed to [*]<[*]<String>>
		Type solvedX = env.get(x);
		assertEquals(solvedX, outerList2, "?x should be narrowed to list of lists of strings");
		
		// After solve: the solver still knows T's constraint
		assertTrue(solver.isConstrained(tVar), "Solver should still record constraint on T after solve");
	}
}
