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
package org.orbisgis.core.ui.components.sif;

import java.util.ArrayList;

import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Type;
import org.gdms.driver.DriverException;
import org.orbisgis.core.sif.multiInputPanel.ComboBoxChoice;

public class NumericFieldLayerCombo extends ComboBoxChoice {
	public NumericFieldLayerCombo(Metadata metadata) throws DriverException {

		int fieldCount = metadata.getFieldCount();

		ArrayList<String> fieldNames = new ArrayList<String>();

		for (int i = 0; i < fieldCount; i++) {

			if (metadata.getFieldType(i).getTypeCode() == Type.INT) {

				fieldNames.add(metadata.getFieldName(i));
			}

			else if (metadata.getFieldType(i).getTypeCode() == Type.SHORT) {

				fieldNames.add(metadata.getFieldName(i));
			}

			else if (metadata.getFieldType(i).getTypeCode() == Type.DOUBLE) {

				fieldNames.add(metadata.getFieldName(i));
			}

			else if (metadata.getFieldType(i).getTypeCode() == Type.FLOAT) {

				fieldNames.add(metadata.getFieldName(i));
			}

			else if (metadata.getFieldType(i).getTypeCode() == Type.LONG) {

				fieldNames.add(metadata.getFieldName(i));
			}

		}

		if (fieldNames.size() > 0) {
			setChoices(fieldNames.toArray(new String[0]));
		} else {
			setChoices(new String[] { "No numeric field" });
		}
	}
}