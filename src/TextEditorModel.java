import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Menu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultEditorKit.CutAction;

public class TextEditorModel extends JFrame {
	boolean opened; //check whether file is opened 
	static boolean changed; //check whether need autosave changes
	String currentFile;
	JTextArea textArea;
	JScrollPane scrollPane;
	JMenuBar menuBar;
	JToolBar toolBar;
	JPanel toolPane;
	JLabel statusBar; //status bar to show open, save details
	JFileChooser fileExplorer;//set default location
	FileNameExtensionFilter filter;
	JMenu search;
	int searchMenuCount;
	JMenu old;	
	Map<String, Integer> mapofBookmark; //sort bookmark tag and position by treemap
	final JCheckBox autoSaveMenuItem;
	
	TextEditorModel () {
    	//set up jframe
    	setPreferredSize (new Dimension (500,500));
    	setTitle("Text Editor");
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    	
    	// set up menubar with 3 columns
    	menuBar = new JMenuBar ();
    	setJMenuBar (menuBar);
    	JMenu file = new JMenu ("File");
    	JMenu tools = new JMenu ("Tools");
    	JMenu help = new JMenu ("Help");
    	menuBar.add(file);
    	menuBar.add(tools);
    	menuBar.add(help);
    	//set up each drop down menu with separator
    	file.add(openMenuItem);
    	JSeparator separator1 = new JSeparator();
    	file.add(separator1);
    	file.add(saveMenuItem);
    	file.add(saveasMenuItem);
    	JSeparator separator2 = new JSeparator();
    	file.add(separator2);
    	file.add(closeMenuItem);
    	JSeparator separator3 = new JSeparator();
    	file.add(separator3);
    	file.add(exitMenuItem);
    	
    	JMenu words = new JMenu ("Words");
    	JMenu bookmark = new JMenu ("Bookmark");
    	old = new JMenu ("Old");
    	search = new JMenu ("Search");
    	tools.add(words);
    	tools.add(bookmark);
    	words.add(countMenuItem);
    	words.add(uniqueMenuItem);    	
    	words.add(search);
    	search.add(searchMenuItem);
    	JSeparator separator4 = new JSeparator();
    	search.add(separator4);
    	bookmark.add(newBookmarkMenuItem);
    	bookmark.add(old);
    	bookmark.add(deleteBookmarkMenuItem);  
    	
    	help.add(aboutMenuItem);
    	help.add(newFeatureMenuItem);
    	autoSaveMenuItem= new JCheckBox("Auto Save");
    	autoSaveMenuItem.setSelected(false);
    	help.add(autoSaveMenuItem);
    	//autosave if the checkbox was clicked
    	autoSaveMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {				
				final AutoSave as = new AutoSave();
				if(autoSaveMenuItem.isSelected()){
					changed = true;
					as.start();				
		    	} else {
		    		changed = false;		    	
		    	}	
			}    		
    	});
    	//set up toolbar with icon button
    	toolBar = new JToolBar(); 
    	JButton cutButton = new JButton(new CutAction());
    	ImageIcon cutIcon = new ImageIcon(getClass().getResource("cut.png"));
    	cutIcon = new ImageIcon(cutIcon.getImage().getScaledInstance(20, 20, BufferedImage.SCALE_SMOOTH));
    	cutButton.setText(null);
    	cutButton.setIcon(cutIcon);
    	cutButton.setToolTipText("Cut");
    	toolBar.add(cutButton);	
    	JButton copyButton = new JButton(new DefaultEditorKit.CopyAction());
    	ImageIcon copyIcon = new ImageIcon(getClass().getResource("copy.png"));
    	copyIcon = new ImageIcon(copyIcon.getImage().getScaledInstance(20, 20, BufferedImage.SCALE_SMOOTH));
    	copyButton.setText(null);
    	copyButton.setIcon(copyIcon);
    	copyButton.setToolTipText("Copy");
    	toolBar.add(copyButton);
    	JButton pasteButton = new JButton(new DefaultEditorKit.PasteAction());
    	ImageIcon pasteIcon = new ImageIcon(getClass().getResource("paste.png"));
    	pasteIcon = new ImageIcon(pasteIcon.getImage().getScaledInstance(20, 20, BufferedImage.SCALE_SMOOTH));
    	pasteButton.setText(null);
    	pasteButton.setIcon(pasteIcon);
    	pasteButton.setToolTipText("Paste");
    	toolBar.add(pasteButton);
    	getContentPane().add(toolBar, BorderLayout.NORTH);
    	
    	textArea = new JTextArea (50,50);
    	textArea.setLineWrap(true);   
    	scrollPane = new JScrollPane (textArea);
    	add(scrollPane, BorderLayout.CENTER);
    	statusBar = new JLabel();
    	statusBar.setText(" ");
    	add(statusBar, BorderLayout.SOUTH);
    	pack();
    	setVisible (true);
    	
    	opened = false;
    	currentFile = "Untitled.txt";
    	fileExplorer = new JFileChooser(System.getProperty("user.dir"));
    	filter = new FileNameExtensionFilter ("Text files", "txt");
    	searchMenuCount=0;
    	mapofBookmark = new TreeMap<String, Integer>(); 
    	
    	
    }
	


	//autosave thread with a boolean "changed" to start or close
	private class AutoSave extends Thread {	
		public void run() {	        	                        
				 while (true) {
					synchronized (new AutoSave()){
						if(changed){
							try{
								 saveFile(currentFile);
								 System.out.println("save");
								 TimeUnit.SECONDS.sleep(15);//save every 15 seconds							 
							}catch(InterruptedException e) {
								 e.printStackTrace();
							}
						}
					}
				 }
		}
	  }	
	   //set up each menu item actionlistener
    JMenuItem openMenuItem = new JMenuItem(new AbstractAction("Open") {
		public void actionPerformed(ActionEvent ae) {
        	//set up text filter for filechooser
        	fileExplorer.setFileFilter(filter);
        	if (fileExplorer.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        		readFile (fileExplorer.getSelectedFile().getAbsolutePath());        	
        		statusBar.setText(fileExplorer.getSelectedFile().getName());
        		opened = true;
        		currentFile = fileExplorer.getSelectedFile().getName();
        	}
        }
    });

    JMenuItem saveMenuItem = new JMenuItem(new AbstractAction("Save") {
		public void actionPerformed(ActionEvent ae) {
        	//open filechooser to choose save location for new files
        	if(!opened){
        		savaAsFile();     		
        		opened = true;
        	}else{
        		// simply save opened file under its same name
        		saveFile(currentFile);
        		statusBar.setText(currentFile +" successfully saved" );
        	}
        }
    });
   
    JMenuItem saveasMenuItem = new JMenuItem(new AbstractAction("Save as ...") {
		public void actionPerformed(ActionEvent ae) {
        	savaAsFile();
        }
    });
    
    JMenuItem closeMenuItem = new JMenuItem(new AbstractAction("Close") {
		public void actionPerformed(ActionEvent ae) {
        	textArea.setText(null);
        	statusBar.setText(" ");
        	currentFile = "Untitled.txt";
        	opened =false;
        	//clear search history, old bookmark list
        	search.removeAll();
        	search.add(searchMenuItem);
        	JSeparator separator4 = new JSeparator();
        	search.add(separator4);
        	old.removeAll();
        	mapofBookmark.clear();
        	autoSaveMenuItem.setSelected(false);
        	changed =false;       			
        }
    });
    
    JMenuItem exitMenuItem = new JMenuItem(new AbstractAction("Exit") {
		public void actionPerformed(ActionEvent ae) {
        System.exit(0);
        }
    });
    
    JMenuItem aboutMenuItem = new JMenuItem(new AbstractAction("About") {
		public void actionPerformed(ActionEvent ae) {
			new AboutMe();
		}
    });
    
    JMenuItem countMenuItem = new JMenuItem(new AbstractAction("Count") {
  		public void actionPerformed(ActionEvent ae) {
          	wordCount();
          }
      });
    
    JMenuItem uniqueMenuItem = new JMenuItem(new AbstractAction("Unique") {
  		public void actionPerformed(ActionEvent ae) {
          	uniqueCount();
          }
      });
    
    JMenuItem searchMenuItem = new JMenuItem(new AbstractAction("Search new ...") {
  		public void actionPerformed(ActionEvent ae) {
          	searchWord();
          }
      });
    
    JMenuItem newBookmarkMenuItem = new JMenuItem(new AbstractAction("New...") {
  		public void actionPerformed(ActionEvent ae) {
			createBookmark();
          }
      });
    
    JMenuItem deleteBookmarkMenuItem = new JMenuItem(new AbstractAction("Delete...") {
  		public void actionPerformed(ActionEvent ae) {
          	delteBookmark();
          }
      });
    
    JMenuItem newFeatureMenuItem = new JMenuItem(new AbstractAction("New Feature...") {
  		public void actionPerformed(ActionEvent ae) {
  			readFile("Readme.txt");
  			statusBar.setText("Readme.txt");
  		}
      });
    
    
    public void readFile (String fileName) {
    	try {
    		FileReader fr = new FileReader (fileName);
    		textArea.read(fr, null);
    		fr.close();
    	} catch (IOException e) {
    		System.out.println(e);
    		System.exit(1);
    	}
    }


	protected void delteBookmark() {	
		while(mapofBookmark.isEmpty()){
			JOptionPane.showMessageDialog(null, "Please add bookmark first");
			break;
		}
		while (!mapofBookmark.isEmpty()) {
			Set<String> tag = mapofBookmark.keySet();
			Object [] tagList = tag.toArray();
			String selectedValue = (String) JOptionPane.showInputDialog(null,    
		            "Select bookmark to delete", "Delete Bookmarks",   
		            JOptionPane.INFORMATION_MESSAGE, null,
		            tagList, tagList[0]);
			if(selectedValue == null || selectedValue.length() == 0 || tag == null || tagList == null){
				break;
			}else{
				mapofBookmark.remove(selectedValue);
				refreshOldBookmark();
			}
			break;
		}
	}

	public void saveFile (String fileName) {
    	try {
    		FileWriter fw = new FileWriter (fileName);
    		textArea.write(fw);
    		fw.close();
    		//statusBar.setText(fileName +" successfully saved" );
    	} catch (IOException e) {
    		JOptionPane.showMessageDialog(TextEditorModel.this,e);
    		System.exit(1);
    	}
    }
 
    public void savaAsFile () {
    	fileExplorer.setFileFilter(filter);
    	if (fileExplorer.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
    		saveFile (fileExplorer.getSelectedFile().getAbsolutePath());
    		statusBar.setText(fileExplorer.getSelectedFile().getName()+" successfully saved" );
    		currentFile = fileExplorer.getSelectedFile().getName();
    	}
    }
    
	protected void wordCount() {
		String wordNum = null;
		if (textArea.getText().trim().equals("")){
			wordNum = "0";
		}else {
			wordNum = String.valueOf(textArea.getText().split("\\s").length);
		}
		statusBar.setText("Word Count: "+wordNum);
	}
       
	protected void uniqueCount() {
		if (textArea.getText().trim().equals("")){
			statusBar.setText("Unique Count: 0");
		}else {		
			String[] words;
			//use a hastset to record unique words
			Set<String> uniqueWords = new HashSet<String>();
			words = textArea.getText().split("[\\W]+");
			for (int i = 0; i < words.length; i++) {
				uniqueWords.add(words[i]);
			}
			statusBar.setText("Unique Words: "+uniqueWords.size());	
		}
	}
	
	public void searchWord() {
		while (true) {
			final String keyword = JOptionPane.showInputDialog(null,"Enter string to search");
			//if no input break infinity loop
			if(keyword == null || keyword.length() == 0){
				break;					
			}else {
				SearchContent searchresult=new SearchContent(textArea, keyword, statusBar);
				if(searchresult.found){
					search.add(new JMenuItem(new AbstractAction(keyword) {
				  		public void actionPerformed(ActionEvent ae) {
				  			new SearchContent(textArea, keyword, statusBar);
				          }
				      }));
					searchMenuCount++;
					//remove the first search history if total search is over 5
					if (searchMenuCount > 5) {
			    		search.remove(2);
			    		searchMenuCount--;
			    	}
				}
				break;
			}
		}
	}
	
	protected void createBookmark()  {
		while (true) {
			final String tag = JOptionPane.showInputDialog(null,"Enter tag Name");
			final int pos = textArea.getCaretPosition();
			//if no input break infinity loop
			if(tag == null || tag.length() == 0){
				break;
			}else {		
				//save tag name and pos in a treemap
				mapofBookmark.put(tag, pos); 		
				refreshOldBookmark();
			}
			break;			
		}
	}
	
	protected void refreshOldBookmark(){
		old.removeAll();
		//readd all old bookmark by alphabetical order
		for(Map.Entry<String,Integer> entry : mapofBookmark.entrySet()) {
			  final String key = entry.getKey();
			  final int value =  entry.getValue();
			  old.add(new JMenuItem(new AbstractAction(key) {
			  		public void actionPerformed(ActionEvent ae) {				  			
			  			new AddBookmark(textArea, value);
			        }
			   }));	
		}
	}
	

}
	
	

