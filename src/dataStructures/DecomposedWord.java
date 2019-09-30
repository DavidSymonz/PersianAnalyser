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

public final class DecomposedWord
{
	public final String prefix;
	public final ArrayList<String> stemWords;
	public final String postfix;
	public ArrayList<String> trailingNegationVerbs;
	
	// This is a bit of a hack, don't use unless you know what you're doing!
	public int nrWordsConsumed;
	
	public DecomposedWord(String prefix, ArrayList<String> stemWords, String postfix)
	{
		this.prefix = prefix;
		this.stemWords = stemWords;
		this.postfix = postfix;
		nrWordsConsumed = 0;
	}
	
	public DecomposedWord(String prefix, String stem, String postfix)
	{
		this.prefix = prefix;
		this.stemWords = new ArrayList<String>(1);
		stemWords.add(stem);
		this.postfix = postfix;
		nrWordsConsumed = 0;
	}
	
	public final void setTrailingNegationVerbSequence(ArrayList<String> trailingNegationVerbs)
	{
		this.trailingNegationVerbs = trailingNegationVerbs;
	}
	
	public final String toString()
	{
		String prefixRep  = prefix  == null ? "" : prefix;
		String postfixRep = postfix == null ? "" : postfix;
		
		if(prefixRep.length() > 0)
			prefixRep = prefixRep + '-';
		
		if(postfixRep.length() > 0)
			postfixRep = '-' + postfixRep;
		
		return '[' + prefixRep + getStemRep() + postfixRep + getNegationVerbsRep() + ']'; 
	}
	
	private final String getStemRep()
	{
		int n = stemWords.size();
		
		String stemRep = stemWords.get(0);
		
		for(int i = 1; i < n; i++)
			stemRep += " " + stemWords.get(i);
		
		return stemRep;
	}
	
	private final String getNegationVerbsRep()
	{
		if(trailingNegationVerbs == null)
			return "";
		
		int n = trailingNegationVerbs.size();
		
		String negationVerbsRep = trailingNegationVerbs.get(0);
		
		for(int i = 1; i < n; i++)
			negationVerbsRep += " " + trailingNegationVerbs.get(i);
		
		return " -> " + negationVerbsRep;
	}
}
