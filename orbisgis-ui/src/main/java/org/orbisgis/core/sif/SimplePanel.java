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
package org.orbisgis.core.sif;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.NonEditableDataSourceException;
import org.gdms.data.file.FileSourceCreation;
import org.gdms.data.schema.Metadata;
import org.gdms.data.memory.MemorySourceDefinition;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DriverException;
import org.gdms.driver.driverManager.DriverLoadException;
import org.gdms.driver.driverManager.DriverManager;
import org.gdms.driver.memory.MemoryDataSetDriver;
import org.gdms.sql.engine.SemanticException;
import org.gdms.sql.engine.ParseException;
import org.orbisgis.utils.I18N;

public class SimplePanel extends JPanel {

    private static final Logger logger = Logger.getLogger(SimplePanel.class);
    private static final String LAST_INPUT = "lastInput"; //$NON-NLS-1$
    private static String dsName = "source"; //$NON-NLS-1$
    // a somehow difficult to stumble upon constant, to represent a canceled action
    public static final String CANCELED_ACTION = String.valueOf(
            Integer.MAX_VALUE);
    private MsgPanel msgPanel;
    private UIPanel panel;
    private OutsideFrame frame;
    private Component firstFocus;

    /**
     * This is the default constructor
     */
    public SimplePanel(OutsideFrame frame, UIPanel panel) {
        this.panel = panel;
        this.frame = frame;
        initialize(panel);
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize(UIPanel panel) {
        JPanel uiPanel = new JPanel();

        uiPanel.setLayout(new BorderLayout());

        Component comp = panel.getComponent();
        fillFirstComponent(comp);
        uiPanel.add(comp, BorderLayout.CENTER);
        msgPanel = new MsgPanel(getIcon());
        msgPanel.setTitle(panel.getTitle());

        this.setLayout(new BorderLayout());
        this.add(msgPanel, BorderLayout.NORTH);

        JPanel centerPanel;
        if (panel instanceof SQLUIPanel) {
            SQLUIPanel sqlPanel = (SQLUIPanel) panel;
            String id = sqlPanel.getId();
            if ((id != null) && (sqlPanel.showFavorites())) {
                JPanel controlPanel;
                try {
                    controlPanel = new ControlPanel(sqlPanel);
                    JPanel split = new JPanel();
                    split.setLayout(new BorderLayout());
                    split.add(controlPanel, BorderLayout.WEST);
                    split.add(uiPanel, BorderLayout.CENTER);
                    centerPanel = split;
                } catch (DriverException e) {
                    logger.error(
                            I18N.getString(
                            "orbisgis.org.orbisgis.sif.simplePanel.errorObtainingFavorites"),
                                 e); //$NON-NLS-1$
                    centerPanel = uiPanel;
                } catch (DataSourceCreationException e) {
                    logger.error(
                            I18N.getString(
                            "orbisgis.org.orbisgis.sif.simplePanel.errorObtainingFavorites"),
                                 e); //$NON-NLS-1$
                    centerPanel = uiPanel;
                }
            } else {
                centerPanel = uiPanel;
            }
        } else {
            centerPanel = uiPanel;
        }
        this.add(centerPanel, BorderLayout.CENTER);
    }

    private boolean fillFirstComponent(Component comp) {
        if (comp instanceof Container) {
            Container cont = (Container) comp;
            for (int i = 0; i < cont.getComponentCount(); i++) {
                if (fillFirstComponent(cont.getComponent(i))) {
                    return true;
                }
            }

            return false;
        } else {
            firstFocus = comp;
            this.addComponentListener(new ComponentAdapter() {

                @Override
                public void componentShown(ComponentEvent e) {
                    firstFocus.requestFocus();
                }
            });

            return true;
        }
    }

    public void initialize() {
        String err = null;
        try {
            err = panel.initialize();
        } catch (Exception e) {
            String msg = I18N.getString(
                    "orbisgis.org.orbisgis.sif.simplePanel.cannotInitializeDialog"); //$NON-NLS-1$
            logger.error(msg, e);
            err = msg + ": " + e.getMessage(); //$NON-NLS-1$
        }
        if (err == null) {
            validateInput();
        } else {
            msgPanel.setError(err);
            frame.cannotContinue();
        }
    }

    void validateInput() {
        String err = null;
        try {
            err = panel.validateInput();
        } catch (Exception e) {
            logger.error(
                    I18N.getString(
                    "orbisgis.org.orbisgis.sif.simplePanel.errorValidatingUIPanel"),
                         e); //$NON-NLS-1$
            err = I18N.getString(
                    "orbisgis.org.orbisgis.sif.simplePanel.errorValidating") + e.
                    getMessage(); //$NON-NLS-1$
        }
        if ((err == null) && (panel instanceof SQLUIPanel)) {
            SQLUIPanel sqlPanel = (SQLUIPanel) panel;
            // Type check
            int[] types = sqlPanel.getFieldTypes();
            String[] values = sqlPanel.getValues();
            for (int i = 0; i < types.length; i++) {
                if (values[i] != null) {
                    switch (types[i]) {
                        case SQLUIPanel.INT:
                            try {
                                Integer.parseInt(values[i]);
                            } catch (NumberFormatException e) {
                                err = sqlPanel.getFieldNames()[i]
                                        + I18N.getString(
                                        "orbisgis.org.orbisgis.sif.simplePanel.mustBeAnIntExpression"); //$NON-NLS-1$
                            }
                            break;
                        case SQLUIPanel.DOUBLE:
                            try {
                                Double.parseDouble(values[i]);
                            } catch (NumberFormatException e) {
                                err = sqlPanel.getFieldNames()[i]
                                        + I18N.getString(
                                        "orbisgis.org.orbisgis.sif.simplePanel.mustBeAFloatExpression"); //$NON-NLS-1$
                            }
                            break;
                    }
                }
            }

            if (err == null) {
                // Evaluate SQL
                registerUISource(sqlPanel);

                String[] validationExpr = sqlPanel.getValidationExpressions();
                String[] errMsgs = sqlPanel.getErrorMessages();
                if (validationExpr != null) {
                    try {
                        for (int i = 0; i < errMsgs.length; i++) {
                            String sql = "select * from source where " //$NON-NLS-1$
                                    + validationExpr[i] + " ;";
                            logger.debug(
                                    I18N.getString(
                                    "orbisgis.org.orbisgis.sif.simplePanel.validatingInterface") + sql); //$NON-NLS-1$
                            DataSource result = UIFactory.dsf.getDataSourceFromSQL(sql);
                            result.open();
                            long rowCount = result.getRowCount();
                            result.close();
                            if (rowCount == 0) {
                                err = errMsgs[i];
                                if (err == null) {
                                    err = I18N.getString(
                                            "orbisgis.org.orbisgis.sif.simplePanel.invalidInput"); //$NON-NLS-1$
                                }
                                break;
                            }

                        }
                    } catch (DriverLoadException e) {
                        logger.error(
                                I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.bugInSIF"),
                                     e); //$NON-NLS-1$
                        msgPanel.setError(I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.couldNotValidateDialog") //$NON-NLS-1$
                                + e.getMessage());
                    } catch (DataSourceCreationException e) {
                        logger.error(
                                I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.bugInSIF"),
                                     e); //$NON-NLS-1$
                        msgPanel.setError(I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.couldNotValidateDialog") //$NON-NLS-1$
                                + e.getMessage());
                    } catch (DriverException e) {
                        logger.error(
                                I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.bugInSIF"),
                                     e); //$NON-NLS-1$
                        msgPanel.setError(I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.couldNotValidateDialog") //$NON-NLS-1$
                                + e.getMessage());
                    } catch (ParseException e) {
                        logger.error(
                                I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.bugInSIF"),
                                     e); //$NON-NLS-1$
                        msgPanel.setError(I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.couldNotValidateDialog") //$NON-NLS-1$
                                + e.getMessage());
                    } catch (SemanticException e) {
                        logger.error(
                                I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.bugInSIF"),
                                     e); //$NON-NLS-1$
                        msgPanel.setError(I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.couldNotValidateDialog") //$NON-NLS-1$
                                + e.getMessage());
                    } catch (NoSuchTableException e) {
                        logger.error(
                                I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.bugInSIF"),
                                     e); //$NON-NLS-1$
                        msgPanel.setError(I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.couldNotValidateDialog") //$NON-NLS-1$
                                + e.getMessage());
                    }
                }
            }
        }

        if (err != null) {
            msgPanel.setError(err);
            frame.cannotContinue();
        } else {
            msgPanel.setText(panel.getInfoText());
            frame.canContinue();
        }
    }

    private void registerUISource(SQLUIPanel sqlPanel) {
        MemoryDataSetDriver omd = new MemoryDataSetDriver(
                sqlPanel.getFieldNames(), getGDMSTypes(sqlPanel.getFieldTypes()));
                try {
                        omd.addValues(getGDMSValues(sqlPanel.getValues(),
                                                    sqlPanel.getFieldTypes()));
                } catch (DriverException ex) {
                }
        if (UIFactory.dsf.exists(dsName)) {
            UIFactory.dsf.remove(dsName);
        }
        UIFactory.dsf.registerDataSource(dsName,
                                         new MemorySourceDefinition(omd, DriverManager.DEFAULT_SINGLE_TABLE_NAME));
    }

    private Value[] getGDMSValues(String[] values, int[] types) {
        Value[] row = new Value[values.length];
        for (int i = 0; i < row.length; i++) {
            if (values[i] == null) {
                row[i] = ValueFactory.createNullValue();
            } else {
                if (values[i].length() == 0) {
                    row[i] = ValueFactory.createNullValue();
                } else {
                    switch (types[i]) {
                        case SQLUIPanel.STRING:
                            row[i] = ValueFactory.createValue(values[i]);
                            break;
                        case SQLUIPanel.INT:
                            row[i] = ValueFactory.createValue(Integer.parseInt(
                                    values[i]));
                            break;
                        case SQLUIPanel.DOUBLE:
                            row[i] = ValueFactory.createValue(Double.parseDouble(
                                    values[i]));
                            break;
                    }
                }
            }
        }
        return row;
    }

    private Type[] getGDMSTypes(int[] fieldTypes) {
        Type[] types = new Type[fieldTypes.length];
        for (int i = 0; i < types.length; i++) {
            switch (fieldTypes[i]) {
                case SQLUIPanel.DOUBLE:
                    types[i] = TypeFactory.createType(Type.DOUBLE);
                    break;
                case SQLUIPanel.INT:
                    types[i] = TypeFactory.createType(Type.INT);
                    break;
                case SQLUIPanel.STRING:
                    types[i] = TypeFactory.createType(Type.STRING);
                    break;
            }
        }

        return types;
    }

    public void saveInput() {
        if (panel instanceof SQLUIPanel) {
            SQLUIPanel sqlPanel = (SQLUIPanel) panel;
            String id = sqlPanel.getId();
            if (id != null) {
                if (sqlPanel.getFieldNames().length > 0) {
                    registerUISource(sqlPanel);
                    try {
                        createLastInput(sqlPanel);
                        registerLastInput(id);
                        DataSource ds = UIFactory.dsf.getDataSource(LAST_INPUT);
                        ds.open();
                        ds.insertEmptyRow();
                        String[] values = sqlPanel.getValues();
                        for (int i = 0; i < values.length; i++) {
                            ds.setString(0, i, values[i]);
                        }
                        ds.commit();
                        ds.close();
                    } catch (DriverException e) {
                        logger.error(
                                I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.errorSavingSIFInput"),
                                     e); //$NON-NLS-1$
                        msgPanel.setError(
                                I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.cannotSaveInput")); //$NON-NLS-1$
                    } catch (NonEditableDataSourceException e) {
                        logger.error(
                                I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.errorSavingSIFInput"),
                                     e); //$NON-NLS-1$
                        throw new RuntimeException(I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.bug"), e); //$NON-NLS-1$
                    } catch (DriverLoadException e) {
                        logger.error(
                                I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.errorSavingSIFInput"),
                                     e); //$NON-NLS-1$
                        throw new RuntimeException(I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.bug"), e); //$NON-NLS-1$
                    } catch (NoSuchTableException e) {
                        logger.error(
                                I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.errorSavingSIFInput"),
                                     e); //$NON-NLS-1$
                        throw new RuntimeException(I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.bug"), e); //$NON-NLS-1$
                    } catch (DataSourceCreationException e) {
                        logger.error(
                                I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.errorSavingSIFInput"),
                                     e); //$NON-NLS-1$
                        msgPanel.setError(
                                I18N.getString(
                                "orbisgis.org.orbisgis.sif.simplePanel.cannotSaveInput")); //$NON-NLS-1$
                    }
                }
            }
        }
    }

    private void createLastInput(SQLUIPanel sqlPanel) throws DriverException {
        File lastInputFile = getLastInputFile(sqlPanel.getId());
        if (lastInputFile.exists()) {
            lastInputFile.delete();
        }
        FileSourceCreation fsc = new FileSourceCreation(lastInputFile,
                                                        getMetadata(sqlPanel));
        UIFactory.dsf.getSourceManager().createDataSource(fsc);
    }

    private Metadata getMetadata(SQLUIPanel sqlPanel) throws DriverException {
        DefaultMetadata ddm = new DefaultMetadata();
        String[] names = sqlPanel.getFieldNames();
        for (int i = 0; i < names.length; i++) {
            ddm.addField(names[i], Type.STRING);
        }

        return ddm;
    }

    private void registerLastInput(String id) {
        if (UIFactory.dsf.exists(LAST_INPUT)) {
            UIFactory.dsf.remove(LAST_INPUT);
        }
        UIFactory.dsf.getSourceManager().register(LAST_INPUT,
                                                  getLastInputFile(id));
    }

    private File getLastInputFile(String id) {
        return new File(UIFactory.baseDir, id + "-last.csv"); //$NON-NLS-1$
    }

    public boolean loadInput(HashMap<String, String> inputs) {
        if (panel instanceof SQLUIPanel) {
            SQLUIPanel sqlPanel = (SQLUIPanel) panel;
            String id = sqlPanel.getId();
            if (id != null) {
                String inputName = inputs.get(id);
                if (inputName != null) {
                    PersistentPanelDecorator pd = new PersistentPanelDecorator(
                            sqlPanel);
                    return pd.loadEntry(inputName);
                } else {
                    File lastInputFile = getLastInputFile(id);
                    if (lastInputFile.exists()) {
                        registerLastInput(id);
                        try {
                            DataSource ds = UIFactory.dsf.getDataSource(
                                    LAST_INPUT);
                            ds.open();
                            if (ds.getRowCount() > 0) {
                                String[] fieldNames = ds.getFieldNames();
                                for (String fieldName : fieldNames) {
                                    sqlPanel.setValue(fieldName, ds.getString(
                                            0, fieldName));
                                }
                            }
                            ds.close();
                        } catch (DriverException e) {
                            logger.error(
                                    I18N.getString(
                                    "orbisgis.org.orbisgis.sif.simplePanel.errorRestoringSIFInput"),
                                         e); //$NON-NLS-1$
                            msgPanel.setError(
                                    I18N.getString(
                                    "orbisgis.org.orbisgis.sif.simplePanel.cannotRestoreLastInput")); //$NON-NLS-1$
                        } catch (DriverLoadException e) {
                            logger.error(
                                    I18N.getString(
                                    "orbisgis.org.orbisgis.sif.simplePanel.errorRestoringSIFInput"),
                                         e); //$NON-NLS-1$
                            msgPanel.setError(
                                    I18N.getString(
                                    "orbisgis.org.orbisgis.sif.simplePanel.cannotRestoreLastInput")); //$NON-NLS-1$
                        } catch (NoSuchTableException e) {
                            logger.error(
                                    I18N.getString(
                                    "orbisgis.org.orbisgis.sif.simplePanel.errorRestoringSIFInput"),
                                         e); //$NON-NLS-1$
                            msgPanel.setError(
                                    I18N.getString(
                                    "orbisgis.org.orbisgis.sif.simplePanel.cannotRestoreLastInput")); //$NON-NLS-1$
                        } catch (DataSourceCreationException e) {
                            logger.error(
                                    I18N.getString(
                                    "orbisgis.org.orbisgis.sif.simplePanel.errorRestoringSIFInput"),
                                         e); //$NON-NLS-1$
                            msgPanel.setError(
                                    I18N.getString(
                                    "orbisgis.org.orbisgis.sif.simplePanel.cannotRestoreLastInput")); //$NON-NLS-1$
                        }
                    }
                }
            }
        }
        return false;
    }

    public ImageIcon getIcon() {
        URL iconURL = panel.getIconURL();
        if (iconURL == null) {
            iconURL = UIFactory.getDefaultIcon();
        }

        if (iconURL != null) {
            return new ImageIcon(iconURL);
        } else {
            return null;
        }
    }

    public Image getIconImage() {
        ImageIcon ii = getIcon();
        if (ii == null) {
            return null;
        } else {
            return ii.getImage();
        }
    }

    public boolean postProcess() {
        String ret = panel.postProcess();
        if (ret == null) {
            return true;
        } else if (ret.equals(CANCELED_ACTION)) {
            // no message, just cancel the action!
            return false;
        } else {
            JOptionPane.showMessageDialog(UIFactory.getMainFrame(), ret);
            return false;
        }
    }
}
