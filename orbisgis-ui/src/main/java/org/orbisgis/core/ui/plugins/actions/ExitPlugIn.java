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
package org.orbisgis.core.ui.plugins.actions;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.orbisgis.core.Services;
import org.orbisgis.core.ui.pluginSystem.AbstractPlugIn;
import org.orbisgis.core.ui.pluginSystem.PlugInContext;
import org.orbisgis.core.ui.pluginSystem.workbench.Names;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchContext;
import org.orbisgis.core.ui.preferences.lookandfeel.OrbisGISIcon;
import org.orbisgis.core.workspace.OrbisGISWorkspace;
import org.orbisgis.utils.I18N;

public class ExitPlugIn extends AbstractPlugIn {

	private JButton btn;
	private JMenuItem menuItem;

	public ExitPlugIn() {
		btn = new JButton(OrbisGISIcon.EXIT_ICON);
		btn.setToolTipText(Names.EXIT);
	}

	public void initialize(PlugInContext context) throws Exception {
		WorkbenchContext wbcontext = context.getWorkbenchContext();
		wbcontext.getWorkbench().getFrame().getMainToolBar().addPlugIn(this,
				btn, context);
		menuItem = context.getFeatureInstaller().addMainMenuItem(this,
				new String[] { Names.FILE }, Names.EXIT, false,
				OrbisGISIcon.EXIT_ICON, null, null, context);
	}

	public boolean execute(PlugInContext context) throws Exception {
		openExitDialog(context.getWorkbenchContext().getWorkbench().getFrame());
		return true;
	}

	public static void openExitDialog(Component component) {
		int answer = JOptionPane.showConfirmDialog(component,I18N.getString("orbisgis.org.orbisgis.core.exit"),
				"OrbisGIS", JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			OrbisGISWorkspace psm = (OrbisGISWorkspace) Services
					.getService(OrbisGISWorkspace.class);
			psm.stopPlugins();
		}
	}

	public boolean isEnabled() {
		btn.setVisible(true);
		menuItem.setVisible(true);
		return true;
	}
}
