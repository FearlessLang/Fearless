package rt.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import base.Bool_0;
import base.False_0;
import base.MF_1;
import base.MF_2;
import base.True_0;
import base.Void_0;
import base.gui.TextEvents_0;
import base.gui.TextField_0;

public class TextField implements TextField_0 {
  
  private final JTextField textField;
  
  public TextField(String text) { this(text, 15); }
           
  public TextField(String text, int col) { this.textField = new JTextField(text, col); }

  @Override
  public Void_0 addActionListener$mut(MF_1 listener_m$) {
    textField.addActionListener(listener -> listener_m$.$hash$mut());
    return Void_0.$self;
  }

  @Override
  public Void_0 enable$mut(Bool_0 b_m$) {
    textField.setEnabled(b_m$ ==True_0.$self);
    return Void_0.$self;
  }

  @Override
  public Bool_0 enable$read() {
    return textField.isEnabled()? True_0.$self: False_0.$self;
  }
  
  public JComponent getImpl() {
    return textField;
  }

  @Override
  public TextField_0 addEvents$mut(MF_2 events_m$) {
    TextEvents textEvents = new TextEvents();
    events_m$.$hash$mut(textEvents);
    if (textEvents.textChangeHandler != null) {
      textField.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
          textEvents.textChangeHandler.$hash$mut(textField.getText());
        }
                 
        @Override
        public void removeUpdate(DocumentEvent e) {
          textEvents.textChangeHandler.$hash$mut(textField.getText());
        }
      
        @Override
        public void changedUpdate(DocumentEvent e) {
          textEvents.textChangeHandler.$hash$mut(textField.getText());
        }
      });
      }
    if (textEvents.keyPressHandler != null) {
      textField.addKeyListener(new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {
        }
                 
        @Override
        public void keyPressed(KeyEvent e) {textEvents.keyPressHandler.$hash$mut(e.getKeyChar());}
                 
        @Override
        public void keyReleased(KeyEvent e) {}
      });
    }
    if (textEvents.focusGainedHandler != null || textEvents.focusLostHandler != null) {
      textField.addFocusListener(new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
          if (textEvents.focusGainedHandler != null) {
            textEvents.focusGainedHandler.$hash$mut();}
          }
        @Override
        public void focusLost(FocusEvent e) {
          if (textEvents.focusLostHandler != null) {
            textEvents.focusLostHandler.$hash$mut();
          }
        }});
    }
    return this;
  }

}
