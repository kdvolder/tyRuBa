package tyRuBa.engine.visitor;

import tyRuBa.engine.Frame;

public abstract class SubstituteOrInstantiateVisitor extends CopyVisitor {

	Frame frame;

	public SubstituteOrInstantiateVisitor(Frame frame) {
		this.frame = frame;
	}
	
	public Frame getFrame() {
		return frame;
	}


}
