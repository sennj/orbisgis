/* 
 * This class has been extract from the TANATO project and reversed to GDMS core.
 * 
 * TANATO  is a library dedicated to the modelling of water pathways based on 
 * triangulate irregular network. TANATO takes into account anthropogenic and 
 * natural artifacts to evaluate their impacts on the watershed response. 
 * It ables to compute watershed, main slope directions and water flow pathways.
 * 
 * This library has been originally created  by Erwan Bocher during his thesis 
 * “Impacts des activités humaines sur le parcours des écoulements de surface dans 
 * un bassin versant bocager : essai de modélisation spatiale. Application au 
 * Bassin versant du Jaudy-Guindy-Bizien (France)”. It has been funded by the 
 * Bassin versant du Jaudy-Guindy-Bizien and Syndicat d’Eau du Trégor.
 * 
 * The new version is developed at French IRSTV institut as part of the 
 * AvuPur project, funded by the French Agence Nationale de la Recherche 
 * (ANR) under contract ANR-07-VULN-01.
 * 
 * TANATO is distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 * 
 * Copyright (C) 2010-2012 IRSTV FR CNRS 2488
 * 
 * TANATO is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * TANATO is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * TANATO. If not, see <http://www.gnu.org/licenses/>.
 * 
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.gdms.sql.function.spatial.tin;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.jdelaunay.delaunay.error.DelaunayError;
import org.jdelaunay.delaunay.geometries.DEdge;
import org.jdelaunay.delaunay.geometries.DPoint;
import org.jdelaunay.delaunay.geometries.DTriangle;

/**
 * A small factory used to create jDelaunay objects from JTS geometries.
 * @author ebocher
 */
public final class TINFeatureFactory {

	/**
	 * We don't want to instanciate any TINFeatureFactory.
	 */
	private TINFeatureFactory(){
	}

        /**
         * A factory to create a DTriangle from a Geometry
         * @param geom
         * @return
         * @throws DelaunayError
         *              If the triangle can't be generated
         * @throws IllegalArgumentException
         *              If there are not exactly 3 coordinates in geom.
         */
        public static DTriangle createDTriangle(Geometry geom) throws DelaunayError {

                Coordinate[] coords = geom.getCoordinates();
                if (coords.length != 4) {
                        throw new IllegalArgumentException("The geometry must be a triangle");
                }
                return new DTriangle(new DEdge(coords[0].x, coords[0].y, coords[0].z, coords[1].x, coords[1].y, coords[1].z), 
			new DEdge(coords[1].x, coords[1].y, coords[1].z, coords[2].x, coords[2].y, coords[2].z),
			new DEdge(coords[2].x, coords[2].y, coords[2].z, coords[0].x, coords[0].y, coords[0].z));
        }

        /**
         * A factory to create a DPoint from a Geometry
         * @param geom
         * @return
         * @throws DelaunayError
         *              If the DPoint can't be generated. In most cases, it means that
         *              the input point has a Double.NaN coordinate.
         * @throws IllegalArgumentException
         *              If there are not exactly 1 coordinates in geom.
         */
        public static DPoint createDPoint(Geometry geom) throws DelaunayError{
                Coordinate[] coords = geom.getCoordinates();
                if (coords.length != 1) {
                        throw new IllegalArgumentException("The geometry must be a point");
                }
                return new DPoint(coords[0]);
        }

	/**
	 * Tries to create a DEdge from the given geometry, which is supposed to be formed
	 * of exactly two coordinates.
	 * @param geom
	 * @return
	 * @throws DelaunayError
         * @throws IllegalArgumentException
         *              If there are not exactly 2 coordinates in geom.
	 */
	public static DEdge createDEdge(Geometry geom) throws DelaunayError {
		Coordinate[] coords = geom.getCoordinates();
		if(coords.length!=2){
			throw new IllegalArgumentException("the geometry is supposed to be line with two points.");
		}
		return new DEdge(new DPoint(coords[0]), new DPoint(coords[1]));
	}

        /**
         * A factory to create a DPoint from a Coordinate.
         * @param geom
         * @return
         * @throws DelaunayError if one of the coordinate values is Double.Nan
         */
        public static DPoint createDPoint(Coordinate coord) throws DelaunayError {
                return new DPoint(coord);
        }

       

}