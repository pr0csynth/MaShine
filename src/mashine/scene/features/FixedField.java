package mashine.scene.features;

import mashine.scene.*;

public final class FixedField extends Feature {
	public FixedField(String fieldName, int fixedValue){
		super("fixed", 1);
		fields.put(fieldName, fixedValue);
	}

	public FixedField(FixedField f){
		super(f);
	}

	public FixedField(Feature f){
		super(f);
	}
}