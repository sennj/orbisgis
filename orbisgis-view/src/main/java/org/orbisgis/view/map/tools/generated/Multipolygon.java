/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able
 * to manipulate and create vector and raster spatial information. OrbisGIS
 * is distributed under GPL 3 license. It is produced  by the geo-informatic team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
 *    Erwan BOCHER, scientific researcher,
 *    Thomas LEDUC, scientific researcher,
 *    Fernando GONZALEZ CORTES, computer engineer.
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OrbisGIS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult:
 *    <http://orbisgis.cerma.archi.fr/>
 *    <http://sourcesup.cru.fr/projects/orbisgis/>
 *
 * or contact directly:
 *    erwan.bocher _at_ ec-nantes.fr
 *    fergonco _at_ gmail.com
 *    thomas.leduc _at_ cerma.archi.fr
 */

package org.orbisgis.view.map.tools.generated;

import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;
import org.orbisgis.core.layerModel.MapContext;
import org.orbisgis.view.map.tool.*;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

public abstract class Multipolygon implements Automaton {
        protected final static I18n I18N = I18nFactory.getI18n(Multipolygon.class);
	private static Logger logger = Logger.getLogger(Multipolygon.class);

	private String status = "Standby";

	private MapContext ec;

	private ToolManager tm;

        @Override
        public ImageIcon getCursor() {
            return null;
        }

        @Override
	public String[] getTransitionLabels() {
		ArrayList<String> ret = new ArrayList<String>();

		if ("Standby".equals(status)) {

			ret.add(I18N.tr("Cancel"));

			ret.add(I18N.tr("Terminate multipolygon"));

		}

		if ("Point".equals(status)) {

			ret.add(I18N.tr("Cancel"));

			ret.add(I18N.tr("Terminate polygon"));

			ret.add(I18N.tr("Terminate multipolygon"));

		}

		return ret.toArray(new String[0]);
	}

        @Override
	public String[] getTransitionCodes() {
		ArrayList<String> ret = new ArrayList<String>();

		if ("Standby".equals(status)) {

			ret.add("esc");

			ret.add("t");

		}

		if ("Point".equals(status)) {

			ret.add("esc");

			ret.add("p");

			ret.add("t");

		}

		return ret.toArray(new String[0]);
	}

        @Override
	public void init(MapContext ec, ToolManager tm) throws TransitionException,
			FinishedAutomatonException {
		logger.info("status: " + status);
		this.ec = ec;
		this.tm = tm;
		status = "Standby";
		transitionTo_Standby(ec, tm);
		if (isFinished(status)) {
			throw new FinishedAutomatonException();
		}
	}

        @Override
	public void transition(String code) throws NoSuchTransitionException,
			TransitionException, FinishedAutomatonException {
		logger.info("transition code: " + code);

		if ("Standby".equals(status)) {

			if ("press".equals(code)) {
				String preStatus = status;
				try {
					status = "Point";
					logger.info("status: " + status);
					double[] v = tm.getValues();
					for (int i = 0; i < v.length; i++) {
						logger.info("value: " + v[i]);
					}
					transitionTo_Point(ec, tm);
					if (isFinished(status)) {
						throw new FinishedAutomatonException();
					}
					return;
				} catch (TransitionException e) {
					status = preStatus;
					throw e;
				}
			}

			if ("t".equals(code)) {
				String preStatus = status;
				try {
					status = "Done";
					logger.info("status: " + status);
					double[] v = tm.getValues();
					for (int i = 0; i < v.length; i++) {
						logger.info("value: " + v[i]);
					}
					transitionTo_Done(ec, tm);
					if (isFinished(status)) {
						throw new FinishedAutomatonException();
					}
					return;
				} catch (TransitionException e) {
					status = preStatus;
					throw e;
				}
			}

		}

		if ("Point".equals(status)) {

			if ("press".equals(code)) {
				String preStatus = status;
				try {
					status = "Point";
					logger.info("status: " + status);
					double[] v = tm.getValues();
					for (int i = 0; i < v.length; i++) {
						logger.info("value: " + v[i]);
					}
					transitionTo_Point(ec, tm);
					if (isFinished(status)) {
						throw new FinishedAutomatonException();
					}
					return;
				} catch (TransitionException e) {
					status = preStatus;
					throw e;
				}
			}

			if ("p".equals(code)) {
				String preStatus = status;
				try {
					status = "Line";
					logger.info("status: " + status);
					double[] v = tm.getValues();
					for (int i = 0; i < v.length; i++) {
						logger.info("value: " + v[i]);
					}
					transitionTo_Line(ec, tm);
					if (isFinished(status)) {
						throw new FinishedAutomatonException();
					}
					return;
				} catch (TransitionException e) {
					status = preStatus;
					throw e;
				}
			}

			if ("t".equals(code)) {
				String preStatus = status;
				try {
					status = "Done";
					logger.info("status: " + status);
					double[] v = tm.getValues();
					for (int i = 0; i < v.length; i++) {
						logger.info("value: " + v[i]);
					}
					transitionTo_Done(ec, tm);
					if (isFinished(status)) {
						throw new FinishedAutomatonException();
					}
					return;
				} catch (TransitionException e) {
					status = preStatus;
					throw e;
				}
			}

		}

		if ("Line".equals(status)) {

			if ("init".equals(code)) {
				String preStatus = status;
				try {
					status = "Standby";
					logger.info("status: " + status);
					double[] v = tm.getValues();
					for (int i = 0; i < v.length; i++) {
						logger.info("value: " + v[i]);
					}
					transitionTo_Standby(ec, tm);
					if (isFinished(status)) {
						throw new FinishedAutomatonException();
					}
					return;
				} catch (TransitionException e) {
					status = preStatus;
					throw e;
				}
			}

		}

		if ("Done".equals(status)) {

			if ("init".equals(code)) {
				String preStatus = status;
				try {
					status = "Standby";
					logger.info("status: " + status);
					double[] v = tm.getValues();
					for (int i = 0; i < v.length; i++) {
						logger.info("value: " + v[i]);
					}
					transitionTo_Standby(ec, tm);
					if (isFinished(status)) {
						throw new FinishedAutomatonException();
					}
					return;
				} catch (TransitionException e) {
					status = preStatus;
					throw e;
				}
			}

		}

		if ("esc".equals(code)) {
			status = "Cancel";
			transitionTo_Cancel(ec, tm);
			if (isFinished(status)) {
				throw new FinishedAutomatonException();
			}
			return;
		}

		throw new NoSuchTransitionException(code);
	}

	public boolean isFinished(String status) {

		if ("Standby".equals(status)) {

			return false;

		}

		if ("Point".equals(status)) {

			return false;

		}

		if ("Line".equals(status)) {

			return false;

		}

		if ("Done".equals(status)) {

			return false;

		}

		if ("Cancel".equals(status)) {

			return true;

		}

		throw new RuntimeException("Invalid status: " + status);
	}

        @Override
	public void draw(Graphics g) throws DrawingException {

		if ("Standby".equals(status)) {
			drawIn_Standby(g, ec, tm);
		}

		if ("Point".equals(status)) {
			drawIn_Point(g, ec, tm);
		}

		if ("Line".equals(status)) {
			drawIn_Line(g, ec, tm);
		}

		if ("Done".equals(status)) {
			drawIn_Done(g, ec, tm);
		}

		if ("Cancel".equals(status)) {
			drawIn_Cancel(g, ec, tm);
		}

	}

	public abstract void transitionTo_Standby(MapContext vc, ToolManager tm)
			throws FinishedAutomatonException, TransitionException;

	public abstract void drawIn_Standby(Graphics g, MapContext vc,
			ToolManager tm) throws DrawingException;

	public abstract void transitionTo_Point(MapContext vc, ToolManager tm)
			throws FinishedAutomatonException, TransitionException;

	public abstract void drawIn_Point(Graphics g, MapContext vc, ToolManager tm)
			throws DrawingException;

	public abstract void transitionTo_Line(MapContext vc, ToolManager tm)
			throws FinishedAutomatonException, TransitionException;

	public abstract void drawIn_Line(Graphics g, MapContext vc, ToolManager tm)
			throws DrawingException;

	public abstract void transitionTo_Done(MapContext vc, ToolManager tm)
			throws FinishedAutomatonException, TransitionException;

	public abstract void drawIn_Done(Graphics g, MapContext vc, ToolManager tm)
			throws DrawingException;

	public abstract void transitionTo_Cancel(MapContext vc, ToolManager tm)
			throws FinishedAutomatonException, TransitionException;

	public abstract void drawIn_Cancel(Graphics g, MapContext vc, ToolManager tm)
			throws DrawingException;

	protected void setStatus(String status) throws NoSuchTransitionException {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

        @Override
	public String getName() {
		return "Multipolygon";
	}

	public String getMessage() {

		if ("Standby".equals(status)) {
			return I18N.tr("Select the first point/terminate multipolygon");
		}

		if ("Point".equals(status)) {
			return I18N.tr("Select the next point/terminate polygon/terminate multipolygon");
		}

		if ("Line".equals(status)) {
			return "";
		}

		if ("Done".equals(status)) {
			return "";
		}

		if ("Cancel".equals(status)) {
			return "";
		}

		throw new RuntimeException();
	}

	public String getConsoleCommand() {
		return "multipolygon";
	}

        @Override
	public String getTooltip() {
		return I18N.tr("Draw a multipolygon");
	}

	private ImageIcon mouseCursor;

	public ImageIcon getImageIcon() {
		if (mouseCursor != null) {
			return mouseCursor;
		} else {
			return null;
		}
	}

	public void setMouseCursor(ImageIcon mouseCursor) {
		this.mouseCursor = mouseCursor;
	}

        @Override
	public void toolFinished(MapContext vc, ToolManager tm)
			throws NoSuchTransitionException, TransitionException,
			FinishedAutomatonException {

		if ("Point".equals(status)) {

			transition("t");

		}

	}

        @Override
	public java.awt.Point getHotSpotOffset() {

		return new java.awt.Point(8, 8);

	}

}