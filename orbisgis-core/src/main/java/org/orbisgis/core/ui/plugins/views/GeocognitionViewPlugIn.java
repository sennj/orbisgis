/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. OrbisGIS is
 * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
 *
 *
 *  Team leader Erwan BOCHER, scientific researcher,
 *
 *  User support leader : Gwendall Petit, geomatic engineer.
 *
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * Copyright (C) 2010 Erwan BOCHER, Pierre-Yves FADET, Alexis GUEGANNO, Maxence LAURENT
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
 *
 * or contact directly:
 * erwan.bocher _at_ ec-nantes.fr
 * gwendall.petit _at_ ec-nantes.fr
 */
package org.orbisgis.core.ui.plugins.views;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JMenuItem;

import org.orbisgis.core.PersistenceException;
import org.orbisgis.core.ui.pluginSystem.PlugInContext;
import org.orbisgis.core.ui.pluginSystem.ViewPlugIn;
import org.orbisgis.core.ui.pluginSystem.workbench.Names;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchContext;
import org.orbisgis.core.ui.plugins.views.geocognition.GeocognitionView;
import org.orbisgis.core.ui.preferences.lookandfeel.OrbisGISIcon;

public class GeocognitionViewPlugIn extends ViewPlugIn {

	private GeocognitionView panel;
	private JMenuItem menuItem;
	private JButton btn;

	public GeocognitionViewPlugIn() {
		btn = new JButton(OrbisGISIcon.GEOCOGNITION_ICON);
		btn.setToolTipText(Names.GEOCOGNITION);
	}

	public GeocognitionView getPanel() {
		return panel;
	}

	public void delete() {
		panel.delete();
	}

	public Component getComponent() {
		return panel;
	}

	public void initialize(PlugInContext context) throws Exception {
		panel = new GeocognitionView();
		panel.initialize();
		menuItem = context.getFeatureInstaller().addMainMenuItem(this,
				new String[] { Names.VIEW }, Names.GEOCOGNITION, true,
				OrbisGISIcon.GEOCOGNITION_ICON, null, panel, context);
		WorkbenchContext wbcontext = context.getWorkbenchContext();
		wbcontext.getWorkbench().getFrame().getViewToolBar().addPlugIn(this,
				btn, context);
	}

	public boolean execute(PlugInContext context) throws Exception {
		getPlugInContext().loadView(getId());
		return true;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isSelected() {
		boolean isSelected = false;
		isSelected = getPlugInContext().viewIsOpen(getId());
		menuItem.setSelected(isSelected);
		return isSelected;
	}

	public void loadStatus() throws PersistenceException {
		panel.loadStatus();
	}

	public void saveStatus() throws PersistenceException {
		panel.saveStatus();
	}

	public String getName() {
		return "Geocognition view";
	}
}
