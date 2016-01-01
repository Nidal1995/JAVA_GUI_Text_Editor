import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

public class AddBookmark {
	
	public AddBookmark(JTextComponent textArea, int pos) {		
		try{
			textArea.moveCaretPosition(pos);
  			Highlighter h = textArea.getHighlighter();
  			h.removeAllHighlights();	
  			//highlight cursor position
  			h.addHighlight(pos, pos+1, DefaultHighlighter.DefaultPainter);
		}catch(BadLocationException e){
			e.printStackTrace();
		}				
	}
	

}

