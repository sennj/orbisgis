/*
 * UrbSAT is a set of spatial functionalities to build morphological
 * and aerodynamic urban indicators. It has been developed on
 * top of GDMS and OrbisGIS. UrbSAT is distributed under GPL 3
 * license. It is produced by the geomatic team of the IRSTV Institute
 * <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
 *    Erwan BOCHER, scientific researcher,
 *    Thomas LEDUC, scientific researcher,
 *    Fernando GONZALEZ CORTES, computer engineer.
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * This file is part of UrbSAT.
 *
 * UrbSAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UrbSAT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UrbSAT. If not, see <http://www.gnu.org/licenses/>.
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
package org.urbsat.utilities;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.gdms.data.DataSource;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.ExecutionException;
import org.gdms.data.SpatialDataSourceDecorator;
import org.gdms.data.metadata.DefaultMetadata;
import org.gdms.data.metadata.Metadata;
import org.gdms.data.metadata.MetadataUtilities;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DriverException;
import org.gdms.driver.ObjectDriver;
import org.gdms.driver.driverManager.DriverLoadException;
import org.gdms.driver.memory.ObjectMemoryDriver;
import org.gdms.source.Source;
import org.gdms.source.SourceManager;
import org.gdms.sql.customQuery.CustomQuery;
import org.gdms.sql.function.FunctionValidator;
import org.gdms.sql.strategies.IncompatibleTypesException;
import org.gdms.sql.strategies.SemanticException;
import org.grap.io.GeoreferencingException;
import org.grap.model.GeoRaster;
import org.grap.model.GeoRasterFactory;
import org.orbisgis.IProgressMonitor;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

/*
 * select Explode() from ile_de_nantes_bati; select
 * GetZDEM('MNT_Nantes_Lambert') from explode_ile_de_nantes_bati_...; select
 * GetZDEM('MNT_Nantes_Lambert','the_geom') from explode_ile_de_nantes_bati_...;
 *
 * select GetZDEM('3x3') from shape;
 */

public class GetZDEM implements CustomQuery {
	/*
	 * This CustomQuery needs to be rewritten using : UPDATE ds SET the_geom =
	 * addZDEM(RasterLayerAlias) WHERE ...;
	 */
	private GeoRaster geoRaster;

	public ObjectDriver evaluate(DataSourceFactory dsf, DataSource[] tables,
			Value[] values, IProgressMonitor pm) throws ExecutionException {
		try {
			final Source dem = dsf.getSourceManager().getSource(
					values[0].toString());
			int type = dem.getType();
			if ((type & SourceManager.RASTER) == SourceManager.RASTER) {
				if (dem.isFileSource()) {
					geoRaster = GeoRasterFactory.createGeoRaster(dem.getFile()
							.getAbsolutePath());
					geoRaster.open();
				} else {
					throw new ExecutionException("The DEM must be a file !");
				}
			} else {
				throw new UnsupportedOperationException(
						"Cannot understand source type: " + type);
			}

			final SpatialDataSourceDecorator inSds = new SpatialDataSourceDecorator(
					tables[0]);
			inSds.open();
			if (2 == values.length) {
				// if no spatial's field's name is provided, the default (first)
				// one is arbitrarily chosen.
				final String spatialFieldName = values[1].toString();
				inSds.setDefaultGeometry(spatialFieldName);
			}

			final ObjectMemoryDriver driver = new ObjectMemoryDriver(
					getMetadata(MetadataUtilities.fromTablesToMetadatas(tables)));

			double height;
			final int rowCount = (int) inSds.getRowCount();
			for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
				final Geometry g = inSds.getGeometry(rowIndex);
				// is the following line usefull ?
				final Geometry gg = new GeometryFactory().createGeometry(g);
				if (g instanceof GeometryCollection) {
					throw new ExecutionException(
							"The input datasource must be exploded first (no GeometryCollection object) !");
				} else {
					final Coordinate[] coordinates = gg.getCoordinates();
					Arrays.sort(coordinates, new Comparator<Coordinate>() {
						public int compare(Coordinate o1, Coordinate o2) {
							return new Double(o1.z - o2.z).intValue();
						}
					});
					final double globalGroundZ = getGroundZ(coordinates[0].x,
							coordinates[0].y);
					height = coordinates[coordinates.length - 1].z
							- globalGroundZ;
					if (Double.isNaN(height)) {
						height = 0;
					}
					for (Coordinate c : coordinates) {
						// final double localGroundZ = getGroundZ(c.x, c.y);
						// c.z = localGroundZ;
						c.z = globalGroundZ;
					}
				}
				driver.addValues(new Value[] {
						ValueFactory.createValue(rowIndex),
						ValueFactory.createValue(height),
						ValueFactory.createValue(gg) });
			}
			inSds.cancel();
			return driver;
		} catch (FileNotFoundException e) {
			throw new ExecutionException(e);
		} catch (IOException e) {
			throw new ExecutionException(e);
		} catch (GeoreferencingException e) {
			throw new ExecutionException(e);
		} catch (DriverException e) {
			throw new ExecutionException(e);
		} catch (DriverLoadException e) {
			throw new ExecutionException(e);
		}
	}

	private double getGroundZ(final double x, final double y)
			throws IOException, GeoreferencingException {
		final Point2D point = geoRaster
				.fromRealWorldCoordToPixelGridCoord(x, y);
		return geoRaster.getGrapImagePlus().getPixelValue((int) point.getX(),
				(int) point.getY());
	}

	public String getDescription() {
		return "Build the ground geometry and calculate the building's height";
	}

	public String getSqlOrder() {
		return "select GetZDEM('the_DEM'[, the_geom]) from myTable;";
	}

	public String getName() {
		return "GetZDEM";
	}

	public Metadata getMetadata(Metadata[] tables) throws DriverException {
		return new DefaultMetadata(new Type[] {
				TypeFactory.createType(Type.INT),
				TypeFactory.createType(Type.DOUBLE),
				TypeFactory.createType(Type.GEOMETRY) }, new String[] { "id",
				"height", "the_geom" });
	}

	public void validateTables(Metadata[] tables) throws SemanticException,
			DriverException {
		FunctionValidator.failIfBadNumberOfTables(this, tables, 1);
		FunctionValidator.failIfNotSpatialDataSource(this, tables[0], 0);
	}

	public void validateTypes(Type[] types) throws IncompatibleTypesException {
		FunctionValidator.failIfBadNumberOfArguments(this, types, 1, 2);
		FunctionValidator.failIfNotOfType(this, types[0], Type.STRING);
		if (2 == types.length) {
			FunctionValidator.failIfNotOfType(this, types[1], Type.GEOMETRY);
		}
	}
}