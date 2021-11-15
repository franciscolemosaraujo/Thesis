package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.jasypt.util.password.StrongPasswordEncryptor;

import dna.Dna;
import model.Coder;
import model.CoderRelation;

/**
 * A dialog for adding and deleting coders, editing coders' permissions, and
 * editing whose documents and statements they can view and edit.
 */
class CoderManager extends JDialog {
	private static final long serialVersionUID = -2714067204780133808L;
	private ArrayList<Coder> coderArrayList;
	private JList<Coder> coderList;
	
	private JLabel nameLabel, colorLabel, pw1Label, pw2Label;
	private JTextField nameField;
	private ColorButton colorButton;
	private JPasswordField pw1Field, pw2Field;
	
	private JCheckBox boxAddDocuments, boxEditDocuments, boxDeleteDocuments, boxImportDocuments;
	private JCheckBox boxAddStatements, boxEditStatements, boxDeleteStatements;
	private JCheckBox boxEditAttributes, boxEditRegex, boxEditStatementTypes, boxEditCoders;
	private JCheckBox boxViewOthersDocuments, boxEditOthersDocuments, boxViewOthersStatements, boxEditOthersStatements;
	
	JTable coderRelationTable;
	CoderRelationTableModel coderRelationTableModel;
	
	Coder selectedCoderCopy;
	
	private JButton reloadButton, addButton, deleteButton, applyButton;

	/**
	 * Constructor: create a new coder manager dialog.
	 */
	CoderManager() {
		this.setModal(true);
		this.setTitle("Coder manager");
		this.setLayout(new BorderLayout());
		
		// get coders from database
		coderArrayList = Dna.sql.getCoders();
		for (int i = 0; i < coderArrayList.size(); i++) {
			coderArrayList.set(i, injectDetailsIntoCoderRelation(coderArrayList.get(i), coderArrayList));
		}
		Coder[] coders = new Coder[coderArrayList.size()];
		coders = coderArrayList.toArray(coders);

		// coder list
		coderList = new JList<Coder>(coders);
		coderList.setCellRenderer(new CoderRenderer());
		coderList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		JScrollPane listScroller = new JScrollPane(coderList);
		listScroller.setPreferredSize(new Dimension(200, 600));
		coderList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					loadCoder();
				}
			}
		});
		this.add(listScroller, BorderLayout.WEST);
		
		// details panel
		nameField = new JTextField(20);
		nameField.setEnabled(false);
		nameLabel = new JLabel("Name", JLabel.TRAILING);
		nameLabel.setLabelFor(nameField);
		nameLabel.setEnabled(false);

		DocumentListener documentListener = new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				if (selectedCoderCopy != null) {
					selectedCoderCopy.setName(nameField.getText());
				}
				checkButtons();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				if (selectedCoderCopy != null) {
					selectedCoderCopy.setName(nameField.getText());
				}
				checkButtons();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				if (selectedCoderCopy != null) {
					selectedCoderCopy.setName(nameField.getText());
				}
				checkButtons();
			}
		};
		nameField.getDocument().addDocumentListener(documentListener);
		
		colorButton = new ColorButton();
		colorButton.setEnabled(false);
		colorLabel = new JLabel("Color", JLabel.TRAILING);
		colorLabel.setLabelFor(colorButton);
		colorLabel.setEnabled(false);
		colorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(CoderManager.this, "Choose color...", colorButton.getColor());
				if (newColor != null) {
					colorButton.setColor(newColor);
				}
				selectedCoderCopy.setColor(newColor);
				checkButtons();
			}
		});
		
		pw1Field = new JPasswordField(20);
		pw1Field.setEnabled(false);
		pw1Label = new JLabel("New password", JLabel.TRAILING);
		pw1Label.setLabelFor(pw1Field);
		pw1Label.setEnabled(false);
		pw2Field = new JPasswordField(20);
		pw2Field.setEnabled(false);
		pw2Label = new JLabel("Repeat new password", JLabel.TRAILING);
		pw2Label.setLabelFor(pw2Field);
		pw2Label.setEnabled(false);
		pw1Field.getDocument().addDocumentListener(documentListener);
		pw2Field.getDocument().addDocumentListener(documentListener);

		JPanel detailsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.insets = new Insets(5, 5, 0, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		detailsPanel.add(nameLabel, gbc);
		gbc.gridx = 1;
		detailsPanel.add(nameField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		detailsPanel.add(colorLabel, gbc);
		gbc.gridx = 1;
		detailsPanel.add(colorButton, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		detailsPanel.add(pw1Label, gbc);
		gbc.gridx = 1;
		detailsPanel.add(pw1Field, gbc);
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 3;
		detailsPanel.add(pw2Label, gbc);
		gbc.gridx = 1;
		detailsPanel.add(pw2Field, gbc);

		CompoundBorder borderDetails;
		borderDetails = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Coder details"));
		detailsPanel.setBorder(borderDetails);

		// permission panel
		JPanel permissionPanel = new JPanel(new GridBagLayout());
		
		boxAddDocuments = new JCheckBox("Permission to add documents");
		boxEditDocuments = new JCheckBox("Permission to edit documents");
		boxDeleteDocuments = new JCheckBox("Permission to delete documents");
		boxImportDocuments = new JCheckBox("Permission to import documents");
		boxAddStatements = new JCheckBox("Permission to add statements");
		boxEditStatements = new JCheckBox("Permission to edit statements");
		boxDeleteStatements = new JCheckBox("Permission to delete statements");
		boxEditAttributes = new JCheckBox("Permission to edit entities/attributes");
		boxEditRegex = new JCheckBox("Permission to edit regex search terms");
		boxEditStatementTypes = new JCheckBox("Permission to edit statement types");
		boxEditCoders = new JCheckBox("Permission to edit coders");
		boxViewOthersDocuments = new JCheckBox("Permission to view documents of other coders");
		boxEditOthersDocuments = new JCheckBox("Permission to edit documents of other coders");
		boxViewOthersStatements = new JCheckBox("Permission to view statements of other coders");
		boxEditOthersStatements = new JCheckBox("Permission to edit statements of other coders");
		
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals(boxAddDocuments)) {
					if (boxAddDocuments.isSelected()) {
						selectedCoderCopy.setPermissionAddDocuments(true);
					} else {
						selectedCoderCopy.setPermissionAddDocuments(false);
					}
				} else if (e.getSource().equals(boxEditDocuments)) {
					if (boxEditDocuments.isSelected()) {
						selectedCoderCopy.setPermissionEditDocuments(true);
					} else {
						selectedCoderCopy.setPermissionEditDocuments(false);
					}
				} else if (e.getSource().equals(boxDeleteDocuments)) {
					if (boxDeleteDocuments.isSelected()) {
						selectedCoderCopy.setPermissionDeleteDocuments(true);
					} else {
						selectedCoderCopy.setPermissionDeleteDocuments(false);
					}
				} else if (e.getSource().equals(boxImportDocuments)) {
					if (boxImportDocuments.isSelected()) {
						selectedCoderCopy.setPermissionImportDocuments(true);
					} else {
						selectedCoderCopy.setPermissionImportDocuments(false);
					}
				} else if (e.getSource().equals(boxAddStatements)) {
					if (boxAddStatements.isSelected()) {
						selectedCoderCopy.setPermissionAddStatements(true);
					} else {
						selectedCoderCopy.setPermissionAddStatements(false);
					}
				} else if (e.getSource().equals(boxEditStatements)) {
					if (boxEditStatements.isSelected()) {
						selectedCoderCopy.setPermissionEditStatements(true);
					} else {
						selectedCoderCopy.setPermissionEditStatements(false);
					}
				} else if (e.getSource().equals(boxDeleteStatements)) {
					if (boxDeleteStatements.isSelected()) {
						selectedCoderCopy.setPermissionDeleteStatements(true);
					} else {
						selectedCoderCopy.setPermissionDeleteStatements(false);
					}
				} else if (e.getSource().equals(boxEditAttributes)) {
					if (boxEditAttributes.isSelected()) {
						selectedCoderCopy.setPermissionEditAttributes(true);
					} else {
						selectedCoderCopy.setPermissionEditAttributes(false);
					}
				} else if (e.getSource().equals(boxEditRegex)) {
					if (boxEditRegex.isSelected()) {
						selectedCoderCopy.setPermissionEditRegex(true);
					} else {
						selectedCoderCopy.setPermissionEditRegex(false);
					}
				} else if (e.getSource().equals(boxEditStatementTypes)) {
					if (boxEditStatementTypes.isSelected()) {
						selectedCoderCopy.setPermissionEditStatementTypes(true);
					} else {
						selectedCoderCopy.setPermissionEditStatementTypes(false);
					}
				} else if (e.getSource().equals(boxEditCoders)) {
					if (boxEditCoders.isSelected()) {
						selectedCoderCopy.setPermissionEditCoders(true);
					} else {
						selectedCoderCopy.setPermissionEditCoders(false);
					}
				} else if (e.getSource().equals(boxViewOthersDocuments)) {
					if (boxViewOthersDocuments.isSelected()) {
						selectedCoderCopy.setPermissionViewOthersDocuments(true);
					} else {
						selectedCoderCopy.setPermissionViewOthersDocuments(false);
					}
				} else if (e.getSource().equals(boxEditOthersDocuments)) {
					if (boxEditOthersDocuments.isSelected()) {
						selectedCoderCopy.setPermissionEditOthersDocuments(true);
					} else {
						selectedCoderCopy.setPermissionEditOthersDocuments(false);
					}
				} else if (e.getSource().equals(boxViewOthersStatements)) {
					if (boxViewOthersStatements.isSelected()) {
						selectedCoderCopy.setPermissionViewOthersStatements(true);
					} else {
						selectedCoderCopy.setPermissionViewOthersStatements(false);
					}
				} else if (e.getSource().equals(boxEditOthersStatements)) {
					if (boxEditOthersStatements.isSelected()) {
						selectedCoderCopy.setPermissionEditOthersStatements(true);
					} else {
						selectedCoderCopy.setPermissionEditOthersStatements(false);
					}
				}
				checkButtons();
			}
		};
		boxAddDocuments.addActionListener(al);
		boxEditDocuments.addActionListener(al);
		boxDeleteDocuments.addActionListener(al);
		boxImportDocuments.addActionListener(al);
		boxAddStatements.addActionListener(al);
		boxEditStatements.addActionListener(al);
		boxDeleteStatements.addActionListener(al);
		boxEditAttributes.addActionListener(al);
		boxEditRegex.addActionListener(al);
		boxEditStatementTypes.addActionListener(al);
		boxEditCoders.addActionListener(al);
		boxViewOthersDocuments.addActionListener(al);
		boxEditOthersDocuments.addActionListener(al);
		boxViewOthersStatements.addActionListener(al);
		boxEditOthersStatements.addActionListener(al);
		
		boxAddDocuments.setEnabled(false);
		boxEditDocuments.setEnabled(false);
		boxDeleteDocuments.setEnabled(false);
		boxImportDocuments.setEnabled(false);
		boxAddStatements.setEnabled(false);
		boxEditStatements.setEnabled(false);
		boxDeleteStatements.setEnabled(false);
		boxEditAttributes.setEnabled(false);
		boxEditRegex.setEnabled(false);
		boxEditStatementTypes.setEnabled(false);
		boxEditCoders.setEnabled(false);
		boxViewOthersDocuments.setEnabled(false);
		boxEditOthersDocuments.setEnabled(false);
		boxViewOthersStatements.setEnabled(false);
		boxEditOthersStatements.setEnabled(false);
		
		GridBagConstraints g = new GridBagConstraints();
		g.weightx = 1.0;
		g.anchor = GridBagConstraints.WEST;
		g.gridx = 0;
		g.gridy = 0;
		permissionPanel.add(boxAddDocuments, g);
		g.gridy++;
		permissionPanel.add(boxEditDocuments, g);
		g.gridy++;
		permissionPanel.add(boxDeleteDocuments, g);
		g.gridy++;
		permissionPanel.add(boxImportDocuments, g);
		g.gridy++;
		permissionPanel.add(boxAddStatements, g);
		g.gridy++;
		permissionPanel.add(boxEditStatements, g);
		g.gridy++;
		permissionPanel.add(boxDeleteStatements, g);
		g.gridy++;
		permissionPanel.add(boxEditAttributes, g);
		g.gridy++;
		permissionPanel.add(boxEditRegex, g);
		g.gridy++;
		permissionPanel.add(boxEditStatementTypes, g);
		g.gridy++;
		permissionPanel.add(boxEditCoders, g);
		g.gridy++;
		permissionPanel.add(boxViewOthersDocuments, g);
		g.gridy++;
		permissionPanel.add(boxEditOthersDocuments, g);
		g.gridy++;
		permissionPanel.add(boxViewOthersStatements, g);
		g.gridy++;
		permissionPanel.add(boxEditOthersStatements, g);

		CompoundBorder borderPermissions;
		borderPermissions = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Permissions"));
		permissionPanel.setBorder(borderPermissions);

		// content panel: details and permissions
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(detailsPanel, BorderLayout.NORTH);
		contentPanel.add(permissionPanel, BorderLayout.CENTER);
		this.add(contentPanel, BorderLayout.CENTER);
		
		// coder relations panel
		JPanel coderRelationsPanel = new JPanel(new BorderLayout());
		coderRelationTableModel = new CoderRelationTableModel();
		coderRelationTable = new JTable(coderRelationTableModel);
		CoderRelationTableCellRenderer coderRelationTableCellRenderer = new CoderRelationTableCellRenderer();
		coderRelationTable.setDefaultRenderer(Coder.class, coderRelationTableCellRenderer);
		coderRelationTable.setDefaultRenderer(boolean.class, coderRelationTableCellRenderer);
		coderRelationTable.getTableHeader().setReorderingAllowed(false);
		JScrollPane coderRelationScrollPane = new JScrollPane(coderRelationTable);
		coderRelationScrollPane.setPreferredSize(new Dimension(600, 300));
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(coderRelationScrollPane, BorderLayout.CENTER);
		
		EmptyBorder tablePanelBorder = new EmptyBorder(5, 5, 5, 5);
		tablePanel.setBorder(tablePanelBorder);
		coderRelationsPanel.add(tablePanel, BorderLayout.CENTER);
		
		CompoundBorder borderRelations = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Coder relations: Whose documents/statements can the coder view/edit?"));
		coderRelationsPanel.setBorder(borderRelations);

		// monitor mouse clicks in the coder relation table to edit boolean permissions
		coderRelationTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int row = coderRelationTable.rowAtPoint(evt.getPoint());
				int col = coderRelationTable.columnAtPoint(evt.getPoint());
				if (selectedCoderCopy.getId() > 1 && row >= 0 && col > 0 && col < 5) { // do not save any changes for Admin coder (ID = 1)
					boolean newValue = !(boolean) coderRelationTable.getValueAt(row, col);
					int coderId = ((Coder) coderRelationTable.getValueAt(row, 0)).getId();
					if (col == 1) {
						selectedCoderCopy.getCoderRelations().get(coderId).setViewDocuments(newValue);
					} else if (col == 2) {
						selectedCoderCopy.getCoderRelations().get(coderId).setEditDocuments(newValue);
					} else if (col == 3) {
						selectedCoderCopy.getCoderRelations().get(coderId).setViewStatements(newValue);
					} else if (col == 4) {
						selectedCoderCopy.getCoderRelations().get(coderId).setEditStatements(newValue);
					}
					coderRelationTable.setValueAt(newValue, row, col);
					checkButtons();
				}
			}
		});
		
		this.add(coderRelationsPanel, BorderLayout.EAST);

		// button panel
		JPanel buttonPanel = new JPanel();

		ImageIcon deleteIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-user-minus.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		deleteButton = new JButton("Delete coder", deleteIcon);
		deleteButton.setToolTipText("Delete the currently selected coder.");
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!coderList.isSelectionEmpty()) {
					int coderId = coderList.getSelectedValue().getId();
					int[] counts = Dna.sql.countCoderItems(coderId);
					int dialog = JOptionPane.showConfirmDialog(CoderManager.this,
							"Delete Coder " + coderId + " from the database?\nThe coder is associated with " + counts[0] + " documents and " + counts[1] + " statements,\nwhich will also be deleted permanently.",
							"Confirmation",
							JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						boolean success = Dna.sql.deleteCoder(coderId);
						if (success) {
							coderList.clearSelection();
							repopulateCoderListFromDatabase(-1); // -1 means select no coder after loading coders
							JOptionPane.showMessageDialog(CoderManager.this, "Coder " + coderId + " was successfully deleted.");
						} else {
							JOptionPane.showMessageDialog(CoderManager.this, "Coder " + coderId + " could not be deleted. Check the message log for details.");
						}
					}
				}
			}
		});
		deleteButton.setEnabled(false);
		buttonPanel.add(deleteButton);

		ImageIcon addIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-user-plus.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		addButton = new JButton("Add new coder...", addIcon);
		addButton.setToolTipText("Create and add a new coder...");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddCoderDialog addCoderDialog = new AddCoderDialog();
				String coderName = addCoderDialog.getCoderName();
				String coderPasswordHash = addCoderDialog.getCoderPasswordHash();
				Color coderColor = addCoderDialog.getCoderColor();
				addCoderDialog.dispose();
				if (coderName != null && coderColor != null && coderPasswordHash != null) {
					int coderId = Dna.sql.addCoder(coderName, coderColor, coderPasswordHash);
					repopulateCoderListFromDatabase(coderId); // loadCoder() is executed when list selection changes...
				}
			}
		});
		buttonPanel.add(addButton);

		ImageIcon reloadIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-rotate-clockwise.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		reloadButton = new JButton("Reload coder", reloadIcon);
		reloadButton.setToolTipText("Reset all the changes made for the current coder and reload the coder details.");
		reloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadCoder();
			}
		});
		reloadButton.setEnabled(false);
		buttonPanel.add(reloadButton);

		ImageIcon applyIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-user-check.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		applyButton = new JButton("Apply / Save", applyIcon);
		applyButton.setToolTipText("Save the changes for the current coder to the database and make them effective.");
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// password
				String newPasswordHash = null;
				String plainPassword = new String(pw1Field.getPassword());
				String repeatPassword = new String(pw2Field.getPassword());
				if (!plainPassword.equals("^\\s*$") && plainPassword.equals(repeatPassword)) {
					StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
					newPasswordHash = passwordEncryptor.encryptPassword(plainPassword);
				}

				// update coder in the database
				if (newPasswordHash != null || !selectedCoderCopy.equals(coderList.getSelectedValue())) {
					int dialog = JOptionPane.showConfirmDialog(CoderManager.this, "Save changes for Coder " + selectedCoderCopy.getId() + " to the database?", "Confirmation", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						boolean success = Dna.sql.updateCoder(selectedCoderCopy, newPasswordHash);
						repopulateCoderListFromDatabase(coderList.getSelectedValue().getId());
						if (success) {
							JOptionPane.showMessageDialog(CoderManager.this, "Changes for Coder " + selectedCoderCopy.getId() + " were successfully saved.");
						} else {
							JOptionPane.showMessageDialog(CoderManager.this, "Changes for Coder " + selectedCoderCopy.getId() + " could not be saved. Check the message log for details.");
						}
					}
				}
			}
		});
		applyButton.setEnabled(false);
		buttonPanel.add(applyButton);

		ImageIcon cancelIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton cancelButton = new JButton("Close / Cancel", cancelIcon);
		cancelButton.setToolTipText("Close the coder manager without saving any changes.");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CoderManager.this.dispose();
			}
		});
		buttonPanel.add(cancelButton);

		this.add(buttonPanel, BorderLayout.SOUTH);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Load all coders from the database and put them into the GUI. A specified
	 * coder can be selected afterwards. If this is not desired, a value of
	 * {@code -1} can be provided.
	 * 
	 * @param selectCoderId  ID of the coder to be selected. Can be {@code -1}.
	 */
	private void repopulateCoderListFromDatabase(int selectCoderId) {
		DefaultListModel<Coder> model = new DefaultListModel<Coder>();
		coderArrayList = Dna.sql.getCoders();
		for (int i = 0; i < coderArrayList.size(); i++) {
			coderArrayList.set(i, injectDetailsIntoCoderRelation(coderArrayList.get(i), coderArrayList));
		}
		int coderIndex = -1;
		for (int i = 0; i < coderArrayList.size(); i++) {
			model.addElement(coderArrayList.get(i));
			if (coderArrayList.get(i).getId() == selectCoderId) {
				coderIndex = i;
			}
		}
		coderList.setModel(model);
		if (coderIndex > -1) {
			coderList.setSelectedIndex(coderIndex);
		}
	}
	
	/**
	 * Check which coder is selected and populate the details, permissions, and
	 * coder relation lists and enable or disable GUI controls.
	 */
	private void loadCoder() {
		if (!coderList.isSelectionEmpty()) { // trigger only if selection has been completed and a coder is selected
			selectedCoderCopy = new Coder(coderList.getSelectedValue());
			if (selectedCoderCopy.getId() == 1) { // do not make boxes editable if it is the Admin coder (ID = 1)
				boxAddDocuments.setEnabled(false);
				boxEditDocuments.setEnabled(false);
				boxDeleteDocuments.setEnabled(false);
				boxImportDocuments.setEnabled(false);
				boxAddStatements.setEnabled(false);
				boxEditStatements.setEnabled(false);
				boxDeleteStatements.setEnabled(false);
				boxEditAttributes.setEnabled(false);
				boxEditRegex.setEnabled(false);
				boxEditStatementTypes.setEnabled(false);
				boxEditCoders.setEnabled(false);
				boxViewOthersDocuments.setEnabled(false);
				boxEditOthersDocuments.setEnabled(false);
				boxViewOthersStatements.setEnabled(false);
				boxEditOthersStatements.setEnabled(false);
			} else {
				boxAddDocuments.setEnabled(true);
				boxEditDocuments.setEnabled(true);
				boxDeleteDocuments.setEnabled(true);
				boxImportDocuments.setEnabled(true);
				boxAddStatements.setEnabled(true);
				boxEditStatements.setEnabled(true);
				boxDeleteStatements.setEnabled(true);
				boxEditAttributes.setEnabled(true);
				boxEditRegex.setEnabled(true);
				boxEditStatementTypes.setEnabled(true);
				boxEditCoders.setEnabled(true);
				boxViewOthersDocuments.setEnabled(true);
				boxEditOthersDocuments.setEnabled(true);
				boxViewOthersStatements.setEnabled(true);
				boxEditOthersStatements.setEnabled(true);
			}
			if (selectedCoderCopy.isPermissionAddDocuments()) {
				boxAddDocuments.setSelected(true);
			} else {
				boxAddDocuments.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditDocuments()) {
				boxEditDocuments.setSelected(true);
			} else {
				boxEditDocuments.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionDeleteDocuments()) {
				boxDeleteDocuments.setSelected(true);
			} else {
				boxDeleteDocuments.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionImportDocuments()) {
				boxImportDocuments.setSelected(true);
			} else {
				boxImportDocuments.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionAddStatements()) {
				boxAddStatements.setSelected(true);
			} else {
				boxAddStatements.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditStatements()) {
				boxEditStatements.setSelected(true);
			} else {
				boxEditStatements.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionDeleteStatements()) {
				boxDeleteStatements.setSelected(true);
			} else {
				boxDeleteStatements.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditAttributes()) {
				boxEditAttributes.setSelected(true);
			} else {
				boxEditAttributes.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditRegex()) {
				boxEditRegex.setSelected(true);
			} else {
				boxEditRegex.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditStatementTypes()) {
				boxEditStatementTypes.setSelected(true);
			} else {
				boxEditStatementTypes.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditCoders()) {
				boxEditCoders.setSelected(true);
			} else {
				boxEditCoders.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionViewOthersDocuments()) {
				boxViewOthersDocuments.setSelected(true);
			} else {
				boxViewOthersDocuments.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditOthersDocuments()) {
				boxEditOthersDocuments.setSelected(true);
			} else {
				boxEditOthersDocuments.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionViewOthersStatements()) {
				boxViewOthersStatements.setSelected(true);
			} else {
				boxViewOthersStatements.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditOthersStatements()) {
				boxEditOthersStatements.setSelected(true);
			} else {
				boxEditOthersStatements.setSelected(false);
			}
			
			nameLabel.setEnabled(true);
			colorLabel.setEnabled(true);
			pw1Label.setEnabled(true);
			pw2Label.setEnabled(true);
			
			nameField.setText(selectedCoderCopy.getName());
			colorButton.setColor(selectedCoderCopy.getColor());

			if (selectedCoderCopy.getId() == 1) { // do not permit editing if it is the Admin coder (ID = 1)
				nameField.setEnabled(false);
				colorButton.setEnabled(false);
				pw1Field.setEnabled(false);
				pw2Field.setEnabled(false);
			} else {
				nameField.setEnabled(true);
				colorButton.setEnabled(true);
				pw1Field.setEnabled(true);
				pw2Field.setEnabled(true);
			}
			
			// coder relations
			coderRelationTableModel.clear();
			coderRelationTable.setEnabled(true);
			for (HashMap.Entry<Integer, CoderRelation> entry : selectedCoderCopy.getCoderRelations().entrySet()) {
				coderRelationTableModel.addRow(entry.getValue());
			}
			
			if (selectedCoderCopy.getId() == 1) { // do not permitting selecting or unselecting coders if the Admin coder is selected (ID = 1)
				coderRelationTable.setEnabled(false);
			} else {
				coderRelationTable.setEnabled(true);
			}
		} else if (coderList.isSelectionEmpty()) { // reset button was pressed
			selectedCoderCopy = null;
			
			boxAddDocuments.setEnabled(false);
			boxEditDocuments.setEnabled(false);
			boxDeleteDocuments.setEnabled(false);
			boxImportDocuments.setEnabled(false);
			boxAddStatements.setEnabled(false);
			boxEditStatements.setEnabled(false);
			boxDeleteStatements.setEnabled(false);
			boxEditAttributes.setEnabled(false);
			boxEditRegex.setEnabled(false);
			boxEditStatementTypes.setEnabled(false);
			boxEditCoders.setEnabled(false);
			boxViewOthersDocuments.setEnabled(false);
			boxEditOthersDocuments.setEnabled(false);
			boxViewOthersStatements.setEnabled(false);
			boxEditOthersStatements.setEnabled(false);
			
			boxAddDocuments.setSelected(false);
			boxEditDocuments.setSelected(false);
			boxDeleteDocuments.setSelected(false);
			boxImportDocuments.setSelected(false);
			boxAddStatements.setSelected(false);
			boxEditStatements.setSelected(false);
			boxDeleteStatements.setSelected(false);
			boxEditAttributes.setSelected(false);
			boxEditRegex.setSelected(false);
			boxEditStatementTypes.setSelected(false);
			boxEditCoders.setSelected(false);
			boxViewOthersDocuments.setSelected(false);
			boxEditOthersDocuments.setSelected(false);
			boxViewOthersStatements.setSelected(false);
			boxEditOthersStatements.setSelected(false);
			
			nameLabel.setEnabled(false);
			nameField.setEnabled(false);
			colorLabel.setEnabled(false);
			colorButton.setEnabled(false);
			pw1Label.setEnabled(false);
			pw1Field.setEnabled(false);
			pw2Label.setEnabled(false);
			pw2Field.setEnabled(false);
			
			nameField.setText("");
			colorButton.setColor(Color.BLACK);
			
			// coder relations
			coderRelationTableModel.clear();
			coderRelationTable.setEnabled(false);
		}
	}

	/**
	 * Add names and colors to the target coders in a coder's relations. The
	 * user supplies a coder whose coder relations should be fixed and an array
	 * list of coders including names and colors, and the function matches the
	 * target coders in the coder relations hash map with the coders in the
	 * array list and saves the name and color in the hash map. It then returns
	 * the coder with the fixed (= completed) coder relations hash map with all
	 * names and colors. The step is necessary because names and colors are not
	 * by default saved in a {@code model.CoderRelation CoderRelation} object.
	 * 
	 * @param c       A coder.
	 * @param coders  An array list of coders.
	 * @return        Coder with fixed coder relations including names/colors.
	 */
	private Coder injectDetailsIntoCoderRelation(Coder c, ArrayList<Coder> coders) {
		for (HashMap.Entry<Integer, CoderRelation> entry : c.getCoderRelations().entrySet()) {
			for (int i = 0; i < coders.size(); i++) {
				if (coders.get(i).getId() == entry.getKey()) {
					CoderRelation cr = entry.getValue();
					cr.setTargetCoderName(coders.get(i).getName());
					cr.setTargetCoderColor(coders.get(i).getColor());
					c.getCoderRelations().put(entry.getKey(), cr);
					break;
				}
			}
		}
		return c;
	}
	
	/**
	 * Check all the details and permissions for changes and adjust buttons.
	 */
	private void checkButtons() {
		boolean valid = true;
		if (coderList.isSelectionEmpty()) {
			valid = false;
			reloadButton.setEnabled(false);
			deleteButton.setEnabled(false);
		} else {
			reloadButton.setEnabled(true);
			if (coderList.getSelectedValue().getId() != 1) {
				deleteButton.setEnabled(true);
			}
		}
		if (coderList.getSelectedValue() != null && coderList.getSelectedValue().getId() == 1) {
			valid = false;
			deleteButton.setEnabled(false);
		}
		if (nameField.getText().matches("^\\s*$")) {
			valid = false;
		}
		
		if (selectedCoderCopy != null && selectedCoderCopy.getId() != 1 && selectedCoderCopy.equals(coderList.getSelectedValue())) {
			valid = false;
			reloadButton.setEnabled(false);
		} else if (selectedCoderCopy != null && selectedCoderCopy.getId() == 1) {
			reloadButton.setEnabled(false);
		} else if (selectedCoderCopy != null) {
			reloadButton.setEnabled(true);
		} else {
			reloadButton.setEnabled(false);
		}
		
		String plainPassword = new String(pw1Field.getPassword());
		String repeatPassword = new String(pw2Field.getPassword());
		if (plainPassword.matches("^\\s+$") || !plainPassword.equals(repeatPassword)) {
			valid = false;
			pw1Field.setForeground(Color.RED);
			pw2Field.setForeground(Color.RED);
		} else {
			pw1Field.setForeground(Color.BLACK);
			pw2Field.setForeground(Color.BLACK);
			if (!plainPassword.matches("^\\s*$")) { // activate apply button if there is a valid new password, irrespective of other coder details
				valid = true;
			}
		}

		if (valid) {
			applyButton.setEnabled(true);
		} else {
			applyButton.setEnabled(false);
		}
	}
	
	/**
	 * A custom Button for displaying and choosing a color
	 */
	private class ColorButton extends JButton {
		private static final long serialVersionUID = -8121834065246525986L;
		private Color color;
		
		public ColorButton() {
			this.color = Color.BLACK;
			this.setPreferredSize(new Dimension(18, 18));
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(color);
			g.fillRect(0, 0, 18, 18);
		}
		
		void setColor(Color color) {
			this.color = color;
			this.repaint();
		}
		
		Color getColor() {
			return this.color;
		}
	}
	
	/**
	 * A table cell renderer that can display coder relations.
	 */
	private class CoderRelationTableCellRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = -4743373298435293984L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value == null) {
				return null;
			} else if (table.convertColumnIndexToModel(column) == 0) {
        		return new CoderBadgePanel((Coder) value, 16, 14);
        	} else if ((boolean) value == true) {
        		ImageIcon eyeIconGreen = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-eye-green.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
        		return new JLabel(eyeIconGreen);
        	} else {
        		ImageIcon eyeIconRed = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-eye-off-red.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
        		return new JLabel(eyeIconRed);
        	}
		}
	}
	
	/**
	 * A table model for coder relations.
	 */
	class CoderRelationTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 6531584132561341365L;
		ArrayList<CoderRelation> coderRelations;

		/**
		 * Create a new coder relation table model.
		 */
		CoderRelationTableModel() {
			this.coderRelations = new ArrayList<CoderRelation>();
		}
		
		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return this.coderRelations.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (row > -1 && row < coderRelations.size()) {
				if (col == 0) {
					return new Coder(coderRelations.get(row).getTargetCoderId(),
							coderRelations.get(row).getTargetCoderName(),
							coderRelations.get(row).getTargetCoderColor());
				} else if (col == 1) {
					return coderRelations.get(row).isViewDocuments();
				} else if (col == 2) {
					return coderRelations.get(row).isEditDocuments();
				} else if (col == 3) {
					return coderRelations.get(row).isViewStatements();
				} else if (col == 4) {
					return coderRelations.get(row).isEditStatements();
				}
			}
			return null;
		}
		
		/**
		 * Set the value for an arbitrary table cell.
		 * 
		 * @param object The object to save for the cell.
		 * @param row    The table row.
		 * @param col    The table column.
		 */
		public void setValueAt(Object object, int row, int col) {
			if (row > -1 && row < coderRelations.size()) {
				if (col == 0) {
					coderRelations.get(row).setTargetCoderId(((Coder) object).getId());
					coderRelations.get(row).setTargetCoderName(((Coder) object).getName());
					coderRelations.get(row).setTargetCoderColor(((Coder) object).getColor());
				} else if (col == 1) {
					coderRelations.get(row).setViewDocuments((boolean) object); 
				} else if (col == 2) {
					coderRelations.get(row).setEditDocuments((boolean) object); 
				} else if (col == 3) {
					coderRelations.get(row).setViewStatements((boolean) object); 
				} else if (col == 4) {
					coderRelations.get(row).setEditStatements((boolean) object); 
				}
			}
			fireTableCellUpdated(row, col);
		}
		
		/**
		 * Is the cell editable?
		 * 
		 * @param row The table row.
		 * @param col The table column.
		 */
		public boolean isCellEditable(int row, int col) {
			if (row > -1 && row < coderRelations.size() && col > 0 && col < 5) {
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Return the name of a column.
		 * 
		 * @param column The column index of the table for which the name should be returned, starting at 0.
		 */
		public String getColumnName(int column) {
			switch(column) {
				case 0: return "Other coder";
				case 1: return "View documents";
				case 2: return "Edit documents";
				case 3: return "View statements";
				case 4: return "Edit statements";
				default: return null;
			}
		}

		/**
		 * Return a row of the table based on the internal array list of coder
		 * relations.
		 * 
		 * @param row Index of the {@link model.CoderRelation CoderRelation}
		 *   object in the array list.
		 * @return The {@link model.CoderRelation CoderRelation} object.
		 */
		public CoderRelation getRow(int row) {
			return coderRelations.get(row);
		}

		/**
		 * Which type of object (i.e., class) shall be shown in the columns?
		 * 
		 * @param col The column index of the table for which the class type
		 *   should be returned, starting at 0.
		 */
		public Class<?> getColumnClass(int col) {
			switch (col) {
				case 0: return Coder.class;
				case 1: return boolean.class;
				case 2: return boolean.class;
				case 3: return boolean.class;
				case 4: return boolean.class;
				default: return null;
			}
		}
		
		/**
		 * Remove all coder relation objects from the model.
		 */
		public void clear() {
			coderRelations.clear();
			fireTableDataChanged();
		}
		
		/**
		 * Add a new coder relation to the model.
		 * 
		 * @param cr A new {@link model.CoderRelation CoderRelation} object.
		 */
		public void addRow(CoderRelation cr) {
			coderRelations.add(cr);
			fireTableRowsInserted(coderRelations.size() - 1, coderRelations.size() - 1);
		}
	}
	
	/**
	 * A dialog window for adding a new coder. It contains a simple form for the
	 * name, color, and password of the new coder as well as two buttons.
	 */
	private class AddCoderDialog extends JDialog {
		private static final long serialVersionUID = 2704900961177249691L;
		private JButton addOkButton, addCancelButton;
		private JTextField addNameField;
		private JPasswordField addPw1Field, addPw2Field;
		private String name;
		private Color color;
		private String passwordHash;
		
		/**
		 * Create a new instance of an add coder dialog.
		 */
		AddCoderDialog() {
			this.setModal(true);
			this.setTitle("Add new coder");
			this.setLayout(new BorderLayout());
			
			JLabel addNameLabel = new JLabel("Name");
			addNameField = new JTextField(20);
			addNameLabel.setLabelFor(addNameField);
			addNameLabel.setToolTipText("Enter the name of a new coder here.");
			addNameField.setToolTipText("Enter the name of a new coder here.");
			addNameField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent arg0) {
					checkAddButtons();
				}
				@Override
				public void insertUpdate(DocumentEvent arg0) {
					checkAddButtons();
				}
				@Override
				public void removeUpdate(DocumentEvent arg0) {
					checkAddButtons();
				}
			});

			ColorButton addColorButton = new ColorButton();
			addColorButton.setColor(new Color(28, 165, 186));
			JLabel addColorLabel = new JLabel("Color", JLabel.TRAILING);
			addColorLabel.setLabelFor(addColorButton);
			addColorButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Color newColor = JColorChooser.showDialog(AddCoderDialog.this, "Choose color...", colorButton.getColor());
					if (newColor != null) {
						addColorButton.setColor(newColor);
					}
				}
			});
			
			addPw1Field = new JPasswordField(20);
			JLabel addPw1Label = new JLabel("Password", JLabel.TRAILING);
			addPw1Label.setLabelFor(addPw1Field);
			addPw2Field = new JPasswordField(20);
			JLabel addPw2Label = new JLabel("Repeat password", JLabel.TRAILING);
			addPw2Label.setLabelFor(addPw2Field);
			DocumentListener addPwListener = new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent arg0) {
					checkAddButtons();
				}
				@Override
				public void insertUpdate(DocumentEvent arg0) {
					checkAddButtons();
				}
				@Override
				public void removeUpdate(DocumentEvent arg0) {
					checkAddButtons();
				}
			};
			addPw1Field.getDocument().addDocumentListener(addPwListener);
			addPw2Field.getDocument().addDocumentListener(addPwListener);

			JPanel formPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.weightx = 1.0;
			gbc.insets = new Insets(5, 5, 0, 5);
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridx = 0;
			gbc.gridy = 0;
			formPanel.add(addNameLabel, gbc);
			gbc.gridx = 1;
			formPanel.add(addNameField, gbc);
			gbc.gridx = 0;
			gbc.gridy = 1;
			formPanel.add(addColorLabel, gbc);
			gbc.gridx = 1;
			formPanel.add(addColorButton, gbc);
			gbc.gridx = 0;
			gbc.gridy = 2;
			formPanel.add(addPw1Label, gbc);
			gbc.gridx = 1;
			formPanel.add(addPw1Field, gbc);
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.gridx = 0;
			gbc.gridy = 3;
			formPanel.add(addPw2Label, gbc);
			gbc.gridx = 1;
			formPanel.add(addPw2Field, gbc);
			this.add(formPanel, BorderLayout.CENTER);
			
			JPanel addButtonPanel = new JPanel();
			ImageIcon addCancelIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
			addCancelButton = new JButton("Cancel", addCancelIcon);
			addCancelButton.setToolTipText("Close without adding a new coder.");
			addCancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AddCoderDialog.this.dispose();
				}
			});
			addButtonPanel.add(addCancelButton);

			ImageIcon addOkIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-check.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
			addOkButton = new JButton("OK", addOkIcon);
			addOkButton.setToolTipText("Add this new coder to the database.");
			addOkButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AddCoderDialog.this.name = addNameField.getText();
					AddCoderDialog.this.color = addColorButton.getColor();
					String addPlainPassword = new String(addPw1Field.getPassword());
					StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
					AddCoderDialog.this.passwordHash = passwordEncryptor.encryptPassword(addPlainPassword);
					AddCoderDialog.this.dispose();
				}
			});
			addOkButton.setEnabled(false);
			addButtonPanel.add(addOkButton);

			this.add(addButtonPanel, BorderLayout.SOUTH);
			
			this.pack();
			this.setLocationRelativeTo(null);
			this.setVisible(true);
		}
		
		/**
		 * Check if all the input is valid and enable or disable the OK button.
		 */
		private void checkAddButtons() {
			boolean valid = true;
			if (addNameField.getText().matches("^\\s*$")) {
				valid = false;
			}
			String p1 = new String(addPw1Field.getPassword());
			String p2 = new String(addPw2Field.getPassword());
			if (p1.matches("^\\s*$") || !p1.equals(p2)) {
				addPw1Field.setForeground(Color.RED);
				addPw2Field.setForeground(Color.RED);
				valid = false;
			} else {
				addPw1Field.setForeground(Color.BLACK);
				addPw2Field.setForeground(Color.BLACK);
			}
			if (valid) {
				addOkButton.setEnabled(true);
			} else {
				addOkButton.setEnabled(false);
			}
		}
		
		/**
		 * Retrieve the new coder name. Accessible from outside the dialog.
		 * 
		 * @return Name of the new coder.
		 */
		String getCoderName() {
			return this.name;
		}
		
		/**
		 * Retrieve the encrypted password hash String. Accessible from outside
		 * the dialog.
		 * 
		 * @return The hash String corresponding to the entered password.
		 */
		String getCoderPasswordHash() {
			return this.passwordHash;
		}
		
		/**
		 * Retrieve the selected color. Accessible from outside the dialog.
		 * 
		 * @return The selected color.
		 */
		Color getCoderColor() {
			return this.color;
		}
	}
}