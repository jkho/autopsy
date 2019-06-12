/*
 * Autopsy Forensic Browser
 *
 * Copyright 2018-2018 Basis Technology Corp.
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
package org.sleuthkit.autopsy.texttranslation;

/**
 * Interface for creating text translators. Implementing classes will be picked
 * up and run by the Text Translation Service.
 */
public interface TextTranslator {

    String translate(String input) throws TranslationException;

    /**
     * Get the name of the TextTranslator implementation
     *
     * @return the name of the TextTranslator
     */
    String getName();

    /**
     * Get the component to display on the settings options panel when this
     * TextTranslator is selected
     *
     * @return the component which displays the settings options
     */
    Component getComponent();

    /**
     * Save the settings as they have been modified in the component.
     */
    void saveSettings();

    /**
     * Returns the hard limit for translation request sizes.
     *
     * @return
     */
    int getMaxPayloadSize();
}
