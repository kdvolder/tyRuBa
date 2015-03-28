/*
 * Created on May 4, 2004
 */
package tyRuBa.engine.factbase;

import annotations.Export;
import tyRuBa.engine.RBComponent;
import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.modes.PredicateMode;

/**
 * A Fact Database.
 * @category FactBase
 * @author riecken
 */
public abstract class FactBase {

    /**
     * Returns true if this factbase is persisted to disk somehow.
     */
    @Export(to="./BDB")
    public abstract boolean isPersistent();

    /**
     * Persists the factbase to disk (if persistent, otherwise does nothing).
     */
    @Export(to="./BDB")
    public abstract void backup();

    /**
     * Adds a fact to the database.
     * @param f the fact to add to the factbase.
     */
    @Export(to="./BDB")
    public abstract void insert(RBComponent f);

    /**
     * Returns true if this factbase is empty.
     */
    @Export(to="./BDB")
    public abstract boolean isEmpty();

    /**
     * Compiles this FactBase. Wraps the basicCompile method to fail if this
     * FactBase is empty.
     * @param mode the mode that this factbase is being compiled for.
     * @param context compilation context.
     */
    public final Compiled compile(PredicateMode mode, CompilationContext context) {
        if (this.isEmpty()) {
            return Compiled.fail;
        } else
            return this.basicCompile(mode, context);
    }

    /**
     * Actually compiles this FactBase.
     * @param mode the mode that this factbase is being compiled for.
     * @param context compilation context.
     */
    @Export(to="./BDB")
    public abstract Compiled basicCompile(PredicateMode mode, CompilationContext context);
}