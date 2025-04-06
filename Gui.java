/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.gui;

/**
 *
 * @author 96650
 */
import javax.swing.*;
import java.awt.*;

public class Gui extends JFrame { 

    public Gui() {
        setTitle("Sports club members");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create all components
        JLabel labelLastName = new JLabel("Last Name : ");
        JTextField textLastName = new JTextField(15);

        JLabel labelFirstName = new JLabel("First name : ");
        JTextField textFirstName = new JTextField(15);

        JLabel labelBirthday = new JLabel("Birthday : ");
        JTextField textBirthday = new JTextField(15);

        JLabel labelAddress = new JLabel("Adress : ");
        JTextArea textAddress = new JTextArea(2, 15);
        JScrollPane addressScrollPane = new JScrollPane(textAddress);

        JLabel labelGender = new JLabel("Gender : ");
        JRadioButton radioMale = new JRadioButton("Male");
        JRadioButton radioFemale = new JRadioButton("Female");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(radioMale);
        genderGroup.add(radioFemale);

        JLabel labelLanguages = new JLabel("Languages : ");
        JCheckBox checkArabic = new JCheckBox("Arabe");
        JCheckBox checkFrench = new JCheckBox("French");
        JCheckBox checkEnglish = new JCheckBox("English");
        JCheckBox checkItalian = new JCheckBox("Italian");

        JLabel labelCategory = new JLabel("Category : ");
        JComboBox<String> comboCategory = new JComboBox<>(new String[]{"Young", "Child"});

        JLabel labelSport = new JLabel("Sport : ");
        JList<String> listSport = new JList<>(new String[]{"Tennis", "VolleyBall", "BasketBall", "HandBall", "FootBall"});
        JScrollPane sportScrollPane = new JScrollPane(listSport);

        JButton buttonPrevious = new JButton("<< Precedent");
        JButton buttonNext = new JButton("Next >>");
        JButton buttonConfirm = new JButton("Confirm");
        JButton buttonCancel = new JButton("Cancel");

        // Set layout manager
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Define larger padding and alignment
        gbc.insets = new Insets(15, 10, 15, 10); // Increase padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL; // Make components fill horizontally
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left

        // Add Last Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(labelLastName, gbc);
        gbc.gridx = 1;
        add(textLastName, gbc);

        // Add First Name field
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(labelFirstName, gbc);
        gbc.gridx = 1;
        add(textFirstName, gbc);

        // Add Birthday field
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(labelBirthday, gbc);
        gbc.gridx = 1;
        add(textBirthday, gbc);

        // Add Category on the same row as Birthday
        gbc.gridx = 2;
        add(labelCategory, gbc);
        gbc.gridx = 3;
        add(comboCategory, gbc);

        // Add Address field
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(labelAddress, gbc);
        gbc.gridx = 1;
        add(addressScrollPane, gbc);

        // Add Gender field
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(labelGender, gbc);
        gbc.gridx = 1;
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        genderPanel.add(radioMale);
        genderPanel.add(radioFemale);
        add(genderPanel, gbc);

        // Add Sport next to Gender
        gbc.gridx = 2;
        add(labelSport, gbc);
        gbc.gridx = 3;
        add(sportScrollPane, gbc);

        // Add Languages section
        gbc.insets = new Insets(0, 10, 0, 10); // Maintain larger padding between languages and other sections
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(labelLanguages, gbc);

        JPanel languagesPanel = new JPanel(new GridLayout(4, 1, 0, 0)); // Grid layout for checkboxes
        languagesPanel.add(checkArabic);
        languagesPanel.add(checkFrench);
        languagesPanel.add(checkEnglish);
        languagesPanel.add(checkItalian);
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridheight = 4; // Span 4 rows for checkboxes
        add(languagesPanel, gbc);

        // Add buttons at the bottom
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(buttonPrevious);
        buttonPanel.add(buttonNext);
        buttonPanel.add(buttonConfirm);
        buttonPanel.add(buttonCancel);
        add(buttonPanel, gbc);

        // Display the window
        this.pack();
        setVisible(true);
    }
    public static void main(String[] args) {
        new Gui();
    }
}
