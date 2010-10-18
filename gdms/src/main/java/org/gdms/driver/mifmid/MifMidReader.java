/*
 * Library offering read and write capabilities for MifMid format
 * Copyright (C) 2009 Micha�l MICHAUD
 * michael.michaud@free.fr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.gdms.driver.mifmid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gdms.data.metadata.DefaultMetadata;
import org.gdms.data.types.Type;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.generic.GenericObjectDriver;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.util.GeometricShapeFactory;

/**
 * Classe permettant de lire et d'�crire un fichier mif et un fichier mid. Cette
 * classe est con�ue pour fonctionner avec les biblioth�ques JTS et JUMP de
 * com.vividsolutions.
 * 
 * @author Micha�l Michaud
 * @version 0.4.3
 */

// History
// version 0.2 (2004-05-15)
// version 0.3 (2005-07-05) : am�lioration du parser MID
// la nouvelle regex pour CSV est tiree du livre Mastering Regular Expression
// dans la collection O'Reilly)
// version 0.3.1 (2005-10-27) : r�soud le probl�me des r�gions poss�dant des
// contours d�finis sur 2 points distincts seulement en ignorant ces derniers
// version 0.3.2 (2005-12-15)
// version 0.4 (2007-08-26) : make it compatible with OpenJUMP 1.2D
// version 0.4.2 (2007-09-15) : change date format from yyyy/MM/dd to yyyyMMdd
// version 0.4.3 (2009-06-13) : fix a bug (pCSV Pattern was not initialized)

public class MifMidReader {

	// **************************************************************************
	// **************************************************************************
	// ******************** Les variables ******************
	// **************************************************************************
	// **************************************************************************

	// **************************************************************************
	// ******************** variables statiques ******************
	// **************************************************************************
	static String lineSeparator = System.getProperty("line.separator");
	private final static Logger log = Logger.getLogger("drivers.mifmid.MifMid");
	private FileHandler logFileHandler = null;
	private final static DateFormat DATE_PARSER = new SimpleDateFormat(
			"yyyyMMdd");

	// **************************************************************************
	// ******************** variables globales ******************
	// **************************************************************************

	private final File mifFile;
	private final File midFile;
	private final RandomAccessFile mifRaf;
	private final RandomAccessFile midRaf;
	private final java.util.logging.Formatter onlyMessage = new java.util.logging.Formatter() {
		public String format(LogRecord record) {
			return record.getMessage() + "\n";
		}
	};
	private final Formatter simpleFormatter = new SimpleFormatter();
	private long fileLength = 0L;
	private int percent = 0;
	private int nbFeatures = 0;

	String version = "300";
	String charset = "WindowsLatin1";
	String delimiter = "\t";
	String unique = null;
	String index = null;
	CoordSys coordSys = null;
	String transform = null;
	PrecisionModel pm = new PrecisionModel();
	ArrayList<String> headerLines = new ArrayList<String>(); // Stocke les
																// lignes du
																// header tel
																// quel
	// pour une copie conforme dans un
	// autre fichier
	ArrayList<String> schemaLines = new ArrayList<String>(); // Stocke les
																// lignes du
																// schema tel
	// quel
	// pour une copie conforme dans un
	// autre fichier

	GeometryFactory factory = new GeometryFactory();
	DefaultMetadata schema = new DefaultMetadata();
	private boolean storeSymbols = false;
	ArrayList<Long> mifAdresses = null;
	ArrayList<Long> midAdresses = null;
	// private Pattern pCSV;
	private Pattern pCSV = Pattern
			.compile(
					"  \\G            #fin du match pr�c�dent                          \n"
							+ "  (?:^|\\t)      #d�but de ligne ou tabulation                    \n"
							+ "  (?:            #champ entre guillemets                          \n"
							+ "     \"                                                           \n"
							+ "     ( (?>[^\"]*+) (?>\"\"[^\"]*+)*+ )                            \n"
							+ "     \"                                                           \n"
							+ "  |              # ou sans guillemet                              \n"
							+ "     ([^\"\\t]*+)                                                 \n"
							+ "  )                                                               \n",
					Pattern.COMMENTS);
	private Pattern pQuote = Pattern.compile("\"\"");
	int rowCount;;

	// **************************************************************************
	// **************************************************************************
	// ******************** Les constructeurs ********************
	// **************************************************************************
	// **************************************************************************

	/**
	 * Creates and initialises a MifMid object with the name of the Mif file to
	 * read from ot to write to.
	 * 
	 * @param file
	 *            the Mif file
	 */
	public MifMidReader(File mifFile, boolean storeSymbols) throws IOException {
		this.mifFile = mifFile;
		char[] midc = mifFile.getCanonicalPath().toCharArray();
		midc[midc.length - 1] = midc[midc.length - 1] == 'f' ? 'd' : 'D';
		midFile = new File(new String(midc));
		this.storeSymbols = storeSymbols;
		fileLength = mifFile.length();
		pm = new PrecisionModel();
		try {
			mifRaf = new RandomAccessFile(mifFile, "rw");
			midRaf = new RandomAccessFile(midFile, "rw");
			// setLogFile("MifMid.log", Level.FINEST);
		} catch (IOException ioe) {
			throw ioe;
		}
		log.setLevel(Level.INFO);
		log.info("Ouverture du fichier " + mifFile.toString());
	}

	protected void finalize() throws Throwable {
		if (mifRaf != null)
			mifRaf.close();
		if (midRaf != null)
			midRaf.close();
	}

	// **************************************************************************
	// **************************************************************************
	// ******************** Les m�thodes ********************
	// **************************************************************************
	// **************************************************************************

	public void close() {
		try {
			mifRaf.close();
			midRaf.close();
			if (logFileHandler != null)
				logFileHandler.close();
		} catch (IOException ioe) {
			log.warning("Impossible de fermer le fichier");
			if (logFileHandler != null)
				logFileHandler.close();
			ioe.printStackTrace();
		}
	}

	public void clear() {
		try {
			mifRaf.setLength(0L);
			midRaf.setLength(0L);
		} catch (IOException ioe) {
			log.warning("Impossible de vider le fichier");
			ioe.printStackTrace();
		}
	}

	/**
	 * Crée un fichier de log.
	 */
	public void setLogFile(String logFileName, Level level) throws IOException {
		try {
			logFileHandler = new FileHandler(logFileName);
			logFileHandler.setFormatter(simpleFormatter);
			logFileHandler.setLevel(level);
			log.setLevel(level);
			log.addHandler(logFileHandler);
		} catch (IOException ioe) {
			throw ioe;
		}
	}

	/**
	 * Read global settings in the header of a Mif file, and return them as a
	 * Properties object.
	 * 
	 * @param file
	 *            file to read
	 * @return a Properties object
	 */
	public void readMMFileProperties() throws IOException,
			FileNotFoundException {
		if (logFileHandler != null)
			logFileHandler.setFormatter(onlyMessage);
		// properties = new Properties();
		try {
			String line;
			mifRaf.seek(0);
			while (null != (line = mifRaf.readLine())) {
				// percent = (int)((mifRaf.getFilePointer()*100L)/fileLength);
				String lineU = line.toUpperCase().trim();
				if (lineU.startsWith("VERSION")) {
					headerLines.add(line);
					String[] versionP = line.split(" ");
					if (versionP.length > 1) {
						version = versionP[1];
						log.fine("   - Version MapInfo = " + versionP[1]);
					}
				} else if (lineU.startsWith("CHARSET")) {
					headerLines.add(line);
					String[] charsetP = line.split(" ");
					if (charsetP.length > 1) {
						charset = charsetP[1];
						log.fine("   - Jeu de caract�res = " + charset);
					}
				} else if (lineU.startsWith("DELIMITER")) {
					headerLines.add(line);
					StringTokenizer st = new StringTokenizer(line);
					st.nextToken();
					delimiter = st.nextToken().substring(1, 2);
					log.fine("   - D�limiteur = "
							+ delimiter
							+ " (0x"
							+ Integer
									.toHexString((int) delimiter.getBytes()[0])
							+ ")");
					setDelimiter(delimiter);
				} else if (lineU.startsWith("UNIQUE")) {
					headerLines.add(line);
					String[] uniqueP = line.split(" ", 2);
					if (uniqueP.length > 1) {
						unique = uniqueP[1];
						log.fine("   - Unique = " + unique);
					}
				} else if (lineU.startsWith("INDEX")) {
					headerLines.add(line);
					String[] uniqueP = line.split(" ", 2);
					if (uniqueP.length > 1) {
						unique = uniqueP[1];
						log.fine("   - Indexes = " + unique);
					}
				} else if (lineU.startsWith("COORDSYS")) {
					headerLines.add(line);
					CoordSys coordSys = new CoordSys(line);
					log.fine("   - CoordSys = " + coordSys.toString());
				} else if (lineU.startsWith("TRANSFORM")) {
					headerLines.add(line);
					String[] transformP = line.split(" ", 2);
					if (transformP.length > 1) {
						transform = transformP[1];
						log.fine("   - Transform = " + transform);
					}
				} else if (lineU.startsWith("DATA")) {
					break;
				} else {
					return;
				}
			}
		} catch (FileNotFoundException fnfe) {
			throw fnfe;
		} catch (IOException ioe) {
			throw ioe;
		}
		return;
	}

	public File getMifFile() {
		return mifFile;
	}

	public File getMidFile() {
		return midFile;
	}

	public void setLogLevel(Level level) {
		log.setLevel(level);
	}

	public PrecisionModel getPrecisionModel() {
		return pm;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		// Regex for CSV file, taken from "Mastering Regular Expression"
		// O'Reilly - Jeffrey E.F. Friedl
		this.delimiter = delimiter;
		String sep = delimiter == "\t" ? "\\t" : delimiter;
		String regex = "  \\G                 #fin du match pr�c�dent                \n"
				+ "  (?:^|"
				+ sep
				+ ")   #d�but de ligne ou virgule             \n"
				+ "  (?:                 #champ entre guillements               \n"
				+ "     \"                                                      \n"
				+ "     ( (?>[^\"]*+) (?>\"\"[^\"]*+)*+ )                       \n"
				+ "     \"                                                      \n"
				+ "  |                   # ou sans guillemet                    \n"
				+ "     ([^\""
				+ sep
				+ "]*+)                                        \n"
				+ "  )                                                          \n";
		pCSV = Pattern.compile(regex, Pattern.COMMENTS);
	}

	public String getUnique() {
		return unique;
	}

	public void setUnique(String unique) {
		this.unique = unique;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public CoordSys getCoordSys() {
		return coordSys;
	}

	public void setCoordSys(CoordSys coordSys) {
		this.coordSys = coordSys;
	}

	public String getTransform() {
		return transform;
	}

	public void setTransform(String transform) {
		this.transform = transform;
	}

	public int getFeatureNumber() {
		return nbFeatures;
	}

	public boolean getStoreSymbols() {
		return storeSymbols;
	}

	public void setStoreSymbols(boolean symbols) {
		storeSymbols = symbols;
	}

	/**
	 * Read global settings in the header of a GeoConcept file, and return them
	 * as a Properties object.
	 * 
	 * @param file
	 *            file to read
	 * @return a Properties object
	 */
	public DefaultMetadata readMMFileFeatureSchema() throws IOException,
			FileNotFoundException, Exception {
		if (logFileHandler != null)
			logFileHandler.setFormatter(onlyMessage);
		try {
			String line;
			mifRaf.seek(0);
			while (null != (line = mifRaf.readLine())) {
				if (line.toUpperCase().startsWith("COLUMNS")) {
					schemaLines.add(line);
					schema.addField("the_geom", Type.GEOMETRY);
					schema.addField("MIF_TEXT", Type.STRING);
					StringTokenizer st = new StringTokenizer(line);
					if (st.hasMoreTokens())
						st.nextToken();
					int nbColumns = 0;
					if (st.hasMoreTokens()) {
						nbColumns = Integer.parseInt(st.nextToken());
					}
					log.info("Number of attributes = " + nbColumns);
					rowCount = nbColumns + 2;
					for (int i = 0; i < nbColumns; i++) {
						line = mifRaf.readLine();
						schemaLines.add(line);
						st = new StringTokenizer(line, " \t\n\r\f(,)");
						String name = st.nextToken();
						String typeA = st.nextToken().toUpperCase();
						if (typeA.startsWith("CHAR")) {
							schema.addField(name, Type.STRING);
							/*
							 * int nbchar =
							 * Integer.parseInt(st.nextToken(" (,)"));
							 * AttributeType charAttribute =
							 * AttributeType.createCharAttributeType(nbchar);
							 * schema.addAttribute(name, charAttribute);
							 */
						}
						// Un integer stock� dans un fichier MIF/MID semble
						// devoir ne pas d�passer 2^31-1
						else if (typeA.startsWith("INTEGER")) {
							schema.addField(name, Type.INT);
						} else if (typeA.startsWith("SMALLINT")) {
							schema.addField(name, Type.INT);
						} else if (typeA.startsWith("DECIMAL")) {
							schema.addField(name, Type.DOUBLE);
							/*
							 * int n = Integer.parseInt(st.nextToken(" (,)"));
							 * int d = Integer.parseInt(st.nextToken(" (,)"));
							 * AttributeType decimalAttribute =
							 * AttributeType.createDecimalAttributeType(n,d);
							 * schema.addAttribute(name, decimalAttribute);
							 */
						} else if (typeA.startsWith("FLOAT")) {
							schema.addField(name, Type.DOUBLE);
						} else if (typeA.startsWith("DATE")) {
							schema.addField(name, Type.DATE);
						} else if (typeA.startsWith("LOGICAL")) {
							schema.addField(name, Type.INT);
							/* schema.addAttribute(name, AttributeType.BOOLEAN); */
						} else {
							throw new Exception("Unknown attribute type : "
									+ typeA);
						}
						// log.info("" + i + name + " : " + typeA);
					}
					if (storeSymbols) {
						schema.addField("@SYMBOL", Type.STRING);
						schema.addField("@PEN", Type.STRING);
						schema.addField("@BRUSH", Type.STRING);
						schema.addField("@SMOOTH", Type.STRING);
						// schema.addAttribute("@CENTER",AttributeType.STRING);
					}
					break;
				} else if (line.toUpperCase().startsWith("DATA")) {
					schemaLines.add(line);
					break;
				} else {
				}
				;
			}
		} catch (FileNotFoundException fnfe) {
			throw fnfe;
		} catch (IOException ioe) {
			throw ioe;
		}

		return schema;
	}

	/**
	 * Creates a Map of indexes for each type/subtype. An indexe is a list of
	 * adresses
	 * 
	 * @param file
	 *            the file to read and to indexe
	 * @return the number of objects in the mif
	 */
	public int createIndexes() throws IOException, FileNotFoundException,
			Exception {
		if (logFileHandler != null)
			logFileHandler.setFormatter(onlyMessage);
		if (mifAdresses != null || midAdresses != null) {
			nbFeatures = 0;
			return mifAdresses.size();
		}
		mifAdresses = new ArrayList<Long>();
		midAdresses = new ArrayList<Long>();
		try {
			String line;
			long offsetMif = 0;
			mifRaf.seek(offsetMif);
			while (null != (line = mifRaf.readLine())) {
				String type = line.trim().toUpperCase();
				if (type.startsWith("DATA")) {
					offsetMif = mifRaf.getFilePointer();
					while (null != (line = mifRaf.readLine())) {
						type = line.trim().toUpperCase();
						if (type.startsWith("POINT")
								|| type.startsWith("PLINE")
								|| type.startsWith("REGION")
								|| type.startsWith("LINE")
								|| type.startsWith("NONE")
								|| type.startsWith("ARC")
								|| type.startsWith("TEXT")
								|| type.startsWith("RECT")
								|| type.startsWith("ROUNDRECT")
								|| type.startsWith("ELLIPSE")
								|| type.startsWith("MULTIPOINT")
								|| type.startsWith("COLLECTION")) {
							mifAdresses.add(new Long(offsetMif));
						}
						offsetMif = mifRaf.getFilePointer();
					}
				}
				offsetMif = mifRaf.getFilePointer();
			}
			long offsetMid = 0;
			midRaf.seek(offsetMid);
			while (null != (line = midRaf.readLine())) {
				midAdresses.add(new Long(offsetMid));
				offsetMid = midRaf.getFilePointer();
			}
		} catch (FileNotFoundException fnfe) {
			throw fnfe;
		} catch (IOException ioe) {
			throw ioe;
		} catch (Exception e) {
			throw e;
		}
		// System.out.println(mifAdresses.size());
		nbFeatures = mifAdresses.size();
		return nbFeatures;
	}

	/**
	 * Return the feature number i of the class class if it exists.
	 * 
	 * @param i
	 *            the object number
	 * @return a Feature
	 */
	public Value[] getValue(int index) throws IOException, Exception {
		if (logFileHandler != null)
			logFileHandler.setFormatter(onlyMessage);
		Value[] values = new Value[rowCount];
		long addressAttributes = midAdresses.get(index).longValue();
		// System.out.print(index + ".");
		int attType = -1;
		String attribute = null;
		try {
			midRaf.seek(addressAttributes);
			String[] attributes = parseMidLine(midRaf.readLine());
			for (int i = 0; i < attributes.length; i++) {
				log.finer(attributes[i]);
				attType = schema.getFieldType(i + 2).getTypeCode();
				attribute = attributes[i];
				// if (attType == AttributeType.STRING || attType instanceof
				// AttributeType.Char){
				if (attType == Type.STRING) {
					values[(i + 2)] = ValueFactory.createValue(attributes[i]);
					// feature.setAttribute((i+2),
					// attribute.substring(1,attribute.length()-1));
				} else if (attType == Type.DATE) {
					if (attributes[i].equals("\"\"")
							|| attributes[i].equals("")) {
						values[(i + 2)] = ValueFactory.createNullValue();
					} else if (attributes[i].startsWith("\"")) {
						values[(i + 2)] = ValueFactory.createValue(DATE_PARSER
								.parse(attribute.trim().substring(1,
										attribute.trim().length() - 1)));
					} else {
						values[(i + 2)] = ValueFactory.createValue(DATE_PARSER
								.parse(attribute.trim()));
					}
				} else if (attributes[i].trim().equals("")) {
					values[(i + 2)] = ValueFactory.createNullValue();
				}
				/*
				 * else if (attType == AttributeType.BOOLEAN){ char c =
				 * attribute.toUpperCase().charAt(0); if (c=='T' || c=='V' ||
				 * c=='O') { feature.setAttribute((i+2), new Boolean(true)); }
				 * else {feature.setAttribute((i+2), new Boolean(false));} }
				 */
				else if (attType == Type.INT) {
					if (attribute.equals("F"))
						values[(i + 2)] = ValueFactory.createValue(0);
					else if (attribute.equals("T"))
						values[(i + 2)] = ValueFactory.createValue(1);
					else
						values[(i + 2)] = ValueFactory.createValue(attribute);
				} else if (attType == Type.DOUBLE) {
					values[(i + 2)] = ValueFactory.createValue(new Double(
							attribute));
				}
				/*
				 * else if (attType instanceof AttributeType.Decimal){
				 * feature.setAttribute((i+2), new
				 * java.math.BigDecimal(attribute.trim())); }
				 */
				else {
				}
			}
		} catch (IOException ioe) {
			throw ioe;
		} catch (Exception e) {
			e.printStackTrace();
			// throw new Exception(e.getMessage() +
			// "\nParsing the MID file : exception at line " +
			// index + " trying to read " + attribute + " as " +
			// attType.toString());
		}
		try {
			long addressGeometry = mifAdresses.get(index).longValue();
			mifRaf.seek(addressGeometry);
			String geometry = mifRaf.readLine();
			StringTokenizer st = new StringTokenizer(geometry);
			String type = st.nextToken().toUpperCase();
			if (type.equals("NONE")) {
				values[0] = ValueFactory.createValue(factory
						.createGeometryCollection(new Geometry[0]));
			} else if (type.equals("POINT")) {
				double x = Double.parseDouble(st.nextToken());
				double y = Double.parseDouble(st.nextToken());
				Point p = new Point(new Coordinate(x, y, Double.NaN), pm, 0);
				values[0] = ValueFactory.createValue(p);
			} else if (type.equals("LINE")) {
				double x1 = Double.parseDouble(st.nextToken());
				double y1 = Double.parseDouble(st.nextToken());
				double x2 = Double.parseDouble(st.nextToken());
				double y2 = Double.parseDouble(st.nextToken());
				LineString line = new LineString(new Coordinate[] {
						new Coordinate(x1, y1, Double.NaN),
						new Coordinate(x2, y2, Double.NaN), }, pm, 0);
				values[0] = ValueFactory.createValue(line);
			} else if (type.equals("PLINE")) {
				int numSections = 1;
				int numPoints = -1;
				if (st.hasMoreTokens()) {
					String secondToken = st.nextToken();
					if (secondToken.toUpperCase().equals("MULTIPLE")) {
						numSections = Integer.parseInt(st.nextToken());
					} else
						numPoints = Integer.parseInt(secondToken);
				}

				LineString[] lines = new LineString[numSections];
				for (int i = 0; i < numSections; i++) {
					if (numPoints == -1 || i > 0) {
						numPoints = Integer.parseInt(mifRaf.readLine().trim());
					}
					Coordinate[] coordinates = new Coordinate[numPoints];
					for (int j = 0; j < numPoints; j++) {
						st = new StringTokenizer(mifRaf.readLine());
						double x = Double.parseDouble(st.nextToken());
						double y = Double.parseDouble(st.nextToken());
						coordinates[j] = new Coordinate(x, y, Double.NaN);
					}
					lines[i] = new LineString(coordinates, pm, 0);
				}
				if (numSections == 1) {
					values[0] = ValueFactory.createValue(lines[0]);
				} else if (numSections > 1) {
					values[0] = ValueFactory.createValue(new MultiLineString(
							lines, pm, 0));
				}
			} else if (type.equals("REGION")) {
				int numPolygons = 1;
				if (st.hasMoreTokens()) {
					numPolygons = Integer.parseInt(st.nextToken());
				} else {
					System.out.println("Erreur parsing a polygon");
				}
				Polygon[] polygons = new Polygon[numPolygons];
				for (int i = 0; i < numPolygons; i++) {
					int numPoints = Integer.parseInt(mifRaf.readLine().trim());
					Coordinate[] coordinates = new Coordinate[numPoints];
					for (int j = 0; j < numPoints; j++) {
						st = new StringTokenizer(mifRaf.readLine());
						double x = Double.parseDouble(st.nextToken());
						double y = Double.parseDouble(st.nextToken());
						coordinates[j] = new Coordinate(x, y, Double.NaN);
					}
					CoordinateList cl = new CoordinateList(coordinates);
					cl.closeRing();
					coordinates = cl.toCoordinateArray();
					try {
						polygons[i] = new Polygon(new LinearRing(coordinates,
								pm, 0), pm, 0);
					} catch (Exception e) {
						// Ignore an invalid polygon which has not been
						// constructed
					}
				}
				if (polygons[0] == null) {
					values[0] = ValueFactory.createValue(factory
							.createMultiPolygon(new Polygon[0]));
				} else {
					values[0] = ValueFactory
							.createValue(region2MultiPolygon(polygons));
				}
				// if (numPolygons==1) {feature.setGeometry(polygons[0]);}
				// else {feature.setGeometry(new MultiPolygon(polygons, pm,
				// 0));}
			} else if (type.equals("ARC")) {
				double x1 = Double.parseDouble(st.nextToken());
				double y1 = Double.parseDouble(st.nextToken());
				double x2 = Double.parseDouble(st.nextToken());
				double y2 = Double.parseDouble(st.nextToken());
				st = new StringTokenizer(mifRaf.readLine());
				double angleIni = Double.parseDouble(st.nextToken()) * Math.PI
						/ 180;
				double angleFin = Double.parseDouble(st.nextToken()) * Math.PI
						/ 180;
				double a = Math.max((x2 - x1), (x1 - x2));
				double b = Math.max((y2 - y1), (y1 - y2));
				double e = Math.sqrt(1 - ((b * b) / (a * a)));
				ArrayList<Coordinate> cc = new ArrayList<Coordinate>();
				double r = a
						* Math.sqrt((1 - (e * e))
								/ (1 - (e * e * Math.cos(angleIni) * Math
										.cos(angleIni))));
				Coordinate coord = new Coordinate((((x1 + x2) / 2) + r
						* Math.cos(angleIni)), (((y1 + y2) / 2) + r
						* Math.sin(angleIni)), Double.NaN);
				cc.add(coord);
				for (int i = 0; i < 24; i++) {
					double angle = Math.PI * (double) i / 12.0;
					if ((angleFin > angleIni && angle > angleIni && angle < angleFin)
							|| (angleFin < angleIni && (angle >= angleIni || angle < angleFin))) {
						coord = new Coordinate((((x1 + x2) / 2) + r
								* Math.cos(Math.PI * i / 12.0)),
								(((y1 + y2) / 2) + r
										* Math.sin(Math.PI * i / 12.0)),
								Double.NaN);
						cc.add(coord);
					}
				}
				LineString line = new LineString((Coordinate[]) cc
						.toArray(new Coordinate[0]), pm, 0);
				values[0] = ValueFactory.createValue(line);
			} else if (type.equals("TEXT")) {
				String text = st.nextToken();
				st = new StringTokenizer(mifRaf.readLine());
				double x1 = Double.parseDouble(st.nextToken());
				double y1 = Double.parseDouble(st.nextToken());
				// double x2 = Double.parseDouble(st.nextToken());
				// double y2 = Double.parseDouble(st.nextToken());
				values[0] = ValueFactory.createValue(new GeometryFactory()
						.createPoint(new Coordinate(x1, y1)));
				values[1] = ValueFactory.createValue(text);
				return values;
			} else if (type.equals("RECT")) {
				double x1 = Double.parseDouble(st.nextToken());
				double y1 = Double.parseDouble(st.nextToken());
				double x2 = Double.parseDouble(st.nextToken());
				double y2 = Double.parseDouble(st.nextToken());
				GeometricShapeFactory gsf = new GeometricShapeFactory(
						new GeometryFactory());
				gsf.setCentre(new Coordinate((x1 + x2) / 2, (y1 + y2) / 2));
				gsf.setWidth(Math.abs(x2 - x1));
				gsf.setHeight(Math.abs(x2 - x1));
				values[0] = ValueFactory.createValue(gsf.createRectangle());
				// feature.setAttribute("MIF_TEXT", text);
				return values;
			} else if (type.equals("ROUNDRECT")) {
				double x1 = Double.parseDouble(st.nextToken());
				double y1 = Double.parseDouble(st.nextToken());
				double x2 = Double.parseDouble(st.nextToken());
				double y2 = Double.parseDouble(st.nextToken());
				GeometricShapeFactory gsf = new GeometricShapeFactory(
						new GeometryFactory());
				gsf.setCentre(new Coordinate((x1 + x2) / 2, (y1 + y2) / 2));
				gsf.setWidth(Math.abs(x2 - x1));
				gsf.setHeight(Math.abs(x2 - x1));
				values[0] = ValueFactory.createValue(gsf.createRectangle());
				// feature.setAttribute("MIF_TEXT", text);
				return values;
			} else if (type.equals("ELLIPSE")) {
				double x1 = Double.parseDouble(st.nextToken());
				double y1 = Double.parseDouble(st.nextToken());
				double x2 = Double.parseDouble(st.nextToken());
				double y2 = Double.parseDouble(st.nextToken());
				GeometricShapeFactory gsf = new GeometricShapeFactory(
						new GeometryFactory());
				gsf.setCentre(new Coordinate((x1 + x2) / 2, (y1 + y2) / 2));
				gsf.setWidth(Math.abs(x2 - x1));
				gsf.setHeight(Math.abs(x2 - x1));
				gsf.setNumPoints(32);
				values[0] = ValueFactory.createValue(gsf.createCircle());
				// feature.setAttribute("MIF_TEXT", text);
				return values;
			}
			// Multi Points are not handled
			else if (type.equals("MULTIPOINT")) {
				//double x1 = Double.parseDouble(st.nextToken());
				//double y1 = Double.parseDouble(st.nextToken());
				//double x2 = Double.parseDouble(st.nextToken());
				//double y2 = Double.parseDouble(st.nextToken());
				// int numSections = 1;
				int numPoints = -1;
				if (st.hasMoreTokens()) {
					numPoints = Integer.parseInt(st.nextToken());
				}
				Point[] pp = new Point[numPoints];
				for (int i = 0; i < numPoints;) {
					// Other points to read on the same line
					if (st.hasMoreTokens()) {
						double x = Double.parseDouble(st.nextToken());
						double y = Double.parseDouble(st.nextToken());
						pp[i++] = new Point(new Coordinate(x, y), pm, 0);
					}
					// Other points to read on the following line
					else if (i < numPoints) {
						st = new StringTokenizer(mifRaf.readLine());
					}
					// No more point
					else
						break;
				}
				values[0] = ValueFactory.createValue(new MultiPoint(pp, pm, 0));
				return values;
			}
			// Collections are not handled
			else if (type.equals("COLLECTION")) {
				// double x1 = Double.parseDouble(st.nextToken());
				// double y1 = Double.parseDouble(st.nextToken());
				// double x2 = Double.parseDouble(st.nextToken());
				// double y2 = Double.parseDouble(st.nextToken());
				// return feature;
				throw new Exception(
						"An error occured reading the object "
								+ index
								+ " of the mif file (byte "
								+ addressGeometry
								+ ")\n"
								+ "The parser for collection type objects has not been implemented");
			} else {
			}
			String app;
			while (null != (app = mifRaf.readLine())) {
				if (!storeSymbols)
					break;
				if (index < (mifAdresses.size() - 1)
						&& mifRaf.getFilePointer() > mifAdresses.get(index + 1)
								.longValue())
					break;
				if (app.trim().toUpperCase().startsWith("SYMBOL")) {
					st = new StringTokenizer(app);
					st.nextToken();
					// TODO be carefull index
					values[4] = ValueFactory.createValue(st.nextToken("")
							.trim());
				} else if (app.trim().toUpperCase().startsWith("PEN")) {
					st = new StringTokenizer(app);
					st.nextToken();
					values[4] = ValueFactory.createValue(st.nextToken("")
							.trim());
				} else if (app.trim().toUpperCase().startsWith("BRUSH")) {
					st = new StringTokenizer(app);
					st.nextToken();
					values[4] = ValueFactory.createValue(st.nextToken("")
							.trim());
				} else if (app.trim().toUpperCase().startsWith("SMOOTH")) {
					st = new StringTokenizer(app);
					st.nextToken();
					values[4] = ValueFactory.createValue(st.nextToken("")
							.trim());
				} else
					;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception e) {
			// e.printStackTrace();
			throw e;
			// throw new Exception("Error while parsing geometry : " +
			// Integer.toString(index));
		}
		/*
		 * log.finest( feature.getGeometry()); for (int i = 1; i <
		 * schema.getAttributeCount(); i++) { log.finest("   - " +
		 * schema.getAttributeName(i) + "=" + feature.getAttribute(i)); }
		 */
		percent = (int) ((mifRaf.getFilePointer() * 100L) / fileLength);
		return values;
	}

	/**
	 * Transform un je de polygones parfois inclus les un dans les autres, en un
	 * MultiPolygone, �ventuellement trou�
	 */
	private Geometry region2MultiPolygon(Polygon[] polygons) throws Exception {
		if (polygons == null || polygons.length == 0)
			throw new Exception("Try to convert a null Region into a Polygon");
		if (polygons[0].isEmpty())
			throw new Exception(
					"First Region has been converted into an empty Polygon");
		ArrayList<Polygon> finalPolys = new ArrayList<Polygon>();
		finalPolys.add(polygons[0]);
		for (int i = 1; i < polygons.length; i++) {
			if (polygons[i] == null)
				continue; // continue if the polygon has been made invalif by
			// the parser
			for (int p = 0; p < finalPolys.size(); p++) {
				Polygon currentPoly = (Polygon) finalPolys.get(p);
				if (currentPoly.contains(polygons[i])) {
					LinearRing[] holes = new LinearRing[currentPoly
							.getNumInteriorRing() + 1];
					for (int h = 0; h < holes.length - 1; h++) {
						holes[h] = (LinearRing) currentPoly.getInteriorRingN(h);
					}
					holes[holes.length - 1] = (LinearRing) polygons[i]
							.getExteriorRing();
					finalPolys.set(p, new GeometryFactory().createPolygon(
							(LinearRing) currentPoly.getExteriorRing(), holes));
					polygons[i] = null;
					break;
				} else {
				}
			}
			if (polygons[i] != null)
				finalPolys.add(polygons[i]);
		}
		if (finalPolys.size() == 1) {
			return (Polygon) finalPolys.get(0);
		} else {
			return new GeometryFactory()
					.createMultiPolygon((Polygon[]) finalPolys
							.toArray(new Polygon[0]));
		}
	}

	/**
	 * D�compose une ligne du fichier MID La difficult� r�side dans le fait
	 * qu'un s�parateur de champ peut �tre inclu dans une cha�ne de caract�res.
	 * 
	 * @param sep
	 *            caract�re s�parateur de champ (ex. ,)
	 * @param quote
	 *            caract�re encadrant les cha�nes de caract�res (ex. ")
	 * @return a string array
	 */
	public String[] parseMidLine(String line) {
		
		ArrayList<String> list = new ArrayList<String>();
		// String regex = "[\\\"].*?[\\\"]|[^"+sep+"\\\"]*";
		// Regex for CSV file, taken from "Mastering Regular Expression"
		// O'Reilly - Jeffrey E.F. Friedl
		/*
		 * sep = sep=="\t"?"\\t":sep; String regex =
		 * "  \\G            #fin du match pr�c�dent                          \n"
		 * + "  (?:^|" + sep +
		 * ")        #d�but de ligne ou virgule                       \n"+
		 * "  (?:            #champ entre guillements                         \n"
		 * +
		 * "     \"                                                           \n"
		 * +
		 * "     ( (?>[^\"]*+) (?>\"\"[^\"]*+)*+ )                            \n"
		 * +
		 * "     \"                                                           \n"
		 * +
		 * "  |              # ou sans guillemet                              \n"
		 * +"     ([^\""+sep+
		 * "]*+)                                                   \n"+
		 * "  )                                                               \n"
		 * ; System.out.println("sep = '" + sep + "'");
		 * System.out.println(regex); Pattern pCSV = Pattern.compile(regex,
		 * Pattern.COMMENTS); Pattern pQuote = Pattern.compile("\"\"");
		 */
		Matcher mQuote = pQuote.matcher("");
		Matcher mCSV = pCSV.matcher("");
		mCSV.reset(line);
		// System.out.println(line);
		while (mCSV.find()) {
			String token;
			String first = mCSV.group(2);
			// test si token contient des guillemets
			if (first != null) {
				token = first;
			} else {
				mQuote.reset(mCSV.group(1));
				token = mQuote.replaceAll("\"");

			}
			list.add(token);
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	public void setPercent() throws IOException {
		try {
			percent = (int) ((mifRaf.getFilePointer() * 100L) / fileLength);
		} catch (IOException ioe) {
			throw ioe;
		}
	}

	/**
	 * Fixe la variable percent indiquant la position du curseur dans le fichier
	 * par rapport � la longueur totale du fichier en %.
	 */
	public void setPercent(int pc) {
		percent = pc;
	}

	/**
	 * Retourne la variable percent indiquant la position du curseur dans le
	 * fichier par rapport à la longueur totale du fichier en %.
	 */
	public int getPercent() throws IOException {
		try {
			if (mifRaf.getFD().valid()) {
				return (int) ((mifRaf.getFilePointer() * 100L) / fileLength);
			} else
				return 0;
		} catch (IOException ioe) {
			throw ioe;
		}
	}

	public GenericObjectDriver read() throws Exception {

		setLogFile("MifMid.log", Level.FINEST);
		readMMFileProperties();

		int rowCount = createIndexes();

		GenericObjectDriver driver = new GenericObjectDriver(
				readMMFileFeatureSchema());
		for (int i = 0; i < rowCount; i++) {
			driver.addValues(getValue(i));
		}

		close();
		
		return driver;
	}
}