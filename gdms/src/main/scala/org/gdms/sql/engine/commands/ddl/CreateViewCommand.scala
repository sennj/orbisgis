/**
 * The GDMS library (Generic Datasource Management System)
 * is a middleware dedicated to the management of various kinds of
 * data-sources such as spatial vectorial data or alphanumeric. Based
 * on the JTS library and conform to the OGC simple feature access
 * specifications, it provides a complete and robust API to manipulate
 * in a SQL way remote DBMS (PostgreSQL, H2...) or flat files (.shp,
 * .csv...). It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 *
 * Team leader : Erwan BOCHER, scientific researcher,
 *
 * User support leader : Gwendall Petit, geomatic engineer.
 *
 * Previous computer developer : Pierre-Yves FADET, computer engineer, Thomas LEDUC,
 * scientific researcher, Fernando GONZALEZ CORTES, computer engineer, Maxence LAURENT,
 * computer engineer.
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * Copyright (C) 2010 Erwan BOCHER, Alexis GUEGANNO, Maxence LAURENT, Antoine GOURLAY
 *
 * Copyright (C) 2012 Erwan BOCHER, Antoine GOURLAY
 *
 * This file is part of Gdms.
 *
 * Gdms is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Gdms is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Gdms. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 *
 * or contact directly:
 * info@orbisgis.org
 */
package org.gdms.sql.engine.commands.ddl

import org.gdms.data.sql.SQLSourceDefinition
import org.gdms.sql.engine.SQLStatement
import org.gdms.sql.engine.SemanticException
import org.gdms.sql.engine.commands.Command
import org.gdms.sql.engine.commands.OutputCommand
import org.gdms.sql.engine.operations.Operation
import org.gdms.sql.engine.GdmSQLPredef._
import org.orbisgis.progress.ProgressMonitor

/**
 * Creates a view from some query.
 * 
 * @param table name of the view
 * @param sql sql string corresponding to the view query
 * @param op operation tree of the view query
 * @param orReplace true if any existing table with the same name has to be silently replaced
 * @author Antoine Gourlay
 * @since 0.1
 */
class CreateViewCommand(table: String, sql: String, op: Operation, orReplace: Boolean) extends Command with OutputCommand {
  
  override def doPrepare = {
    // checks that there is no table with this name
    if (!orReplace && dsf.getSourceManager.exists(table)) {
      throw new SemanticException("There already is a registered table named '" + table + "'.")
    }
  }

  protected final def doWork(r: Iterator[RowStream])(implicit pm: Option[ProgressMonitor]) = {
    val s = new SQLStatement(sql, op)(dsf.getProperties)
    
    if (orReplace) {
      dsf.getSourceManager.remove(table)
    }
    
    dsf.getSourceManager.register(table, new SQLSourceDefinition(s))

    Iterator.empty
  }
  
  val getResult = null

  // no result
  override val getMetadata = null
}