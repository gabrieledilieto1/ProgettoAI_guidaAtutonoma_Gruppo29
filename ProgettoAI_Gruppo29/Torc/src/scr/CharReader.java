package scr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CharReader extends JFrame {

    private JTextField inputField;
    private final SimpleDriver sd;

    public CharReader(SimpleDriver sd) {
        this.sd = sd;

        setTitle("Comandi tastiera - CharReader");
        setSize(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        inputField = new JTextField(20);
        inputField.setEditable(false); 
        add(inputField);

        
        inputField.addKeyListener(new KeyAdapter() { //per la lettura da tastiera
            @Override
            public void keyPressed(KeyEvent e) {
                char ch = e.getKeyChar();
                if (ch == 'l') {
                    sd.setLettura(!sd.isLettura());
                    System.out.println("Lettura: " + sd.isLettura());
                } else {
                    sd.setCh(ch);
                    System.out.println("Tasto premuto: " + ch);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                sd.setCh(' ');
                System.out.println("Tasto rilasciato - reset");
            }
        });

        setVisible(true);

        
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }
}
