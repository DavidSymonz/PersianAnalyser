package dataStructures;
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

import java.util.ArrayList;
import java.util.HashMap;
import analyser.ConfigReader;
import utils.UFile;
import utils.UList;
import utils.UMaths;
import utils.USort;
import dataStructures.MyTable;

public final class FolderSummary
{
	// ================================================================
	// ======================== Static Fields =========================
	// ================================================================
	public static final byte LC_COUNT_INDEX = 0;
	public static final int HC_COUNT_INDEX = 1;
	
	private static final int MAX_FILE_NAME_LENGTH = 100; // Must be >= 4
	
	// ================================================================
	// ======================== Static Methods ========================
	// ================================================================
	
	// ================================================================
	// ============================ Fields ============================
	// ================================================================
	private final ConfigReader configReader;
	
	// Statistical data.
	int nrFiles;
	int nrTokensInFolder;
	int totalLc;
	int totalHc;
	int nrSuperlatives;
	double totalComplexityScore;
	
	// Derived statistical data.
	double averageComplexityScore;
	double complexityScoreBasedOnTotals;
	HashMap<ArrayList<String>, Integer[]> complexityStemWordCounts;
	
	// Output.
	private final MyTable<String> filesTable;
	private final MyTable<String> statisticsTable;
	private final MyTable<String> complexityStemWordTable;
	
	// ================================================================
	// ======================== Constructor(s) ========================
	// ================================================================
	public FolderSummary(ConfigReader configReader)
	{
		this.configReader = configReader;
		
		// Initialise statistics values.
		nrFiles = 0;
		nrTokensInFolder = 0;
		totalLc = 0;
		totalHc = 0;
		nrSuperlatives = 0;
		totalComplexityScore = 0.0;
		
		// Initialise derived values.
		averageComplexityScore = 0.0;
		complexityScoreBasedOnTotals = 0.0;
		complexityStemWordCounts = new HashMap<ArrayList<String>, Integer[]>();
		
		filesTable = new MyTable<String>();
		filesTable.setColLabels(new String[]
		{
			"Nr Tokens", "LC Count", "HC Count", "Complexity Score", "File Name"
		});
		
		statisticsTable = new MyTable<String>();
		statisticsTable.setColLabels(new String[]
		{
			"Nr Files", "Total tokens", "Total LC Count", "Total HC Count", "Complexity Score (totals)",
			"Complexity Score (average)"
		});
		
		complexityStemWordTable = new MyTable<String>();
		complexityStemWordTable.setColLabels(new String[]
		{
			"Counted as LC", "Counted as HC", "Influence (HC - LC)", "Complexity stem word"
		});
	}
	
	// ================================================================
	// ======================== Public Methods ========================
	// ================================================================
	public final void addFileSummary(String fileName, FileSummary fileSummary)
	{
		filesTable.addRow(new String[]
		{
			"" + fileSummary.nrTokensInFile,
			"" + fileSummary.totalLcCount,
			"" + fileSummary.totalHcCount,
			"" + fileSummary.complexityScore,
			cropFileNameIfRequired(fileName)
		});
		
		// Only add to the totals if the complexity score is non-NaN. Adding a NaN complexity score
		// to the total would result in the total being set to NaN - which we don't want! Instead
		// the file will be treated as though it never existed (as far as the summary statistics
		// are concerned). That means we also don't want to increase the nrFiles variable because
		// we don't want to divide by more files than we actually considered when computing the
		// average complexity score.
		if(!UMaths.isNaN(fileSummary.complexityScore))
		{
			nrFiles++;
			nrTokensInFolder += fileSummary.nrTokensInFile;
			totalLc += fileSummary.totalLcCount;
			totalHc += fileSummary.totalHcCount;
			totalComplexityScore += fileSummary.complexityScore;
			
			// Record how often each stem word was counted as having low/high complexity.
			recordComplexityCounts(
				fileSummary.collectedLcComplexityWords, LC_COUNT_INDEX, complexityStemWordCounts);
			
			recordComplexityCounts(
				fileSummary.collectedHcComplexityWords, HC_COUNT_INDEX, complexityStemWordCounts);
		}
	}
	
	private final void recordComplexityCounts(ArrayList<DecomposedWord> complexityWords,
		int complexityIndex, HashMap<ArrayList<String>, Integer[]> complexityStemWordCounts)
	{
		for(DecomposedWord complexityWord : complexityWords)
		{
			// Count the number of superlatives. Their stems are not counted though.
			if(configReader.farsiSuperlativePostfix.equals(complexityWord.postfix))
			{
				nrSuperlatives++;
				continue;
			}
			
			// Look for an existing mapping for this complexity word's stem.
			Integer[] counts = complexityStemWordCounts.get(complexityWord.stemWords);
			
			// If there is no mapping yet, create one.
			if(counts == null)
			{
				counts = new Integer[]{0, 0};
				complexityStemWordCounts.put(complexityWord.stemWords, counts);
			}
			// Increment the appropriate count.
			counts[complexityIndex]++;
		}
	}
	
	public final void generateStatistics()
	{
		complexityScoreBasedOnTotals = (double)totalHc / (totalLc + totalHc);
		averageComplexityScore = totalComplexityScore / nrFiles;
		
		statisticsTable.addRow(new String[]
		{
			"" + nrFiles,
			"" + nrTokensInFolder,
			"" + totalLc,
			"" + totalHc,
			"" + complexityScoreBasedOnTotals,
			"" + averageComplexityScore
		});
		// Sort by complexity score.
		filesTable.sortRowsByColValues(3, USort.ASCENDING_COMPARATOR_STRING_TO_DOUBLE);
		
		// Make a table of complexity stem word counts.
		Integer[] counts;
		for(ArrayList<String> stem : complexityStemWordCounts.keySet())
		{
			counts = complexityStemWordCounts.get(stem);
			
			complexityStemWordTable.addRow(new String[]
			{
				"" + counts[LC_COUNT_INDEX],
				"" + counts[HC_COUNT_INDEX],
				"" + (counts[HC_COUNT_INDEX] - counts[LC_COUNT_INDEX]),
				UList.listToString(stem)
			});
		}
		// Sort it by the "influence" column.
		complexityStemWordTable.sortRowsByColValues(2, USort.ASCENDING_COMPARATOR_STRING_TO_DOUBLE);
	}
	
	public final String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(filesTable.toString());
		sb.append(UFile.NEWLINE);
		sb.append(UFile.NEWLINE);
		sb.append(UFile.NEWLINE);
		sb.append(UFile.NEWLINE);
		sb.append(statisticsTable.toString());
		sb.append(UFile.NEWLINE);
		sb.append(UFile.NEWLINE);
		sb.append(UFile.NEWLINE);
		sb.append(UFile.NEWLINE);
		sb.append(complexityStemWordTable.toString());
		sb.append(UFile.NEWLINE);
		sb.append(UFile.NEWLINE);
		sb.append("nrSuperlatives = " + nrSuperlatives);
		sb.append(UFile.NEWLINE);
		return sb.toString();
	}
	
	// ================================================================
	// ======================= Private Methods ========================
	// ================================================================
	private final String cropFileNameIfRequired(String fileName)
	{
		// Remove ".txt" ending
		fileName = fileName.substring(0, fileName.length() - 4);
		
		// File name is not too long.
		if(fileName.length() <= MAX_FILE_NAME_LENGTH)
			return fileName;
		
		// File name is too long and needs to be cropped.
		return fileName.substring(0, MAX_FILE_NAME_LENGTH - 3) + "...";
	}
}
