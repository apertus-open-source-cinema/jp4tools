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
package movie2dngui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
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
public class Movie2DNGUIView extends FrameView implements Runnable {

    public Movie2DNGUIView(SingleFrameApplication app) {
        super(app);

        initComponents();
        Movie2DNGUIView.this.getFrame().setBounds(0, 0, 600, 600);
        Movie2DNGUIView.this.getFrame().setSize(600, 600);


        String osname = System.getProperty("os.name");
        if (osname.startsWith("Linux")) {
            //everything alright
        } else {
            //uh oh!
            JOptionPane.showMessageDialog(Movie2DNGUIView.this.getFrame(), "This Program currently works ONLY under Linux, I am afraid you are using an unsupported OS!");
        }

        fc = new JFileChooser();

        SourcePath = "";
        SourceFilename = "";
        DestinationPath = "";
        movie2dngVersion = "";
        Movie2DNGUIView.this.getFrame().setSize(1024, 600);

        // check Version of movie2dng
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("movie2dng --version");
        } catch (IOException ex) {
            Logger.getLogger(Movie2DNGUIView.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line[] = new String[64];
        int j = 0;
        try {
            while ((line[j] = in.readLine()) != null) {
                j++;
            }
            movie2dngVersion = line[0];
        } catch (IOException ex) {
            Logger.getLogger(Movie2DNGUIView.class.getName()).log(Level.SEVERE, null, ex);
        }
        movie2dngVersionLabel.setText("Version: " + movie2dngVersion);

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = Movie2DNGUIApp.getApplication().getMainFrame();
            aboutBox = new Movie2DNGUIAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        Movie2DNGUIApp.getApplication().show(aboutBox);
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
        keepJP4s = new javax.swing.JCheckBox();
        DestinationLabel = new javax.swing.JLabel();
        SameAsSource = new javax.swing.JRadioButton();
        DifferentFolder = new javax.swing.JRadioButton();
        BayerShiftLabel = new javax.swing.JLabel();
        BayerShift = new javax.swing.JComboBox();
        ConvertButton = new javax.swing.JButton();
        SelectDestination = new javax.swing.JButton();
        subfolders = new javax.swing.JRadioButton();
        movie2dngVersionLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        LogArea = new javax.swing.JTextArea();
        FrameLimiterLabel = new javax.swing.JLabel();
        FrameLimiterAll = new javax.swing.JRadioButton();
        FrameLimiterNumber = new javax.swing.JRadioButton();
        FrameLimiterNumberField = new javax.swing.JTextField();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        destinationbuttongroup = new javax.swing.ButtonGroup();
        FrameLimiterGroup = new javax.swing.ButtonGroup();

        mainPanel.setName("mainPanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(movie2dngui.Movie2DNGUIApp.class).getContext().getResourceMap(Movie2DNGUIView.class);
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

        keepJP4s.setText(resourceMap.getString("keepJP4s.text")); // NOI18N
        keepJP4s.setEnabled(false);
        keepJP4s.setName("keepJP4s"); // NOI18N
        keepJP4s.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepJP4sActionPerformed(evt);
            }
        });

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

        destinationbuttongroup.add(DifferentFolder);
        DifferentFolder.setText(resourceMap.getString("DifferentFolder.text")); // NOI18N
        DifferentFolder.setEnabled(false);
        DifferentFolder.setName("DifferentFolder"); // NOI18N
        DifferentFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DifferentFolderActionPerformed(evt);
            }
        });

        BayerShiftLabel.setText(resourceMap.getString("BayerShiftLabel.text")); // NOI18N
        BayerShiftLabel.setEnabled(false);
        BayerShiftLabel.setName("BayerShiftLabel"); // NOI18N

        BayerShift.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3" }));
        BayerShift.setToolTipText(resourceMap.getString("BayerShift.toolTipText")); // NOI18N
        BayerShift.setEnabled(false);
        BayerShift.setName("BayerShift"); // NOI18N
        BayerShift.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BayerShiftActionPerformed(evt);
            }
        });

        ConvertButton.setText(resourceMap.getString("ConvertButton.text")); // NOI18N
        ConvertButton.setEnabled(false);
        ConvertButton.setName("ConvertButton"); // NOI18N
        ConvertButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ConvertButtonMouseClicked(evt);
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

        destinationbuttongroup.add(subfolders);
        subfolders.setText(resourceMap.getString("subfolders.text")); // NOI18N
        subfolders.setEnabled(false);
        subfolders.setName("subfolders"); // NOI18N
        subfolders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subfoldersActionPerformed(evt);
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

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ConvertButton, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE)
                    .addComponent(command, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                        .addComponent(JBUtton_SelectSource)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 429, Short.MAX_VALUE)
                        .addComponent(movie2dngVersionLabel))
                    .addComponent(CommandLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(keepJP4s, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                        .addComponent(BayerShiftLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BayerShift, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(DestinationLabel)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                    .addComponent(DifferentFolder)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(SelectDestination)
                                    .addGap(20, 20, 20)))
                            .addComponent(SameAsSource)
                            .addComponent(subfolders))
                        .addGap(18, 18, 18)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(FrameLimiterNumber)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(FrameLimiterNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(FrameLimiterAll)
                            .addComponent(FrameLimiterLabel))
                        .addGap(112, 112, 112)))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(keepJP4s)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BayerShiftLabel)
                    .addComponent(BayerShift, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(DestinationLabel)
                    .addComponent(FrameLimiterLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(DifferentFolder)
                            .addComponent(SelectDestination)))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(SameAsSource)
                            .addComponent(FrameLimiterAll))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(subfolders)
                            .addComponent(FrameLimiterNumber)
                            .addComponent(FrameLimiterNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ConvertButton))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(movie2dngui.Movie2DNGUIApp.class).getContext().getActionMap(Movie2DNGUIView.class, this);
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

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 597, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 411, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
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
            FrameLimiterNumberField.setEnabled(true);

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

    private void ConvertButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ConvertButtonMouseClicked
        ConvertButton.setEnabled(false);
        ConvertProcess = null;
        try {
            ConvertProcess = Runtime.getRuntime().exec(command.getText());
            LogArea.append("executing: \"" + command.getText() + "\"\n\r");
        } catch (IOException ex) {
            Logger.getLogger(Movie2DNGUIView.class.getName()).log(Level.SEVERE, null, ex);
        }
        ReadConvertProgress = new Thread(this);
        if (!ReadConvertProgress.isAlive()) {
            ReadConvertProgress.start();
        }
    }//GEN-LAST:event_ConvertButtonMouseClicked

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

    public void run() {
        while (Thread.currentThread() == ReadConvertProgress) {
            BufferedReader in = new BufferedReader(new InputStreamReader(ConvertProcess.getInputStream()));
            String line[] = new String[128];
            int j = 0;
            try {
                while ((line[j] = in.readLine()) != null) {

                    // TODO update progress bar
                    LogArea.append(line[j] + "\n\r");
                    LogArea.setCaretPosition(LogArea.getDocument().getLength());

                    j++;
                    if (j > 10) {
                        break;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Movie2DNGUIView.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Thread.sleep(20); //ms
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void UpdateCommand() {
        String commandstring = "movie2dng --gui ";
        if (keepJP4s.isSelected()) {
            commandstring += "--keep-jp4 "; // Keep JP4s
        }

        commandstring += "--shift=" + BayerShift.getSelectedIndex() + " "; // Bayer Shift

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
    private javax.swing.JComboBox BayerShift;
    private javax.swing.JLabel BayerShiftLabel;
    private javax.swing.JLabel CommandLabel;
    private javax.swing.JButton ConvertButton;
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
    private javax.swing.JTextField command;
    private javax.swing.ButtonGroup destinationbuttongroup;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox keepJP4s;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JLabel movie2dngVersionLabel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
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
}
