//Kode merupakan contoh yang tersedia pada https://opensource.com/article/20/12/write-your-own-text-editor dan telah diedit seusuai kebutuhan

package com.texteditor;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileSystemView;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.texteditor.cipher.aes.AES;

public final class TextEditor extends JFrame implements ActionListener {
    private static JTextArea area;
    private static JFrame frame;
    private static int returnValue = 0;

    private static JTextField tf;

    public TextEditor() { run(); }

    public void run() {
        frame = new JFrame("Text Edit");

        // Set the look-and-feel (LNF) of the application
        // Try to default to whatever the host system prefers
        try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        Logger.getLogger(TextEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Set attributes of the app window
        area = new JTextArea();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(area);
        frame.setSize(640, 480);
        frame.setVisible(true);

        // Build the menu
        JMenuBar menu_main = new JMenuBar();

        JMenu menu_file = new JMenu("File");

        JMenuItem menuitem_new = new JMenuItem("New");
        JMenuItem menuitem_open = new JMenuItem("Open");
        JMenuItem menuitem_save = new JMenuItem("Save");
        JMenuItem menuitem_quit = new JMenuItem("Quit");

        JPanel panel = new JPanel();
        JLabel l = new JLabel("Key :");
        tf = new JTextField(10);
        tf.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                String value = tf.getText();
                int l = value.length();
                if ((ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9') || ke.getKeyCode() == KeyEvent.VK_BACK_SPACE ) {
                    tf.setEditable(true);
                } 
                else {
                    tf.setEditable(false);
                }
            }
        });
        panel.add(l);
        panel.add(tf);

        JScrollPane scroll = new JScrollPane (area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);


        menuitem_new.addActionListener(this);
        menuitem_open.addActionListener(this);
        menuitem_save.addActionListener(this);
        menuitem_quit.addActionListener(this);

        menu_main.add(menu_file);

        menu_file.add(menuitem_new);
        menu_file.add(menuitem_open);
        menu_file.add(menuitem_save);
        menu_file.add(menuitem_quit);

        frame.setJMenuBar(menu_main);

        frame.getContentPane().add(BorderLayout.SOUTH, panel);

        frame.add(scroll);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String ingest = "";
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setDialogTitle("Choose destination.");
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        String ae = e.getActionCommand();

        //OPEN
        if (ae.equals("Open")) {
            returnValue = jfc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File f = new File(jfc.getSelectedFile().getAbsolutePath());
                try{
                    FileReader read = new FileReader(f);
                    Scanner scan = new Scanner(read);
                    while(scan.hasNextLine()){
                        String line = scan.nextLine();// + "\n";
                        ingest = ingest + line;
                    }
                    if (tf.getText() != "") {
                        AES aes = new AES();
                        String plain = aes.decrypt(ingest, tf.getText());
                        if (plain == null) {
                            plain = "Error decrypting text.";
                        }
                        area.setText(plain);
                    }
                    else {
                        area.setText(ingest);
                    }
                    
                }
                catch ( FileNotFoundException ex) { ex.printStackTrace(); }
            }
        }

        // SAVE
        else if (ae.equals("Save")) {
            returnValue = jfc.showSaveDialog(null);
            try {
                File f = new File(jfc.getSelectedFile().getAbsolutePath());
                FileWriter out = new FileWriter(f);
                AES aes = new AES();
                String cipher = aes.encrypt(area.getText(), tf.getText());
                out.write(cipher);
                out.close();
            } 
            catch (FileNotFoundException ex) {
                Component f = null;
                JOptionPane.showMessageDialog(f,"File not found.");
            } 
            catch (IOException ex) {
                Component f = null;
                JOptionPane.showMessageDialog(f,"Error.");
            }
        } 
        
        //NEW
        else if (ae.equals("New")) {
            area.setText("");
        } 
        
        //QUIT
        else if (ae.equals("Quit")) { 
            System.exit(0); 
        }
    }
}