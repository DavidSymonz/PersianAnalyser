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
import utils.UArray;
import utils.UFile;
import utils.UIndentPrinter;

public final class StringTree
{
	private final StringTreeNode root;
	
	public StringTree()
	{
		root = new StringTreeNode();
	}
	
	public final void addValidWordSequence(String[] validSequence)
	{
		addValidWordSequence(UArray.arrayToArrayList(validSequence));
	}
	
	public final void addValidWordSequence(ArrayList<String> validSequence)
	{
		int sequenceLength = validSequence.size();
		
		if(sequenceLength == 0)
			return;
		
		StringTreeNode currentNode = root;
		StringTreeNode nextNode;
		String currentWord;
		
		for(int i = 0; i < sequenceLength; i++)
		{
			// The next word in the sequence to be added.
			currentWord = validSequence.get(i);
			
			// Get the child node (if it already exists).
			nextNode = currentNode.getChild(currentWord);
			
			// The node does not yet exist.
			if(nextNode == null)
			{
				nextNode = new StringTreeNode();
				currentNode.addChild(currentWord, nextNode);
			}
			// Go to child node.
			currentNode = nextNode;
		}
		// The valid sequence ends here, so this is an accepting state.
		currentNode.isAcceptingState = true;
	}
	
	public final boolean isSingleWordValid(String word)
	{
		if(root.children == null)
			return false;
		
		StringTreeNode childNode = root.children.get(word);
		
		if(childNode == null)
			return false;
		
		return childNode.isAcceptingState;
	}
	
	public final int getLongestAcceptSequenceLength(String[] words, int startIndex)
	{
		StringTreeNode currentNode = root;
		String currentWord;
		int longestSoFar = 0;
		
		for(int i = startIndex; i < words.length; i++)
		{
			// The next word in the sequence to be added.
			currentWord = words[i];
			
			// Get the child node (if it exists).
			currentNode = currentNode.getChild(currentWord);
			
			// The node does not yet exist.
			if(currentNode == null)
				return longestSoFar;
			
			if(currentNode.isAcceptingState)
				longestSoFar = i - startIndex + 1;
		}
		return longestSoFar;
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
	
	private final void toStringRecursive(StringBuilder sb, UIndentPrinter indent, StringTreeNode node)
	{
		for(String word : node.children.keySet())
		{
			// Note that childNode cannot be null because word is an existing key and we never
			// put null values in the "children" HashMap.
			StringTreeNode childNode = node.getChild(word);
			
			sb.append(indent.toString());
			sb.append(childNode.isAcceptingState ? "(" + word + ")" : word);
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
	private final class StringTreeNode
	{
		public HashMap<String, StringTreeNode> children;
		public boolean isAcceptingState;
		
		// Constructor.
		public StringTreeNode()
		{
			isAcceptingState = false;
			children = null;
		}
		
		public final void addChild(String word, StringTreeNode childNode)
		{
			if(children == null)
				children = new HashMap<String, StringTreeNode>();
			
			children.put(word, childNode);
		}
		
		public final StringTreeNode getChild(String word)
		{
			if(children == null)
				return null;
			
			return children.get(word);
		}
	}
}
