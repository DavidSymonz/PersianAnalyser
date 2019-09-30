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
import utils.UList;
import dataStructures.DecomposedWord;

public final class ComplexityChecker
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
	public ComplexityChecker(ConfigReader configReader)
	{
		this.listsReader = configReader;
	}
	
	// ================================================================
	// ======================== Public Methods ========================
	// ================================================================
	public final DecomposedWord check(String[] tokenisedSentence, int i)
	{
		// See if a complexity word is identified.
		DecomposedWord complexityWord = checkForComplexityWord(tokenisedSentence, i);
		
		// If none was identified return.
		if(complexityWord == null)
			return null;
		
		// If a complexity word was identified check for trailing negation verbs.
		// The sequence (if any) must start at the first index after the identified complexity word.
		int indexAfterComplexityWord = i + complexityWord.nrWordsConsumed;
		
		// Find a sequence (if any) and assign it to the complexity word.
		complexityWord.setTrailingNegationVerbSequence(
			checkForTrailingNegationVerbSeq(tokenisedSentence, indexAfterComplexityWord));
		
		// If there is a sequence, then add it's length to the number of words consumed.
		if(complexityWord.trailingNegationVerbs != null)
			complexityWord.nrWordsConsumed += complexityWord.trailingNegationVerbs.size();
		
		// Return the complexity word.
		return complexityWord;
	}
	
	public final int complexityOf(DecomposedWord complexityWord)
	{
		// Any superlative is classified as low complexity.
		if(listsReader.farsiSuperlativePostfix.equals(complexityWord.postfix))
			return -1;
		
		// The word is not a superlative. Examine its stem and consider pre- and postfixes.
		return getPrefixComplexity(complexityWord.prefix)
			* getPostfixComplexity(complexityWord.postfix)
			* getStemComplexity(complexityWord.stemWords)
			* getNegationSeqComplexity(complexityWord.trailingNegationVerbs);
	}
	
	// ================================================================
	// ======================= Private Methods ========================
	// ================================================================
	private final ArrayList<String> checkForTrailingNegationVerbSeq(String[] tokenisedSentence,
		int indexAfterComplexityWord)
	{
		// Get the length of the longest sequence of negating verbs you can find.
		int matchLength = negatingVerbs_nrMatchingWords(tokenisedSentence, indexAfterComplexityWord);
		
		// None found.
		if(matchLength < 1)
			return null;
		
		// A sequence of one or more negating verbs was found. Return it.
		return collectMatchingWords(tokenisedSentence, indexAfterComplexityWord, matchLength);
	}
	
	private final DecomposedWord checkForComplexityWord(String[] tokenisedSentence, int i)
	{
		int matchLength = complexity_nrMatchingWords(tokenisedSentence, i);
		
		// Case: Direct multi-word match.
		if(matchLength > 1)
			return directMultiWordMatch(tokenisedSentence, i, matchLength);
		
		// Case: Direct single-word match.
		if(matchLength == 1)
			return directSingleWordMatch(tokenisedSentence, i);
		
		// Case: No direct match.
		return noDirectMatch(tokenisedSentence, i);
	}
	
	private final DecomposedWord directMultiWordMatch(String[] tokenisedSentence, int i,
		int matchLength)
	{
		// No prefix allowed, no postfix allowed.
		// The stem is the concatenation of all words in the matching sequence.
		DecomposedWord result = new DecomposedWord(null,
			collectMatchingWords(tokenisedSentence, i, matchLength), null);
		result.nrWordsConsumed = matchLength;
		return result;
	}
	
	private final DecomposedWord directSingleWordMatch(String[] tokenisedSentence, int i)
	{
		// Check for a disconnected prefix.
		String prefix = null;
		int prefixIndex = i - 1;
		if(prefixIndex >= 0 && complexity_isPrefix(tokenisedSentence[prefixIndex]))
			prefix = tokenisedSentence[prefixIndex];
		
		// Check for a disconnected postfix.
		String postfix = null;
		int postfixIndex = i + 1;
		if(postfixIndex < tokenisedSentence.length
			&& complexity_isPostfix(tokenisedSentence[postfixIndex]))
		{
			postfix = tokenisedSentence[postfixIndex];
		}
		
		// The stem is the whole "currentWord" (nothing cut off).
		DecomposedWord result = new DecomposedWord(prefix, tokenisedSentence[i], postfix);
		result.nrWordsConsumed = postfix == null ? 1 : 2;
		return result;
	}
	
	// Case: No direct match (need to check for integrated pre- and/or postfixes)!
	private final DecomposedWord noDirectMatch(String[] tokenisedSentence, int i)
	{
		// Backup of the current token (also a shortcut to the original token).
		String word = tokenisedSentence[i];
		
		// First see if there is an integrated postfix.
		ArrayList<Integer> postfixLengths = complexity_integratedPostfixLengths(word);
		
		// The word does NOT have any integrated postfixes.
		if(postfixLengths.isEmpty())
			return noIntegratedPostfix(tokenisedSentence, i);
		
		// There are one or more possible postfixes. Try them all.
		DecomposedWord match;
		for(int postfixLength : postfixLengths)
		{
			// If the word is the postfix on its own, then there is nothing left to match with.
			// Since the postfix length only gets longer with each iteration, there is no point
			// continuing, so don't waste more time and stop right away.
			if(postfixLength >= word.length())
				return null;
			
			// Try the current postfix(-length).
			match = hasIntegratedPostfix(tokenisedSentence, i, postfixLength);
			
			// Return as soon as you have a match. Theoretically there could be a longer postfix
			// that also has a match, when there are multiple solutions we choose the one with
			// the shortest postfix.
			if(match != null)
				return match;
		}
		// Still not match after having checked all possible prefixes.
		return null;
	}
	
	private final DecomposedWord noIntegratedPostfix(String[] tokenisedSentence, int i)
	{
		// No postfix and no match (yet). See if there is an integrated prefix.
		ArrayList<Integer> prefixLengths = complexity_integratedPrefixLengths(tokenisedSentence[i]);
		
		// The word does NOT have any integrated prefixes. There cannot be a match.
		if(prefixLengths.isEmpty())
			return null;
		
		// There are one or more possible prefixes. Try them all.
		DecomposedWord match;
		for(int prefixLength : prefixLengths)
		{
			// If the word is the prefix on its own, then there is nothing left to match with.
			// Since the prefix length only gets longer with each iteration, there is no point
			// continuing, so stop now and don't lose time.
			if(prefixLength >= tokenisedSentence[i].length())
				return null;
			
			// Try the current prefix(-length).
			match = noIntegratedPostfixButPrefix(tokenisedSentence, i, prefixLength);
			
			// Return as soon as you have a match. Theoretically there could be a longer prefix
			// that also has a match, when there are multiple solutions we choose the one with
			// the shortest prefix.
			if(match != null)
				return match;
		}
		// Still not match after having checked all possible prefixes.
		return null;
	}
	
	private final DecomposedWord noIntegratedPostfixButPrefix(String[] tokenisedSentence, int i,
		int prefixLength)
	{
		// Backup of the current token (also a shortcut to the original token).
		String word = tokenisedSentence[i];
		
		// The word has an integrated prefix! Remove it from the current token.
		tokenisedSentence[i] = word.substring(prefixLength, word.length());
		
		// Try for another match now the prefix has been removed.
		// Note: This does allow a multi-word matches to have pre- and/or postfixes!
		// This is apparently never the case anyway so it is irrelevant if it is allowed or not.
		int matchLength = complexity_nrMatchingWords(tokenisedSentence, i);
		
		// No match even after removing the prefix.
		if(matchLength == 0)
		{
			// Put the removed prefix back.
			tokenisedSentence[i] = word;
			return null;
		}
		
		// Save the prefix.
		String prefix = word.substring(0, prefixLength);
		
		// Check for a disconnected postfix.
		String postfix = null;
		int postfixIndex = i + matchLength;
		if(postfixIndex < tokenisedSentence.length
			&& complexity_isPostfix(tokenisedSentence[postfixIndex]))
		{
			postfix = tokenisedSentence[postfixIndex];
		}
		
		// Create the result.
		DecomposedWord result = new DecomposedWord(prefix,
			collectMatchingWords(tokenisedSentence, i, matchLength), postfix);
		result.nrWordsConsumed = matchLength + (postfix == null ? 0 : 1);
		
		// Put the removed prefix back.
		tokenisedSentence[i] = word;
		return result;
	}
	
	private final DecomposedWord hasIntegratedPostfix(String[] tokenisedSentence, int i,
		int postfixLength)
	{
		// Backup of the current token (also a shortcut to the original token).
		String word = tokenisedSentence[i];
		
		// The word has an integrated postfix! Remove it from the current token.
		tokenisedSentence[i] = word.substring(0, word.length() - postfixLength);
		
		// Save the postfix.
		String postfix = word.substring(word.length() - postfixLength, word.length());
		
		// Stem (required to be of length 1) + integrated postfix.
		if(complexity_singleWordMatch(tokenisedSentence[i]))
		{
			// Check for a disconnected prefix.
			String prefix = null;
			int prefixIndex = i - 1;
			if(prefixIndex >= 0 && complexity_isPrefix(tokenisedSentence[prefixIndex]))
				prefix = tokenisedSentence[prefixIndex];
			
			DecomposedWord result = new DecomposedWord(prefix, tokenisedSentence[i], postfix);
			result.nrWordsConsumed = 1;
			
			// Put the removed postfix back.
			tokenisedSentence[i] = word;
			return result;
		}
		
		// No match even after removing the postfix. See if there are ALSO a prefixes.
		ArrayList<Integer> prefixLengths = complexity_integratedPrefixLengths(tokenisedSentence[i]);
		
		// The word does NOT have any integrated prefixes. There cannot be a match.
		if(prefixLengths.isEmpty())
		{
			// Put the removed postfix back.
			tokenisedSentence[i] = word;
			return null;
		}
		
		// There are one or more possible prefixes. Try them all.
		DecomposedWord match;
		for(int prefixLength : prefixLengths)
		{
			// Note: DO NOT return if the prefix is the whole remaining token! This leaves no word
			// stem to match BUT we may later add the postfix back on, thus creating a non-zero
			// length stem! I think this is only relevant when you have a complexity word that is
			// also identical to a valid postfix. Probably doesn't exist, but just in case...
			
			// Try the current prefix(-length).
			match = hasIntegratedPostfixAndPrefix(tokenisedSentence, i, postfix, prefixLength);
			
			// Return as soon as you have a match. Theoretically there could be a longer prefix
			// that also has a match; when there are multiple solutions we choose the one with
			// the shortest prefix.
			if(match != null)
			{
				// Put the removed postfix back.
				tokenisedSentence[i] = word;
				return match;
			}
		}
		// Still not match after having checked all possible prefixes.
		// Put the removed postfix back.
		tokenisedSentence[i] = word;
		return null;
	}
	
	private final DecomposedWord hasIntegratedPostfixAndPrefix(String[] tokenisedSentence, int i,
		String postfix, int prefixLength)
	{
		// Backup of the current token (also a shortcut to the original token).
		String word = tokenisedSentence[i];
		
		// There is an integrated prefix. Remove it from the current token.
		tokenisedSentence[i] =
			tokenisedSentence[i].substring(prefixLength, tokenisedSentence[i].length());
		
		// Save the prefix.
		String prefix = word.substring(0, prefixLength);
		
		// Try again for a match.
		if(complexity_singleWordMatch(tokenisedSentence[i]))
		{
			DecomposedWord result = new DecomposedWord(prefix, tokenisedSentence[i], postfix);
			result.nrWordsConsumed = 1;
			
			// Put the removed pre- and postfixes back.
			tokenisedSentence[i] = word;
			return result;
		}
		
		// Still no match. There is just one possibility left: The postfix could have belonged to
		// the stem and we should not have removed it. So put it back on and see if we get a match
		// with the prefix taken off but the postfix (back) on.
		tokenisedSentence[i] += postfix;
		postfix = null;
		
		// There is the possibility of a multi-word match. Apparently this doesn't happen after a
		// prefix (which is present in this case) but I don't see why I should disallow it.
		// It will probably just not come up, but if it does and is undesirable then instead of
		// getting the match length use complexity_singleWordMatch.
		// The code for a single-word only match is commented out below.
		int matchLength = complexity_nrMatchingWords(tokenisedSentence, i);
		if(matchLength > 0)
		{
			// Check for a disconnected postfix.
			int postfixIndex = i + matchLength;
			if(postfixIndex < tokenisedSentence.length
				&& complexity_isPostfix(tokenisedSentence[postfixIndex]))
			{
				postfix = tokenisedSentence[postfixIndex];
			}
			
			DecomposedWord result = new DecomposedWord(
				prefix, collectMatchingWords(tokenisedSentence, i, matchLength), postfix);
			result.nrWordsConsumed = matchLength + postfix == null ? 0 : 1;
			
			// Reset the token to it's original value.
			tokenisedSentence[i] = word;
			return result;
		}
		
		/*
		if(analyser.complexity_singleWordMatch(tokenisedSentence[i]))
		{
			// Check for a disconnected postfix.
			int postfixIndex = i + 1;
			if(postfixIndex < tokenisedSentence.length
			 	&& analyser.complexity_isPostfix(tokenisedSentence[postfixIndex]))
			{
				postfix = tokenisedSentence[postfixIndex];
			}
			
			DecomposedWord result = new DecomposedWord(prefix, tokenisedSentence[i], postfix);
			result.nrWordsConsumed = 1 + postfix == null ? 0 : 1;
			
			// Reset the token to it's original value.
			tokenisedSentence[i] = word;
			
			return result;
		}
		*/
		
		// Run out of possibilities. There is no match.
		// Reset the token to it's original value.
		tokenisedSentence[i] = word;
		return null;
	}
	
	// ------------------ HELPERS --------------
	
	private final ArrayList<String> collectMatchingWords(String[] tokenisedSentence,
		int startIndex, int matchLength)
	{
		ArrayList<String> matchingWordList = new ArrayList<String>(matchLength);
		
		for(int i = 0; i < matchLength; i++)
			matchingWordList.add(tokenisedSentence[startIndex + i]);
		
		return matchingWordList;
	}
	
	// ------------------ WORD DISECTION --------------
	
	private final int complexity_nrMatchingWords(String[] tokenisedSentence, int i)
	{
		return listsReader.complexityWordSequenceTree.getLongestAcceptSequenceLength(
			tokenisedSentence, i);
	}
	
	private final boolean complexity_singleWordMatch(String word)
	{
		return listsReader.complexityWordSequenceTree.isSingleWordValid(word);
	}
	
	private final boolean complexity_isPrefix(String wholeWord)
	{
		return listsReader.prefixLookupTable.containsKey(wholeWord);
	}
	
	private final boolean complexity_isPostfix(String wholeWord)
	{
		return listsReader.postfixLookupTable.containsKey(wholeWord);
	}
	
	private final ArrayList<Integer> complexity_integratedPrefixLengths(String word)
	{
		return listsReader.prefixTree.getAllAcceptSequenceLengths_leftToRight(word, 0);
	}
	
	private final ArrayList<Integer> complexity_integratedPostfixLengths(String word)
	{
		return listsReader.postfixTree.getAllAcceptSequenceLengths_rightToLeft(
			word, word.length() - 1);
	}
	
	// ------------------ NEGATING VERBS --------------
	
	private final int negatingVerbs_nrMatchingWords(String[] tokenisedSentence, int i)
	{
		return listsReader.negatingVerbSequenceTree.getLongestAcceptSequenceLength(
			tokenisedSentence, i);
	}
	
	// ------------------ COMPLEXITY CATEGORISATION --------------
	
	private final byte getStemComplexity(ArrayList<String> stemWords)
	{
		if(listsReader.lcWords.contains(stemWords))
			return -1;
		
		if(listsReader.hcWords.contains(stemWords))
			return 1;
		
		// Could not get the complexity value of the stem (the actual complexity word(s))!
		// Warn the user and return 0 to completely neutralise the word.
		System.out.println("Error: Could not asess the complexity of "
			+ UList.listToString(stemWords));
		return 0;
	}
	
	private final byte getPrefixComplexity(String prefix)
	{
		// There is no prefix. In this case there is no effect on the complexity of the word,
		// so return 1 (the multiplicative identity).
		if(prefix == null)
			return 1;
		
		// Get the prefix's complexity value.
		Byte prefixComplexity = listsReader.prefixLookupTable.get(prefix);
		if(prefixComplexity != null)
			return prefixComplexity;
		
		// Could not get the complexity value of the given prefix.
		// Warn the user and treat as though the prefix did not exist.
		System.out.println("Error: Could not asess the prefix " + prefix + " for complexity");
		return 1;
	}
	
	private final byte getPostfixComplexity(String postfix)
	{
		// There is no postfix. In this case there is no effect on the complexity of the word,
		// so return 1 (the multiplicative identity).
		if(postfix == null)
			return 1;
		
		// Get the postfix's complexity value.
		Byte postfixComplexity = listsReader.postfixLookupTable.get(postfix);
		if(postfixComplexity != null)
			return postfixComplexity;
		
		// Could not get the complexity value of the given postfix.
		// Warn the user and treat as though the postfix did not exist.
		System.out.println("Error: Could not asess the postfix " + postfix + " for complexity");
		return 1;
	}
	
	private final byte getNegationSeqComplexity(ArrayList<String> trailingNegationVerbs)
	{
		return (byte)(trailingNegationVerbs == null ? 1 : -1);
	}
}
