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

import java.util.ArrayList;
import main.Config;
import dataStructures.DecomposedWord;
import dataStructures.SentenceSummary;

public final class SentenceAnalyser
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
	private final SuperlativeChecker supervativeChecker;
	private final ExceptionWordChecker exceptionWordChecker;
	private final ComplexityChecker complexityChecker;
	
	// ================================================================
	// ======================== Constructor(s) ========================
	// ================================================================
	public SentenceAnalyser(ConfigReader configReader)
	{
		exceptionWordChecker = new ExceptionWordChecker(configReader);
		supervativeChecker = new SuperlativeChecker(configReader);
		complexityChecker = new ComplexityChecker(configReader);
	}
	
	// ================================================================
	// ======================== Public Methods ========================
	// ================================================================
	public final SentenceSummary analyseSentence(String originalSentence,
		String[] tokenisedSentence)
	{
		ArrayList<DecomposedWord> complexityMatches = new ArrayList<DecomposedWord>();
		
		int i = 0;
		int nrWords = tokenisedSentence.length;
		DecomposedWord complexityWord;
		int exceptionSequenceLength;
		
		while(i < nrWords)
		{
			// See if we can skip over one or more exception words before running any other checks.
			exceptionSequenceLength = exceptionWordChecker.check(tokenisedSentence, i);
			if(exceptionSequenceLength > 0)
			{
				i += exceptionSequenceLength;
				continue;
			}
			
			// TODO For testing only!
			Config.checkForUnknownSymbols(tokenisedSentence[i]);
			
			// Test if the current word is a superlative.
			complexityWord = supervativeChecker.check(tokenisedSentence, i);
			if(complexityWord != null)
			{
				complexityMatches.add(complexityWord);
				i += complexityWord.nrWordsConsumed;
				continue;
			}
			
			// Check if the currentWord is a complexity indicator.
			complexityWord = complexityChecker.check(tokenisedSentence, i);
			if(complexityWord != null)
			{
				complexityMatches.add(complexityWord);
				i += complexityWord.nrWordsConsumed;
				continue;
			}
			
			// No rules applied. Go to the next word.
			i++;
		}
		return buildSentenceSummary(originalSentence, tokenisedSentence, complexityMatches);
	}
	
	// ================================================================
	// ======================= Private Methods ========================
	// ================================================================
	private final SentenceSummary buildSentenceSummary(String originalSentence,
		String[] tokenisedSentence, ArrayList<DecomposedWord> complexityMatches)
	{
		ArrayList<DecomposedWord> lcList = new ArrayList<DecomposedWord>();
		ArrayList<DecomposedWord> hcList = new ArrayList<DecomposedWord>();
		int complexityResult;
		
		for(DecomposedWord complexityMatch : complexityMatches)
		{
			// Categorise the complexity word into low or high complexity.
			complexityResult = complexityChecker.complexityOf(complexityMatch);
			
			if(complexityResult == -1)
				lcList.add(complexityMatch);
			else if(complexityResult == 1)
				hcList.add(complexityMatch);
			
			// Note: The complexity result could also be 0. In this case the user will already have
			// been informed of failure to categorise the stem of the complexity word. The word is
			// not added to either list i.e. is treated as though it never existed.
		}
		return new SentenceSummary(originalSentence, tokenisedSentence, lcList, hcList);
	}
}
