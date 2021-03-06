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
package org.orbisgis.core.ui.plugins.views.beanShellConsole.javaManager.autocompletion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.orbisgis.core.ui.plugins.views.beanShellConsole.javaManager.PackageReflection;
import org.orbisgis.core.ui.plugins.views.beanShellConsole.javaManager.parser.ASTAllocationExpression;
import org.orbisgis.core.ui.plugins.views.beanShellConsole.javaManager.parser.ASTClassOrInterfaceType;
import org.orbisgis.core.ui.plugins.views.beanShellConsole.javaManager.parser.ASTImportDeclaration;
import org.orbisgis.core.ui.plugins.views.beanShellConsole.javaManager.parser.ASTName;
import org.orbisgis.core.ui.plugins.views.beanShellConsole.javaManager.parser.ASTPrimaryExpression;
import org.orbisgis.core.ui.plugins.views.beanShellConsole.javaManager.parser.ASTPrimaryPrefix;
import org.orbisgis.core.ui.plugins.views.beanShellConsole.javaManager.parser.ASTPrimarySuffix;
import org.orbisgis.core.ui.plugins.views.beanShellConsole.javaManager.parser.ASTReferenceType;
import org.orbisgis.core.ui.plugins.views.beanShellConsole.javaManager.parser.Node;

public class AutoCompletionVisitor extends AbstractVisitor {

	private static final String EXCEPTION_MSG = "Code completion exception";

	private static Logger logger = Logger
			.getLogger(AutoCompletionVisitor.class);

	private TreeSet<Option> options = new TreeSet<Option>(
			new Comparator<Option>() {

				@Override
				public int compare(Option o1, Option o2) {
					return o1.getSortString().toLowerCase().compareTo(
							o2.getSortString().toLowerCase());
				}

			});

	public AutoCompletionVisitor() {
	}

	public void setCompletionCase(String text, Node cu, int line, int col) {
		CompletionUtils.setCompletionCase(text, cu, line, col);
		options.clear();
	}

	private void completeName(Node node) throws SecurityException,
			ClassNotFoundException, CannotAutocompleteException,
			NoSuchFieldException {
		String[] parts = CompletionUtils.getNodeUtils().getParts(node);
		Class<? extends Object> clazz = null;
		for (int i = 0; i < parts.length - 1; i++) {
			clazz = CompletionUtils.getType(clazz, parts[i]);
		}

		if (clazz != null) {
			completeMembersOfClass(parts[parts.length - 1], clazz);
		} else {
			String prefix = CompletionUtils.getNodeUtils().getPrefix(node)
					.toLowerCase();

			// Classes from java.lang and from imports section
			addClasses(prefix);

			// var references
			String[] varNames = CompletionUtils.getVarVisitor().getVarNames();
			for (String varName : varNames) {
				if (varName.toLowerCase().startsWith(prefix)) {
					options.add(new VariableOption(prefix, varName));
				}
			}

			// attribute references
			String[] attributeNames = CompletionUtils.getVarVisitor()
					.getAttributeNames();
			for (String varName : attributeNames) {
				if (varName.toLowerCase().startsWith(prefix)) {
					options.add(new FieldOption(prefix, varName));
				}
			}

			// arg references
			String[] argNames = CompletionUtils.getMethodParameterVisitor()
					.getArgNames();
			for (String argName : argNames) {
				if (argName.toLowerCase().startsWith(prefix)) {
					options.add(new VariableOption(prefix, argName));
				}
			}

			// script methods
			ScriptMethodVisitor.Method[] methods = CompletionUtils
					.getScriptMethodVisitor().getMethods(prefix);
			for (ScriptMethodVisitor.Method method : methods) {
				this.options.add(new MethodOption(prefix, method.getName(),
						method.getArgs()));
			}

		}
	}

	private void addClasses(String prefix) throws ClassNotFoundException {
		ArrayList<String> classes = CompletionUtils.getPr().getClasses(
				"java.lang", true);
		classes.addAll(CompletionUtils.getImportsVisitor()
				.getImportedClassFullNames());
		classes.addAll(CompletionUtils.getPr().getClassStartingBy(prefix));
		for (String langClazz : classes) {
			String simpleName = CompletionUtils.getClassSimpleName(langClazz);
			if (simpleName.toLowerCase().startsWith(prefix)) {
				options.add(new ClassOption(prefix, langClazz, Class.forName(
						langClazz).isInterface()));
			}
		}
	}

	private void completeMembersOfClass(String prefix,
			Class<? extends Object> clazz) {
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (field.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
				if (Modifier.isPublic(field.getModifiers())) {
					options.add(new VariableOption(prefix, field.getName()));
				}
			}
		}
		if (clazz.isArray()) {
			if ("length".startsWith(prefix.toLowerCase())) {
				options.add(new VariableOption(prefix, "length"));
			}
		}
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
				if (Modifier.isPublic(method.getModifiers())) {
					options.add(new MethodOption(prefix, method.getName(),
							method.getParameterTypes()));
				}
			}
		}
	}

	@Override
	public Object visit(ASTImportDeclaration node, Object data) {
		Node nameNode = node.jjtGetChild(0);
		if (CompletionUtils.getNodeUtils().isAtCursor(nameNode)) {
			String prefix = CompletionUtils.getNodeUtils().getPrefix(nameNode);
			String[] parts = prefix.split("\\Q.\\E");
			if (prefix.endsWith(".")) {
				String[] newParts = new String[parts.length + 1];
				System.arraycopy(parts, 0, newParts, 0, parts.length);
				newParts[newParts.length - 1] = "";
				parts = newParts;
			}
			PackageReflection pr = CompletionUtils.getPr();
			String pack;
			int lastIndexOfPoint = prefix.lastIndexOf('.');
			if (lastIndexOfPoint == -1) {
				pack = null;
			} else {
				pack = prefix.substring(0, lastIndexOfPoint);
			}
			String[] names = pr.getClassNamesStartingBy(pack,
					parts[parts.length - 1]);
			for (String className : names) {
				options.add(new VariableOption(parts[parts.length - 1],
						className));
			}

		}
		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTPrimaryExpression node, Object data) {
		if (CompletionUtils.getNodeUtils().isAtCursor(node)) {
			ASTPrimaryPrefix primaryPrefix = (ASTPrimaryPrefix) node
					.jjtGetChild(0);
			if (CompletionUtils.getNodeUtils().isAtCursor(primaryPrefix)) {
				// If the cursor is at the primaryPrefix
				Node name = primaryPrefix.jjtGetChild(0);
				if (name instanceof ASTName) {
					// If it's a reference
					try {
						completeName(name);
					} catch (RuntimeException e) {
						logger.warn(EXCEPTION_MSG, e);
					} catch (ClassNotFoundException e) {
						logger.warn(EXCEPTION_MSG, e);
					} catch (CannotAutocompleteException e) {
						logger.warn(EXCEPTION_MSG, e);
					} catch (NoSuchFieldException e) {
						logger.warn(EXCEPTION_MSG, e);
					}
				}
			} else {
				for (int i = 1; i < node.jjtGetNumChildren(); i++) {
					Node primarySuffix = node.jjtGetChild(i);
					if (CompletionUtils.getNodeUtils()
							.isAtCursor(primarySuffix)) {
						if (primarySuffix.jjtGetNumChildren() == 0) {
							// It's just a .methodName
							ArrayList<ASTPrimarySuffix> previousSuffixes = new ArrayList<ASTPrimarySuffix>();
							for (int j = 1; j < i; j++) {
								previousSuffixes.add((ASTPrimarySuffix) node
										.jjtGetChild(j));
							}
							String prefix = CompletionUtils.getNodeUtils()
									.getPrefix(primarySuffix).substring(1);
							try {
								Class<? extends Object> type = CompletionUtils
										.getType(
												primaryPrefix,
												previousSuffixes
														.toArray(new ASTPrimarySuffix[previousSuffixes
																.size()]));
								completeMembersOfClass(prefix, type);
							} catch (SecurityException e) {
								logger.warn(EXCEPTION_MSG, e);
							} catch (ClassNotFoundException e) {
								logger.warn(EXCEPTION_MSG, e);
							} catch (CannotAutocompleteException e) {
								logger.warn(EXCEPTION_MSG, e);
							} catch (NoSuchFieldException e) {
								logger.warn(EXCEPTION_MSG, e);
							} catch (NoSuchMethodException e) {
								logger.warn(EXCEPTION_MSG, e);
							}
						}
					}
				}
			}
		}
		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTReferenceType node, Object data) {
		if (CompletionUtils.getNodeUtils().isAtCursor(node)) {
			String prefix = CompletionUtils.getNodeUtils().getPrefix(node)
					.toLowerCase();
			try {
				addClasses(prefix);
			} catch (ClassNotFoundException e) {
				logger.warn(EXCEPTION_MSG, e);
			}
		}

		return super.visit(node, data);
	}

	@Override
	public Object visit(ASTAllocationExpression node, Object data) {
		if (node.jjtGetChild(0) instanceof ASTClassOrInterfaceType) {
			if (CompletionUtils.getNodeUtils().isAtCursor(node.jjtGetChild(0))) {
				// new Clas*sName()
				String prefix = CompletionUtils.getNodeUtils().getPrefix(
						node.jjtGetChild(0)).toLowerCase();

				// Add constructors from java.lang, imported, and not imported
				ArrayList<String> classes = CompletionUtils.getPr().getClasses(
						"java.lang", true);
				classes.addAll(CompletionUtils.getImportsVisitor()
						.getImportedClassFullNames());
				classes.addAll(CompletionUtils.getPr().getClassStartingBy(
						prefix));
				for (String clazz : classes) {
					try {
						String[] parts = clazz.split("\\.");
						if (parts[parts.length - 1].toLowerCase().startsWith(
								prefix)) {
							Class<?> cl = Class.forName(clazz);
							Constructor<?>[] constructors = cl
									.getConstructors();
							if (cl.isInterface()) {
								options.add(new InlineImplementationOption(
										prefix, cl.getSimpleName(), cl
												.getName()));
							} else {
								for (Constructor<?> constructor : constructors) {
									if (Modifier.isPublic(constructor
											.getModifiers())) {
										options.add(new ConstructorOption(
												prefix, cl.getSimpleName(), cl
														.getName(), constructor
														.getParameterTypes()));
									}
								}
							}
						}
					} catch (ClassNotFoundException e) {
						logger.warn(EXCEPTION_MSG, e);
					}
				}
			}
		}

		return super.visit(node, data);
	}

	public Option[] getOptions() {
		return options.toArray(new Option[options.size()]);
	}
}
