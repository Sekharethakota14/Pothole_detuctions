package com.mainfolder.Controller;

import java.io.File;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.mainfolder.Service.Implementationclass;

@RequestMapping("/image")
@RestController
public class MainController {
	@Autowired
	private Implementationclass service;
	@Value("${image.output.folder}")
	private String outputFolder;
	@Value("${image.intermediate.write}")
	private String writeIntermediate;
	@Value("${image.input.folder}")
	private String inputFolder;

	@PostMapping("/actions")
	public ResponseEntity<String> getProcessedImageData() throws Exception {
		try {

			ArrayList<String[]> matchingFiles = getFiles(inputFolder);
			for (String[] filePair : matchingFiles) {
				String depthPath = inputFolder + File.separator + filePair[0];
				String intensityPath = inputFolder + File.separator + filePair[1];

				int[][] depth = service.getImagePixelsdata(depthPath);
				int[][] intensity = service.getImagePixelsdata(intensityPath);
				String fileSavedName = filePair[0].substring(filePair[0].lastIndexOf("\\") + 1,
						filePair[0].indexOf("_"));

				if (depth.length != intensity.length || depth[0].length != intensity[0].length) 
				{
					return new ResponseEntity<>("Both images are Not in same size", HttpStatus.NOT_ACCEPTABLE);
				}

				double[] depthmean = service.calculateMean(depth);
				double[] depthStdDev = service.calculateStdDev(intensity);
				double[] maxarray = service.maxarray(depthmean, depthStdDev);
				double[] minarray = service.minarray(depthmean, depthStdDev);
				int[][] correlationMatrix = service.correlationMatrix(depth, intensity);
				boolean[] depthbool = service.getBool(depth);
				boolean[] intensitybool = service.getBool(intensity);
				boolean[] noise = service.getNoise(depthbool, intensitybool);
				if ("YES".equalsIgnoreCase(writeIntermediate)) 
				{
					// If we mention yes in Application.properties
					service.writeDatatoXL(outputFolder, fileSavedName, depth, intensity, correlationMatrix, depthmean,
							depthStdDev, maxarray, minarray);
				}
				boolean[] correlationmatrix_bool = service.getBool(correlationMatrix);
				boolean[] green_scale_df = service.getNoise(noise, correlationmatrix_bool);
				int[][] greenScaleImg = service.getGreenScaleImg(green_scale_df, intensity);
				service.createOutputImage(greenScaleImg, green_scale_df, outputFolder, fileSavedName);

			}
		} catch (Exception e)
		{
			return new ResponseEntity<>("File Not Found" + e, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<String>("Success", HttpStatus.OK);

	}

	public static ArrayList<String[]> getFiles(String folderPath)
	{
		File folder = new File(folderPath);
		File[] files = folder.listFiles();
		ArrayList<String[]> matchingTuples = new ArrayList<>();

		if (files != null) {
			ArrayList<String> filesDepth = new ArrayList<>();
			for (File file : files) {
				if (file.getName().endsWith("_depth.jpg")) {
					filesDepth.add(file.getName());
				}
			}

			for (String depthFile : filesDepth) {
				String prefix = depthFile.substring(0, depthFile.length() - 10); // Remove '_depth.jpg' suffix
				String intensityFile = prefix + "_intensity.jpg";

				File intensityFilePath = new File(folderPath + File.separator + intensityFile);
				if (intensityFilePath.exists()) {
					matchingTuples.add(new String[] { depthFile, intensityFile });
				}
			}
		} else {
			System.out.println("The specified folder '" + folderPath + "' does not exist.");
		}

		return matchingTuples;
	}

}