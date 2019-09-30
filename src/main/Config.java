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
import java.util.ArrayList;
import java.util.HashSet;
import utils.UChar;
import utils.UFile;

public final class Config
{
	// ================================================================
	// ======================== Static Fields =========================
	// ================================================================
	public static final boolean TEST_MODE = false;
	
	public static final String CONFIG_FOLDER = "config";
	
	public static final String LC_FILE = "lc.txt";
	public static final String HC_FILE = "hc.txt";
	public static final String PREFIX_FILE = "prefixes.txt";
	public static final String POSTFIX_FILE = "postfixes.txt";
	public static final String NEGATING_VERBS_FILE = "negatingVerbs.txt";
	public static final String EXCEPTION_WORDS_FILE = "exceptionWords.txt";
	public static final String SUBSTITUTION_FILE = "substitutions.txt";
	public static final String ALPHABET_FILE = "alphabet.txt";
	
	private static ArrayList<String[]> substitutionRules;
	private static ArrayList<String> deletionRules;
	private static HashSet<Character> knownCharacters;
	
	// ================================================================
	// ======================== Static Methods ========================
	// ================================================================
	public static final boolean readAlphabetFile()
	{
		knownCharacters = new HashSet<Character>();
		
		BufferedReader br = UFile.reader_force(Config.CONFIG_FOLDER, Config.ALPHABET_FILE);
		if(br == null)
		{
			System.out.println("Could not open file: " + Config.ALPHABET_FILE);
			return false;
		}
		
		ArrayList<String> alphabetLines = UFile.readLines(br);
		if(alphabetLines == null)
		{
			System.out.println("Failed to read from: " + Config.ALPHABET_FILE);
			return false;
		}
		
		// Process each line in the alphabet file.
		String line;
		char[] charactersInLine;
		for(int i = 0; i < alphabetLines.size(); i++)
		{
			// Get the line as a string.
			line = alphabetLines.get(i);
			
			// There shouldn't be any whitespace but remove it just in case.
			line = line.trim();
			
			// Separate into individual characters.
			charactersInLine = line.toCharArray();
			
			// Add each character to the set of known characters.
			for(char c : charactersInLine)
				knownCharacters.add(c);
		}
		return true;
	}
	
	public static final void checkForUnknownSymbols(String[] wordSequence)
	{
		for(String word : wordSequence)
			checkForUnknownSymbols(word);
	}
	
	public static final void checkForUnknownSymbols(String word)
	{
		char[] charsInWord = word.toCharArray();
		
		for(char c : charsInWord)
		{
			if(!knownCharacters.contains(c))
				System.out.println("Warning: Unknown character: >" + c + "<");
		}
	}
	
	public static final boolean readSubstitutionFile()
	{
		substitutionRules = new ArrayList<String[]>();
		deletionRules = new ArrayList<String>();
		
		BufferedReader br = UFile.reader_force(Config.CONFIG_FOLDER, Config.SUBSTITUTION_FILE);
		if(br == null)
		{
			System.out.println("Could not open file: " + Config.SUBSTITUTION_FILE);
			return false;
		}
		
		ArrayList<String> substitutionRuleList = UFile.readLines(br);
		if(substitutionRuleList == null)
		{
			System.out.println("Failed to read from: " + Config.SUBSTITUTION_FILE);
			return false;
		}
		
		// Process each postfix-value pair.
		String substitutionRuleAsOneString;
		String[] lhsAndRhsSeparated;
		for(int i = 0; i < substitutionRuleList.size(); i++)
		{
			// The prefix to be processed.
			substitutionRuleAsOneString = substitutionRuleList.get(i);
			
			// Split the rule into lhs (the string to be replaced) and rhs (the replacement string).
			lhsAndRhsSeparated = substitutionRuleAsOneString.split("\t");
			
			// There must be exactly two parts (lhs and rhs). If not complain.
			if(lhsAndRhsSeparated.length != 2)
			{
				System.out.println("Invalid substitution rule: " + substitutionRuleAsOneString);
				return false;
			}
			
			// Store the rule in the appropriate list.
			// Note: The user writes "DELETE" in the file because they cannot but "" (empty string)
			// due to the requirement of having exactly one lhs and one rhs value.
			if(lhsAndRhsSeparated[1].equals("DELETE"))
				deletionRules.add(lhsAndRhsSeparated[0]);
			else
				substitutionRules.add(lhsAndRhsSeparated);
		}
		return true;
	}
	
	public static final String standardisePunctuation(String s)
	{
		// Convert Farsi's slightly different punctuation into standard punctuation.
		// This is done before splitting into sentences because we want to be able to split on
		// standard punctuation.
		s = s.replaceAll(UChar.FARSI_SEMICOLON_S, ";");
		s = s.replaceAll(UChar.FARSI_QUESTION_MARK_S, "?");
		s = s.replaceAll(UChar.FARSI_COMMA_S, ",");
		
		// Return the string with standardised punctuation.
		return s;
	}
	
	// Applied to a whole sentence at a time before it is split into individual words.
	public static final String standardiseSourceSentence(String s)
	{
		// Actually just applying the same rules as to all other strings.
		return cleanString(s);
	}
	
	// Applied to the individual words in a sentence read from a source file.
	public static final String standardiseSourceToken(String s)
	{
		// Currently not in use.
		return s;
	}
	
	public static final String cleanString(String s)
	{
		// Substitute similar characters with their standard equivalent.
		s = performSubstitutions(s);
		
		// Delete unwanted characters.
		s = performDeletions(s);
		
		// Remove any leading and/or trailing whitespace.
		s = s.trim();
		
		// Converts the string to lower case if applicable to the language in use.
		s = toLowerCase(s);
		
		// Return the cleaned string.
		return s;
	}
	
	public static final String performSubstitutions(String s)
	{
		// Very annoying invisible character.
		s = s.replaceAll(UChar.ZERO_WIDTH_NON_JOINER_S, " ");
		
		// Replace all "half space" characters will standard spaces.
		s = s.replaceAll(UChar.HALF_SPACE_S, " ");
		
		// Brackets are also a regular expression concept, so you can't just use "(" as the regex!
		// You can either escape like this: "\\(" or use a group that is indicated using square
		// brackets: "[(]". The thing inside the brackets is the actual regular expression you're
		// looking for. Brackets are replaced by spaces rather than deleted because deletion may
		// join adjacent words together without a space in between.
		s = s.replaceAll("[(]", " ");
		s = s.replaceAll("[)]", " ");
		
		// User defined substitutions.
		for(String[] rule : substitutionRules)
			s = s.replaceAll(rule[0], rule[1]);
		
		return s;
	}
	
	public static final String performDeletions(String s)
	{
		// Deleting invisible characters here rather than using the substitution file.
		// This is done to avoid confusion with invisible characters in the file.
		s = s.replaceAll(UChar.RIGHT_TO_LEFT_OVERRIDE_S, "");
		s = s.replaceAll(UChar.LEFT_TO_RIGHT_OVERRIDE_S, "");
		s = s.replaceAll(UChar.POP_DIRECTIONAL_FORMATTING_S, "");
		
		// Remove all punctuation.
		s = s.replaceAll("\\p{Punct}", "");
		
		// User defined deletions.
		for(String rule : deletionRules)
			s = s.replaceAll(rule, "");
		
		return s;
	}
	
	private static final String toLowerCase(String s)
	{
		// Probably best not do this for Farsi.
		//s = s.toLowerCase();
		return s;
	}
	
	// For detecting zero-length or invisible characters.
	public static final void specialPrintln(String s)
	{
		char[] chars = s.toCharArray();
		int value;
		for(int i = 0; i < chars.length; i++)
		{
			value = chars[i];
			System.out.print(value + " ");
		}
		System.out.println();
	}
	
	public static final void specialPrint(String s)
	{
		char[] chars = s.toCharArray();
		int value;
		for(int i = 0; i < chars.length; i++)
		{
			value = chars[i];
			System.out.print(value + " ");
		}
	}
}
