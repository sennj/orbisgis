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
package org.orbisgis.core.ui.editors.map.tools;

import java.awt.geom.Rectangle2D;
import java.util.Observable;

import javax.swing.AbstractButton;

import org.apache.log4j.Logger;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreationException;
import org.gdms.driver.DriverException;
import org.gdms.driver.driverManager.DriverLoadException;
import org.gdms.sql.engine.ParseException;
import org.orbisgis.core.DataManager;
import org.orbisgis.core.Services;
import org.orbisgis.core.background.BackgroundJob;
import org.orbisgis.core.background.BackgroundManager;
import org.orbisgis.core.background.DefaultJobId;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.layerModel.MapContext;
import org.orbisgis.core.ui.editors.map.tool.ToolManager;
import org.orbisgis.core.ui.editors.map.tool.TransitionException;
import org.orbisgis.core.ui.pluginSystem.PlugInContext;
import org.orbisgis.core.ui.plugins.views.information.InformationManager;
import org.orbisgis.progress.ProgressMonitor;
import org.orbisgis.utils.I18N;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.WKTWriter;

public class InfoTool extends AbstractRectangleTool {

	private static Logger logger = Logger.getLogger(InfoTool.class);
	AbstractButton button;

	@Override
	public AbstractButton getButton() {
		return button;
	}

        @Override
	public void setButton(AbstractButton button) {
		this.button = button;
	}

	@Override
	public void update(Observable o, Object arg) {
		PlugInContext.checkTool(this);
	}

	@Override
	protected void rectangleDone(Rectangle2D rect,
			boolean smallerThanTolerance, MapContext vc, ToolManager tm)
			throws TransitionException {
		ILayer layer = vc.getSelectedLayers()[0];
		DataSource sds = layer.getDataSource();
		String sql = null;
		try {
			GeometryFactory gf = ToolManager.toolsGeometryFactory;
			double minx = rect.getMinX();
			double miny = rect.getMinY();
			double maxx = rect.getMaxX();
			double maxy = rect.getMaxY();

			Coordinate lowerLeft = new Coordinate(minx, miny);
			Coordinate upperRight = new Coordinate(maxx, maxy);
			LinearRing envelopeShell = gf.createLinearRing(new Coordinate[] {
					lowerLeft, new Coordinate(minx, maxy), upperRight,
					new Coordinate(maxx, miny), lowerLeft, });
			Geometry geomEnvelope = gf.createPolygon(envelopeShell,
					new LinearRing[0]);
			WKTWriter writer = new WKTWriter();
			sql = "select * from " + layer.getName() + " where ST_intersects("
					+ sds.getMetadata().getFieldName(sds.getSpatialFieldIndex()) + ", ST_geomfromtext('"
					+ writer.write(geomEnvelope) + "'));";
			BackgroundManager bm = (BackgroundManager) Services
					.getService(BackgroundManager.class);
			bm.backgroundOperation(new DefaultJobId(
					"org.orbisgis.jobs.InfoTool"), new PopulateViewJob(sql));
		} catch (DriverException e) {
			throw new TransitionException(e);
		} catch (DriverLoadException e) {
			throw new RuntimeException(e);
		}
	}

        @Override
	public boolean isEnabled(MapContext vc, ToolManager tm) {
		if (vc.getSelectedLayers().length == 1) {
			try {
				if (vc.getSelectedLayers()[0].isVectorial()) {
					return vc.getSelectedLayers()[0].isVisible();
				}
			} catch (DriverException e) {
				return false;
			}
		}

		return false;
	}

        @Override
	public boolean isVisible(MapContext vc, ToolManager tm) {
		return true;
	}

	private class PopulateViewJob implements BackgroundJob {

		private String sql;

		public PopulateViewJob(String sql) {
			this.sql = sql;
		}

                @Override
		public String getTaskName() {
			return "Getting info";
		}

                @Override
		public void run(ProgressMonitor pm) {
			try {
				logger.debug("Info query: " + sql);
				final DataSource ds = ((DataManager) Services
						.getService(DataManager.class)).getDataSourceFactory()
						.getDataSourceFromSQL(sql, pm);
				if (!pm.isCancelled()) {
					try {
						Services.getService(InformationManager.class)
								.setContents(ds);
					} catch (DriverException e) {
						Services.getErrorManager().error(
								"Cannot show the data", e);
					}
				}
			} catch (DataSourceCreationException e) {
				Services.getErrorManager().error("Cannot get the result", e);
			} catch (DriverException e) {
				Services.getErrorManager().error("Cannot access the data", e);
			} catch (DriverLoadException e) {
				Services.getErrorManager().error("Cannot execute the query", e);
			} catch (ParseException e) {
				Services.getErrorManager().error(
						"Cannot parse the instruction", e);
			}
		}
	}

        @Override
	public String getName() {
		return I18N.getString("orbisgis.core.ui.editors.map.tool.information");
	}

}
