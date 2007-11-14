package org.orbisgis.geoview.toc;

import org.gdms.data.SourceAlreadyExistsException;
import org.gdms.source.SourceManager;
import org.gdms.sql.instruction.TableNotFoundException;
import org.orbisgis.core.OrbisgisCore;
import org.orbisgis.core.resourceTree.BasicResource;
import org.orbisgis.core.resourceTree.IResource;
import org.orbisgis.geoview.layerModel.IGdmsLayer;
import org.orbisgis.geoview.layerModel.ILayer;

public class GdmsLayerResource extends BasicResource implements ILayerResource {

	private IGdmsLayer layer;

	public GdmsLayerResource(IGdmsLayer layer) {
		super(layer.getName());
		this.layer = layer;
	}

	public ILayer getLayer() {
		return layer;
	}

	public void syncWithLayerModel() {

	}

	@Override
	public void setName(String newName) {
		try {
			SourceManager sourceManager = OrbisgisCore.getDSF()
					.getSourceManager();
			if (!sourceManager.exists(getName())) {
				sourceManager.removeName(getName());
			}
			sourceManager.addName(layer.getMainName(), newName);
		} catch (TableNotFoundException e) {
			throw new RuntimeException("bug!", e);
		} catch (SourceAlreadyExistsException e) {
			throw new IllegalArgumentException("Name already exists: "
					+ newName);
		}
		super.setName(newName);
	}

	@Override
	public void removeFrom(IResource parent) {
		OrbisgisCore.getDSF().getSourceManager().removeName(getName());
	}

	@Override
	public void addTo(IResource parent) {
		SourceManager sourceManager = OrbisgisCore.getDSF().getSourceManager();
		if (!layer.getMainName().equals(layer.getName())) {
			try {
				sourceManager.addName(layer.getMainName(), getName());
			} catch (TableNotFoundException e) {
				throw new RuntimeException("Bug", e);
			} catch (SourceAlreadyExistsException e) {
				throw new RuntimeException("Bug", e);
			}
		}
	}

	@Override
	public void move(IResource dropNode) {
		super.removeFrom(getParent());
		super.addTo(dropNode);
	}

}
