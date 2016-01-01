
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

public class SearchContent {
	boolean found = false; //record the search result success or not
	public SearchContent(JTextComponent textArea,String keyword, JLabel statusBar) {	
		Highlighter h = textArea.getHighlighter();
		//remove previous highlights
		h.removeAllHighlights();
		try{
			Document doc = textArea.getDocument();
			String text = doc.getText(0, doc.getLength());
			
			int pos = 0;
			int occurence = 0;
			while((pos = text.indexOf(keyword,pos)) >= 0) {
				h.addHighlight(pos, pos+keyword.length(), DefaultHighlighter.DefaultPainter);
				found = true;
				pos+=keyword.length();
				occurence++;
			}
			statusBar.setText(keyword+": "+occurence+" occurences.");

			if (found) {
		         // Get the rectangle of the where the text would be visible...
                Rectangle viewRect = textArea.modelToView(text.indexOf(keyword,pos));
                // Scroll to make the rectangle visible
                textArea.scrollRectToVisible(viewRect);			
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}				
	}
	
	public boolean foundresult() {
		if (found){
			return true;
		}		
		return false;		
	}
}
