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
package org.orbisgis.core.layerModel;

import java.awt.Graphics2D;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.gdms.data.DataSource;
import org.gdms.data.edition.EditionEvent;
import org.gdms.data.edition.EditionListener;
import org.gdms.data.edition.MultipleEditionEvent;
import org.orbisgis.core.renderer.legend.Legend;
import org.orbisgis.core.renderer.legend.LegendListener;
import org.orbisgis.core.renderer.legend.RenderException;
import org.orbisgis.core.renderer.symbol.Symbol;
import org.orbisgis.utils.I18N;

public class LegendDecorator implements Legend, EditionListener {

	private final static Logger logger = Logger
			.getLogger(LegendDecorator.class);

	private Legend legend;
	private ArrayList<Symbol> symbols = new ArrayList<Symbol>();
	private DataSource sds;

	private boolean valid = true;

	public LegendDecorator(Legend legend) {
		this.legend = legend;
	}

	public String getName() {
		return legend.getName();
	}

	public Symbol getSymbol(DataSource sds, long row)
			throws RenderException {
		return symbols.get((int) row);
	}

	public void initialize(DataSource ds)
			throws RenderException {
		this.sds = ds;
		legend.preprocess(sds);
		symbols.clear();
		try {
			for (int i = 0; i < ds.getRowCount(); i++) {
				Symbol symbol = legend.getSymbol(sds, i);
				if (symbol != null) {
					symbols.add(symbol);
				} else {
					symbols.add(null);
				}
			}
			valid = true;
		} catch (Exception e) {
			valid = false;
			// Catch exception since we don't trust Legend implementations
			throw new RenderException(I18N.getString("orbisgis-core.org.orbisgis.layerModel.LegendDecorator.cannotCacheSymbols"), e); //$NON-NLS-1$
		}
	}

	public void setName(String name) {
		legend.setName(name);
	}

	public void multipleModification(MultipleEditionEvent e) {
		EditionEvent[] events = e.getEvents();
		for (EditionEvent editionEvent : events) {
			singleModification(editionEvent);
		}
	}

	public void singleModification(EditionEvent e) {
		if ((legend.getSymbolsToUpdateOnRowModification() == Legend.ALL)
				|| (e.getType() == EditionEvent.RESYNC)) {
			try {
				initialize(sds);
			} catch (RenderException e1) {
				valid = false;
				logger.error(I18N.getString("orbisgis-core.org.orbisgis.layerModel.LegendDecorator.cannotUpdateSymbol"), e1); //$NON-NLS-1$
			}
		} else {
			switch (e.getType()) {
			case EditionEvent.DELETE:
				symbols.remove(e.getRowIndex());
				break;
			case EditionEvent.INSERT:
				try {
					symbols.add((int) e.getRowIndex(), legend.getSymbol(sds, e
							.getRowIndex()));
				} catch (RenderException e1) {
					symbols.add(null);
					logger.error(I18N.getString("orbisgis-core.org.orbisgis.layerModel.LegendDecorator.cannotUpdateSymbol"), e1); //$NON-NLS-1$
				}
				break;
			case EditionEvent.MODIFY:
				try {
					symbols.set((int) e.getRowIndex(), legend.getSymbol(sds, e
							.getRowIndex()));
				} catch (RenderException e1) {
					symbols.set((int) e.getRowIndex(), null);
					logger.error(I18N.getString("orbisgis-core.org.orbisgis.layerModel.LegendDecorator.cannotUpdateSymbol"), e1); //$NON-NLS-1$
				}
				break;
			}
		}
	}

	public void addLegendListener(LegendListener listener) {
		legend.addLegendListener(listener);
	}

	public void removeLegendListener(LegendListener listener) {
		legend.removeLegendListener(listener);
	}

	public Legend getLegend() {
		return legend;
	}

	public String getLegendTypeId() {
		return legend.getLegendTypeId();
	}

	public void setJAXBObject(Object jaxbObject) {
		legend.setJAXBObject(jaxbObject);
	}

	public Legend newInstance() {
		return legend.newInstance();
	}

	public Object getJAXBObject() {
		return legend.getJAXBObject();
	}

	public void preprocess(DataSource sds)
			throws RenderException {
		legend.preprocess(sds);
	}

	public void drawImage(Graphics2D g) {
		legend.drawImage(g);
	}

	public int[] getImageSize(Graphics2D g) {
		return legend.getImageSize(g);
	}

	public int getMaxScale() {
		return legend.getMaxScale();
	}

	public int getMinScale() {
		return legend.getMinScale();
	}

	public void setMaxScale(int max) {
		legend.setMaxScale(max);
	}

	public void setMinScale(int min) {
		legend.setMinScale(min);
	}

	@Override
	public String getJAXBContext() {
		return legend.getJAXBContext();
	}

	@Override
	public String getLegendTypeName() {
		return legend.getLegendTypeName();
	}

	public boolean isValid() {
		return valid;
	}

	@Override
	public int getSymbolsToUpdateOnRowModification() {
		return legend.getSymbolsToUpdateOnRowModification();
	}

	@Override
	public boolean isVisible() {
		return legend.isVisible();
	}

	@Override
	public void setVisible(boolean visible) {
		legend.setVisible(visible);
	}
}
