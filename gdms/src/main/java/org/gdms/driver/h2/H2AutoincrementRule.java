package org.gdms.driver.h2;

import org.gdms.data.types.Constraint;
import org.gdms.data.types.Type;
import org.gdms.driver.jdbc.AbstractConversionRule;
import org.gdms.driver.jdbc.ConversionRule;

public class H2AutoincrementRule extends AbstractConversionRule implements
		ConversionRule {

	public boolean canApply(Type type) {
		return (type.getTypeCode() == Type.INT)
				&& type.getBooleanConstraint(Constraint.AUTO_INCREMENT);
	}

	public String getTypeName() {
		return "identity";
	}

	public int[] getValidConstraints() {
		return null;
	}

	@Override
	protected int getOutputTypeCode() {
		return Type.INT;
	}

}
