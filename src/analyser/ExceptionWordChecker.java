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

public final class ExceptionWordChecker
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
	private final ConfigReader listsReader;
	
	// ================================================================
	// ======================== Constructor(s) ========================
	// ================================================================
	public ExceptionWordChecker(ConfigReader configReader)
	{
		this.listsReader = configReader;
	}
	
	// ================================================================
	// ======================== Public Methods ========================
	// ================================================================
	public final int check(String[] tokenisedSentence, int i)
	{
		/*
		 * Note:
		 * We make the assumption that the exceptionWordsTree has been built using exception
		 * words that have already got pre and/or postfixes on them.
		 * This allows us to check for whole words only - without having to dissect them.
		 */
		return listsReader.exceptionWordsTree.getLongestAcceptSequenceLength(tokenisedSentence, i);
	}
	
	// ================================================================
	// ======================= Private Methods ========================
	// ================================================================
}
