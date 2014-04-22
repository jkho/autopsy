/*
 * Autopsy Forensic Browser
 *
 * Copyright 2013-2014 Basis Technology Corp.
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
package org.sleuthkit.autopsy.corecomponents;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.sleuthkit.autopsy.datamodel.ContentUtils;
import org.sleuthkit.autopsy.ingest.IngestManager;

final class GeneralPanel extends javax.swing.JPanel {

    private static final String KEEP_PREFERRED_VIEWER = "keepPreferredViewer"; //NON-NLS
    private static final String USE_LOCAL_TIME = "useLocalTime"; //NON-NLS
    private static final String DS_HIDE_KNOWN = "dataSourcesHideKnown"; // Default false NON-NLS
    private static final String VIEWS_HIDE_KNOWN = "viewsHideKnown"; // Default true NON-NLS
    private final Preferences prefs = NbPreferences.forModule(this.getClass());

    GeneralPanel(GeneralOptionsPanelController controller) {
        initComponents();
        ContentUtils.setDisplayInLocalTime(useLocalTimeRB.isSelected());
        // TODO listen to changes in form fields and call controller.changed()
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        useBestViewerRB = new javax.swing.JRadioButton();
        keepCurrentViewerRB = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        useLocalTimeRB = new javax.swing.JRadioButton();
        useGMTTimeRB = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        dataSourcesHideKnownCB = new javax.swing.JCheckBox();
        viewsHideKnownCB = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        numberOfFileIngestThreadsComboBox = new javax.swing.JComboBox();

        buttonGroup1.add(useBestViewerRB);
        useBestViewerRB.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(useBestViewerRB, org.openide.util.NbBundle.getMessage(GeneralPanel.class, "GeneralPanel.useBestViewerRB.text")); // NOI18N
        useBestViewerRB.setToolTipText(org.openide.util.NbBundle.getMessage(GeneralPanel.class, "GeneralPanel.useBestViewerRB.toolTipText")); // NOI18N
        useBestViewerRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useBestViewerRBActionPerformed(evt);
            }
        });

        buttonGroup1.add(keepCurrentViewerRB);
        org.openide.awt.Mnemonics.setLocalizedText(keepCurrentViewerRB, org.openide.util.NbBundle.getMessage(GeneralPanel.class, "GeneralPanel.keepCurrentViewerRB.text")); // NOI18N
        keepCurrentViewerRB.setToolTipText(org.openide.util.NbBundle.getMessage(GeneralPanel.class, "GeneralPanel.keepCurrentViewerRB.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GeneralPanel.class, "GeneralPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GeneralPanel.class, "GeneralPanel.jLabel2.text")); // NOI18N

        buttonGroup3.add(useLocalTimeRB);
        useLocalTimeRB.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(useLocalTimeRB, org.openide.util.NbBundle.getMessage(GeneralPanel.class, "GeneralPanel.useLocalTimeRB.text")); // NOI18N

        buttonGroup3.add(useGMTTimeRB);
        org.openide.awt.Mnemonics.setLocalizedText(useGMTTimeRB, org.openide.util.NbBundle.getMessage(GeneralPanel.class, "GeneralPanel.useGMTTimeRB.text")); // NOI18N
        useGMTTimeRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useGMTTimeRBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(GeneralPanel.class, "GeneralPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dataSourcesHideKnownCB, org.openide.util.NbBundle.getMessage(GeneralPanel.class, "GeneralPanel.dataSourcesHideKnownCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(viewsHideKnownCB, org.openide.util.NbBundle.getMessage(GeneralPanel.class, "GeneralPanel.viewsHideKnownCB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(GeneralPanel.class, "GeneralPanel.jLabel4.text")); // NOI18N

        numberOfFileIngestThreadsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4" }));
        numberOfFileIngestThreadsComboBox.setSelectedIndex(1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(useLocalTimeRB)
                                    .addComponent(useGMTTimeRB))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(keepCurrentViewerRB)
                                    .addComponent(useBestViewerRB)
                                    .addComponent(dataSourcesHideKnownCB)
                                    .addComponent(viewsHideKnownCB)
                                    .addComponent(numberOfFileIngestThreadsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useBestViewerRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keepCurrentViewerRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataSourcesHideKnownCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewsHideKnownCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useLocalTimeRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useGMTTimeRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(numberOfFileIngestThreadsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void useBestViewerRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useBestViewerRBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_useBestViewerRBActionPerformed

    private void useGMTTimeRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useGMTTimeRBActionPerformed
         ContentUtils.setDisplayInLocalTime(useLocalTimeRB.isSelected());
    }//GEN-LAST:event_useGMTTimeRBActionPerformed

    void load() {
        boolean keepPreferredViewer = prefs.getBoolean(KEEP_PREFERRED_VIEWER, false);
        keepCurrentViewerRB.setSelected(keepPreferredViewer);
        useBestViewerRB.setSelected(!keepPreferredViewer);
        boolean useLocalTime = prefs.getBoolean(USE_LOCAL_TIME, true);
        useLocalTimeRB.setSelected(useLocalTime);
        useGMTTimeRB.setSelected(!useLocalTime);
        dataSourcesHideKnownCB.setSelected(prefs.getBoolean(DS_HIDE_KNOWN, false));
        viewsHideKnownCB.setSelected(prefs.getBoolean(VIEWS_HIDE_KNOWN, true));
        numberOfFileIngestThreadsComboBox.setSelectedItem(IngestManager.getInstance().getNumberOfFileIngestThreads());
    }

    void store() {
        prefs.putBoolean(KEEP_PREFERRED_VIEWER, keepCurrentViewerRB.isSelected());
        prefs.putBoolean(USE_LOCAL_TIME, useLocalTimeRB.isSelected());
        prefs.putBoolean(DS_HIDE_KNOWN, dataSourcesHideKnownCB.isSelected());
        prefs.putBoolean(VIEWS_HIDE_KNOWN, viewsHideKnownCB.isSelected());
        IngestManager.getInstance().setNumberOfFileIngestThreads(Integer.valueOf(numberOfFileIngestThreadsComboBox.getSelectedItem().toString()));
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JCheckBox dataSourcesHideKnownCB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JRadioButton keepCurrentViewerRB;
    private javax.swing.JComboBox numberOfFileIngestThreadsComboBox;
    private javax.swing.JRadioButton useBestViewerRB;
    private javax.swing.JRadioButton useGMTTimeRB;
    private javax.swing.JRadioButton useLocalTimeRB;
    private javax.swing.JCheckBox viewsHideKnownCB;
    // End of variables declaration//GEN-END:variables
}
