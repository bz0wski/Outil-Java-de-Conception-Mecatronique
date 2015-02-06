package org.ica.utilityClasses;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ContextInformationValidator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Event;


/**
 * This class is responsible for creating typing assist for completing parameter names
 * when creating a new {@code GlobalEquation} object.
 * @author Salim AHMED
 *
 */

public class ContentAssist {
	private final WordTracker wordTracker = new WordTracker(200);
	private final TextViewer textViewer;

	
/**
 * Default constructor which initialises the completion proposals.
 * @param suggestions : A {@code List} of completion proposals for the given context.
 * @param textViewer : The {@link TextViewer } on which the completion proposal is attached.
 */
	public ContentAssist(List<String> suggestions, TextViewer textViewer) {
		for (int i = 0; i < suggestions.size(); i++) {
			wordTracker.add(suggestions.get(i));
		}
		this.textViewer = textViewer;
		buildControls();
	}
/**
 * Builds the controls for the completion proposal.
 */
	private void buildControls() {
		
		textViewer.setDocument(new Document());
		final ContentAssistant assistant = new ContentAssistant();
		assistant.setContentAssistProcessor(
				new ContentAssistProvider(wordTracker),
				IDocument.DEFAULT_CONTENT_TYPE);

		assistant.install(textViewer);
		assistant.setEmptyMessage("no proposals");

		textViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {

				String input = String.valueOf((char)e.keyCode);
				if(input.matches("\\w")){					
					assistant.showPossibleCompletions();
				}
				
			}
		});
		
	}
}


/**
 * This class contains the functionality that provides the content assist.
 * @author Salim AHMED
 *
 */
class ContentAssistProvider implements IContentAssistProcessor{
	private WordTracker wordTracker;
	private ContextInformationValidator contextInfoValidator;
	private String lastError;
	private final static String OPERATEURS_AVEC_PARENTHESES= "([+|\\-|/|^|%|*|=|<|>|(|)|,])";
	
	public ContentAssistProvider(WordTracker tracker){
		this.wordTracker = tracker;
		contextInfoValidator = new ContextInformationValidator(this);
		
	
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {		
		IDocument document = viewer.getDocument();
		document.addDocumentListener(new IDocumentListener() {
			
			//I install a document changed listener to notify the Modify listeners on the text field to register
			//the recent modification from the suggestion list.
			@Override
			public void documentChanged(DocumentEvent event) {
				if(!event.fText.isEmpty()){
					viewer.getTextWidget().notifyListeners(SWT.Modify, new Event());
				}
			}	
			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
				
			}
		});
		int currOffset = offset - 1;
				
		try {
			String currWord = "";
			char currChar;
			while (currOffset > -1 && !Character.isWhitespace(currChar = document.getChar(currOffset)) && !String.valueOf(document.getChar(currOffset)).matches(OPERATEURS_AVEC_PARENTHESES) ) {
				currWord = currChar + currWord;
				currOffset--;
			}
			List<?> suggestions = wordTracker.suggest(currWord);
			
			
			ICompletionProposal[] proposals = null;
			if (suggestions.size() > 0) {
				proposals = buildProposals(suggestions, currWord,
						offset - currWord.length());
				lastError = null;
			}
			
			return proposals;
		} catch (BadLocationException e) {
			e.printStackTrace();
			lastError = e.getMessage();
			return null;
		}
	}
	/**
	 * This sub routine builds and returns the  {@link ICompletionProposal} set of proposals.
	 * @param suggestions : The {@link List} of suggestions to populate the actual autocomplete suggestions.
	 * @param replacedWord : The word to be replaced.
	 * @param offset : Current caret offset.
	 * @return the proposals for auto completion.
	 */
	private ICompletionProposal[] buildProposals(List<?> suggestions,
			String replacedWord, int offset) {
		ICompletionProposal[] proposals = new ICompletionProposal[suggestions
		                                                          .size()];
		int index = 0;
		for (Iterator<?> i = suggestions.iterator(); i.hasNext();) {
			String currSuggestion = (String) i.next();
			proposals[index] = new CompletionProposal(currSuggestion, offset,
					replacedWord.length(), currSuggestion.length());
			
			index++;
		}
		return proposals;
	}


	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		lastError = "No Context Information available";
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		//we always wait for the user to explicitly trigger completion
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		//we have no context information
		return null;
	}

	@Override
	public String getErrorMessage() {

		return lastError;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		
		return contextInfoValidator;
	}

}
/**
 * This class contains the functionality that tracks the word insertion and provides 
 * autocompletion assist.
 * @author Salim AHMED
 *
 */
class WordTracker {
	private int maxQueueSize;

	private List<String> wordBuffer;

	private Map<String, String> knownWords = new HashMap<String, String>();

	public WordTracker(int queueSize) {
		maxQueueSize = queueSize;
		wordBuffer = new LinkedList<String>();
	}

	public int getWordCount() {
		return wordBuffer.size();
	}

	public void add(String word) {
		if (wordIsNotKnown(word)) {
			flushOldestWord();
			insertNewWord(word);
		}
	}
/**
 * This method adds a new word to the word buffer and also the known words dictionary.
 * @param word : The word to be added
 */
	private void insertNewWord(String word) {
		wordBuffer.add(0, word);
		knownWords.put(word, word);
	}
/**
 * This method removes the oldest word from the word buffer and also the the word dictionary.
 */
	private void flushOldestWord() {
		if (wordBuffer.size() == maxQueueSize) {
			String removedWord = (String) wordBuffer.remove(maxQueueSize - 1);
			knownWords.remove(removedWord);
		}
	}
/**
 * Returns a boolean value to indicate whether the word is in the word dictionary.
 * @param word : The word presence in the dictionary is to be verified.
 * @return True if the word is found, otherwise false.
 */
	private boolean wordIsNotKnown(String word) {
		return knownWords.get(word) == null;
	}
/**
 * This method suggests a list of possible completions for the word received as a parameter, it does this by  verifying from the 
 * word dictionary if the word received as a parameter is a possible prefix to any of the words in the word dictionary.
 * @param word : The word to provide a list of possible completions for.
 * @return A list of possible completions.
 */
	public List<String> suggest(String word) {
		List<String> suggestions = new LinkedList<String>();
		for (Iterator<String> i = wordBuffer.iterator(); i.hasNext();) {
			String currWord = (String) i.next();
			
			if (currWord.regionMatches(true, 0, word, 0, word.length())) {
				suggestions.add(currWord);
				
			}
		}
		return suggestions;
	}

}
