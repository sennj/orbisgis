package org.orbisgis.plugin.view.ui.workbench;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class GeoCatalogSearch extends JPanel {
	private static final long serialVersionUID = 1L;

	public GeoCatalogSearch() {
		add(new JTextField("Search a DataSource", 13));
		add(new JButton("Search"));
	}
}