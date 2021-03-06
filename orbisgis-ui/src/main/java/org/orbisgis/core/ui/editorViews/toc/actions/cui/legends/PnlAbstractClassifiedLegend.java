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
/*
 * JPanelUniqueSymbolLegend.java
 *
 * Created on 27 de febrero de 2008, 18:20
 */

package org.orbisgis.core.ui.editorViews.toc.actions.cui.legends;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.gdms.data.types.Type;
 
import org.orbisgis.core.renderer.legend.Legend;
import org.orbisgis.core.renderer.legend.carto.ClassifiedLegend;
import org.orbisgis.core.renderer.symbol.Symbol;
import org.orbisgis.core.renderer.symbol.SymbolFactory;
import org.orbisgis.core.sif.CRFlowLayout;
import org.orbisgis.core.sif.CarriageReturn;
import org.orbisgis.core.sif.UIFactory;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.ConstraintSymbolFilter;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.LegendContext;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.SymbolBuilder;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.SymbolFilter;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.legend.ILegendPanel;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.legends.table.ClassifiedLegendTableModel;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.legends.table.SymbolValueCellRenderer;

/**
 *
 */
public abstract class PnlAbstractClassifiedLegend extends javax.swing.JPanel
		implements ILegendPanel {

	private ClassifiedLegend legend;
	protected LegendContext legendContext;
	protected ClassifiedLegendTableModel tableModel;

	public PnlAbstractClassifiedLegend(ClassifiedLegendTableModel tableModel,
			ClassifiedLegend initialLegend) {
		this.tableModel = tableModel;
		this.legend = initialLegend;
		legend.setName(legend.getLegendTypeName());
	}

	private void refreshButtons() {
		boolean someField = canAdd();
		jButtonAddAll.setEnabled(someField);
		jButtonAddOne.setEnabled(someField);

		jButtonDel.setEnabled(table.getSelectedRow() != -1);
	}

	protected abstract boolean canAdd();

	/**
	 * init the table and their events
	 */
	private void initList() {
		table.setModel(tableModel);
		table.setRowHeight(25);

		table.setDefaultRenderer(Symbol.class, new SymbolValueCellRenderer());

		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					int col = table.getSelectedColumn();
					if (col == 0) {
						int row = table.getSelectedRow();
						SymbolBuilder symbolEditor = new SymbolBuilder(true,
								legendContext, getSymbolFilter());
						symbolEditor.setSymbol((Symbol) tableModel.getValueAt(
								row, 0));

						if (UIFactory.showDialog(symbolEditor)) {
							Symbol sym = symbolEditor.getSymbolComposite();
							tableModel.setValueAt(sym, row, col);
						}
					}
				}
			}

			private SymbolFilter getSymbolFilter() {
				return new ConstraintSymbolFilter(legendContext
						.getGeometryTypeConstraint());
			}

		});

		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

                        @Override
					public void valueChanged(ListSelectionEvent e) {
						refreshButtons();
					}

				});
	}

	/**
	 * Creates a random symbol with a random color for the fill and black
	 * outline.
	 * 
	 * @param constraint
	 * @return Symbol
	 */
	protected Symbol createRandomSymbol() {
		Random rand = new Random();

		int g2 = rand.nextInt(255);
		int r2 = rand.nextInt(255);
		int b2 = rand.nextInt(255);

		Color outline = Color.black;
		Color fill = new Color(r2, g2, b2);

		Symbol lineSymbol = SymbolFactory.createLineSymbol(fill, 1);
		Symbol pointSymbol = SymbolFactory.createPointCircleSymbol(outline,
				fill, 10);
		Symbol polygonSymbol = SymbolFactory.createPolygonSymbol(outline, fill);
		Type geometryTypeConstraint = legendContext.getGeometryTypeConstraint();
		Symbol s;
		if (geometryTypeConstraint == null) {
			s = SymbolFactory.createSymbolComposite(polygonSymbol, lineSymbol,
					pointSymbol);
		} else {
			switch (geometryTypeConstraint.getTypeCode()) {
			case Type.LINESTRING:
			case Type.MULTILINESTRING:
				s = lineSymbol;
				break;
			case Type.POINT:
			case Type.MULTIPOINT:
				s = pointSymbol;
				break;
			case Type.POLYGON:
			case Type.MULTIPOLYGON:
				s = polygonSymbol;
				break;
			default:
				throw new RuntimeException("Can't find a symbol for : "+geometryTypeConstraint.getTypeCode());
			}
		}

		return s;
	}

	private void initComponents() {
		jCheckBoxRestOfValues = new javax.swing.JCheckBox();
		jCheckBoxOrder = new javax.swing.JCheckBox();
		jPanelTable = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		table = new javax.swing.JTable();
		jPanelButtons = new javax.swing.JPanel();
		jButtonAddAll = new javax.swing.JButton();
		jButtonAddOne = new javax.swing.JButton();
		jButtonDel = new javax.swing.JButton();

		setLayout(new CRFlowLayout());

		add(getTopPanel());
		add(new CarriageReturn());

		JPanel pnlChecks = new JPanel();
		jCheckBoxRestOfValues.setText("rest of values");
		jCheckBoxRestOfValues.addActionListener(new ActionListener() {

                        @Override
			public void actionPerformed(ActionEvent e) {
				jCheckBoxRestOfValuesActionPerformed();
			}
		});
		pnlChecks.add(jCheckBoxRestOfValues);
		pnlChecks.add(new CarriageReturn());

		jCheckBoxOrder.setText("order");
		jCheckBoxOrder.addActionListener(new ActionListener() {
                        @Override
			public void actionPerformed(ActionEvent evt) {
				jCheckBoxOrderActionPerformed(evt);
			}
		});
		pnlChecks.add(jCheckBoxOrder);
		add(pnlChecks);
		add(new CarriageReturn());

		jScrollPane1.setPreferredSize(new java.awt.Dimension(454, 175));

		table.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

		}, new String[] { "Symbol", "Value", "Label" }));
		jScrollPane1.setViewportView(table);

		jPanelTable.add(jScrollPane1);

		add(jPanelTable);
		add(new CarriageReturn());

		jButtonAddAll.setText("Add all");
		jButtonAddAll.addActionListener(new java.awt.event.ActionListener() {
                        @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addAllAction();
			}
		});
		jPanelButtons.add(jButtonAddAll);

		jButtonAddOne.setText("Add");
		jButtonAddOne.addActionListener(new java.awt.event.ActionListener() {
                        @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addOneAction();
			}
		});
		jPanelButtons.add(jButtonAddOne);

		jButtonDel.setText("Delete");
		jButtonDel.addActionListener(new java.awt.event.ActionListener() {
                        @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonDelActionPerformed(evt);
			}
		});
		jPanelButtons.add(jButtonDel);

		add(jPanelButtons);
	}

	protected abstract JPanel getTopPanel();

	/**
	 * adds all the values in the layer to the table.
	 * 
	 * @param evt
	 */
	protected abstract void addAllAction();

	/**
	 * adds one more value to the table. will be a copy of the last (if exists
	 * any) or a new one with a NullValue.
	 * 
	 * @param evt
	 */
	protected abstract void addOneAction();

	private void jButtonDelActionPerformed(java.awt.event.ActionEvent evt) {
		int[] rows = table.getSelectedRows();
		tableModel.deleteRows(rows);
	}

	/**
	 * 
	 * @param evt
	 */
	private void jCheckBoxRestOfValuesActionPerformed() {
		boolean isSelected = jCheckBoxRestOfValues.isSelected();
		tableModel.setShowRestOfValues(isSelected);
	}

	private void jCheckBoxOrderActionPerformed(java.awt.event.ActionEvent evt) {
		tableModel.setOrdered(jCheckBoxOrder.isSelected());
	}

	private javax.swing.JButton jButtonAddAll;
	private javax.swing.JButton jButtonAddOne;
	private javax.swing.JButton jButtonDel;
	private javax.swing.JCheckBox jCheckBoxOrder;
	private javax.swing.JCheckBox jCheckBoxRestOfValues;
	private javax.swing.JPanel jPanelButtons;
	private javax.swing.JPanel jPanelTable;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTable table;

        @Override
	public Component getComponent() {
		return this;
	}

        @Override
	public Legend getLegend() {
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
		return legend;
	}

        @Override
	public boolean acceptsGeometryType(int geometryType) {
		return true;
	}

        @Override
	public void setLegend(Legend legend) {
		this.legend = (ClassifiedLegend) legend;
		this.tableModel.setLegend(this.legend);
		table.setModel(this.tableModel);

		jCheckBoxRestOfValues
				.setSelected(!(this.legend.getDefaultSymbol() == null));
		jCheckBoxRestOfValuesActionPerformed();
		refreshButtons();
	}

        @Override
	public void initialize(LegendContext lc) {
		this.legendContext = lc;
		initComponents();
		initList();
	}

        @Override
	public String validateInput() {
		if (legend.getClassificationCount() == 0) {
			return "At least a value classification should be added";
		}
		return null;
	}
}
