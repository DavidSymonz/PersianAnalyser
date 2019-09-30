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
import java.util.HashMap;
import utils.UFile;
import utils.UIndentPrinter;

public final class CharTree
{
	private final CharTreeNode root;
	
	public CharTree()
	{
		root = new CharTreeNode();
	}
	
	public final void addValidCharacterSequence_leftToRight(String charSequence)
	{
		int sequenceLength = charSequence.length();
		
		if(sequenceLength == 0)
			return;
		
		CharTreeNode currentNode = root;
		CharTreeNode nextNode;
		char currentChar;
		
		for(int i = 0; i < sequenceLength; i++)
		{
			// The next character in the sequence to be added.
			currentChar = charSequence.charAt(i);
			
			// Get the child node (if it already exists).
			nextNode = currentNode.getChild(currentChar);
			
			// The node does not yet exist.
			if(nextNode == null)
			{
				nextNode = new CharTreeNode();
				currentNode.addChild(currentChar, nextNode);
			}
			// Go to child node.
			currentNode = nextNode;
		}
		// The valid sequence ends here, so this is an accepting state.
		currentNode.isAcceptingState = true;
	}
	
	public final void addValidCharacterSequence_rightToLeft(String charSequence)
	{
		int sequenceLength = charSequence.length();
		
		if(sequenceLength == 0)
			return;
		
		CharTreeNode currentNode = root;
		CharTreeNode nextNode;
		char currentChar;
		
		for(int i = sequenceLength - 1; i >= 0; i--)
		{
			// The next character in the sequence to be added.
			currentChar = charSequence.charAt(i);
			
			// Get the child node (if it already exists).
			nextNode = currentNode.getChild(currentChar);
			
			// The node does not yet exist.
			if(nextNode == null)
			{
				nextNode = new CharTreeNode();
				currentNode.addChild(currentChar, nextNode);
			}
			// Go to child node.
			currentNode = nextNode;
		}
		// The valid sequence ends here, so this is an accepting state.
		currentNode.isAcceptingState = true;
	}
	
	public final boolean isSingleCharValid(char c)
	{
		if(root.children == null)
			return false;
		
		CharTreeNode childNode = root.children.get(c);
		
		if(childNode == null)
			return false;
		
		return childNode.isAcceptingState;
	}
	
	public final int getLongestAcceptSequenceLength_leftToRight(String charSequence, int startIndex)
	{
		CharTreeNode currentNode = root;
		char currentChar;
		int longestSoFar = 0;
		
		for(int i = startIndex; i < charSequence.length(); i++)
		{
			// The next word in the sequence to be added.
			currentChar = charSequence.charAt(i);
			
			// Get the child node (if it exists).
			currentNode = currentNode.getChild(currentChar);
			
			// The node does not yet exist.
			if(currentNode == null)
				return longestSoFar;
			
			if(currentNode.isAcceptingState)
				longestSoFar = i - startIndex + 1;
		}
		return longestSoFar;
	}
	
	public final int getLongestAcceptSequenceLength_rightToLeft(String charSequence, int startIndex)
	{
		CharTreeNode currentNode = root;
		char currentChar;
		int longestSoFar = 0;
		
		for(int i = startIndex; i >= 0; i--)
		{
			// The next word in the sequence to be added.
			currentChar = charSequence.charAt(i);
			
			// Get the child node (if it exists).
			currentNode = currentNode.getChild(currentChar);
			
			// The node does not yet exist.
			if(currentNode == null)
				return longestSoFar;
			
			if(currentNode.isAcceptingState)
				longestSoFar = startIndex - i + 1;
		}
		return longestSoFar;
	}
	
	public final ArrayList<Integer> getAllAcceptSequenceLengths_leftToRight(String charSequence, int startIndex)
	{
		ArrayList<Integer> acceptSequenceLengths = new ArrayList<Integer>();
		CharTreeNode currentNode = root;
		char currentChar;
		
		for(int i = startIndex; i < charSequence.length(); i++)
		{
			// The next word in the sequence to be added.
			currentChar = charSequence.charAt(i);
			
			// Get the child node (if it exists).
			currentNode = currentNode.getChild(currentChar);
			
			// The node does not yet exist.
			if(currentNode == null)
				return acceptSequenceLengths;
			
			if(currentNode.isAcceptingState)
				acceptSequenceLengths.add(i - startIndex + 1);
		}
		return acceptSequenceLengths;
	}
	
	public final ArrayList<Integer> getAllAcceptSequenceLengths_rightToLeft(String charSequence, int startIndex)
	{
		ArrayList<Integer> acceptSequenceLengths = new ArrayList<Integer>();
		CharTreeNode currentNode = root;
		char currentChar;
		
		for(int i = startIndex; i >= 0; i--)
		{
			// The next word in the sequence to be added.
			currentChar = charSequence.charAt(i);
			
			// Get the child node (if it exists).
			currentNode = currentNode.getChild(currentChar);
			
			// The node does not yet exist.
			if(currentNode == null)
				return acceptSequenceLengths;
			
			if(currentNode.isAcceptingState)
				acceptSequenceLengths.add(startIndex - i + 1);
		}
		return acceptSequenceLengths;
	}
	
	public final String toString()
	{
		if(root.children == null)
			return "";
		
		// Initialise for recursive call.
		StringBuilder sb = new StringBuilder();
		UIndentPrinter indent = new UIndentPrinter("  ");
		
		// Recursively build the string.
		toStringRecursive(sb, indent, root);
		
		// The recursive method adds a trailing NEWLINE which we remove.
		sb.setLength(sb.length() - UFile.NEWLINE.length());
		return sb.toString();
	}
	
	private final void toStringRecursive(StringBuilder sb, UIndentPrinter indent, CharTreeNode node)
	{
		for(Character c : node.children.keySet())
		{
			// Note that childNode cannot be null because word is an existing key and we never
			// put null values in the "children" HashMap.
			CharTreeNode childNode = node.getChild(c);
			
			sb.append(indent.toString());
			sb.append(childNode.isAcceptingState ? "(" + c + ")" : c);
			sb.append(UFile.NEWLINE);
			
			if(childNode != null && childNode.children != null)
			{
				indent.incIndent();
				toStringRecursive(sb, indent, childNode);
				indent.decIndent();
			}
		}
	}
	
	// ---- Private inner class ----
	private final class CharTreeNode
	{
		public HashMap<Character, CharTreeNode> children;
		public boolean isAcceptingState;
		
		// Constructor.
		public CharTreeNode()
		{
			isAcceptingState = false;
			children = null;
		}
		
		public final void addChild(char c, CharTreeNode childNode)
		{
			if(children == null)
				children = new HashMap<Character, CharTreeNode>();
			
			children.put(c, childNode);
		}
		
		public final CharTreeNode getChild(char c)
		{
			if(children == null)
				return null;
			
			return children.get(c);
		}
	}
}
