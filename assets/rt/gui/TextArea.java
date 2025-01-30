package rt.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import base.Bool_0;
import base.False_0;
import base.MF_1;
import base.MF_2;
import base.True_0;
import base.Void_0;
import base.gui.TextArea_0;

public class TextArea implements TextArea_0 {

  private final JTextArea textArea;

  public TextArea(String text) {
    this(text, 10, 10);
  }

  public TextArea(String text, int rows, int columns) {
    this.textArea = new JTextArea(text, rows, columns);
  }
  @Override
  public Void_0 addActionListener$mut(MF_1 listener_m$) {
    textArea.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
          listener_m$.$hash$mut();
        }
      }
    });
    return Void_0.$self;
  }

  @Override
  public Void_0 enable$mut(Bool_0 b_m$) {
    textArea.setEnabled(b_m$ ==True_0.$self);
    return Void_0.$self;
  }

  @Override
  public Bool_0 enable$read() {
    return textArea.isEnabled()? True_0.$self: False_0.$self;
  }

  @Override
  public TextArea_0 addEvents$mut(MF_2 events_m$) {
    TextEvents textEvents = new TextEvents();
    events_m$.$hash$mut(textEvents);
    if (textEvents.textChangeHandler != null) {
      textArea.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
          textEvents.textChangeHandler.$hash$mut(textArea.getText());
        }
                 
        @Override
        public void removeUpdate(DocumentEvent e) {
          textEvents.textChangeHandler.$hash$mut(textArea.getText());
        }
      
        @Override
        public void changedUpdate(DocumentEvent e) {
          textEvents.textChangeHandler.$hash$mut(textArea.getText());
        }
      });
      }
    if (textEvents.keyPressHandler != null) {
      textArea.addKeyListener(new KeyListener() {
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
      textArea.addFocusListener(new FocusListener() {
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
  
  public JComponent getImpl() {
    return textArea;
  }

}
