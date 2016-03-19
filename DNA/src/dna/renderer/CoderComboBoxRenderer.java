package dna.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;

import dna.dataStructures.Coder;

public class CoderComboBoxRenderer implements ListCellRenderer<Object> {
	
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value == null) {
			return new JLabel("");
		} else {
			Coder coder = (Coder) value;
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			if (isSelected) {
				UIDefaults defaults = javax.swing.UIManager.getDefaults();
				Color bg = defaults.getColor("List.selectionBackground");
				panel.setBackground(bg);
			}
			JButton colorButton = new JButton();
			colorButton.setPreferredSize(new Dimension(12, 16));
			colorButton.setBackground(coder.getColor());
			panel.add(colorButton);
			
			String name = coder.getName();
			
			int nameLength = name.length();
			if (nameLength > 22) {
				nameLength = 22 - 3;
				name = name.substring(0,  nameLength);
				name = name + "...";
			}
			
			panel.add(new JLabel(name));
			return panel;
		}
	}
}