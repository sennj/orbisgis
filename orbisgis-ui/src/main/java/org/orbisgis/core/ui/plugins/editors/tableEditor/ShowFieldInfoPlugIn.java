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
package org.orbisgis.core.ui.plugins.editors.tableEditor;

import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Constraint;
import org.gdms.data.types.ConstraintFactory;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.driver.DriverException;
import org.orbisgis.core.Services;
import org.orbisgis.core.ui.editor.IEditor;
import org.orbisgis.core.ui.editors.table.TableEditableElement;
import org.orbisgis.core.ui.pluginSystem.AbstractPlugIn;
import org.orbisgis.core.ui.pluginSystem.PlugInContext;
import org.orbisgis.core.ui.pluginSystem.message.ErrorMessages;
import org.orbisgis.core.ui.pluginSystem.workbench.Names;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchContext;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchFrame;
import org.orbisgis.core.ui.plugins.views.output.OutputManager;
import org.orbisgis.core.ui.preferences.lookandfeel.OrbisGISIcon;
import org.orbisgis.utils.I18N;

public class ShowFieldInfoPlugIn extends AbstractPlugIn {

	public boolean execute(PlugInContext context) throws Exception {
		IEditor editor = context.getActiveEditor();
		final TableEditableElement element = (TableEditableElement) editor
				.getElement();
		try {
			Metadata metadata = element.getDataSource().getMetadata();
			OutputManager om = Services.getService(OutputManager.class);
			om
					.print(I18N
							.getString("orbisgis.org.orbisgis.core.ui.plugins.editors.tableEditor.fieldName")
							+ metadata.getFieldName(getSelectedColumn()) + "\n");
			Type type = metadata.getFieldType(getSelectedColumn());
			om
					.print(I18N
							.getString("orbisgis.org.orbisgis.core.ui.plugins.editors.tableEditor.fieldType")
							+ TypeFactory.getTypeName(type.getTypeCode())
							+ "\n"
							+ I18N
									.getString("orbisgis.org.orbisgis.core.ui.plugins.editors.tableEditor.fieldConstraints")
							+ ":\n");
			Constraint[] cons = type.getConstraints();
			for (Constraint constraint : cons) {
				om.print("  "
						+ ConstraintFactory.getConstraintName(constraint
								.getConstraintCode()) + ": "
						+ constraint.getConstraintHumanValue() + "\n");
			}

		} catch (DriverException e) {
			ErrorMessages.error(ErrorMessages.CannotAccessFieldInformation, e);
		}
		return true;
	}

	public void initialize(PlugInContext context) throws Exception {
		WorkbenchContext wbContext = context.getWorkbenchContext();
		WorkbenchFrame frame = wbContext.getWorkbench()
				.getFrame().getTableEditor();
		context.getFeatureInstaller().addPopupMenuItem(frame, this,
				new String[] { Names.POPUP_TABLE_SHOWFIELD_PATH1 },
				Names.POPUP_TABLE_SHOWFIELD_GROUP, false,
				OrbisGISIcon.INFO, wbContext);
	}

	public boolean isEnabled() {
		return (getPlugInContext().getTableEditor() != null)
				&& (getSelectedColumn() != -1);
	}
}
