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
package org.orbisgis.core.ui.editorViews.toc.actions.cui.legends.table;

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.gdms.data.values.Value;
import org.orbisgis.core.renderer.legend.carto.ClassifiedLegend;
import org.orbisgis.core.renderer.legend.carto.LegendFactory;
import org.orbisgis.core.sif.UIFactory;

public abstract class ClassifiedLegendTableModel extends AbstractTableModel
        implements TableModel {

        private ClassifiedLegend classifiedLegend = LegendFactory.createUniqueValueLegend();
        private boolean ordered = false;
        private Integer[] valueIndex;
        private boolean showRestOfValues;

        public void deleteRows(int[] rows) {
                Arrays.sort(rows);
                for (int i = rows.length - 1; i >= 0; i--) {
                        int row = rows[i];
                        if (ordered) {
                                row = getValueIndex(row);
                        }
                        if (row == classifiedLegend.getClassificationCount()) {
                                JOptionPane.showMessageDialog(UIFactory.getMainFrame(),
                                        "Cannot delete 'Rest of values'", "Error",
                                        JOptionPane.ERROR_MESSAGE);
                        } else {
                                classifiedLegend.removeClassification(row);
                        }
                }
                invalidateOrder();
                fireTableStructureChanged();
        }

        public int getRowCount() {
                int ret = classifiedLegend.getClassificationCount();
                if (showRestOfValues) {
                        ret++;
                }
                return ret;
        }

        public void setOrdered(boolean selected) {
                ordered = selected;
                invalidateOrder();
        }

        protected int getSortedIndex(int index) {
                if (ordered) {
                        index = getValueIndex(index);
                }

                return index;
        }

        protected void invalidateOrder() {
                valueIndex = null;
                fireTableDataChanged();
        }

        private int getValueIndex(int index) {
                if (valueIndex == null) {
                        TreeSet<Integer> values = new TreeSet<Integer>(
                                new Comparator<Integer>() {

                                        public int compare(Integer i1, Integer i2) {
                                                Value v1 = getOrderValue(i1.intValue());
                                                Value v2 = getOrderValue(i2.intValue());

                                                if (v1.isNull()) {
                                                        return -1;
                                                } else if (v2.isNull()) {
                                                        return 1;
                                                } else {
                                                        Value l = v1.less(v2);
                                                        if (!l.isNull()) {
                                                                return l.getAsBoolean() ? -1 : 1;
                                                        }
                                                }


                                                return -1;
                                        }
                                });

                        for (int i = 0; i < classifiedLegend.getClassificationCount(); i++) {
                                values.add(new Integer(i));
                        }

                        valueIndex = values.toArray(new Integer[values.size()]);
                }

                return valueIndex[index];
        }

        protected abstract Value getOrderValue(int intValue);

        public void setLegend(ClassifiedLegend legend) {
                invalidateOrder();
                fireTableDataChanged();
                this.classifiedLegend = legend;
        }

        public void setShowRestOfValues(boolean showRestOfValues) {
                this.showRestOfValues = showRestOfValues;
                if (!showRestOfValues) {
                        classifiedLegend.setDefaultSymbol(null);
                        classifiedLegend.setDefaultLabel(null);
                }
                fireTableDataChanged();
        }
}
