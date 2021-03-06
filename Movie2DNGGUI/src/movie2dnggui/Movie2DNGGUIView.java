/*! Copyright (C) 2010 Apertus, All Rights Reserved
 *! Author : Apertus Team
 *! Description: GUI for the JP4(6) to DNG converter
-----------------------------------------------------------------------------**
 *!
 *!  This program is free software: you can redistribute it and/or modify
 *!  it under the terms of the GNU General Public License as published by
 *!  the Free Software Foundation, either version 3 of the License, or
 *!  (at your option) any later version.
 *!
 *!  This program is distributed in the hope that it will be useful,
 *!  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *!  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *!  GNU General Public License for more details.
 *!
 *!  You should have received a copy of the GNU General Public License
 *!  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *!
-----------------------------------------------------------------------------**/
package movie2dnggui;

import java.awt.Dimension;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The application's main frame.
 */
public class Movie2DNGGUIView extends FrameView implements Runnable {

    boolean Converting = false;

    public Movie2DNGGUIView(SingleFrameApplication app) {
        super(app);

        initComponents();

        String osname = System.getProperty("os.name");
        if (osname.startsWith("Linux")) {
            //everything alright
        } else {
            //uh oh!
            JOptionPane.showMessageDialog(Movie2DNGGUIView.this.getFrame(), "This Program currently works ONLY under Linux, I am afraid you are using an unsupported OS!");
        }

        fc = new JFileChooser();

        SourcePath = "";
        SourceFilename = "";
        DestinationPath = "";
        movie2dngVersion = "";

        ClipFrameCountTotal = 0;
        ClipFrameCountConverted = 0;
        ClipFrameCountLimit = 0;
        converter_firstline = true;
        conversion_complete = false;

        // check if "movie2dng" is installed
        // check Version of movie2dng
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("movie2dng --version");
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line[] = new String[64];
            int j = 0;
            try {
                while ((line[j] = in.readLine()) != null) {
                    j++;
                }
                movie2dngVersion = line[0];
            } catch (IOException ex) {
                Logger.getLogger(Movie2DNGGUIView.class.getName()).log(Level.SEVERE, null, ex);
            }
            movie2dngVersionLabel.setText("Version: " + movie2dngVersion);

            // compare current version to latest known version
            float LatestKnownVersion = 0.1f;
            float CurrentVersion = Float.parseFloat(movie2dngVersion.substring(9));
            if (LatestKnownVersion > CurrentVersion) {
                JOptionPane.showMessageDialog(Movie2DNGGUIView.this.getFrame(), "movie2dng Version is: " + movie2dngVersion + " - outdated! Please update to the most recent version!");
            }

        } catch (IOException ex) {
            Logger.getLogger(Movie2DNGGUIView.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(Movie2DNGGUIView.this.getFrame(), "movie2dng not found! This is absolutely required. This program will not work without it.");

        }

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");

        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");

        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        });

        // set the default window size
        this.getFrame().setMinimumSize(new Dimension(550, 550));
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = Movie2DNGGUIApp.getApplication().getMainFrame();
            aboutBox = new Movie2DNGGUIAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        Movie2DNGGUIApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        JBUtton_SelectSource = new javax.swing.JButton();
        CommandLabel = new javax.swing.JLabel();
        command = new javax.swing.JTextField();
        StopButton = new javax.swing.JButton();
        movie2dngVersionLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        LogArea = new javax.swing.JTextArea();
        TabbedSettingsPanel = new javax.swing.JTabbedPane();
        SettingsPanel = new javax.swing.JPanel();
        DestinationLabel = new javax.swing.JLabel();
        SameAsSource = new javax.swing.JRadioButton();
        subfolders = new javax.swing.JRadioButton();
        DifferentFolder = new javax.swing.JRadioButton();
        SelectDestination = new javax.swing.JButton();
        CreateDNGs = new javax.swing.JCheckBox();
        CreatePGMs = new javax.swing.JCheckBox();
        CreateJPEGs = new javax.swing.JCheckBox();
        keepJP4s = new javax.swing.JCheckBox();
        AdvancedSettingsPanel = new javax.swing.JPanel();
        BayerShiftLabel = new javax.swing.JLabel();
        BayerShift = new javax.swing.JComboBox();
        FrameLimiterLabel = new javax.swing.JLabel();
        FrameLimiterAll = new javax.swing.JRadioButton();
        FrameLimiterNumber = new javax.swing.JRadioButton();
        FrameLimiterNumberField = new javax.swing.JTextField();
        ConverterProgressBar = new javax.swing.JProgressBar();
        ConvertButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        destinationbuttongroup = new javax.swing.ButtonGroup();
        FrameLimiterGroup = new javax.swing.ButtonGroup();

        mainPanel.setName("mainPanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(movie2dnggui.Movie2DNGGUIApp.class).getContext().getResourceMap(Movie2DNGGUIView.class);
        JBUtton_SelectSource.setText(resourceMap.getString("JBUtton_SelectSource.text")); // NOI18N
        JBUtton_SelectSource.setName("JBUtton_SelectSource"); // NOI18N
        JBUtton_SelectSource.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JBUtton_SelectSourceMouseClicked(evt);
            }
        });

        CommandLabel.setText(resourceMap.getString("CommandLabel.text")); // NOI18N
        CommandLabel.setEnabled(false);
        CommandLabel.setName("CommandLabel"); // NOI18N

        command.setFont(resourceMap.getFont("command.font")); // NOI18N
        command.setText(resourceMap.getString("command.text")); // NOI18N
        command.setEnabled(false);
        command.setName("command"); // NOI18N

        StopButton.setText(resourceMap.getString("StopButton.text")); // NOI18N
        StopButton.setEnabled(false);
        StopButton.setName("StopButton"); // NOI18N
        StopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StopButtonActionPerformed(evt);
            }
        });

        movie2dngVersionLabel.setText(resourceMap.getString("movie2dngVersionLabel.text")); // NOI18N
        movie2dngVersionLabel.setName("movie2dngVersionLabel"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        LogArea.setColumns(20);
        LogArea.setEditable(false);
        LogArea.setRows(5);
        LogArea.setName("LogArea"); // NOI18N
        jScrollPane1.setViewportView(LogArea);

        TabbedSettingsPanel.setName("TabbedSettingsPanel"); // NOI18N

        SettingsPanel.setName("SettingsPanel"); // NOI18N

        DestinationLabel.setText(resourceMap.getString("DestinationLabel.text")); // NOI18N
        DestinationLabel.setEnabled(false);
        DestinationLabel.setName("DestinationLabel"); // NOI18N

        destinationbuttongroup.add(SameAsSource);
        SameAsSource.setSelected(true);
        SameAsSource.setText(resourceMap.getString("SameAsSource.text")); // NOI18N
        SameAsSource.setEnabled(false);
        SameAsSource.setName("SameAsSource"); // NOI18N
        SameAsSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SameAsSourceActionPerformed(evt);
            }
        });

        destinationbuttongroup.add(subfolders);
        subfolders.setText(resourceMap.getString("subfolders.text")); // NOI18N
        subfolders.setEnabled(false);
        subfolders.setName("subfolders"); // NOI18N
        subfolders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subfoldersActionPerformed(evt);
            }
        });

        destinationbuttongroup.add(DifferentFolder);
        DifferentFolder.setText(resourceMap.getString("DifferentFolder.text")); // NOI18N
        DifferentFolder.setEnabled(false);
        DifferentFolder.setName("DifferentFolder"); // NOI18N
        DifferentFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DifferentFolderActionPerformed(evt);
            }
        });

        SelectDestination.setText(resourceMap.getString("SelectDestination.text")); // NOI18N
        SelectDestination.setEnabled(false);
        SelectDestination.setName("SelectDestination"); // NOI18N
        SelectDestination.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SelectDestinationMouseClicked(evt);
            }
        });

        CreateDNGs.setSelected(true);
        CreateDNGs.setText(resourceMap.getString("CreateDNGs.text")); // NOI18N
        CreateDNGs.setToolTipText(resourceMap.getString("CreateDNGs.toolTipText")); // NOI18N
        CreateDNGs.setEnabled(false);
        CreateDNGs.setName("CreateDNGs"); // NOI18N
        CreateDNGs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateDNGsActionPerformed(evt);
            }
        });

        CreatePGMs.setText(resourceMap.getString("CreatePGMs.text")); // NOI18N
        CreatePGMs.setToolTipText(resourceMap.getString("CreatePGMs.toolTipText")); // NOI18N
        CreatePGMs.setEnabled(false);
        CreatePGMs.setName("CreatePGMs"); // NOI18N
        CreatePGMs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreatePGMsActionPerformed(evt);
            }
        });

        CreateJPEGs.setText(resourceMap.getString("CreateJPEGs.text")); // NOI18N
        CreateJPEGs.setToolTipText(resourceMap.getString("CreateJPEGs.toolTipText")); // NOI18N
        CreateJPEGs.setEnabled(false);
        CreateJPEGs.setName("CreateJPEGs"); // NOI18N
        CreateJPEGs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateJPEGsActionPerformed(evt);
            }
        });

        keepJP4s.setText(resourceMap.getString("keepJP4s.text")); // NOI18N
        keepJP4s.setToolTipText(resourceMap.getString("keepJP4s.toolTipText")); // NOI18N
        keepJP4s.setEnabled(false);
        keepJP4s.setName("keepJP4s"); // NOI18N
        keepJP4s.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepJP4sActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout SettingsPanelLayout = new javax.swing.GroupLayout(SettingsPanel);
        SettingsPanel.setLayout(SettingsPanelLayout);
        SettingsPanelLayout.setHorizontalGroup(
            SettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(DestinationLabel)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SettingsPanelLayout.createSequentialGroup()
                            .addComponent(DifferentFolder)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(SelectDestination)))
                    .addComponent(SameAsSource)
                    .addComponent(subfolders))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(SettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(CreateDNGs)
                    .addComponent(CreatePGMs)
                    .addComponent(CreateJPEGs)
                    .addComponent(keepJP4s))
                .addContainerGap(232, Short.MAX_VALUE))
        );
        SettingsPanelLayout.setVerticalGroup(
            SettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SettingsPanelLayout.createSequentialGroup()
                        .addComponent(CreateDNGs)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CreatePGMs)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CreateJPEGs)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keepJP4s))
                    .addGroup(SettingsPanelLayout.createSequentialGroup()
                        .addComponent(DestinationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(SettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SettingsPanelLayout.createSequentialGroup()
                                .addGap(56, 56, 56)
                                .addGroup(SettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(DifferentFolder)
                                    .addComponent(SelectDestination)))
                            .addGroup(SettingsPanelLayout.createSequentialGroup()
                                .addComponent(SameAsSource)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(subfolders)))))
                .addContainerGap(71, Short.MAX_VALUE))
        );

        TabbedSettingsPanel.addTab(resourceMap.getString("SettingsPanel.TabConstraints.tabTitle"), SettingsPanel); // NOI18N

        AdvancedSettingsPanel.setName("AdvancedSettingsPanel"); // NOI18N

        BayerShiftLabel.setText(resourceMap.getString("BayerShiftLabel.text")); // NOI18N
        BayerShiftLabel.setEnabled(false);
        BayerShiftLabel.setName("BayerShiftLabel"); // NOI18N

        BayerShift.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Autodetect (Default)", "0", "1", "2", "3" }));
        BayerShift.setToolTipText(resourceMap.getString("BayerShift.toolTipText")); // NOI18N
        BayerShift.setEnabled(false);
        BayerShift.setName("BayerShift"); // NOI18N
        BayerShift.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BayerShiftActionPerformed(evt);
            }
        });

        FrameLimiterLabel.setText(resourceMap.getString("FrameLimiterLabel.text")); // NOI18N
        FrameLimiterLabel.setEnabled(false);
        FrameLimiterLabel.setName("FrameLimiterLabel"); // NOI18N

        FrameLimiterGroup.add(FrameLimiterAll);
        FrameLimiterAll.setSelected(true);
        FrameLimiterAll.setText(resourceMap.getString("FrameLimiterAll.text")); // NOI18N
        FrameLimiterAll.setEnabled(false);
        FrameLimiterAll.setName("FrameLimiterAll"); // NOI18N
        FrameLimiterAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FrameLimiterAllActionPerformed(evt);
            }
        });

        FrameLimiterGroup.add(FrameLimiterNumber);
        FrameLimiterNumber.setText(resourceMap.getString("FrameLimiterNumber.text")); // NOI18N
        FrameLimiterNumber.setEnabled(false);
        FrameLimiterNumber.setName("FrameLimiterNumber"); // NOI18N
        FrameLimiterNumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FrameLimiterNumberActionPerformed(evt);
            }
        });

        FrameLimiterNumberField.setText(resourceMap.getString("FrameLimiterNumberField.text")); // NOI18N
        FrameLimiterNumberField.setEnabled(false);
        FrameLimiterNumberField.setName("FrameLimiterNumberField"); // NOI18N
        FrameLimiterNumberField.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                FrameLimiterNumberFieldCaretUpdate(evt);
            }
        });
        FrameLimiterNumberField.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                FrameLimiterNumberFieldCaretPositionChanged(evt);
            }
        });
        FrameLimiterNumberField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                FrameLimiterNumberFieldPropertyChange(evt);
            }
        });
        FrameLimiterNumberField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                FrameLimiterNumberFieldKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout AdvancedSettingsPanelLayout = new javax.swing.GroupLayout(AdvancedSettingsPanel);
        AdvancedSettingsPanel.setLayout(AdvancedSettingsPanelLayout);
        AdvancedSettingsPanelLayout.setHorizontalGroup(
            AdvancedSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AdvancedSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AdvancedSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(FrameLimiterAll)
                    .addComponent(FrameLimiterLabel)
                    .addGroup(AdvancedSettingsPanelLayout.createSequentialGroup()
                        .addComponent(FrameLimiterNumber)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(FrameLimiterNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(AdvancedSettingsPanelLayout.createSequentialGroup()
                        .addComponent(BayerShiftLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BayerShift, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(350, Short.MAX_VALUE))
        );
        AdvancedSettingsPanelLayout.setVerticalGroup(
            AdvancedSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AdvancedSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(FrameLimiterLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FrameLimiterAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(AdvancedSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FrameLimiterNumber)
                    .addComponent(FrameLimiterNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(AdvancedSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BayerShiftLabel)
                    .addComponent(BayerShift, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(66, Short.MAX_VALUE))
        );

        TabbedSettingsPanel.addTab(resourceMap.getString("AdvancedSettingsPanel.TabConstraints.tabTitle"), AdvancedSettingsPanel); // NOI18N

        ConverterProgressBar.setName("ConverterProgressBar"); // NOI18N

        ConvertButton.setText(resourceMap.getString("ConvertButton.text")); // NOI18N
        ConvertButton.setEnabled(false);
        ConvertButton.setName("ConvertButton"); // NOI18N
        ConvertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConvertButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(TabbedSettingsPanel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(ConverterProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ConvertButton, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(StopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(command, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                        .addComponent(JBUtton_SelectSource)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 454, Short.MAX_VALUE)
                        .addComponent(movie2dngVersionLabel))
                    .addComponent(CommandLabel, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(JBUtton_SelectSource)
                    .addComponent(movie2dngVersionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(CommandLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(command, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TabbedSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ConverterProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(StopButton)
                        .addComponent(ConvertButton)))
                .addContainerGap())
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(movie2dnggui.Movie2DNGGUIApp.class).getContext().getActionMap(Movie2DNGGUIView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setComponent(mainPanel);
        setMenuBar(menuBar);
    }// </editor-fold>//GEN-END:initComponents

    private void JBUtton_SelectSourceMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_JBUtton_SelectSourceMouseClicked
        int returnVal = fc.showOpenDialog(JBUtton_SelectSource);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            SourcePath = file.getPath();
            SourcePath = SourcePath.replaceAll(" ", "\\ "); // For Linux paths with spaces
            SourceFilename = file.getName();
            int sep = file.getPath().lastIndexOf("/");
            DestinationPath = file.getPath().substring(0, sep);
            DestinationPath = DestinationPath.replaceAll(" ", "\\ "); // For Linux paths with spaces
            DestinationCustomPath = DestinationPath;

            ConvertButton.setEnabled(true);
            keepJP4s.setEnabled(true);
            BayerShift.setEnabled(true);
            BayerShiftLabel.setEnabled(true);
            SameAsSource.setEnabled(true);
            subfolders.setEnabled(true);
            DifferentFolder.setEnabled(true);
            command.setEnabled(true);
            CommandLabel.setEnabled(true);
            DestinationLabel.setEnabled(true);

            FrameLimiterLabel.setEnabled(true);
            FrameLimiterAll.setEnabled(true);
            FrameLimiterNumber.setEnabled(true);

            CreateDNGs.setEnabled(true);
            CreatePGMs.setEnabled(true);
            CreateJPEGs.setEnabled(true);
            keepJP4s.setEnabled(true);

            UpdateCommand();
        } else {
        }
    }//GEN-LAST:event_JBUtton_SelectSourceMouseClicked

    private void keepJP4sActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keepJP4sActionPerformed
        UpdateCommand();
    }//GEN-LAST:event_keepJP4sActionPerformed

    private void DifferentFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DifferentFolderActionPerformed
        if (DifferentFolder.isSelected()) {
            SelectDestination.setEnabled(true);
        } else {
            SelectDestination.setEnabled(false);
        }
        UpdateCommand();
    }//GEN-LAST:event_DifferentFolderActionPerformed

    private void SameAsSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SameAsSourceActionPerformed
        if (DifferentFolder.isSelected()) {
            SelectDestination.setEnabled(true);
        } else {
            SelectDestination.setEnabled(false);
        }
        UpdateCommand();
    }//GEN-LAST:event_SameAsSourceActionPerformed

    private void SelectDestinationMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SelectDestinationMouseClicked
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(SelectDestination);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            int sep = file.getPath().lastIndexOf("/");
            DestinationCustomPath = file.getPath().substring(0, sep);

            UpdateCommand();
        } else {
        }
    }//GEN-LAST:event_SelectDestinationMouseClicked

    private void subfoldersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subfoldersActionPerformed
        if (DifferentFolder.isSelected()) {
            SelectDestination.setEnabled(true);
        } else {
            SelectDestination.setEnabled(false);
        }
        UpdateCommand();
    }//GEN-LAST:event_subfoldersActionPerformed

    private void BayerShiftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BayerShiftActionPerformed
        UpdateCommand();
    }//GEN-LAST:event_BayerShiftActionPerformed

    private void FrameLimiterAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FrameLimiterAllActionPerformed
        if (FrameLimiterNumber.isSelected()) {
            FrameLimiterNumberField.setEnabled(true);
        } else {
            FrameLimiterNumberField.setEnabled(false);
        }
        UpdateCommand();
    }//GEN-LAST:event_FrameLimiterAllActionPerformed

    private void FrameLimiterNumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FrameLimiterNumberActionPerformed
        if (FrameLimiterNumber.isSelected()) {
            FrameLimiterNumberField.setEnabled(true);
        } else {
            FrameLimiterNumberField.setEnabled(false);
        }
        UpdateCommand();
    }//GEN-LAST:event_FrameLimiterNumberActionPerformed

    private void FrameLimiterNumberFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_FrameLimiterNumberFieldPropertyChange
        if (!FrameLimiterNumberField.getText().equals("")) {
            boolean valid = false;
            try {
                Integer.parseInt(FrameLimiterNumberField.getText());
                valid = true;
            } catch (NumberFormatException e) {
                valid = false;
            }
            if (!valid) {
                FrameLimiterNumberField.setText("0");
            }
            UpdateCommand();
        }
    }//GEN-LAST:event_FrameLimiterNumberFieldPropertyChange

    private void FrameLimiterNumberFieldCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_FrameLimiterNumberFieldCaretUpdate
        if (!FrameLimiterNumberField.getText().equals("")) {
            boolean valid = false;
            try {
                Integer.parseInt(FrameLimiterNumberField.getText());
                valid = true;
            } catch (NumberFormatException e) {
                valid = false;
            }
            if (!valid) {
                FrameLimiterNumberField.setText("0");
            }
            UpdateCommand();
        }
    }//GEN-LAST:event_FrameLimiterNumberFieldCaretUpdate

    private void FrameLimiterNumberFieldCaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_FrameLimiterNumberFieldCaretPositionChanged
        if (!FrameLimiterNumberField.getText().equals("")) {
            boolean valid = false;
            try {
                Integer.parseInt(FrameLimiterNumberField.getText());
                valid = true;
            } catch (NumberFormatException e) {
                valid = false;
            }
            if (!valid) {
                FrameLimiterNumberField.setText("0");
            }
            UpdateCommand();
        }
    }//GEN-LAST:event_FrameLimiterNumberFieldCaretPositionChanged

    private void FrameLimiterNumberFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_FrameLimiterNumberFieldKeyReleased
        if (!FrameLimiterNumberField.getText().equals("")) {
            boolean valid = false;
            try {
                Integer.parseInt(FrameLimiterNumberField.getText());
                valid = true;
            } catch (NumberFormatException e) {
                valid = false;
            }
            if (!valid) {
                FrameLimiterNumberField.setText("0");
            }
            UpdateCommand();
        }
    }//GEN-LAST:event_FrameLimiterNumberFieldKeyReleased

    private void CreateJPEGsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateJPEGsActionPerformed
        UpdateCommand();
    }//GEN-LAST:event_CreateJPEGsActionPerformed

    private void CreatePGMsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreatePGMsActionPerformed
        UpdateCommand();
    }//GEN-LAST:event_CreatePGMsActionPerformed

    private void CreateDNGsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateDNGsActionPerformed
        UpdateCommand();
    }//GEN-LAST:event_CreateDNGsActionPerformed

    private void ConvertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConvertButtonActionPerformed
        ConvertButton.setEnabled(false);
        StopButton.setEnabled(true);
        Converting = true;

        if (FrameLimiterNumberField.isEnabled() && !FrameLimiterNumberField.getText().equals("")) {
            ClipFrameCountLimit = Integer.parseInt(FrameLimiterNumberField.getText());
        }
        converter_firstline = true;
        conversion_complete = false;

        ConvertProcess = null;
        try {
            ConvertProcess = Runtime.getRuntime().exec(command.getText());
            LogArea.append("executing: \"" + command.getText() + "\"\n\r");
        } catch (IOException ex) {
            Logger.getLogger(Movie2DNGGUIView.class.getName()).log(Level.SEVERE, null, ex);
        }
        ReadConvertProgress = new Thread(this);
        if (!ReadConvertProgress.isAlive()) {
            ReadConvertProgress.start();
        }
    }//GEN-LAST:event_ConvertButtonActionPerformed

    private void StopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StopButtonActionPerformed
        if (Converting) {
            Thread.yield();
            ConvertProcess.destroy();
            ConvertButton.setEnabled(true);
            StopButton.setEnabled(false);
            Converting = false;
        }
    }//GEN-LAST:event_StopButtonActionPerformed

    public void run() {
        while (Thread.currentThread() == ReadConvertProgress) {
            BufferedReader in = new BufferedReader(new InputStreamReader(ConvertProcess.getInputStream()));
            String line[] = new String[128];
            if (!FrameLimiterNumber.isSelected()) {
                ClipFrameCountLimit = 0;
            }

            int j = 0;
            try {
                while ((line[j] = in.readLine()) != null) {
                    if (converter_firstline) {
                        ClipFrameCountTotal = Integer.parseInt(line[j]);
                        converter_firstline = false;
                    } else {
                        if (line[j].equals("")) {
                            conversion_complete = true;
                        } else {
                            ClipFrameCountConverted = Integer.parseInt(line[j]);
                        }
                    }
                    int progress = 0;
                    if (ClipFrameCountLimit != 0) {
                        progress = (int) (((float) ClipFrameCountConverted / (float) ClipFrameCountLimit) * 100);
                    } else {
                        progress = (int) (((float) ClipFrameCountConverted / (float) ClipFrameCountTotal) * 100);
                    }
                    ConverterProgressBar.setValue(progress);

                    // Update Log Area
                    LogArea.append(line[j] + "\n\r");
                    LogArea.setCaretPosition(LogArea.getDocument().getLength());

                    j++;
                    if (j > 10) {
                        break;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Movie2DNGGUIView.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Thread.sleep(20); //milliseconds
            } catch (InterruptedException e) {
                break;
            }
            if (conversion_complete) {
                Thread.yield();
                StopButton.setText("Convert");
            }
        }
    }

    private void UpdateCommand() {
        String commandstring = "movie2dng --gui ";
        if (CreateDNGs.isSelected()) {
            commandstring += "--dng "; // Create DNGs
        }
        if (keepJP4s.isSelected()) {
            commandstring += "--jp4 "; // Create JP4s
        }
        if (CreateJPEGs.isSelected()) {
            commandstring += "--jpeg "; // Create JPEGs
        }
        if (CreatePGMs.isSelected()) {
            commandstring += "--pgm "; // Create PGMs
        }

        if (BayerShift.getSelectedIndex() != 0) {
            commandstring += "--shift=" + ((int) BayerShift.getSelectedIndex() - 1) + " "; // Bayer Shift
        }
        if (FrameLimiterNumber.isSelected()) {
            commandstring += "--frames " + FrameLimiterNumberField.getText() + " "; // N frame conversion
        }

        commandstring += SourcePath + " "; // SOURCE

        if (SameAsSource.isSelected()) {
            if (SourceFilename != "") {
                commandstring += DestinationPath + "/" + SourceFilename.substring(0, SourceFilename.lastIndexOf('.')) + "_NNNNN"; // DESTINATION
            }
        }
        if (subfolders.isSelected()) {
            if (SourceFilename != "") {
                commandstring += DestinationPath + "/" + SourceFilename.substring(0, SourceFilename.lastIndexOf('.')) + "/" + SourceFilename.substring(0, SourceFilename.lastIndexOf('.')) + "_NNNNN"; // DESTINATION
            }

        }

        if (DifferentFolder.isSelected()) {
            if (SourceFilename != "") {
                commandstring += DestinationCustomPath + "/" + SourceFilename.substring(0, SourceFilename.lastIndexOf('.')) + "_NNNNN"; // DESTINATION
            }

        }

        command.setText(commandstring);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AdvancedSettingsPanel;
    private javax.swing.JComboBox BayerShift;
    private javax.swing.JLabel BayerShiftLabel;
    private javax.swing.JLabel CommandLabel;
    private javax.swing.JButton ConvertButton;
    private javax.swing.JProgressBar ConverterProgressBar;
    private javax.swing.JCheckBox CreateDNGs;
    private javax.swing.JCheckBox CreateJPEGs;
    private javax.swing.JCheckBox CreatePGMs;
    private javax.swing.JLabel DestinationLabel;
    private javax.swing.JRadioButton DifferentFolder;
    private javax.swing.JRadioButton FrameLimiterAll;
    private javax.swing.ButtonGroup FrameLimiterGroup;
    private javax.swing.JLabel FrameLimiterLabel;
    private javax.swing.JRadioButton FrameLimiterNumber;
    private javax.swing.JTextField FrameLimiterNumberField;
    private javax.swing.JButton JBUtton_SelectSource;
    private javax.swing.JTextArea LogArea;
    private javax.swing.JRadioButton SameAsSource;
    private javax.swing.JButton SelectDestination;
    private javax.swing.JPanel SettingsPanel;
    private javax.swing.JButton StopButton;
    private javax.swing.JTabbedPane TabbedSettingsPanel;
    private javax.swing.JTextField command;
    private javax.swing.ButtonGroup destinationbuttongroup;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox keepJP4s;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JLabel movie2dngVersionLabel;
    private javax.swing.JRadioButton subfolders;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private JFileChooser fc;
    private String SourcePath;
    private String SourceFilename;
    private String DestinationPath;
    private String DestinationCustomPath;
    private String movie2dngVersion;
    private Thread ReadConvertProgress;
    private Process ConvertProcess;
    private int ClipFrameCountTotal;
    private int ClipFrameCountConverted;
    private int ClipFrameCountLimit;
    private boolean converter_firstline;
    private boolean conversion_complete;
}
