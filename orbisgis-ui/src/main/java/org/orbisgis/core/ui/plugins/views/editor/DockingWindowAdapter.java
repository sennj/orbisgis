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
package org.orbisgis.core.ui.plugins.views.editor;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.View;

public abstract class DockingWindowAdapter implements DockingWindowListener {

	public void viewFocusChanged(View arg0, View arg1) {

	}

	public void windowAdded(DockingWindow arg0, DockingWindow arg1) {

	}

	public void windowClosed(DockingWindow arg0) {

	}

	public void windowClosing(DockingWindow arg0)
			throws OperationAbortedException {

	}

	public void windowDocked(DockingWindow arg0) {

	}

	public void windowDocking(DockingWindow arg0)
			throws OperationAbortedException {

	}

	public void windowHidden(DockingWindow arg0) {

	}

	public void windowMaximized(DockingWindow arg0) {

	}

	public void windowMaximizing(DockingWindow arg0)
			throws OperationAbortedException {

	}

	public void windowMinimized(DockingWindow arg0) {

	}

	public void windowMinimizing(DockingWindow arg0)
			throws OperationAbortedException {

	}

	public void windowRemoved(DockingWindow arg0, DockingWindow arg1) {

	}

	public void windowRestored(DockingWindow arg0) {

	}

	public void windowRestoring(DockingWindow arg0)
			throws OperationAbortedException {

	}

	public void windowShown(DockingWindow arg0) {

	}

	public void windowUndocked(DockingWindow arg0) {

	}

	public void windowUndocking(DockingWindow arg0)
			throws OperationAbortedException {

	}

}
