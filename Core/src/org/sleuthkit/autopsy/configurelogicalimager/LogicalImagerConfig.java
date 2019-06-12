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
package org.sleuthkit.autopsy.configurelogicalimager;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Logical Imager Configuration file JSON
 */
public class LogicalImagerConfig {

    @SerializedName("finalize-image-writer")
    @Expose(serialize = true) 
    private boolean finalizeImageWriter;

    @SerializedName("rule-sets")
    @Expose(serialize = true) 
    private List<LogicalImagerRuleSet> ruleSets;
    
    public LogicalImagerConfig() {
        this.finalizeImageWriter = false;
        this.ruleSets = new ArrayList<>();
    }
    
    public LogicalImagerConfig(
            boolean finalizeImageWriter,
            List<LogicalImagerRuleSet> ruleSets
    ) {
        this.finalizeImageWriter = finalizeImageWriter;
        this.ruleSets = ruleSets;
    }

    public boolean isFinalizeImageWriter() {
        return finalizeImageWriter;
    }

    public void setFinalizeImageWriter(boolean finalizeImageWriter) {
        this.finalizeImageWriter = finalizeImageWriter;
    }

    public List<LogicalImagerRuleSet> getRuleSets() {
        return ruleSets;
    }

    public void setRuleSet(List<LogicalImagerRuleSet> ruleSets) {
        this.ruleSets = ruleSets;
    }    
}
