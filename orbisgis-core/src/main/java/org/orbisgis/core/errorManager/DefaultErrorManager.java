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
package org.orbisgis.core.errorManager;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.orbisgis.utils.I18N;

public class DefaultErrorManager implements ErrorManager {

	private static Logger logger = Logger.getLogger(DefaultErrorManager.class);

	private ArrayList<ErrorListener> listeners = new ArrayList<ErrorListener>();

	public void error(String userMsg, Throwable exception) {
		logger.error(userMsg, exception);
		for (ErrorListener listener : listeners) {
			try {
				listener.error(userMsg, exception);
			} catch (Throwable t) {
				logger.error(I18N.getString("orbisgis.org.orbisgis.defaultErrorManager.ErrorManagingException"), t); //$NON-NLS-1$
			}
		}
	}

	public void warning(String userMsg, Throwable exception) {
		logger.error(I18N.getString("orbisgis-core.org.orbisgis.defaultErrorManager.warning") + userMsg, exception); //$NON-NLS-1$
		for (ErrorListener listener : listeners) {
			try {
				listener.warning(userMsg, exception);
			} catch (Throwable t) {
				logger.error(I18N.getString("orbisgis.org.orbisgis.defaultErrorManager.ErrorManagingException"), t); //$NON-NLS-1$
			}
		}
	}

	public void error(String userMsg) {
		error(userMsg, null);
	}

	public void addErrorListener(ErrorListener listener) {
		listeners.add(listener);
	}

	public void removeErrorListener(ErrorListener listener) {
		listeners.remove(listener);
	}

	public void warning(String userMsg) {
		warning(userMsg, null);
	}

}
