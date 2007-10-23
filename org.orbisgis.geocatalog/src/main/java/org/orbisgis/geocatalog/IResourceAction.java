package org.orbisgis.geocatalog;

import org.orbisgis.geocatalog.resources.IResource;

public interface IResourceAction {

	boolean acceptsEmptySelection();

	boolean accepts(IResource selectedNode);

	void execute(CatalogModel catalogModel, IResource currentNode);

}
