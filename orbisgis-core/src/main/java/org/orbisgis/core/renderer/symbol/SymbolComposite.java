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
package org.orbisgis.core.renderer.symbol;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import org.gdms.driver.DriverException;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.core.renderer.RenderContext;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.gdms.data.types.Type;

class SymbolComposite extends AbstractSymbol implements Symbol {

	private Symbol[] symbols;

	SymbolComposite(Symbol[] symbols) {
		this.symbols = symbols;
		setName("Symbol composite");
	}

        @Override
	public Envelope draw(Graphics2D g, Geometry geom, MapTransform mt,
			RenderContext permission) throws DriverException {
		Envelope ret = null;
		for (Symbol symbol : symbols) {
			Envelope area = symbol.draw(g, geom, mt, permission);
			if (ret == null) {
				ret = area;
			} else {
				ret.expandToInclude(area);
			}
		}

		return ret;
	}

        @Override
	public boolean acceptGeometry(Geometry geom) {
		for (Symbol symbol : symbols) {
			if (symbol.acceptGeometry(geom)) {
				return true;
			}
		}
		return false;
	}

        @Override
	public boolean acceptGeometryType(Type geomType) {
		for (Symbol symbol : symbols) {
			if (symbol.acceptGeometryType(geomType)) {
				return true;
			}
		}
		return false;
	}

        @Override
	public int getSymbolCount() {
		return symbols.length;
	}

        @Override
	public Symbol getSymbol(int i) {
		return symbols[i];
	}

	@Override
	public boolean acceptsChildren() {
		return true;
	}

        @Override
	public String getId() {
		return "org.orbisgis.symbol.Composite";
	}

        @Override
	public Symbol cloneSymbol() {
		Symbol[] children = new Symbol[symbols.length];
		for (int i = 0; i < children.length; i++) {
			children[i] = symbols[i].cloneSymbol();
		}
		return new SymbolComposite(children);
	}

        @Override
	public String getClassName() {
		return "Composite";
	}

	@Override
	public Symbol deriveSymbol(Color color) {
		ArrayList<Symbol> newSymbols = new ArrayList<Symbol>();
		for (int i = 0; i < symbols.length; i++) {
			Symbol derivedSymbol = symbols[i].deriveSymbol(color);
			if (derivedSymbol != null) {
				newSymbols.add(derivedSymbol);
			}
		}

		return new SymbolComposite(newSymbols.toArray(new Symbol[newSymbols.size()]));
	}

}
