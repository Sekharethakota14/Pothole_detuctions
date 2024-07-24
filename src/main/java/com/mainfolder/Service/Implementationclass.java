package com.mainfolder.Service;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.util.FastMath;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class Implementationclass {

	// Here I using Apache Commons Math3 dependency for Calculating the Mathematical
	// Operations
	public int[][] getImagePixelsdata(String absoluteFilePath) throws Exception {
		try {
			System.out.println("Check 0");

			File imageFile = new File(absoluteFilePath);
			BufferedImage image = ImageIO.read(imageFile);
			int width = image.getWidth() - 100;
			int height = image.getHeight() - 100;
			int[][] pixelValues = new int[height][width];

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					int pixel = image.getRGB(j, i);
					int red = (pixel >> 16) & 0xff;
					int green = (pixel >> 8) & 0xff;
					int blue = pixel & 0xff;
					int greyscale = (red + green + blue) / 3;
					pixelValues[i][j] = greyscale;
				}
			}
			RealMatrix matrix = new Array2DRowRealMatrix(pixelValues.length, pixelValues[0].length);
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					matrix.setEntry(i, j, pixelValues[i][j]);
				}
			}
			System.out.println("Check 1");

			return pixelValues;
		} catch (IOException e) {
			return null;
		}
	}

	// Here I am Using Apache Commons Math3 dependencies for Simple calculations
	public double[] calculateMean(int[][] val) {
		double[] means = new double[val.length];
		for (int i = 0; i < val.length; i++) {
			Mean mean = new Mean();
			for (int j = 0; j < val[i].length; j++) {
				mean.increment(val[i][j]);
			}
			means[i] = mean.getResult();
		}
		System.out.println("Check 2");

		return means;
	}

	// Using Apache Commons Math3 dependencies for Simple calculations
	public double[] calculateStdDev(int[][] val) {
		double[] stdDevs = new double[val.length];
		for (int i = 0; i < val.length; i++) {
			StandardDeviation stdDev = new StandardDeviation();
			for (int j = 0; j < val[i].length; j++) {
				stdDev.increment(val[i][j]);
			}
			stdDevs[i] = stdDev.getResult();
		}
		System.out.println("Check 3");

		return stdDevs;
	}

	public double[] maxarray(double[] array1, double[] array2) {
		double[] result = new double[array1.length];
		for (int i = 0; i < array1.length; i++) {
			result[i] = FastMath.max(array1[i], array2[i]);
		}
		return result;
	}

	public double[] minarray(double[] array1, double[] array2) {
		double[] result = new double[array1.length];
		for (int i = 0; i < array1.length; i++) {
			result[i] = FastMath.min(array1[i], array2[i]);
		}
		return result;
	}

	public int[][] correlationMatrix(int[][] depth, int[][] intensity) {
		int height = depth.length;
		int width = depth[0].length;
		int[][] result = new int[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				result[i][j] = depth[i][j] - intensity[i][j];
			}
		}
		return result;
	}

	public boolean[] getBool(int[][] val) {
		boolean[] result = new boolean[val.length];
		for (int i = 0; i < val.length; i++) {
			result[i] = true;
			for (int j = 0; j < val[i].length; j++) {
				if (val[i][j] == 0) {
					result[i] = false;
					break;
				}
			}
		}
		return result;
	}

	public boolean[] getNoise(boolean[] array1, boolean[] array2) {
		boolean[] result = new boolean[array1.length];
		for (int i = 0; i < array1.length; i++) {
			result[i] = array1[i] && array2[i];
		}
		return result;
	}

	public static int[][] getGreenScaleImg(boolean[] greenScaleDf, int[][] intensity) {
		int rows = intensity.length - 100;
		int cols = intensity[0].length - 100;
		int k = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (k < greenScaleDf.length && !greenScaleDf[k]) {
					intensity[i][j] = 255;
					k++;
				}
			}
		}
		return intensity;
	}

	public void createOutputImage(int[][] greenScaleImg, boolean[] green_scale_df, String outputFolder,
			String fileSavedName) {
		int height = greenScaleImg.length;
		int width = greenScaleImg[0].length;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int lightGreyThreshold = 190;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = greenScaleImg[y][x];
				if (pixel >= lightGreyThreshold) {
					image.setRGB(x, y, (0 << 16) | (255 << 8) | 0);
				} else {
					image.setRGB(x, y, (pixel << 16) | (pixel << 8) | pixel);
				}
			}
		}
		try {
			ImageIO.write(image, "jpg", new File(outputFolder + "\\" + fileSavedName + ".jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeDatatoXL(String outputFolder, String fileSavedName, int[][] depth, int[][] intensity,
			int[][] correlationMatrix, double[] depthmean, double[] depthStdDev, double[] maxarr, double[] minarr)
			throws IOException {
		SXSSFWorkbook workbook = new SXSSFWorkbook(120);
		FileOutputStream fos = new FileOutputStream(outputFolder + "\\" + fileSavedName + ".xlsx");
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		// Create sheets
		SXSSFSheet sheet1 = workbook.createSheet("DEPTH");
		SXSSFSheet sheet2 = workbook.createSheet("INTENSITY");
		SXSSFSheet sheet3 = workbook.createSheet("NOISE-REDUCTION");
		SXSSFSheet sheet4 = workbook.createSheet("DIFF MATRIX");
		SXSSFSheet sheet5 = workbook.createSheet("FINAL CLASSIFICATION");
		// Create column names for depth and mean
		String[] depthColumnNames = { "Depth" };
		String[] meanColumnNames = { "Mean", "Stddiv", "", "", "Max", "Min" };

		// Write column names for depth
		Row depthHeaderRow = sheet1.createRow(0);
		for (int i = 0; i < depthColumnNames.length; i++) {
			Cell cell = depthHeaderRow.createCell(i);
			cell.setCellValue(depthColumnNames[i]);
		}

		// Write column names for mean
		Row meanHeaderRow = sheet1.getRow(0); // Assuming the header row already exists
		for (int i = 0; i < meanColumnNames.length; i++) {
			Cell cell = meanHeaderRow.createCell(depth[0].length + 2 + i); // Add 2 empty columns
			cell.setCellValue(meanColumnNames[i]);
		}

		// Write depth data
		for (int i = 0; i < depth.length; i++) {
			Row row = sheet1.createRow(i + 1); // Start from the second row after the header
			for (int j = 0; j < depth[i].length; j++) {
				Cell cell = row.createCell(j);
				cell.setCellValue(depth[i][j]);
			}
			// Add two empty columns after depth values
			for (int k = 0; k < 2; k++) {
				row.createCell(depth[i].length + k);
			}
			// Add depth mean values beside depth values with two empty columns in between
			Cell meanCell = row.createCell(depth[i].length + 2);
			meanCell.setCellValue(depthmean[i]);
			Cell stddevCell = row.createCell(depth[i].length + 2 + 1);
			stddevCell.setCellValue(depthStdDev[i]);
			Cell maxarrCell = row.createCell(depth[i].length + 6);
			maxarrCell.setCellValue(maxarr[i]);
			Cell minarrCell = row.createCell(depth[i].length + 7);
			minarrCell.setCellValue(minarr[i]);
		}

		double[] intensitymean = calculateMean(intensity);
		double[] intensityStdDev = calculateStdDev(intensity);
		double[] intensitymaxarr = maxarray(intensitymean, intensityStdDev);
		double[] intensityminarr = maxarray(intensitymean, intensityStdDev);
		String[] intensityColumnNames = { "Intensity" };
		String[] intensitymeanColumnNames = { "Mean", "Stddiv", "", "", "Max", "Min" };
		Row intensityHeaderRow = sheet2.createRow(0);
		for (int i = 0; i < intensityColumnNames.length; i++) {
			Cell cell = intensityHeaderRow.createCell(i);
			cell.setCellValue(intensityColumnNames[i]);
		}
		// Write column names for mean
		Row intensitymeanHeaderRow = sheet2.getRow(0); // Assuming the header row already exists
		for (int i = 0; i < intensitymeanColumnNames.length; i++) {
			Cell cell = intensitymeanHeaderRow.createCell(intensity[0].length + 2 + i); // Add 2 empty columns
			cell.setCellValue(meanColumnNames[i]);
		}

		for (int i = 0; i < intensity.length; i++) {
			Row row = sheet2.createRow(i + 1); // Start from the second row after the header
			for (int j = 0; j < intensity[i].length; j++) {
				Cell cell = row.createCell(j);
				cell.setCellValue(intensity[i][j]);
			}
			// Add two empty columns after depth values
			for (int k = 0; k < 2; k++) {
				row.createCell(intensity[i].length + k);
			}
			// Add depth mean values beside depth values with two empty columns in between
			Cell meanCell = row.createCell(intensity[i].length + 2);
			meanCell.setCellValue(intensitymean[i]);
			Cell stddevCell = row.createCell(intensity[i].length + 2 + 1);
			stddevCell.setCellValue(intensityStdDev[i]);
			Cell maxarrCell = row.createCell(intensity[i].length + 6);
			maxarrCell.setCellValue(intensitymaxarr[i]);
			Cell minarrCell = row.createCell(intensity[i].length + 7);
			minarrCell.setCellValue(intensityminarr[i]);
		}
		String[] collerationColumnNames = { "DIFF MATRIX" };
		String[] collerationmeanColumnNames = { "Mean", "Stddiv", "", "", "Max", "Min" };
		Row diffHeaderRow = sheet4.createRow(0);
		for (int i = 0; i < collerationColumnNames.length; i++) {
			Cell cell = diffHeaderRow.createCell(i);
			cell.setCellValue(collerationColumnNames[i]);
		}
		// Write column names for mean
		Row collerationmeanHeaderRow = sheet4.getRow(0); // Assuming the header row already exists
		for (int i = 0; i < collerationmeanColumnNames.length; i++) {
			Cell cell = collerationmeanHeaderRow.createCell(intensity[0].length + 2 + i); // Add 2 empty columns
			cell.setCellValue(collerationmeanColumnNames[i]);
		}

		double[] collarationmean = calculateMean(correlationMatrix);
		double[] collerationStdDev = calculateStdDev(correlationMatrix);
		double[] collaramaxarr = maxarray(collarationmean, collerationStdDev);
		double[] collaraminarr = maxarray(collaramaxarr, collerationStdDev);

		// Write colerationmatrix data
		for (int i = 0; i < correlationMatrix.length; i++) {
			Row row = sheet4.createRow(i + 1); // Start from the second row after the header
			for (int j = 0; j < correlationMatrix[i].length; j++) {
				Cell cell = row.createCell(j);
				cell.setCellValue(correlationMatrix[i][j]);
			}
			// Add two empty columns after depth values
			for (int k = 0; k < 2; k++) {
				row.createCell(correlationMatrix[i].length + k);
			}
			Cell meanCell = row.createCell(correlationMatrix[i].length + 2);
			meanCell.setCellValue(collarationmean[i]);
			Cell stddevCell = row.createCell(correlationMatrix[i].length + 2 + 1);
			stddevCell.setCellValue(collerationStdDev[i]);
			Cell maxarrCell = row.createCell(correlationMatrix[i].length + 6);
			maxarrCell.setCellValue(collaramaxarr[i]);
			Cell minarrCell = row.createCell(correlationMatrix[i].length + 7);
			minarrCell.setCellValue(collaraminarr[i]);
		}

		Row finalHeaderRow = sheet5.createRow(0);
		for (int i = 0; i < collerationColumnNames.length; i++) {
			Cell cell = finalHeaderRow.createCell(i);
			cell.setCellValue(collerationColumnNames[i]);
		}

		for (int i = 0; i < correlationMatrix.length; i++) {
			Row row = sheet5.createRow(i + 1); // Start from the second row after the header
			for (int j = 0; j < correlationMatrix[i].length; j++) {
				Cell cell = row.createCell(j);
				cell.setCellValue(correlationMatrix[i][j]);
			}
		}
		Row noicedepthHeaderRow = sheet3.createRow(0);
		for (int i = 0; i < depthColumnNames.length; i++) {
			Cell cell = noicedepthHeaderRow.createCell(i);
			cell.setCellValue(depthColumnNames[i]);
		}
		Row noicemeanHeaderRow = sheet3.getRow(0); // Assuming the header row already exists
		for (int i = 0; i < intensityColumnNames.length; i++) {
			Cell cell = noicemeanHeaderRow.createCell(intensity[0].length + 5 + i); // Add 2 empty columns
			cell.setCellValue(intensityColumnNames[i]);
		}

		for (int i = 0; i < depth.length; i++) {
			Row row = sheet3.createRow(i + 1); // Start from the second row after the header
			for (int j = 0; j < depth[i].length; j++) {
				Cell cell = row.createCell(j);
				cell.setCellValue(depth[i][j]);
			}

			// Add intensity data with a 5-column gap
			for (int j = 0; j < intensity[i].length; j++) {
				Cell cell = row.createCell(depth[0].length + 5 + j);
				cell.setCellValue(intensity[i][j]);
			}
		}
		workbook.write(bos);
		workbook.dispose();
		bos.close();
	}

}
