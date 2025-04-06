/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.examsample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Calculator extends JFrame {
    private JLabel l1, l2, l3, l4;
    private JTextField t1, t2, t3;
    private JComboBox c1;
    private JButton b1, b2;
    String[] arr = {"+", "-", "*", "/"};

    public Calculator(String title) {
        super(title);
        this.setLocation(250, 200);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel
        JPanel p = (JPanel) this.getContentPane();
        p.setLayout(new GridLayout(2,1));

        // Panel p1 for input fields and labels
        JPanel p1 = new JPanel(new FlowLayout());

        l1 = new JLabel("Number 1");
        t1 = new JTextField(10);
        l2 = new JLabel("Operation");
        c1 = new JComboBox(arr);
        l3 = new JLabel("Number 2");
        t2 = new JTextField(10);
        l4 = new JLabel("=");
        t3 = new JTextField(10);
        t3.setEditable(false);  // As per the instructions, result field is non-editable

        // Add components to panel p1
        p1.add(l1);
        p1.add(t1);
        p1.add(l2);
        p1.add(c1);
        p1.add(l3);
        p1.add(t2);
        p1.add(l4);
        p1.add(t3);

        // Panel p2 for buttons
        JPanel p2 = new JPanel(new FlowLayout());
        b1 = new JButton("Calculate");
        b2 = new JButton("Cancel");

        // Add buttons to panel p2
        p2.add(b1);
        p2.add(b2);

        // Add p1 and p2 to the main panel p
        p.add(p1);
        p.add(p2);

        this.pack();
        this.setVisible(true);

        // Add ActionListeners
        b1.addActionListener(new ButtonListener());
        b2.addActionListener(new CancelButtonListener());
    }

    private class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (t1.getText().equals("") || t2.getText().equals("")) {
                    throw new EmptyFieldException("Empty Fields!!!");
                }

                double num1 = Double.parseDouble(t1.getText());
                double num2 = Double.parseDouble(t2.getText());
                String operation = (String) c1.getSelectedItem();
                double result = 0;

                // Use if-else statements to handle operations
                if (operation.equals("+")) {
                    result = num1 + num2;
                } else if (operation.equals("-")) {
                    result = num1 - num2;
                } else if (operation.equals("*")) {
                    result = num1 * num2;
                } else if (operation.equals("/")) {
                    if (num2 == 0) {
                        throw new ArithmeticException("/ by zero");
                    }
                    result = num1 / num2;
                }

                t3.setText(Double.toString(result));  // Using Double.toString(result)
            } catch (EmptyFieldException e2) {
                JOptionPane.showMessageDialog(null, e2.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
            } catch (NumberFormatException e3) {
                JOptionPane.showMessageDialog(null, "Input Error!!\nEnter a numerical value.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (ArithmeticException e4) {
                JOptionPane.showMessageDialog(null, "/ by zero", "Input Error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception e5) {
                JOptionPane.showMessageDialog(null, "Input Error!!\nEnter a valid value.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Class for the Cancel button with the clear function directly inside it
    private class CancelButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Clear the fields directly here
            t1.setText("");
            t2.setText("");
            t3.setText("");
        }
    }

    public static void main(String[] args) {
      new Calculator("Calculator");
    }
}

// Custom Exception Class



