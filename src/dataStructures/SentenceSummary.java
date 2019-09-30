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
import utils.UArray;
import utils.UFile;
import utils.UList;

public final class SentenceSummary
{
	// Raw data.
	public final String originalSentence;
	public final String[] tokenisedSentence;
	public final ArrayList<DecomposedWord> lcList;
	public final ArrayList<DecomposedWord> hcList;
	
	// Constructor.
	public SentenceSummary(String originalSentence, String[] tokenisedSentence,
		ArrayList<DecomposedWord> lcList, ArrayList<DecomposedWord> hcList)
	{
		this.originalSentence = originalSentence;
		this.tokenisedSentence = tokenisedSentence;
		this.lcList = lcList;
		this.hcList = hcList;
	}
	
	public final int getLcCount()
	{
		return lcList.size();
	}
	
	public final int getHcCount()
	{
		return hcList.size();
	}
	
	public final String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("Original     : ");
		sb.append(originalSentence);
		sb.append(UFile.NEWLINE);
		
		sb.append("Tokenised    : ");
		sb.append(UArray.arrayToString(tokenisedSentence));
		sb.append(UFile.NEWLINE);
		
		sb.append("LC words (");
		sb.append(getLcCount());
		sb.append(") : ");
		sb.append(UList.listToString(lcList));
		sb.append(UFile.NEWLINE);
		
		sb.append("HC words (");
		sb.append(getHcCount());
		sb.append(") : ");
		sb.append(UList.listToString(hcList));
		sb.append(UFile.NEWLINE);
		
		return sb.toString();
	}
}
