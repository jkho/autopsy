/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011-2019 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.casemodule;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessor;

/**
 * Panel for adding an logical image file from drive letters. Allows the user
 * to select a file.
 */
@Messages({
    "LogicalImagerPanel.messageLabel.selectedImage=Selected folder",
    "LogicalImagerPanel.messageLabel.noImageSelected=No image selected",
    "LogicalImagerPanel.messageLabel.driveHasNoImages=Drive has no images",
    "LogicalImagerPanel.selectAcquisitionFromDriveLabel.text=Select acquisition from Drive",
})
@SuppressWarnings("PMD.SingularField") // UI widgets cause lots of false positives
public class LogicalImagerPanel extends JPanel implements DocumentListener {

    private static final long serialVersionUID = 1L;
    private static final String SPARSE_IMAGE_VHD = "sparse_image.vhd"; //NON-NLS
    private static final String SELECTED_IMAGE = Bundle.LogicalImagerPanel_messageLabel_selectedImage();
    private static final String NO_IMAGE_SELECTED = Bundle.LogicalImagerPanel_messageLabel_noImageSelected();
    private static final String DRIVE_HAS_NO_IMAGES = Bundle.LogicalImagerPanel_messageLabel_driveHasNoImages();
    private static final String[] EMPTY_LIST_DATA = {};

    private final JFileChooser fileChooser = new JFileChooser();
    private final Pattern regex = Pattern.compile("Logical_Imager_(.+)_(\\d{4})(\\d{2})(\\d{2})_(\\d{2})_(\\d{2})_(\\d{2})");
    private final String contextName;
    private Path choosenImageDirPath;
    private TableModel imageTableModel;
    
    /**
     * Creates new form LogicalImagerPanel
     *
     * @param context            A string context name used to read/store last
     *                           used settings.
     */
    private LogicalImagerPanel(String context) {
        this.contextName = context;
        initComponents();
        clearImageTable();
    }

    /**
     * Creates and returns an instance of a LogicalImagerPanel.
     *
     * @param context            A string context name used to read/store last
     *                           used settings.
     * 
     * @return instance of the LogicalImagerPanel
     */
    @Messages({
        "LogicalImagerPanel.messageLabel.clickScanOrBrowse=Click SCAN or BROWSE button to find images"
    })
    public static synchronized LogicalImagerPanel createInstance(String context) {
        LogicalImagerPanel instance = new LogicalImagerPanel(context);
        // post-constructor initialization of listener support without leaking references of uninitialized objects
        instance.messageLabel.setText(Bundle.LogicalImagerPanel_messageLabel_clickScanOrBrowse());
        return instance;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        scanButton = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();
        selectDriveLabel = new javax.swing.JLabel();
        driveListScrollPane = new javax.swing.JScrollPane();
        driveList = new javax.swing.JList<>();
        selectAcquisitionFromDriveLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();
        imageScrollPane = new javax.swing.JScrollPane();
        imageTable = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();

        setMinimumSize(new java.awt.Dimension(0, 65));
        setPreferredSize(new java.awt.Dimension(403, 65));

        org.openide.awt.Mnemonics.setLocalizedText(topLabel, org.openide.util.NbBundle.getMessage(LogicalImagerPanel.class, "LogicalImagerPanel.topLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(LogicalImagerPanel.class, "LogicalImagerPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(scanButton, org.openide.util.NbBundle.getMessage(LogicalImagerPanel.class, "LogicalImagerPanel.scanButton.text")); // NOI18N
        scanButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getMessage(LogicalImagerPanel.class, "LogicalImagerPanel.messageLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectDriveLabel, org.openide.util.NbBundle.getMessage(LogicalImagerPanel.class, "LogicalImagerPanel.selectDriveLabel.text")); // NOI18N

        driveList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        driveList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                driveListMouseClicked(evt);
            }
        });
        driveList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                driveListKeyReleased(evt);
            }
        });
        driveListScrollPane.setViewportView(driveList);

        org.openide.awt.Mnemonics.setLocalizedText(selectAcquisitionFromDriveLabel, org.openide.util.NbBundle.getMessage(LogicalImagerPanel.class, "LogicalImagerPanel.selectAcquisitionFromDriveLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(LogicalImagerPanel.class, "LogicalImagerPanel.jLabel6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(LogicalImagerPanel.class, "LogicalImagerPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        imageScrollPane.setPreferredSize(new java.awt.Dimension(346, 402));

        imageTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        imageTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        imageTable.setShowHorizontalLines(false);
        imageTable.setShowVerticalLines(false);
        imageTable.getTableHeader().setReorderingAllowed(false);
        imageTable.setUpdateSelectionOnSort(false);
        imageTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imageTableMouseClicked(evt);
            }
        });
        imageTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                imageTableKeyReleased(evt);
            }
        });
        imageScrollPane.setViewportView(imageTable);
        imageTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(238, 238, 238)
                .addComponent(topLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messageLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(selectDriveLabel)
                                            .addGap(289, 289, 289))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addComponent(scanButton)
                                            .addGap(126, 126, 126)))
                                    .addGap(36, 36, 36)
                                    .addComponent(browseButton))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(driveListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(28, 28, 28)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(selectAcquisitionFromDriveLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(imageScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 346, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(346, 346, 346)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(144, 144, 144)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(68, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(topLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scanButton)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectDriveLabel)
                    .addComponent(selectAcquisitionFromDriveLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(imageScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(driveListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE))
                .addGap(26, 26, 26)
                .addComponent(messageLabel)
                .addGap(154, 154, 154))
        );
    }// </editor-fold>//GEN-END:initComponents

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B"; //NON-NLS
        } 
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i"); //NON-NLS
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre); //NON-NLS
    }
    
    @Messages({
        "LogicalImagerPanel.messageLabel.scanningExternalDrives=Scanning external drives for sparse_image.vhd ...",
        "LogicalImagerPanel.messageLabel.noExternalDriveFound=No drive found"
    })
    private void scanButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanButtonActionPerformed
        // Scan external drives for sparse_image.vhd
        clearImageTable();
        setNormalMessage(Bundle.LogicalImagerPanel_messageLabel_scanningExternalDrives());
        Vector<String> listData = new Vector<>();
        File[] roots = File.listRoots();
        int firstRemovableDrive = -1;
        int i = 0;
        for (File root : roots) {
            String description = FileSystemView.getFileSystemView().getSystemTypeDescription(root);
            long spaceInBytes = root.getTotalSpace();
            String sizeWithUnit = humanReadableByteCount(spaceInBytes, false);
            listData.add(root + " (" + description + ") (" + sizeWithUnit + ")");
            if (firstRemovableDrive == -1) {
                try {
                    FileStore fileStore = Files.getFileStore(root.toPath());
                    if ((boolean) fileStore.getAttribute("volume:isRemovable")) { //NON-NLS
                        firstRemovableDrive = i;
                    }
                } catch (IOException ex) {
                    ; // skip
                }
            }
            i++;
        }
        driveList.setListData(listData);
        if (!listData.isEmpty()) {
            // auto-select the first external drive, if any
            driveList.setSelectedIndex(firstRemovableDrive == -1 ? 0 : firstRemovableDrive);
            driveListMouseClicked(null);
            driveList.requestFocusInWindow();
        } else {
            setErrorMessage(Bundle.LogicalImagerPanel_messageLabel_noExternalDriveFound());
        }
    }//GEN-LAST:event_scanButtonActionPerformed

    @Messages({
        "LogicalImagerPanel.messageLabel.directoryDoesNotContainSparseImage=Directory {0} does not contain {1}",
        "LogicalImagerPanel.messageLabel.directoryFormatInvalid=Directory {0} does not match format Logical_Imager_HOSTNAME_yyyymmdd_HH_MM_SS"        
    })
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        imageTable.clearSelection();
        choosenImageDirPath = null;
        setErrorMessage(NO_IMAGE_SELECTED);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int retval = fileChooser.showOpenDialog(this);
        if (retval == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getPath();
            Matcher m = regex.matcher(path);
            if (m.find()) {
                Path vhdPath = Paths.get(path, SPARSE_IMAGE_VHD);
                if (!vhdPath.toFile().exists()) {
                    setErrorMessage(Bundle.LogicalImagerPanel_messageLabel_directoryDoesNotContainSparseImage(path,SPARSE_IMAGE_VHD));
                    firePropertyChange(DataSourceProcessor.DSP_PANEL_EVENT.UPDATE_UI.toString(), true, false);
                    return;
                }
                choosenImageDirPath = Paths.get(path);
                setNormalMessage(SELECTED_IMAGE + " " + path);
                firePropertyChange(DataSourceProcessor.DSP_PANEL_EVENT.UPDATE_UI.toString(), false, true);                
            } else {
                setErrorMessage(Bundle.LogicalImagerPanel_messageLabel_directoryFormatInvalid(path));
                firePropertyChange(DataSourceProcessor.DSP_PANEL_EVENT.UPDATE_UI.toString(), true, false);
                return;
            }
        } else {
            firePropertyChange(DataSourceProcessor.DSP_PANEL_EVENT.UPDATE_UI.toString(), true, false);
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void imageTableSelect() {
        int index = imageTable.getSelectedRow();
        if (index != -1) {
            choosenImageDirPath = Paths.get((String) imageTableModel.getValueAt(index, 2));
            setNormalMessage(SELECTED_IMAGE + " " + choosenImageDirPath.toString());
            firePropertyChange(DataSourceProcessor.DSP_PANEL_EVENT.UPDATE_UI.toString(), false, true);
        } else {
            choosenImageDirPath = null;
            setErrorMessage(NO_IMAGE_SELECTED);
            firePropertyChange(DataSourceProcessor.DSP_PANEL_EVENT.UPDATE_UI.toString(), true, false);
        }        
    }

    private void imageTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageTableMouseClicked
        imageTableSelect();
    }//GEN-LAST:event_imageTableMouseClicked

    private void driveListSelect() {
        String selectedStr = driveList.getSelectedValue();
        if (selectedStr == null) {
            return;
        }
        String driveLetter = selectedStr.substring(0, 3);
        File directory = new File(driveLetter);
        File[] fList = directory.listFiles();

        if (fList != null) {
            imageTableModel = new ImageTableModel();
            int row = 0;
            // Find all directories with name like Logical_Imager_HOSTNAME_yyyymmdd_HH_MM_SS
            // and has a sparse_image.vhd file in it
            for (File file : fList) {      
                if (file.isDirectory() 
                    && Paths.get(driveLetter, file.getName(), SPARSE_IMAGE_VHD).toFile().exists()) {
                    String dir = file.getName();
                    Matcher m = regex.matcher(dir);
                    if (m.find()) {
                        String imageDirPath = driveLetter + dir;
                        String hostname = m.group(1);
                        String year = m.group(2);
                        String month = m.group(3);
                        String day = m.group(4);
                        String hour = m.group(5);
                        String minute = m.group(6);
                        String second = m.group(7);
                        String extractDate = year + "/" + month + "/" + day 
                                + " " + hour + ":" + minute + ":" + second;
                        imageTableModel.setValueAt(hostname, row, 0);
                        imageTableModel.setValueAt(extractDate, row, 1);
                        imageTableModel.setValueAt(imageDirPath, row, 2);
                        row++;
                    }
                }
            }
            selectAcquisitionFromDriveLabel.setText(Bundle.LogicalImagerPanel_selectAcquisitionFromDriveLabel_text()
                    + " " + driveLetter);
            imageTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            imageTable.setModel(imageTableModel);
            fixImageTableColumnWidth();
            // If there are any images, select the first one
            if (imageTable.getRowCount() > 0) {
                imageTable.setRowSelectionInterval(0, 0);
                imageTableSelect();
            } else {
                choosenImageDirPath = null;
                setErrorMessage(DRIVE_HAS_NO_IMAGES);
            }
        }                
    }
    
    private void fixImageTableColumnWidth() {
        int width = imageScrollPane.getPreferredSize().width - 2;
        imageTable.getColumnModel().getColumn(0).setPreferredWidth((int) (.60 * width));
        imageTable.getColumnModel().getColumn(1).setPreferredWidth((int) (.40 * width));
    }
    
    private void setErrorMessage(String msg) {
        messageLabel.setForeground(Color.red);
        messageLabel.setText(msg);        
    }
    
    private void setNormalMessage(String msg) {
        messageLabel.setForeground(Color.black);
        messageLabel.setText(msg);        
    }

    private void clearImageTable() {
        imageTableModel = new ImageTableModel();
        imageTable.setModel(imageTableModel);        
        fixImageTableColumnWidth();
    }
    
    private void driveListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_driveListMouseClicked
        driveListSelect();
    }//GEN-LAST:event_driveListMouseClicked

    private void driveListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_driveListKeyReleased
        driveListSelect();
    }//GEN-LAST:event_driveListKeyReleased

    private void imageTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_imageTableKeyReleased
        imageTableSelect();
    }//GEN-LAST:event_imageTableKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JList<String> driveList;
    private javax.swing.JScrollPane driveListScrollPane;
    private javax.swing.JScrollPane imageScrollPane;
    private javax.swing.JTable imageTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JButton scanButton;
    private javax.swing.JLabel selectAcquisitionFromDriveLabel;
    private javax.swing.JLabel selectDriveLabel;
    private javax.swing.JLabel topLabel;
    // End of variables declaration//GEN-END:variables

    public void reset() {
        //reset the UI elements to default
        choosenImageDirPath = null;
        driveList.setListData(EMPTY_LIST_DATA);
        clearImageTable();
        setNormalMessage(Bundle.LogicalImagerPanel_messageLabel_clickScanOrBrowse());
    }

    /**
     * Should we enable the next button of the wizard?
     *
     * @return true if a proper image has been selected, false otherwise
     */
    public boolean validatePanel() {
        return choosenImageDirPath != null && choosenImageDirPath.toFile().exists();
    }
    
    Path getImageDirPath() {
        return choosenImageDirPath;
    }
    
    public void setMessageLabel(String message) {
        messageLabel.setText(message);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    void storeSettings() {
    }

    private class ImageTableModel extends AbstractTableModel {
        private final List<String> hostnames = new ArrayList<>();
        private final List<String> extractDates = new ArrayList<>();
        private final List<String> imageDirPaths = new ArrayList<>();

        @Override
        public int getRowCount() {
            return hostnames.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Messages({
            "LogicalImagerPanel.imageTable.columnModel.title0=Hostname",
            "LogicalImagerPanel.imageTable.columnModel.title1=Extracted Date"
        })
        @Override
        public String getColumnName(int column) {
            String colName = null;
            switch (column) {
                case 0:
                    colName = Bundle.LogicalImagerPanel_imageTable_columnModel_title0();
                    break;
                case 1:
                    colName = Bundle.LogicalImagerPanel_imageTable_columnModel_title1();
                    break;
                default:
                    break;
            }
            return colName;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Object ret = null;
            switch (columnIndex) {
                case 0:
                    ret = hostnames.get(rowIndex);
                    break;
                case 1:
                    ret = extractDates.get(rowIndex);
                    break;
                case 2:
                    ret = imageDirPaths.get(rowIndex);
                    break;
                default:
                    throw new UnsupportedOperationException("Invalid table column index: " + columnIndex); //NON-NLS
            }
            return ret;
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    hostnames.add((String) aValue);
                    break;
                case 1:
                    extractDates.add((String) aValue);
                    break;
                case 2:
                    imageDirPaths.add((String) aValue);
                    break;
                default:
                    throw new UnsupportedOperationException("Invalid table column index: " + columnIndex); //NON-NLS
            }
            // Only show the hostname and extractDates column
            if (columnIndex < 2) {
                super.setValueAt(aValue, rowIndex, columnIndex);
            }
        }
    }
}
