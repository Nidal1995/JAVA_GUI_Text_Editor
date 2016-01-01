import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class AboutMe {
	AboutMe() {
    	//import image to about dialog 
		ImageIcon icon1 = new ImageIcon(getClass().getResource("head.jpg"));
    	JOptionPane.showMessageDialog( null, "Text Editor"
    			 +"\nCreated by Jason Yang"
    			 +"\n"
                 + "\nContact Info: "
                 + "\nEmail: mingjiey@andrew.cmu.edu"   
                 + "\n"
                 + "\nVersion 4.0"
                 + "\nReleased on 12/06/15"
                 + "\n"
                 + "\nCarnegie Mellon University"
                 + "\nPittsburgh,PA", 
                 "About Text Editor", JOptionPane.INFORMATION_MESSAGE,icon1 );	// TODO Auto-generated method stub
    }
	
}
