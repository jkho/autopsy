/*
 * Autopsy Forensic Browser
 *
 * Copyright 2019 Basis Technology Corp.
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
package org.sleuthkit.autopsy.contentviewers;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

/**
 * A file content viewer for HTML files.
 */
@SuppressWarnings("PMD.SingularField") // UI widgets cause lots of false positives
final class HtmlPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private static final String TEXT_TYPE = "text/plain";
    private final JFXPanel jfxPanel = new JFXPanel();
    private WebView webView;
    private String htmlText;

    /**
     * Creates new form HtmlViewerPanel
     */
    HtmlPanel() {
        initComponents();
        Platform.runLater(() -> {
            webView = new WebView();
            //disable the context menu so they can't open linked pages by right clicking
            webView.setContextMenuEnabled(false);
            //disable java script
            webView.getEngine().setJavaScriptEnabled(false);
            //disable clicking on links 
            webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                    if (newValue == Worker.State.SUCCEEDED) {
                        disableHyperLinks();
                    }
                }
            });
            Scene scene = new Scene(webView);
            jfxPanel.setScene(scene);
            jfxPanel.setPreferredSize(htmlJPanel.getPreferredSize());
            htmlJPanel.add(jfxPanel);
        });
    }

    /**
     * Set the text pane's HTML text and refresh the view to display it.
     *
     * @param htmlText The HTML text to be applied to the text pane.
     */
    void setHtmlText(String htmlText) {
        this.htmlText = htmlText;
        refresh();
    }

    /**
     * Clear the HTML in the text pane and disable the show/hide button.
     */
    void reset() {
        Platform.runLater(() -> {
            webView.getEngine().loadContent("", TEXT_TYPE);
        });
        showImagesToggleButton.setEnabled(false);
    }

    /**
     * Cleans out input HTML string
     *
     * @param htmlInString The HTML string to cleanse
     *
     * @return The cleansed HTML String
     */
    private String cleanseHTML(String htmlInString) {
        org.jsoup.nodes.Document doc = Jsoup.parse(htmlInString);
        // remove all 'img' tags.
        doc.select("img").stream().forEach(Node::remove);
        // remove all 'span' tags, these are often images which are ads
        doc.select("span").stream().forEach(Node::remove);
        return doc.html();
    }

    /**
     * Refresh the panel to reflect the current show/hide images setting.
     */
    @Messages({
        "HtmlPanel_showImagesToggleButton_show=Show Images",
        "HtmlPanel_showImagesToggleButton_hide=Hide Images",
        "Html_text_display_error=The HTML text cannot be displayed, it may not be correctly formed HTML.",})
    private void refresh() {
        if (false == htmlText.isEmpty()) {
            try {
                if (showImagesToggleButton.isSelected()) {
                    showImagesToggleButton.setText(Bundle.HtmlPanel_showImagesToggleButton_hide());
                    Platform.runLater(() -> {
                        webView.getEngine().loadContent(htmlText);
                    });
                } else {
                    showImagesToggleButton.setText(Bundle.HtmlPanel_showImagesToggleButton_show());
                    Platform.runLater(() -> {
                        webView.getEngine().loadContent(cleanseHTML(htmlText));
                    });
                }
                showImagesToggleButton.setEnabled(true);
            } catch (Exception ignored) {
                Platform.runLater(() -> {
                    webView.getEngine().loadContent(Bundle.Html_text_display_error(), TEXT_TYPE);
                });
            }
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

        showImagesToggleButton = new javax.swing.JToggleButton();
        htmlJPanel = new javax.swing.JPanel();

        org.openide.awt.Mnemonics.setLocalizedText(showImagesToggleButton, org.openide.util.NbBundle.getMessage(HtmlPanel.class, "HtmlPanel.showImagesToggleButton.text")); // NOI18N
        showImagesToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showImagesToggleButtonActionPerformed(evt);
            }
        });

        htmlJPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(showImagesToggleButton)
                .addGap(0, 95, Short.MAX_VALUE))
            .addComponent(htmlJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(showImagesToggleButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(htmlJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void showImagesToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showImagesToggleButtonActionPerformed
        refresh();
    }//GEN-LAST:event_showImagesToggleButtonActionPerformed

    /**
     * Disable the click events on hyper links so that new pages can not be
     * opened.
     */
    private void disableHyperLinks() {
        Platform.runLater(() -> {
            Document document = webView.getEngine().getDocument();
            if (document != null) {
                NodeList nodeList = document.getElementsByTagName("a");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    ((EventTarget) nodeList.item(i)).addEventListener("click", (evt) -> {
                        evt.preventDefault();
                        evt.stopPropagation();
                    }, true);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel htmlJPanel;
    private javax.swing.JToggleButton showImagesToggleButton;
    // End of variables declaration//GEN-END:variables
}
