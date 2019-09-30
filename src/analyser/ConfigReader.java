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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import main.Config;
import utils.UArray;
import utils.UFile;
import utils.UString;
import dataStructures.CharTree;
import dataStructures.StringTree;

public class ConfigReader
{
	// ================================================================
	// ======================== Static Fields =========================
	// ================================================================
	private static final String FARSI_SUPERLATIVE_POSTFIX = "ترین";
	
	// ================================================================
	// ======================== Static Methods ========================
	// ================================================================
	
	// ================================================================
	// ============================ Fields ============================
	// ================================================================
	// Configurations and data read from file.
	public HashSet<ArrayList<String>> lcWords;
	public HashSet<ArrayList<String>> hcWords;
	public HashMap<String, Byte> prefixLookupTable;
	public HashMap<String, Byte> postfixLookupTable;
	public String farsiSuperlativePostfix;
	public ArrayList<String[]> substitutionRules;
	
	// Metadata.
	public final StringTree complexityWordSequenceTree;
	public final StringTree exceptionWordsTree;
	public final StringTree negatingVerbSequenceTree;
	public final CharTree prefixTree;
	public final CharTree postfixTree;
	
	// ================================================================
	// ======================== Constructor(s) ========================
	// ================================================================
	public ConfigReader()
	{
		lcWords = new HashSet<ArrayList<String>>();
		hcWords = new HashSet<ArrayList<String>>();
		prefixLookupTable = new HashMap<String, Byte>();
		postfixLookupTable = new HashMap<String, Byte>();
		
		complexityWordSequenceTree = new StringTree();
		exceptionWordsTree = new StringTree();
		negatingVerbSequenceTree = new StringTree();
		prefixTree = new CharTree();
		postfixTree = new CharTree();
	}
	
	// ================================================================
	// ======================== Public Methods ========================
	// ================================================================
	public final boolean setup()
	{
		System.out.println("Reading LC file...");
		if(!readLcFile())
			return false;
		
		System.out.println("Reading HC file...");
		if(!readHcFile())
			return false;
		
		System.out.println("Reading prefix file...");
		if(!readPrefixFile())
			return false;
		
		System.out.println("Reading postfix file...");
		if(!readPostfixFile())
			return false;
		
		System.out.println("Reading negatingVerbs file...");
		if(!readNegatingVerbsFile())
			return false;
		
		System.out.println("Reading exception words file...");
		if(!readExceptionWordsFile())
			return false;
		
		System.out.println("Reading superlative file...");
		if(!readSuperlativeFile())
			return false;
		
		return true;
	}
	
	// ================================================================
	// ======================= Private Methods ========================
	// ================================================================
	private final boolean readLcFile()
	{
		BufferedReader br = UFile.reader_force(Config.CONFIG_FOLDER, Config.LC_FILE);
		if(br == null)
		{
			System.out.println("Could not open file: " + Config.LC_FILE);
			return false;
		}
		
		ArrayList<String> lcWordSequencesList = UFile.readLines(br);
		if(lcWordSequencesList == null)
		{
			System.out.println("Failed to read from: " + Config.LC_FILE);
			return false;
		}
		
		// Process each sequence of words.
		String sequenceAsOneString;
		String[] sequenceAsSeparateWords;
		for(int i = 0; i < lcWordSequencesList.size(); i++)
		{
			// The sequence to be processed.
			sequenceAsOneString = lcWordSequencesList.get(i);
			
			// Clean up the string.
			sequenceAsOneString = Config.cleanString(sequenceAsOneString);
			
			// Split into individual words on any whitespace boundary.
			sequenceAsSeparateWords = sequenceAsOneString.split(UFile.ANY_WHITE_SPACE_REG_EX);
			
			// Add the sequence to the tree.
			complexityWordSequenceTree.addValidWordSequence(sequenceAsSeparateWords);
			
			// Also add to the LC HashSet.
			lcWords.add(UArray.arrayToArrayList(sequenceAsSeparateWords));
			
			// TODO For testing.
			Config.checkForUnknownSymbols(sequenceAsSeparateWords);
		}
		return true;
	}
	
	private final boolean readHcFile()
	{
		BufferedReader br = UFile.reader_force(Config.CONFIG_FOLDER, Config.HC_FILE);
		if(br == null)
		{
			System.out.println("Could not open file: " + Config.HC_FILE);
			return false;
		}
		
		ArrayList<String> hcWordSequencesList = UFile.readLines(br);
		if(hcWordSequencesList == null)
		{
			System.out.println("Failed to read from: " + Config.HC_FILE);
			return false;
		}
		
		// Process each sequence of words.
		String sequenceAsOneString;
		String[] sequenceAsSeparateWords;
		for(int i = 0; i < hcWordSequencesList.size(); i++)
		{
			// The sequence to be processed.
			sequenceAsOneString = hcWordSequencesList.get(i);
			
			// Clean up the string.
			sequenceAsOneString = Config.cleanString(sequenceAsOneString);
			
			// Split into individual words on any whitespace boundary.
			sequenceAsSeparateWords = sequenceAsOneString.split(UFile.ANY_WHITE_SPACE_REG_EX);
			
			// Add the sequence to the tree.
			complexityWordSequenceTree.addValidWordSequence(sequenceAsSeparateWords);
			
			// Also add to the HC HashSet.
			hcWords.add(UArray.arrayToArrayList(sequenceAsSeparateWords));
			
			// TODO For testing.
			Config.checkForUnknownSymbols(sequenceAsSeparateWords);
		}
		return true;
	}
	
	private final boolean readPrefixFile()
	{
		BufferedReader br = UFile.reader_force(Config.CONFIG_FOLDER, Config.PREFIX_FILE);
		if(br == null)
		{
			System.out.println("Could not open file: " + Config.PREFIX_FILE);
			return false;
		}
		
		ArrayList<String> prefixAndComplexityValueList = UFile.readLines(br);
		if(prefixAndComplexityValueList == null)
		{
			System.out.println("Failed to read from: " + Config.PREFIX_FILE);
			return false;
		}
		
		// Process each prefix-value pair.
		String prefixAndValueAsOneString;
		String[] prefixAndValueSeparated;
		for(int i = 0; i < prefixAndComplexityValueList.size(); i++)
		{
			// The prefix to be processed.
			prefixAndValueAsOneString = prefixAndComplexityValueList.get(i);
			
			// Make sure there is no leading or trailing whitespace that could mess up the split.
			prefixAndValueAsOneString = prefixAndValueAsOneString.trim();
			
			// Split into key and value on any whitespace boundary.
			prefixAndValueSeparated = prefixAndValueAsOneString.split(UFile.ANY_WHITE_SPACE_REG_EX);
			
			// Interpret the second string as the complexity value.
			Byte valueAsByte = UString.parseByte(prefixAndValueSeparated[1]);
			if(valueAsByte == null)
			{
				System.out.println(prefixAndValueAsOneString + " is not a valid key-value pair!");
				return false;
			}
			// Clean up the string.
			String cleanedPrefix = Config.cleanString(prefixAndValueSeparated[0]);
			
			// Put the (cleaned) key into the tree in "normal" left to right order!
			prefixTree.addValidCharacterSequence_leftToRight(cleanedPrefix);
			
			// Also put the (cleaned) key into prefix lookup table.
			prefixLookupTable.put(cleanedPrefix, valueAsByte);
			
			// TODO For testing.
			Config.checkForUnknownSymbols(cleanedPrefix);
		}
		return true;
	}
	
	private final boolean readPostfixFile()
	{
		BufferedReader br = UFile.reader_force(Config.CONFIG_FOLDER, Config.POSTFIX_FILE);
		if(br == null)
		{
			System.out.println("Could not open file: " + Config.POSTFIX_FILE);
			return false;
		}
		
		ArrayList<String> postfixAndComplexityValueList = UFile.readLines(br);
		if(postfixAndComplexityValueList == null)
		{
			System.out.println("Failed to read from: " + Config.POSTFIX_FILE);
			return false;
		}
		
		// Process each postfix-value pair.
		String postfixAndValueAsOneString;
		String[] postfixAndValueSeparated;
		for(int i = 0; i < postfixAndComplexityValueList.size(); i++)
		{
			// The prefix to be processed.
			postfixAndValueAsOneString = postfixAndComplexityValueList.get(i);
			
			// Make sure there is no leading or trailing whitespace that could mess up the split.
			postfixAndValueAsOneString = postfixAndValueAsOneString.trim();
			
			// Split into key and value on any whitespace boundary.
			postfixAndValueSeparated = postfixAndValueAsOneString.split(UFile.ANY_WHITE_SPACE_REG_EX);
			
			// Interpret the second string as the complexity value.
			Byte valueAsByte = UString.parseByte(postfixAndValueSeparated[1]);
			if(valueAsByte == null)
			{
				System.out.println(postfixAndValueAsOneString + " is not a valid key-value pair!");
				return false;
			}
			// Clean up the string.
			String cleanedPostfix = Config.cleanString(postfixAndValueSeparated[0]);
			
			// Put the (cleaned) key into the tree in "reverse" right to left order!
			postfixTree.addValidCharacterSequence_rightToLeft(cleanedPostfix);
			
			// Also put the (cleaned) key into postfix lookup table.
			postfixLookupTable.put(cleanedPostfix, valueAsByte);
			
			// TODO For testing.
			Config.checkForUnknownSymbols(cleanedPostfix);
		}
		return true;
	}
	
	private final boolean readNegatingVerbsFile()
	{
		BufferedReader br = UFile.reader_force(Config.CONFIG_FOLDER, Config.NEGATING_VERBS_FILE);
		if(br == null)
		{
			System.out.println("Could not open file: " + Config.NEGATING_VERBS_FILE);
			return false;
		}
		
		ArrayList<String> negatingVerbSequencesList = UFile.readLines(br);
		if(negatingVerbSequencesList == null)
		{
			System.out.println("Failed to read from: " + Config.NEGATING_VERBS_FILE);
			return false;
		}
		
		// Process each sequence of words.
		String sequenceAsOneString;
		String[] sequenceAsSeparateWords;
		for(int i = 0; i < negatingVerbSequencesList.size(); i++)
		{
			// The sequence to be processed.
			sequenceAsOneString = negatingVerbSequencesList.get(i);
			
			// Clean up the string.
			sequenceAsOneString = Config.cleanString(sequenceAsOneString);
			
			// Split into individual words on any whitespace boundary.
			sequenceAsSeparateWords = sequenceAsOneString.split(UFile.ANY_WHITE_SPACE_REG_EX);
			
			// Add the sequence to the tree.
			negatingVerbSequenceTree.addValidWordSequence(sequenceAsSeparateWords);
			
			// TODO For testing.
			Config.checkForUnknownSymbols(sequenceAsSeparateWords);
		}
		return true;
	}
	
	private final boolean readExceptionWordsFile()
	{
		BufferedReader br = UFile.reader_force(Config.CONFIG_FOLDER, Config.EXCEPTION_WORDS_FILE);
		if(br == null)
		{
			System.out.println("Could not open file: " + Config.EXCEPTION_WORDS_FILE);
			return false;
		}
		
		ArrayList<String> exceptionWordSequencesList = UFile.readLines(br);
		if(exceptionWordSequencesList == null)
		{
			System.out.println("Failed to read from: " + Config.EXCEPTION_WORDS_FILE);
			return false;
		}
		
		// Process each sequence of words.
		String sequenceAsOneString;
		String[] sequenceAsSeparateWords;
		for(int i = 0; i < exceptionWordSequencesList.size(); i++)
		{
			// The sequence to be processed.
			sequenceAsOneString = exceptionWordSequencesList.get(i);
			
			// Clean up the string.
			sequenceAsOneString = Config.cleanString(sequenceAsOneString);
			
			// Split into individual words on any whitespace boundary.
			sequenceAsSeparateWords = sequenceAsOneString.split(UFile.ANY_WHITE_SPACE_REG_EX);
			
			// Add the sequence to the tree.
			exceptionWordsTree.addValidWordSequence(sequenceAsSeparateWords);
			
			// TODO For testing.
			Config.checkForUnknownSymbols(sequenceAsSeparateWords);
		}
		return true;
	}
	
	private final boolean readSuperlativeFile()
	{
		farsiSuperlativePostfix = FARSI_SUPERLATIVE_POSTFIX;
		return true;
	}
}
