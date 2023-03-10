package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import dna.Dna;
import logger.Logger.LogListener;
import logger.LoggerDialog;
import sql.Sql;

/**
 * A status bar panel showing the database on the left and messages on the right. 
 */
class StatusBar extends JPanel implements LogListener {
	private static final long serialVersionUID = -1987834394140569531L;
	JLabel urlLabel, documentRefreshIconLabel, statementRefreshIconLabel;
	JProgressBar documentProgressBar, statementProgressBar;
	int numWarnings, numErrors;
	JButton messageIconButton, warningButton, errorButton;
	
	/**
	 * Create a new status bar.
	 */
	public StatusBar() {
		this.setLayout(new BorderLayout());
		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ImageIcon openDatabaseIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-database.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		JButton databaseButton = new JButton(openDatabaseIcon);

		databaseButton.setContentAreaFilled(false);
		databaseButton.setBorderPainted(false);
		databaseButton.setBorder(null);
		databaseButton.setMargin(new Insets(0, 0, 0, 0));
		leftPanel.add(databaseButton);
		urlLabel = new JLabel("");
		leftPanel.add(urlLabel);
		this.add(leftPanel, BorderLayout.WEST);
		
		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		ImageIcon refreshIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-refresh.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		documentRefreshIconLabel = new JLabel(refreshIcon);
		documentRefreshIconLabel.setVisible(false);
		rightPanel.add(documentRefreshIconLabel);
		
		documentProgressBar = new JProgressBar();
	    documentProgressBar.setIndeterminate(true);
	    documentProgressBar.setString("Reloading documents...");
	    documentProgressBar.setStringPainted(true);
	    documentProgressBar.setVisible(false);
	    rightPanel.add(documentProgressBar);
	    
		statementRefreshIconLabel = new JLabel(refreshIcon);
		statementRefreshIconLabel.setVisible(false);
		rightPanel.add(statementRefreshIconLabel);
		
		statementProgressBar = new JProgressBar();
		statementProgressBar.setIndeterminate(true);
		statementProgressBar.setString("Reloading statements...");
		statementProgressBar.setStringPainted(true);
		statementProgressBar.setVisible(false);
	    rightPanel.add(statementProgressBar);
		
		ImageIcon messageIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-bug.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		messageIconButton = new JButton(messageIcon);
		messageIconButton.setContentAreaFilled(false);
		messageIconButton.setBorderPainted(false);
		messageIconButton.setBorder(null);
		messageIconButton.setMargin(new Insets(0, 0, 0, 0));
		numWarnings = 0;
		numErrors = 0;
		
		errorButton = new JButton(numErrors + "");
		errorButton.setContentAreaFilled(false);
		errorButton.setBorderPainted(false);
		errorButton.setForeground(new Color(153, 0, 0));
		errorButton.setBorder(null);
		errorButton.setMargin(new Insets(0, 0, 0, 0));
		errorButton.setVisible(false);
		
		warningButton = new JButton(numWarnings + "");
		warningButton.setContentAreaFilled(false);
		warningButton.setBorderPainted(false);
		warningButton.setForeground(new Color(220, 153, 0));
		warningButton.setBorder(null);
		warningButton.setMargin(new Insets(0, 0, 0, 0));
		warningButton.setVisible(false);

		ActionListener messageButtonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new LoggerDialog();
			}
		};
		messageIconButton.addActionListener(messageButtonListener);
		errorButton.addActionListener(messageButtonListener);
		warningButton.addActionListener(messageButtonListener);
		
		rightPanel.add(messageIconButton);
		rightPanel.add(errorButton);
		rightPanel.add(warningButton);
		this.add(rightPanel, BorderLayout.EAST);
	}
	
	/**
	 * Read the database URL from the {@link Sql} object and update it in
	 * the status bar. Show an empty string if no database is open.
	 */
	public void updateUrl() {
		if (Dna.sql.getConnectionProfile() == null) {
			this.urlLabel.setText("");
			this.urlLabel.setVisible(false);
		} else {
			this.urlLabel.setText(Dna.sql.getConnectionProfile().getUrl());
			this.urlLabel.setVisible(true);
		}
	}
	
	/**
	 * Refresh the count of warnings and errors. The respective
	 * count is only shown if it is greater than zero.
	 *  
	 * @param warnings Number of new warnings in the logger.
	 * @param errors Number of new errors in the logger.
	 */
	public void updateLog(int warnings, int errors) {
		this.numWarnings = warnings;
		if (this.numWarnings == 0) {
			warningButton.setVisible(false);
		} else if (this.numWarnings == 1) {
			warningButton.setVisible(true);
			warningButton.setText(this.numWarnings + " warning");
		} else {
			warningButton.setVisible(true);
			warningButton.setText(this.numWarnings + " warnings");
		}
		
		this.numErrors = errors;
		if (this.numErrors == 0) {
			errorButton.setVisible(false);
		} else if (this.numErrors == 1) {
			errorButton.setVisible(true);
			errorButton.setText(this.numErrors + " error");
		} else {
			errorButton.setVisible(true);
			errorButton.setText(this.numErrors + " errors");
		}
	}

	/**
	 * Start displaying a statement refresh message in the status bar.
	 */
	public void statementRefreshStart() {
		this.statementRefreshIconLabel.setVisible(true);
		this.statementProgressBar.setVisible(true);
	}

	/**
	 * Stop displaying the statement refresh message in the status bar.
	 */
	public void statementRefreshEnd() {
		this.statementRefreshIconLabel.setVisible(false);
		this.statementProgressBar.setVisible(false);
	}

	/**
	 * Start displaying a document refresh message in the status bar.
	 */
	public void documentRefreshStart() {
		this.documentRefreshIconLabel.setVisible(true);
		this.documentProgressBar.setVisible(true);
	}

	/**
	 * Stop displaying the document refresh message in the status bar.
	 */
	public void documentRefreshEnd() {
		this.documentRefreshIconLabel.setVisible(false);
		this.documentProgressBar.setVisible(false);
	}

	/**
	 * Check if a document or statement refresh is in progress, meaning that
	 * either a document or a statement swing worker is still running and a
	 * status bar message is being displayed.
	 * 
	 * @return  Boolean indicating if a refresh swing worker is running.
	 */
	public boolean isRefreshInProgress() {
		if (this.documentProgressBar.isVisible() || this.statementProgressBar.isVisible()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Listen to changes in the Logger in DNA and respond by updating the event
	 * counts in the status bar.
	 */
	@Override
	public void processLogEvents() {
		int numWarnings = 0;
		int numErrors = 0;
		for (int i = 0; i < Dna.logger.getRowCount(); i++) {
			if (Dna.logger.getRow(i).getPriority() == 2) {
				numWarnings++;
			} else if (Dna.logger.getRow(i).getPriority() == 3) {
				numErrors++;
			}
		}
		updateLog(numWarnings, numErrors);
	}
}