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
package org.orbisgis.core.geocognition.mapContext;

import org.orbisgis.core.OrbisGISPersitenceConfig;
import org.orbisgis.core.PersistenceException;
import org.orbisgis.core.geocognition.GeocognitionElementFactory;
import org.orbisgis.core.geocognition.GeocognitionExtensionElement;
import org.orbisgis.core.geocognition.symbology.GeocognitionLegendFactory;
import org.orbisgis.core.layerModel.MapContext;

public class GeocognitionMapContextFactory implements
		GeocognitionElementFactory {

	@Override
	public String getJAXBContextPath() {
		String legendsPath = new GeocognitionLegendFactory()
				.getJAXBContextPath();
		String mapPath = org.orbisgis.core.layerModel.persistence.MapContext.class
				.getName();
		mapPath = mapPath.substring(0, mapPath.lastIndexOf('.'));
		if (legendsPath != null) {
			mapPath += ":" + legendsPath;
		}
		return mapPath;
	}

	@Override
	public GeocognitionExtensionElement createElementFromXML(Object xmlObject,
			String contentTypeId) throws PersistenceException {
		return new GeocognitionMapContext(xmlObject, this);
	}

	@Override
	public GeocognitionExtensionElement createGeocognitionElement(Object object) {
		GeocognitionMapContext ret = new GeocognitionMapContext(
				(MapContext) object, this);
		return ret;
	}

	@Override
	public boolean accepts(Object o) {
		return o instanceof MapContext;
	}

	@Override
	public boolean acceptContentTypeId(String typeId) {
		return OrbisGISPersitenceConfig.GEOCOGNITION_MAPCONTEXT_FACTORY_ID
				.equals(typeId);
	}

}
