/*
 * The GDMS library (Generic Datasources Management System)
 * is a middleware dedicated to the management of various kinds of
 * data-sources such as spatial vectorial data or alphanumeric. Based
 * on the JTS library and conform to the OGC simple feature access
 * specifications, it provides a complete and robust API to manipulate
 * in a SQL way remote DBMS (PostgreSQL, H2...) or flat files (.shp,
 * .csv...). GDMS is produced  by the geomatic team of the IRSTV
 * Institute <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
 *    Erwan BOCHER, scientific researcher,
 *    Thomas LEDUC, scientific researcher,
 *    Fernando GONZALEZ CORTES, computer engineer.
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * This file is part of GDMS.
 *
 * GDMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GDMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GDMS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult:
 *    <http://orbisgis.cerma.archi.fr/>
 *    <http://sourcesup.cru.fr/projects/orbisgis/>
 *    <http://listes.cru.fr/sympa/info/orbisgis-developers/>
 *    <http://listes.cru.fr/sympa/info/orbisgis-users/>
 *
 * or contact directly:
 *    erwan.bocher _at_ ec-nantes.fr
 *    fergonco _at_ gmail.com
 *    thomas.leduc _at_ cerma.archi.fr
 */
package org.gdms.data.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.gdms.data.values.Value;

public class DefaultType implements Type {
	private Constraint[] constraints;

	private int typeCode;

	public static Map<Integer, String> typesDescription = new HashMap<Integer, String>();

	static {
		java.lang.reflect.Field[] fields = Type.class.getFields();
		for (int i = 0; i < fields.length; i++) {
			try {
				typesDescription.put((Integer) fields[i].get(null), fields[i]
						.getName());
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}
	}

	/**
	 * @param description
	 * @param typeCode
	 * @throws InvalidTypeException
	 */
	DefaultType(final int typeCode) throws InvalidTypeException {
		this(new Constraint[0], typeCode);
	}

	/**
	 * @param constraints
	 * @param description
	 * @param typeCode
	 * @throws InvalidTypeException
	 */
	DefaultType(final Constraint[] constraints, final int typeCode) {
		if (null == constraints) {
			this.constraints = new Constraint[0];
		} else {
			this.constraints = constraints;
		}
		this.typeCode = typeCode;

		// In case of a geometric type, the GeometryConstraint is mandatory
		if (Type.GEOMETRY == typeCode) {
			if (null == getConstraint(Constraint.GEOMETRY_TYPE)) {
				final List<Constraint> lc = new LinkedList<Constraint>(Arrays
						.asList(constraints));
				lc.add(new GeometryConstraint(GeometryConstraint.MIXED));
				this.constraints = (Constraint[]) lc.toArray(new Constraint[lc
						.size()]);
			}
		}
	}

	/**
	 * @see org.gdms.data.types.Type#getConstraints()
	 */
	public Constraint[] getConstraints() {
		return constraints;
	}

	/**
	 * @see org.gdms.data.types.Type#getTypeCode()
	 */
	public int getTypeCode() {
		return typeCode;
	}

	public String check(final Value value) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getConstraintValue(final int constraint) {
		final Constraint c = getConstraint(constraint);
		return (null == c) ? null : c.getConstraintValue();
	}

	public boolean isRemovable() {
		for (Constraint c : constraints) {
			if (!c.allowsFieldRemoval()) {
				return false;
			}
		}

		return true;
	}

	public Constraint getConstraint(int constraint) {
		for (Constraint c : constraints) {
			if (c.getConstraintCode() == constraint) {
				return c;
			}
		}
		return null;
	}

	/**
	 * @see org.gdms.data.types.Type#getIntConstraint(org.gdms.data.types.ConstraintNames)
	 */
	public int getIntConstraint(int constraint) {
		String value = getConstraintValue(constraint);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("The constraint cannot "
						+ "be expressed as an int: " + constraint);
			}
		} else {
			return -1;
		}
	}

	public boolean getBooleanConstraint(int constraint) {
		String value = getConstraintValue(constraint);
		if (value != null) {
			return Boolean.parseBoolean(value);
		} else {
			return false;
		}
	}

	public Constraint[] getConstraints(int constraintMask) {
		ArrayList<Constraint> ret = new ArrayList<Constraint>();
		for (Constraint constraint : constraints) {
			if ((constraint.getConstraintCode() & constraintMask) > 0) {
				ret.add(constraint);
			}
		}

		return ret.toArray(new Constraint[0]);
	}
}