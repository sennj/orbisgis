/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-1012 IRSTV (FR CNRS 2488)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.core.renderer.legend.carto;

import org.gdms.data.values.Value;
import org.orbisgis.core.renderer.symbol.Symbol;

public interface UniqueValueLegend extends ClassifiedLegend {

	/**
	 * Adds a classification to the legend
	 * 
	 * @param value
	 *            Classification value
	 * @param symbol
	 *            Symbol to draw the features that is equal the specified
	 *            classification value
	 * @param label
	 *            Human readable description for the classification
	 */
	void addClassification(Value value, Symbol symbol, String label);

	/**
	 * Gets the value of the i-th classification
	 * 
	 * @param index
	 * @return
	 */
	Value getValue(int index);

	/**
	 * Sets the label for the specified classification
	 * 
	 * @param index
	 * @param value
	 */
	void setValue(int index, Value value);

}
