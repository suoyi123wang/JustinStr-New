package cn.ios.casegen.zother.diff;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class MyButtonEditor extends AbstractCellEditor implements TableCellEditor {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -6546334664166791132L;

	private JPanel panel;

	private JButton button;

	private Object value;

	private String first;

	private String second;

	private String className;

	public MyButtonEditor() {

		initButton();

		initPanel();

		panel.add(this.button, BorderLayout.CENTER);
	}

	private void initButton() {
		button = new JButton();

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UNZip.openMerge(second, first, className);
			}
		});

	}

	private void initPanel() {
		panel = new JPanel();

		panel.setLayout(new BorderLayout());
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		this.first = table.getValueAt(row, 1).toString();
		this.second = table.getValueAt(row, 2).toString();
		String methodName = table.getValueAt(row, 3).toString();
		this.className = methodName.substring(1,methodName.indexOf(":"));
		return panel;
	}

	@Override
	public Object getCellEditorValue() {
		return value;
	}

}