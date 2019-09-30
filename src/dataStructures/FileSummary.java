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
import utils.UFile;

public final class FileSummary
{
	// Detailed data about each sentence.
	public final ArrayList<SentenceSummary> sentenceSummaries;
	
	// Statistical data.
	public int nrTokensInFile;
	public int totalLcCount;
	public int totalHcCount;
	public double complexityScore;
	public final ArrayList<DecomposedWord> collectedLcComplexityWords;
	public final ArrayList<DecomposedWord> collectedHcComplexityWords;
	
	// Constructor.
	public FileSummary(ArrayList<SentenceSummary> sentenceSummaries)
	{
		this.sentenceSummaries = sentenceSummaries;
		
		// Initialise statistics values.
		nrTokensInFile = 0;
		totalLcCount = 0;
		totalHcCount = 0;
		complexityScore = 0.0;
		collectedLcComplexityWords = new ArrayList<DecomposedWord>();
		collectedHcComplexityWords = new ArrayList<DecomposedWord>();
	}
	
	// ---- Public methods ----
	public final void generateStatistics()
	{
		// Go through all sentences.
		int n = sentenceSummaries.size();
		SentenceSummary sentenceSummary;
		for(int i = 0; i < n; i++)
		{
			sentenceSummary = sentenceSummaries.get(i);
			
			nrTokensInFile += sentenceSummary.tokenisedSentence.length;
			totalLcCount += sentenceSummary.getLcCount();
			totalHcCount += sentenceSummary.getHcCount();
			
			collectedLcComplexityWords.addAll(sentenceSummary.lcList);
			collectedHcComplexityWords.addAll(sentenceSummary.hcList);
		}
		complexityScore = (double)totalHcCount / (totalLcCount + totalHcCount);
	}
	
	public final String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		// Go through all sentences.
		int n = sentenceSummaries.size();
		for(int i = 0; i < n; i++)
		{
			sb.append(sentenceSummaries.get(i).toString());
			sb.append(UFile.NEWLINE);
		}
		
		// Append summarising statistics.
		sb.append(UFile.NEWLINE);
		sb.append("---- STATISTICS ----");
		sb.append(UFile.NEWLINE);
		sb.append(UFile.NEWLINE);
		sb.append(UFile.NEWLINE);
		
		// Set up a statistics table.
		MyTable<String> statisticsTable = new MyTable<String>();
		statisticsTable.setColLabels(new String[]
		{
			"Nr Tokens", "Total LC Count", "Total HC Count", "Complexity Score"
		});
		
		// Add data to the statistics table.
		statisticsTable.addRow(new String[]
		{
			"" + nrTokensInFile,
			"" + totalLcCount,
			"" + totalHcCount,
			"" + complexityScore
		});
		
		// Add the statistics table to the string builder.
		sb.append(statisticsTable.toString());
		sb.append(UFile.NEWLINE);
		
		// Return the lot.
		return sb.toString();
	}
}
