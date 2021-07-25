package guiCoder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataListener;

import sql.Sql;

/**
 * Class for creating a coder password dialog and returning the password.
 */
public class CoderPasswordCheckDialog {
	public Coder coder = null;
	public String password = null;

	/**
	 * Constructor with unknown coder (because the connection profile was
	 * encrypted and the coder details could not be read yet before getting the
	 * password from here).
	 */
	public CoderPasswordCheckDialog() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel pwLabel = new JLabel("Please enter the password for the coder in the connection profile.");
		panel.add(pwLabel, BorderLayout.NORTH);
		
		JPasswordField pw = new JPasswordField(20);
		panel.add(pw);
		
		@SuppressWarnings("serial")
		JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			@Override
			public void selectInitialValue() {
				pw.requestFocusInWindow();
			}
		};
		JDialog dialog = pane.createDialog(null, "Coder verification");
		dialog.setVisible(true);
		if ((int) pane.getValue() == 0) { // 0 = OK button pressed
			password = new String(pw.getPassword());
		}
	}
	
	/**
	 * Constructor with multiple coders to select from or assuming the coder
	 * saved in the connection profile in the SQL connection.
	 * 
	 * @param sql  Sql connection from which the coder(s) can be
	 *   retrieved.
	 * @param chooseCoder  boolean value indicating whether a combo box should
	 *   be created to select the coder (true) or whether the coder from the SQL
	 *   connection profile should be used (false).
	 */
	public CoderPasswordCheckDialog(Sql sql, boolean chooseCoder) {
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel questionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel pwLabel = new JLabel("Please enter the password for coder: ");
		questionPanel.add(pwLabel);
		JComboBox<Coder> comboBox = new JComboBox<Coder>();

		if (chooseCoder == true) {
			ArrayList<Coder> coders = sql.getCoders();
			CoderComboBoxModel comboBoxModel = new CoderComboBoxModel(coders);
			comboBox.setModel(comboBoxModel);
			comboBox.setRenderer(new CoderComboBoxRenderer());
			comboBox.setSelectedIndex(0);
			questionPanel.add(comboBox);
		} else {
			coder = sql.getCoder(sql.getConnectionProfile().getCoderId());
			CoderBadgePanel cdp = new CoderBadgePanel(coder);
			questionPanel.add(cdp);
		}
		panel.add(questionPanel, BorderLayout.NORTH);
		
		JPasswordField pw = new JPasswordField(20);
		panel.add(pw);
		
		@SuppressWarnings("serial")
		JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			@Override
			public void selectInitialValue() {
				pw.requestFocusInWindow();
			}
		};
		JDialog dialog = pane.createDialog(null, "Coder verification");
		dialog.setVisible(true);
		if ((int) pane.getValue() == 0) { // 0 = OK button pressed
			password = new String(pw.getPassword());
			coder = (Coder) comboBox.getSelectedItem();
		}
	}
	
	public Coder getCoder() {
		return this.coder;
	}
	
	public String getPassword() {
		return this.password;
	}

	private class CoderComboBoxRenderer implements ListCellRenderer<Object> {
		
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (value == null) {
				return new JLabel("select coder...");
			} else {
				Coder coder = (Coder) value;
				return new CoderBadgePanel(coder);
			}
		}
	}

	public class CoderComboBoxModel extends AbstractListModel<Coder> implements ComboBoxModel<Coder> {
		private static final long serialVersionUID = 8412600030500406168L;
		private Object selectedItem;
		Vector<ListDataListener> listeners = new Vector<ListDataListener>();
		ArrayList<Coder> coders;
		
		public CoderComboBoxModel(ArrayList<Coder> coders) {
			this.coders = coders;
		}
		
		@Override
		public int getSize() {
			return coders.size();
		}
		
		@Override
		public Coder getElementAt(int index) {
			return coders.get(index);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
			
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}
		
		@Override
		public void setSelectedItem(Object anItem) {
			selectedItem = anItem;
			//fireContentsChanged(this, 0, getSize() - 1);
		}
		
		@Override
		public Object getSelectedItem() {
			return selectedItem;
		}
		
		public void clear() {
			selectedItem = null;
			fireContentsChanged(this, 0, 0);
		}
	}
}