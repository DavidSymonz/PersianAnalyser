package analyser;
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
import java.io.IOException;
import java.util.ArrayList;
import main.Config;
import dataStructures.FileSummary;
import dataStructures.SentenceSummary;

public final class FileAnalyser
{
	// ================================================================
	// ======================== Static Fields =========================
	// ================================================================
	
	// ================================================================
	// ======================== Static Methods ========================
	// ================================================================
	
	// ================================================================
	// ============================ Fields ============================
	// ================================================================
	private final SentenceAnalyser sentenceAnalyser;
	
	// ================================================================
	// ======================== Constructor(s) ========================
	// ================================================================
	public FileAnalyser(ConfigReader configReader)
	{
		this.sentenceAnalyser = new SentenceAnalyser(configReader);
	}
	
	// ================================================================
	// ======================== Public Methods ========================
	// ================================================================
	public final FileSummary analyseFile(BufferedReader fileReader)
	{
		// Get the whole file as one long string.
		String wholeFileAsString = wholeFileAsString(fileReader);
		
		// Could not read file.
		if(wholeFileAsString == null)
			return null;
		
		// Standardises characters you split on. This obviously has to be done before the split
		// i.e. it cannot wait until you clean up the individual sentences.
		wholeFileAsString = Config.standardisePunctuation(wholeFileAsString);
		
		// Split the string representing the whole file at every sentence-ending punctuation symbol.
		String[] originalSentenceArray = wholeFileAsString.split("(?<=[.!?;])\\s*");
		
		// Split the string representing the whole file into sentences.
		ArrayList<String[]> tokenisedSentences = tokeniseSentences(originalSentenceArray);
		
		// Now perform the analysis.
		return analyseAllSentences(originalSentenceArray, tokenisedSentences);
	}
	
	// ================================================================
	// ======================= Private Methods ========================
	// ================================================================
	private final String wholeFileAsString(BufferedReader fileReader)
	{
		StringBuilder sb = new StringBuilder();
		String line;
		
		// Read each line until the end of the file is reached.
		try
		{
			while((line = fileReader.readLine()) != null)
			{
				sb.append(line);
				sb.append(" ");
			}
		}
		catch(IOException e)
		{
			return null;
		}
		return sb.toString();
	}
	
	private final ArrayList<String[]> tokeniseSentences(String[] originalSentenceArray)
	{
		// A list of all sentences as arrays of tokens (words).
		ArrayList<String[]> tokenisedSentences = new ArrayList<String[]>();
		
		for(String originalSentence : originalSentenceArray)
		{
			// Standardise the source sentence. This removes any unwanted characters and may
			// substitute certain characters with others (e.g. There is sometimes a Farsi and an
			// Arabic version of the same letter. In order to be able to match these letters we
			// consistently substitute one for the other such that the strings that are compared
			// all use the same script.
			originalSentence = Config.standardiseSourceSentence(originalSentence);
			
			// Split the sentence at any and all white space characters.
			String[] sentenceAsTokenArray = originalSentence.split("\\s+");
			
			// A second "per-word" cleaning may be required.
			for(int i = 0; i < sentenceAsTokenArray.length; i++)
				sentenceAsTokenArray[i] = Config.standardiseSourceToken(sentenceAsTokenArray[i]);
			
			// Add the word array to the list of sentences.
			tokenisedSentences.add(sentenceAsTokenArray);
		}
		return tokenisedSentences;
	}
	
	private final FileSummary analyseAllSentences(String[] originalSentenceArray,
		ArrayList<String[]> tokenisedSentences)
	{
		ArrayList<SentenceSummary> sentenceSummaries = new ArrayList<SentenceSummary>();
		
		int n = originalSentenceArray.length;
		String originalSentence;
		String[] tokenisedSentence;
		SentenceSummary sentenceSummary;
		
		for(int i = 0; i < n; i++)
		{
			originalSentence = originalSentenceArray[i];
			tokenisedSentence = tokenisedSentences.get(i);
			
			sentenceSummary = sentenceAnalyser.analyseSentence(originalSentence, tokenisedSentence);
			sentenceSummaries.add(sentenceSummary);
		}
		return new FileSummary(sentenceSummaries);
	}
}
