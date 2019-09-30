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

import dataStructures.DecomposedWord;

public class SuperlativeChecker
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
	private final String superlativePostfix;
	private final int superlativePostfixLength;
	
	// ================================================================
	// ======================== Constructor(s) ========================
	// ================================================================
	public SuperlativeChecker(ConfigReader configReader)
	{
		superlativePostfix = configReader.farsiSuperlativePostfix;
		superlativePostfixLength = superlativePostfix.length();
	}
	
	// ================================================================
	// ======================== Public Methods ========================
	// ================================================================
	public final DecomposedWord check(String[] tokenisedSentence, int i)
	{
		String word = tokenisedSentence[i];
		
		// The current word has the superlative postfix.
		if(word.endsWith(superlativePostfix))
		{
			// Extract the stem of the word.
			String stem = word.substring(0, word.length() - superlativePostfixLength);
			
			// Create a corresponding DecomposedWord instance.
			DecomposedWord superlative = new DecomposedWord(null, stem, superlativePostfix);
			
			// One word has been used up.
			superlative.nrWordsConsumed = 1;
			
			return superlative;
		}
		
		// If there are no more words left in the sentence, the superlative ending cannot be
		// disconnected. In this case there cannot be a superlative.
		// But the reverse of this condition is added in the following test, so we don't need this!
		//if(i + 1 >= tokenisedSentence.length)
		//	return null;
		
		// There is another word following the current word. Check if it is the superlative-postfix.
		// Note: An exact match is required i.e. the postfix must be equal to the entire next word.
		if(i + 1 < tokenisedSentence.length && tokenisedSentence[i + 1].equals(superlativePostfix))
		{
			// Yes, the next word is a disconnected superlative-postfix.
			// The current word is the stem and the next word is the postfix.
			DecomposedWord superlative = new DecomposedWord(null, word, superlativePostfix);
			
			// Two words have been used up (the current word and the disconnected postfix).
			superlative.nrWordsConsumed = 2;
			
			return superlative;
		}
		
		// The word is neither a superlative, nor a stem with disconnected superlative-postfix.
		return null;
	}
	
	// ================================================================
	// ======================= Private Methods ========================
	// ================================================================
}
