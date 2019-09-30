package main;
/*
 * PersianAnalyer is a tool for evaluating the conceptual complexity of text in Farsi.
 * Copyright (C) 2019 David Symons. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This code is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License version 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * Please contact David Symons at das57@st-andrews.ac.uk if you need additional information
 * or have any questions.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import analyser.FileAnalyser;
import analyser.ConfigReader;
import dataStructures.FileSummary;
import dataStructures.FolderSummary;
import utils.UFile;
import utils.UOperatingSystem;

public final class PersianAnalyser
{
	private static int nrWarnings = 0;
	
	public static final void main(String[] args)
	{
		// Read in the alphabet (list of all valid characters).
		if(!Config.readAlphabetFile())
		{
			System.out.println("Error: Could not read the alphabet file!");
			waitForUserToPressEnter();
			return;
		}
		
		// Setup the Config class.
		if(!Config.readSubstitutionFile())
		{
			System.out.println("Error: Could not read the substitution file!");
			waitForUserToPressEnter();
			return;
		}
		
		// Create a file analyser.
		ConfigReader configReader = createConfigReader();
		if(configReader == null)
		{
			System.out.println("Error: Could not create a ConfigReader!");
			waitForUserToPressEnter();
			return;
		}
		System.out.println("ConfigReader is ready.\n");
		
		// Get the folder containing the files to be analysed.
		File sourceFolder = getSourceFolder();
		if(sourceFolder == null)
		{
			System.out.println("Error: Invalid source folder!");
			waitForUserToPressEnter();
			return;
		}
		
		// Process the selected folder.
		processFolder(configReader, sourceFolder);
		
		// If there are warnings, give the user time to read them.
		if(nrWarnings != 0)
			waitForUserToPressEnter();
	}
	
	private static final ConfigReader createConfigReader()
	{
		// Create a configReader.
		ConfigReader configReader = new ConfigReader();
		
		// Return the configReader if it is successfully set up.
		if(configReader.setup())
			return configReader;
		
		// If setup failed return null.
		return null;
	}
	
	private static final File getSourceFolder()
	{
		if(Config.TEST_MODE)
			return new File(UOperatingSystem.getDesktop(), "testFilesFarsi");
		
		// Create a file chooser.
		JFileChooser fileChooser = new JFileChooser(UOperatingSystem.getDesktop());
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		// Ask the user to select a source folder.
		File sourceFolder = null;
		try
		{
			JFrame f = new JFrame();
			f.setState(JFrame.ICONIFIED);
			f.setVisible(true);
			
			if(fileChooser.showOpenDialog(f) == JFileChooser.APPROVE_OPTION)
				sourceFolder = fileChooser.getSelectedFile();
			f.setVisible(false);
			f.dispose();
		}
		catch(Exception ex)
		{
			
		}
		return sourceFolder;
	}
	
	private static final void processFolder(ConfigReader configReader, File sourceFolder)
	{
		System.out.println("Selected source folder: " + sourceFolder.getAbsolutePath() + "\n");
		
		File[] sourceFiles = sourceFolder.listFiles();
		String outputFolder = sourceFolder + File.separator + "FarsiAnalyser";
		
		// Create a file analyser.
		FileAnalyser analyser = new FileAnalyser(configReader);
		FolderSummary folderSummary = new FolderSummary(configReader);
		
		// Go through all files in the source folder.
		for(File sourceFile : sourceFiles)
		{
			// Skip directories and files that don't have a .txt extension.
			if(sourceFile.isDirectory() || !sourceFile.getName().endsWith(".txt"))
				continue;
			
			System.out.println("Analysing: " + sourceFile.getAbsolutePath());
			
			// Open the file to be analysed. Skip this file if it cannot be read.
			BufferedReader fileReader = UFile.reader_file(sourceFile);
			if(fileReader == null)
			{
				nrWarnings++;
				System.out.println("Skipping this file as it could not be opened!\n");
				continue;
			}
			
			// Create an output file for the summary. Skip this file in case of failure.
			BufferedWriter fileWriter = UFile.writer_cwdOrAbs(outputFolder, sourceFile.getName());
			if(fileWriter == null)
			{
				nrWarnings++;
				System.out.println("Skipping this file as the output file " + outputFolder
					+ File.separator + sourceFile.getName() + " couldn't be created!\n");
				continue;
			}
			// Process the current source file.
			FileSummary fileSummary = processFile(analyser, fileReader, fileWriter);
			
			// If successful add a summary of the current source file to the folder summary.
			if(fileSummary != null)
				folderSummary.addFileSummary(sourceFile.getName(), fileSummary);
		}
		// Output the folder summary.
		writeFolderSummary(folderSummary, outputFolder);
	}
	
	private static final FileSummary processFile(FileAnalyser analyser, BufferedReader fileReader,
		BufferedWriter fileSummaryWriter)
	{
		// Run the analysis.
		FileSummary fileSummary = analyser.analyseFile(fileReader);
		
		// Close the file as soon as we're done.
		closeFileReader(fileReader);
		
		// Check for an error reading the file.
		if(fileSummary == null)
		{
			nrWarnings++;
			System.out.println("Skipping this file as an error occured while analysing it!\n");
			return null;
		}
		
		// Results successfully obtained! Write them to the output file.
		System.out.println("Writing file summary.");
		fileSummary.generateStatistics();
		writeToFile(fileSummaryWriter, fileSummary.toString());
		
		// Return a summary of the processed file.
		return fileSummary;
	}
	
	private static final void writeFolderSummary(FolderSummary folderSummary, String outputFolder)
	{
		System.out.println("Writing folder summary.");
		
		// Create an output file to which to write the folder summary.
		BufferedWriter folderSummaryWriter = UFile.writer_cwdOrAbs(outputFolder, "SUMMARY.txt");
		
		// Make sure the file was opened correctly.
		if(folderSummaryWriter != null)
		{
			// Prepare for writing the folder summary.
			folderSummary.generateStatistics();
			
			// Write it to the solder summary file.
			writeToFile(folderSummaryWriter, folderSummary.toString());
		}
		else
		{
			// Warn the user that the summary file cold not be created.
			nrWarnings++;
			System.out.println("Warning: Folder summary file could not be created!");
		}
	}
	
	private static final void closeFileReader(BufferedReader fileReader)
	{
		try
		{
			fileReader.close();
		}
		catch(IOException e)
		{
			// Can't do anything about this, we made best efforts.
		}
	}
	
	private static final void writeToFile(BufferedWriter fileWriter, String content)
	{
		try
		{
			fileWriter.write(content);
			System.out.println("Done.\n");
		}
		catch(IOException e)
		{
			nrWarnings++;
			System.out.println("Warning: There was an error writing to the file!");
		}
		finally
		{
			try
			{
				// Close the writer when done.
				fileWriter.close();
			}
			catch(IOException e)
			{
				// Can't do anything about this, we made best efforts.
			}
		}
	}
	
	private static final void waitForUserToPressEnter()
	{
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
		sc.close();
	}
}
