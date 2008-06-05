package org.orbisgis.renderer.legend;

import java.awt.image.ColorModel;

public class RasterLegend extends AbstractLegend implements Legend {

	private ColorModel colorModel = null;
	private float opacity = 1.0f;

	public RasterLegend(ColorModel colorModel, float opacity) {
		this.colorModel = colorModel;
		this.opacity = opacity;
	}

	public ColorModel getColorModel() {
		return colorModel;
	}

	public float getOpacity() {
		return opacity;
	}

	public int getNumLayers() {
		return 1;
	}

	public Symbol getSymbol(long row) throws RenderException {
		return null;
	}

	public String getLegendTypeName() {
		return "Raster legend";
	}

	// public void setRangeColors(final double[] ranges, final Color[] colors)
	// throws OperationException, IOException, GeoreferencingException {
	// checkRangeColors(ranges, colors);
	//
	// // TODO : is it really necessary ?
	// setRangeValues(ranges[0], ranges[ranges.length - 1]);
	//
	// final int nbOfColors = 256;
	// final byte[] reds = new byte[nbOfColors];
	// final byte[] greens = new byte[nbOfColors];
	// final byte[] blues = new byte[nbOfColors];
	// final double delta = (ranges[ranges.length - 1] - ranges[0])
	// / (nbOfColors - 1);
	// double x = ranges[0] + delta;
	//
	// for (int i = 1, j = 0; i < nbOfColors; i++, x += delta) {
	// while (!((x >= ranges[j]) && (x < ranges[j + 1]))
	// && (colors.length > j + 1)) {
	// j++;
	// }
	// reds[i] = (byte) colors[j].getRed();
	// greens[i] = (byte) colors[j].getGreen();
	// blues[i] = (byte) colors[j].getBlue();
	// }
	// // default color for NaN pixels :
	// reds[0] = (byte) Color.BLACK.getRed();
	// greens[0] = (byte) Color.BLACK.getGreen();
	// blues[0] = (byte) Color.BLACK.getBlue();
	//
	// try {
	// setLUT(new IndexColorModel(8, nbOfColors, reds, greens, blues));
	// } catch (IOException e) {
	// throw new OperationException(e);
	// }
	// }
	//
	// private void checkRangeColors(final double[] ranges, final Color[]
	// colors)
	// throws OperationException {
	// if (ranges.length != colors.length + 1) {
	// throw new OperationException(
	// "Ranges.length not equal to Colors.length + 1 !");
	// }
	// for (int i = 1; i < ranges.length; i++) {
	// if (ranges[i - 1] > ranges[i]) {
	// throw new OperationException(
	// "Ranges array needs to be sorted !");
	// }
	// }
	// if (colors.length > 256) {
	// throw new OperationException(
	// "Colors.length must be less than 256 !");
	// }
	// }

}