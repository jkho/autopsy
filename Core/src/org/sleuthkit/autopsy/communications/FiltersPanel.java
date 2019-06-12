/*
 * Autopsy Forensic Browser
 *
 * Copyright 2017-2018 Basis Technology Corp.
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
package org.sleuthkit.autopsy.communications;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.casemodule.Case;
import static org.sleuthkit.autopsy.casemodule.Case.Events.CURRENT_CASE;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.core.UserPreferences;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.coreutils.ThreadConfined;
import org.sleuthkit.autopsy.ingest.IngestManager;
import static org.sleuthkit.autopsy.ingest.IngestManager.IngestModuleEvent.DATA_ADDED;
import org.sleuthkit.autopsy.ingest.ModuleDataEvent;
import org.sleuthkit.datamodel.Account;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.CommunicationsFilter;
import org.sleuthkit.datamodel.CommunicationsFilter.AccountTypeFilter;
import org.sleuthkit.datamodel.CommunicationsFilter.DateRangeFilter;
import org.sleuthkit.datamodel.CommunicationsFilter.DeviceFilter;
import org.sleuthkit.datamodel.DataSource;
import static org.sleuthkit.datamodel.Relationship.Type.CALL_LOG;
import static org.sleuthkit.datamodel.Relationship.Type.MESSAGE;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Panel that holds the Filter control widgets and triggers queries against the
 * CommunicationsManager on user filtering changes.
 */
@SuppressWarnings("PMD.SingularField") // UI widgets cause lots of false positives
final public class FiltersPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(FiltersPanel.class.getName());

    /**
     * Map from Account.Type to the checkbox for that account type's filter.
     */
    @ThreadConfined(type = ThreadConfined.ThreadType.AWT)
    private final Map<Account.Type, JCheckBox> accountTypeMap = new HashMap<>();

    /**
     * Map from datasource device id to the checkbox for that datasource.
     */
    @ThreadConfined(type = ThreadConfined.ThreadType.AWT)
    private final Map<String, JCheckBox> devicesMap = new HashMap<>();

    /**
     * Listens to ingest events to enable refresh button
     */
    private final PropertyChangeListener ingestListener;

    /**
     * Flag that indicates the UI is not up-sto-date with respect to the case DB
     * and it should be refreshed (by reapplying the filters).
     */
    private boolean needsRefresh;

    /**
     * Listen to check box state changes and validates that at least one box is
     * selected for device and account type ( other wise there will be no
     * results)
     */
    private final ItemListener validationListener;

    /**
     * Is the device account type filter enabled or not. It should be enabled
     * when the Table/Brows mode is active and disabled when the visualization
     * is active. Initially false since the browse/table mode is active
     * initially.
     */
    private boolean deviceAccountTypeEnabled;

    @NbBundle.Messages({"refreshText=Refresh Results", "applyText=Apply"})
    public FiltersPanel() {
        initComponents();
        deviceRequiredLabel.setVisible(false);
        accountTypeRequiredLabel.setVisible(false);
        startDatePicker.setDate(LocalDate.now().minusWeeks(3));
        endDatePicker.setDateToToday();
        startDatePicker.getSettings().setVetoPolicy(
                //no end date, or start is before end
                startDate -> endCheckBox.isSelected() == false
                || startDate.compareTo(endDatePicker.getDate()) <= 0
        );
        endDatePicker.getSettings().setVetoPolicy(
                //no start date, or end is after start
                endDate -> startCheckBox.isSelected() == false
                || endDate.compareTo(startDatePicker.getDate()) >= 0
        );

        updateTimeZone();
        validationListener = itemEvent -> validateFilters();

        updateFilters(true);
        UserPreferences.addChangeListener(preferenceChangeEvent -> {
            if (preferenceChangeEvent.getKey().equals(UserPreferences.DISPLAY_TIMES_IN_LOCAL_TIME) ||
                    preferenceChangeEvent.getKey().equals(UserPreferences.TIME_ZONE_FOR_DISPLAYS)) {
                updateTimeZone();
            }
        });

        this.ingestListener = pce -> {
            String eventType = pce.getPropertyName();
            if (eventType.equals(DATA_ADDED.toString())) {
                // Indicate that a refresh may be needed, unless the data added is Keyword or Hashset hits
                ModuleDataEvent eventData = (ModuleDataEvent) pce.getOldValue();
                if (null != eventData
                        && eventData.getBlackboardArtifactType().getTypeID() != BlackboardArtifact.ARTIFACT_TYPE.TSK_KEYWORD_HIT.getTypeID()
                        && eventData.getBlackboardArtifactType().getTypeID() != BlackboardArtifact.ARTIFACT_TYPE.TSK_HASHSET_HIT.getTypeID()) {
                    updateFilters(false);
                    needsRefresh = true;
                    validateFilters();
                }
            }
        };

        applyFiltersButton.addActionListener(e -> applyFilters());
        refreshButton.addActionListener(e -> applyFilters());
    }

    /**
     * Validate that filters are in a consistent state and will result in some
     * results. Checks that at least one device and at least one account type is
     * selected. Disables the apply and refresh button and shows warnings if the
     * filters are not valid.
     */
    private void validateFilters() {
        boolean someDevice = devicesMap.values().stream().anyMatch(JCheckBox::isSelected);
        boolean someAccountType = accountTypeMap.values().stream().anyMatch(JCheckBox::isSelected);

        deviceRequiredLabel.setVisible(someDevice == false);
        accountTypeRequiredLabel.setVisible(someAccountType == false);

        applyFiltersButton.setEnabled(someDevice && someAccountType);
        refreshButton.setEnabled(someDevice && someAccountType && needsRefresh);
        needsRefreshLabel.setVisible(needsRefresh);
    }

    /**
     * Update the filter widgets, and apply them.
     */
    void updateAndApplyFilters(boolean initialState) {
        updateFilters(initialState);
        applyFilters();
    }

    private void updateTimeZone() {
        dateRangeLabel.setText("Date Range (" + Utils.getUserPreferredZoneId().toString() + "):");
    }

    /**
     * Updates the filter widgets to reflect he data sources/types in the case.
     */
    private void updateFilters(boolean initialState) {
        updateAccountTypeFilter();
        updateDeviceFilter(initialState);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        IngestManager.getInstance().addIngestModuleEventListener(ingestListener);
        Case.addEventTypeSubscriber(EnumSet.of(CURRENT_CASE), evt -> {
            //clear the device filter widget when the case changes.
            devicesMap.clear();
            devicesPane.removeAll();
        });
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        IngestManager.getInstance().removeIngestModuleEventListener(ingestListener);
    }

    /**
     * Populate the Account Types filter widgets
     */
    private void updateAccountTypeFilter() {

        //TODO: something like this commented code could be used to show only
        //the account types that are found:
        //final CommunicationsManager communicationsManager = Case.getCurrentOpenCase().getSleuthkitCase().getCommunicationsManager();
        //List<Account.Type> accountTypesInUse = communicationsManager.getAccountTypesInUse();
        //accountTypesInUSe.forEach(...)
        Account.Type.PREDEFINED_ACCOUNT_TYPES.forEach(type -> {
            if (type.equals(Account.Type.CREDIT_CARD)) {
                //don't show a check box for credit cards
            } else {
                accountTypeMap.computeIfAbsent(type, t -> {
                    final JCheckBox jCheckBox = new JCheckBox(
                            "<html><table cellpadding=0><tr><td><img src=\""
                            + FiltersPanel.class.getResource(Utils.getIconFilePath(type))
                            + "\"/></td><td width=" + 3 + "><td>" + type.getDisplayName() + "</td></tr></table></html>",
                            true
                    );
                    jCheckBox.addItemListener(validationListener);
                    accountTypePane.add(jCheckBox);
                    if (t.equals(Account.Type.DEVICE)) {
                        //Deveice type filter is enabled based on whether we are in table or graph view.
                        jCheckBox.setEnabled(deviceAccountTypeEnabled);
                    }
                    return jCheckBox;
                });
            }
        });
    }

    /**
     * Populate the devices filter widgets
     */
    private void updateDeviceFilter(boolean initialState) {
        try {
            final SleuthkitCase sleuthkitCase = Case.getCurrentCaseThrows().getSleuthkitCase();

            for (DataSource dataSource : sleuthkitCase.getDataSources()) {
                String dsName = sleuthkitCase.getContentById(dataSource.getId()).getName();
                //store the device id in the map, but display a datasource name in the UI.
                devicesMap.computeIfAbsent(dataSource.getDeviceId(), ds -> {
                    final JCheckBox jCheckBox = new JCheckBox(dsName, initialState);
                    jCheckBox.addItemListener(validationListener);
                    devicesPane.add(jCheckBox);
                    return jCheckBox;
                });
            }
        } catch (NoCurrentCaseException ex) {
            logger.log(Level.INFO, "Filter update cancelled.  Case is closed.");
        } catch (TskCoreException tskCoreException) {
            logger.log(Level.SEVERE, "There was a error loading the datasources for the case.", tskCoreException);
        }
    }

    /**
     * Given a list of subFilters, set the states of the panel controls
     * accordingly.
     *
     * @param subFilters A list of subFilters
     */
    public void setFilters(CommunicationsFilter commFilter) {
        List<CommunicationsFilter.SubFilter> subFilters = commFilter.getAndFilters();
        subFilters.forEach(subFilter -> {
            if( subFilter instanceof DeviceFilter ) {
                setDeviceFilter((DeviceFilter)subFilter);
            } else if( subFilter instanceof AccountTypeFilter) {
                setAccountTypeFilter((AccountTypeFilter) subFilter);
            }
        });
    }

    /**
     * Sets the state of the device filter checkboxes
     *
     * @param deviceFilter Selected devices
     */
    private void setDeviceFilter(DeviceFilter deviceFilter) {
        Collection<String> deviceIDs = deviceFilter.getDevices();
        devicesMap.forEach((type, cb) -> {
            cb.setSelected(deviceIDs.contains(type));
        });
    }

     /**
     * Set the state of the account type checkboxes to match the passed in filter
     *
     * @param typeFilter Account Types to be selected
     */
    private void setAccountTypeFilter(AccountTypeFilter typeFilter){

        accountTypeMap.forEach((type, cb) -> {
            cb.setSelected(typeFilter.getAccountTypes().contains(type));
        });
    }

    /**
     * Set up the startDatePicker and startCheckBox based on the passed in
     * DateControlState.
     *
     * @param state new control state
     */
    private void setStartDateControlState(DateControlState state) {
        startDatePicker.setDate(state.getDate());
        startCheckBox.setSelected(state.isEnabled());
        startDatePicker.setEnabled(state.isEnabled());
    }

    /**
     * Set up the endDatePicker and endCheckBox based on the passed in
     * DateControlState.
     *
     * @param state new control state
     */
    private void setEndDateControlState(DateControlState state) {
        endDatePicker.setDate(state.getDate());
        endCheckBox.setSelected(state.isEnabled());
        endDatePicker.setEnabled(state.isEnabled());
    }

    @Subscribe
    void filtersBack(CVTEvents.StateChangeEvent event) {
        if(event.getCommunicationsState().getCommunicationsFilter() != null){
            setFilters(event.getCommunicationsState().getCommunicationsFilter());
            setStartDateControlState(event.getCommunicationsState().getStartControlState());
            setEndDateControlState(event.getCommunicationsState().getEndControlState());
            needsRefresh = false;
            validateFilters();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.GridBagLayout());

        topPane.setLayout(new java.awt.GridBagLayout());

        filtersTitleLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/communications/images/funnel.png"))); // NOI18N
        filtersTitleLabel.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.filtersTitleLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        topPane.add(filtersTitleLabel, gridBagConstraints);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/communications/images/arrow-circle-double-135.png"))); // NOI18N
        refreshButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.refreshButton.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        topPane.add(refreshButton, gridBagConstraints);

        applyFiltersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/communications/images/tick.png"))); // NOI18N
        applyFiltersButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.applyFiltersButton.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        topPane.add(applyFiltersButton, gridBagConstraints);

        needsRefreshLabel.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.needsRefreshLabel.text")); // NOI18N
        needsRefreshLabel.setForeground(new java.awt.Color(255, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        topPane.add(needsRefreshLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.weightx = 1.0;
        add(topPane, gridBagConstraints);

        scrollPane.setBorder(null);

        mainPanel.setLayout(new java.awt.GridBagLayout());

        limitPane.setLayout(new java.awt.GridBagLayout());

        mostRecentLabel.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.mostRecentLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 9);
        limitPane.add(mostRecentLabel, gridBagConstraints);

        limitComboBox.setEditable(true);
        limitComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "10000", "5000", "1000", "500", "100" }));
        limitComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limitComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        limitPane.add(limitComboBox, gridBagConstraints);

        limitTitlePanel.setLayout(new java.awt.GridBagLayout());

        limitHeaderLabel.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.limitHeaderLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        limitTitlePanel.add(limitHeaderLabel, gridBagConstraints);

        limitErrorMsgLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/error-icon-16.png"))); // NOI18N
        limitErrorMsgLabel.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.limitErrorMsgLabel.text")); // NOI18N
        limitErrorMsgLabel.setForeground(new java.awt.Color(255, 0, 0));
        limitErrorMsgLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        limitTitlePanel.add(limitErrorMsgLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        limitPane.add(limitTitlePanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 15, 0);
        mainPanel.add(limitPane, gridBagConstraints);

        startDatePicker.setEnabled(false);

        dateRangeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/calendar.png"))); // NOI18N
        dateRangeLabel.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.dateRangeLabel.text")); // NOI18N

        startCheckBox.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.startCheckBox.text")); // NOI18N
        startCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                startCheckBoxStateChanged(evt);
            }
        });

        endCheckBox.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.endCheckBox.text")); // NOI18N
        endCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                endCheckBoxStateChanged(evt);
            }
        });

        endDatePicker.setEnabled(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(dateRangeLabel)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(endCheckBox)
                        .addGap(12, 12, 12)
                        .addComponent(endDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(startCheckBox)
                        .addGap(12, 12, 12)
                        .addComponent(startDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {endCheckBox, startCheckBox});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(dateRangeLabel)
                .addGap(6, 6, 6)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(endDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(endCheckBox)))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        mainPanel.add(dateRangePane, gridBagConstraints);

        devicesPane.setLayout(new java.awt.GridBagLayout());

        unCheckAllDevicesButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.unCheckAllDevicesButton.text")); // NOI18N
        unCheckAllDevicesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unCheckAllDevicesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 9);
        devicesPane.add(unCheckAllDevicesButton, gridBagConstraints);

        devicesLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/image.png"))); // NOI18N
        devicesLabel.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.devicesLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        devicesPane.add(devicesLabel, gridBagConstraints);

        checkAllDevicesButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.checkAllDevicesButton.text")); // NOI18N
        checkAllDevicesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkAllDevicesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        devicesPane.add(checkAllDevicesButton, gridBagConstraints);

        devicesScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        devicesScrollPane.setMinimumSize(new java.awt.Dimension(27, 75));

        devicesListPane.setMinimumSize(new java.awt.Dimension(4, 100));
        devicesListPane.setLayout(new javax.swing.BoxLayout(devicesListPane, javax.swing.BoxLayout.Y_AXIS));
        devicesScrollPane.setViewportView(devicesListPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        devicesPane.add(devicesScrollPane, gridBagConstraints);

        deviceRequiredLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/error-icon-16.png"))); // NOI18N
        deviceRequiredLabel.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.deviceRequiredLabel.text")); // NOI18N
        deviceRequiredLabel.setForeground(new java.awt.Color(255, 0, 0));
        deviceRequiredLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        devicesPane.add(deviceRequiredLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        mainPanel.add(devicesPane, gridBagConstraints);

        accountTypesPane.setLayout(new java.awt.GridBagLayout());

        unCheckAllAccountTypesButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.unCheckAllAccountTypesButton.text")); // NOI18N
        unCheckAllAccountTypesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unCheckAllAccountTypesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 9);
        accountTypesPane.add(unCheckAllAccountTypesButton, gridBagConstraints);

        accountTypesLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/accounts.png"))); // NOI18N
        accountTypesLabel.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.accountTypesLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        accountTypesPane.add(accountTypesLabel, gridBagConstraints);

        checkAllAccountTypesButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.checkAllAccountTypesButton.text")); // NOI18N
        checkAllAccountTypesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkAllAccountTypesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        accountTypesPane.add(checkAllAccountTypesButton, gridBagConstraints);

        accountTypesScrollPane.setPreferredSize(new java.awt.Dimension(2, 200));

        accountTypeListPane.setLayout(new javax.swing.BoxLayout(accountTypeListPane, javax.swing.BoxLayout.Y_AXIS));
        accountTypesScrollPane.setViewportView(accountTypeListPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        accountTypesPane.add(accountTypesScrollPane, gridBagConstraints);

        accountTypeRequiredLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/error-icon-16.png"))); // NOI18N
        accountTypeRequiredLabel.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.accountTypeRequiredLabel.text")); // NOI18N
        accountTypeRequiredLabel.setForeground(new java.awt.Color(255, 0, 0));
        accountTypeRequiredLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        accountTypesPane.add(accountTypeRequiredLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        mainPanel.add(accountTypesPane, gridBagConstraints);

        scrollPane.setViewportView(mainPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(scrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Post an event with the new filters.
     */
    private void applyFilters() {
        CVTEvents.getCVTEventBus().post(new CVTEvents.FilterChangeEvent(getFilter(), getStartControlState(), getEndControlState()));
        needsRefresh = false;
        validateFilters();
    }

    /**
     * Get an instance of CommunicationsFilters base on the current panel state.
     *
     * @return an instance of CommunicationsFilter
     */
    protected CommunicationsFilter getFilter() {
        CommunicationsFilter commsFilter = new CommunicationsFilter();
        commsFilter.addAndFilter(getDeviceFilter());
        commsFilter.addAndFilter(getAccountTypeFilter());
        commsFilter.addAndFilter(getDateRangeFilter());
        commsFilter.addAndFilter(new CommunicationsFilter.RelationshipTypeFilter(
                ImmutableSet.of(CALL_LOG, MESSAGE)));
        return commsFilter;
    }

    /**
     * Get a DeviceFilter that matches the state of the UI widgets.
     *
     * @return a DeviceFilter
     */
    private DeviceFilter getDeviceFilter() {
        DeviceFilter deviceFilter = new DeviceFilter(
                devicesMap.entrySet().stream()
                        .filter(entry -> entry.getValue().isSelected())
                        .map(Entry::getKey)
                        .collect(Collectors.toSet()));
        return deviceFilter;
    }

    /**
     * Get an AccountTypeFilter that matches the state of the UI widgets
     *
     * @return an AccountTypeFilter
     */
    private AccountTypeFilter getAccountTypeFilter() {
        AccountTypeFilter accountTypeFilter = new AccountTypeFilter(
                accountTypeMap.entrySet().stream()
                        .filter(entry -> entry.getValue().isSelected())
                        .map(entry -> entry.getKey())
                        .collect(Collectors.toSet()));
        return accountTypeFilter;
    }

    /**
     * Get an DateRangeFilter that matches the state of the UI widgets
     *
     * @return an DateRangeFilter
     */
    private DateRangeFilter getDateRangeFilter() {
        ZoneId zone = Utils.getUserPreferredZoneId();

        return new DateRangeFilter( startCheckBox.isSelected() ? startDatePicker.getDate().atStartOfDay(zone).toEpochSecond() : 0,
                                    endCheckBox.isSelected() ? endDatePicker.getDate().atStartOfDay(zone).toEpochSecond() : 0);
    }

    private DateControlState getStartControlState() {
        return new DateControlState (startDatePicker.getDate(), startCheckBox.isSelected());
    }

    private DateControlState getEndControlState() {
        return new DateControlState (endDatePicker.getDate(), endCheckBox.isSelected());
    }

    /**
     * Enable or disable the device account type filter. The filter should be
     * disabled for the browse/table mode and enabled for the visualization.
     *
     * @param enable True to enable the device account type filter, False to
     *               disable it.
     */
    void setDeviceAccountTypeEnabled(boolean enable) {
        deviceAccountTypeEnabled = enable;
        JCheckBox deviceCheckbox = accountTypeMap.get(Account.Type.DEVICE);
        if (deviceCheckbox != null) {
            deviceCheckbox.setEnabled(deviceAccountTypeEnabled);
        }
    }

    /**
     * Set the selection state of all the account type check boxes
     *
     * @param selected The selection state to set the check boxes to.
     */
    @ThreadConfined(type = ThreadConfined.ThreadType.AWT)
    private void setAllAccountTypesSelected(boolean selected) {
        setAllSelected(accountTypeMap, selected);
    }

    /**
     * Set the selection state of all the device check boxes
     *
     * @param selected The selection state to set the check boxes to.
     */
    @ThreadConfined(type = ThreadConfined.ThreadType.AWT)
    private void setAllDevicesSelected(boolean selected) {
        setAllSelected(devicesMap, selected);
    }

    /**
     * Helper method that sets all the checkboxes in the given map to the given
     * selection state.
     *
     * @param map      A map from anything to JCheckBoxes.
     * @param selected The selection state to set all the checkboxes to.
     */
    @ThreadConfined(type = ThreadConfined.ThreadType.AWT)
    private void setAllSelected(Map<?, JCheckBox> map, boolean selected) {
        map.values().forEach(box -> box.setSelected(selected));
    }

    private void unCheckAllAccountTypesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unCheckAllAccountTypesButtonActionPerformed
        setAllAccountTypesSelected(false);
    }//GEN-LAST:event_unCheckAllAccountTypesButtonActionPerformed

    private void checkAllAccountTypesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkAllAccountTypesButtonActionPerformed
        setAllAccountTypesSelected(true);
    }//GEN-LAST:event_checkAllAccountTypesButtonActionPerformed

    private void unCheckAllDevicesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unCheckAllDevicesButtonActionPerformed
        setAllDevicesSelected(false);
    }//GEN-LAST:event_unCheckAllDevicesButtonActionPerformed

    private void checkAllDevicesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkAllDevicesButtonActionPerformed
        setAllDevicesSelected(true);
    }//GEN-LAST:event_checkAllDevicesButtonActionPerformed

    private void startCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_startCheckBoxStateChanged
        startDatePicker.setEnabled(startCheckBox.isSelected());
    }//GEN-LAST:event_startCheckBoxStateChanged

    private void endCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_endCheckBoxStateChanged
        endDatePicker.setEnabled(endCheckBox.isSelected());
    }//GEN-LAST:event_endCheckBoxStateChanged

    /**
     * A class to wrap the state of the date controls that consist of a date picker
     * and a checkbox.
     *
     */
    final class DateControlState {
        private final LocalDate date;
        private final boolean enabled;

        /**
         * Wraps the state of the date controls that consist of a date picker
         * and checkbox
         *
         * @param date LocalDate value of the datepicker
         * @param enabled State of the checkbox
         */
        protected DateControlState(LocalDate date, boolean enabled) {
            this.date = date;
            this.enabled = enabled;
        }

        /**
         * Returns the given LocalDate from the datepicker
         *
         * @return Current state LocalDate
         */
        public LocalDate getDate(){
            return date;
        }

        /**
         * Returns the given state of the datepicker checkbox
         *
         * @return boolean, whether or not the datepicker was enabled
         */
        public boolean isEnabled() {
            return enabled;
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final javax.swing.JPanel accountTypePane = new javax.swing.JPanel();
    private final javax.swing.JLabel accountTypeRequiredLabel = new javax.swing.JLabel();
    private final javax.swing.JLabel accountTypesLabel = new javax.swing.JLabel();
    private final javax.swing.JButton applyFiltersButton = new javax.swing.JButton();
    private final javax.swing.JButton checkAllAccountTypesButton = new javax.swing.JButton();
    private final javax.swing.JButton checkAllDevicesButton = new javax.swing.JButton();
    private final javax.swing.JLabel dateRangeLabel = new javax.swing.JLabel();
    private final javax.swing.JLabel deviceRequiredLabel = new javax.swing.JLabel();
    private final javax.swing.JLabel devicesLabel = new javax.swing.JLabel();
    private final javax.swing.JPanel devicesPane = new javax.swing.JPanel();
    private final javax.swing.JCheckBox endCheckBox = new javax.swing.JCheckBox();
    private final com.github.lgooddatepicker.components.DatePicker endDatePicker = new com.github.lgooddatepicker.components.DatePicker();
    private final javax.swing.JLabel filtersTitleLabel = new javax.swing.JLabel();
    private final javax.swing.JComboBox<String> limitComboBox = new javax.swing.JComboBox<>();
    private final javax.swing.JLabel limitErrorMsgLabel = new javax.swing.JLabel();
    private final javax.swing.JLabel limitHeaderLabel = new javax.swing.JLabel();
    private final javax.swing.JPanel limitPane = new javax.swing.JPanel();
    private final javax.swing.JPanel limitTitlePanel = new javax.swing.JPanel();
    private final javax.swing.JPanel mainPanel = new javax.swing.JPanel();
    private final javax.swing.JLabel mostRecentLabel = new javax.swing.JLabel();
    private final javax.swing.JLabel needsRefreshLabel = new javax.swing.JLabel();
    private final javax.swing.JButton refreshButton = new javax.swing.JButton();
    private final javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane();
    private final javax.swing.JCheckBox startCheckBox = new javax.swing.JCheckBox();
    private final com.github.lgooddatepicker.components.DatePicker startDatePicker = new com.github.lgooddatepicker.components.DatePicker();
    private final javax.swing.JPanel topPane = new javax.swing.JPanel();
    private final javax.swing.JButton unCheckAllAccountTypesButton = new javax.swing.JButton();
    private final javax.swing.JButton unCheckAllDevicesButton = new javax.swing.JButton();
    // End of variables declaration//GEN-END:variables
}
