/**
 * The GDMS library (Generic Datasource Management System)
 * is a middleware dedicated to the management of various kinds of
 * data-sources such as spatial vectorial data or alphanumeric. Based
 * on the JTS library and conform to the OGC simple feature access
 * specifications, it provides a complete and robust API to manipulate
 * in a SQL way remote DBMS (PostgreSQL, H2...) or flat files (.shp,
 * .csv...).
 *
 * Gdms is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV FR CNRS 2488
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
package org.gdms.sql.function.alphanumeric;

import org.apache.log4j.Logger;

import org.gdms.data.DataSourceFactory;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.sql.function.AbstractScalarFunction;
import org.gdms.sql.function.BasicFunctionSignature;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.FunctionSignature;
import org.gdms.sql.function.ScalarArgument;

/**
 * Replace all occurrences of a specific string into an other string.
 */
public class ReplaceString extends AbstractScalarFunction {

        private static final Logger LOG = Logger.getLogger(ReplaceString.class);

        @Override
        public Value evaluate(DataSourceFactory dsf, Value... arg0) throws FunctionException {
                LOG.trace("Evaluating");
                String text;
                if (arg0[0].isNull()) {
                        return ValueFactory.createNullValue();
                } else {
                        // Get the arguments <-- this (only) comment is really helpful...
                        text = arg0[0].getAsString();
                        String textFrom = arg0[1].getAsString();
                        String textTo = arg0[2].getAsString();

                        text = text.replace(textFrom, textTo);

                }
                return ValueFactory.createValue(text);

        }

        @Override
        public String getDescription() {

                return "Replace all occurrences of a specific string";
        }

        @Override
        public String getName() {

                return "Replace";
        }

        @Override
        public String getSqlOrder() {

                return "select Replace(string text, from text, to text) from myTable";
        }

        @Override
        public Type getType(Type[] arg0) {
                return TypeFactory.createType(Type.STRING);
        }

        @Override
        public FunctionSignature[] getFunctionSignatures() {
                return new FunctionSignature[]{
                                new BasicFunctionSignature(getType(null), 
                                        ScalarArgument.STRING,
                                        ScalarArgument.STRING,
                                        ScalarArgument.STRING
                                )
                        };
        }
}
