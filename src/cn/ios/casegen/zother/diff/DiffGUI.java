package cn.ios.casegen.zother.diff;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.TableCellRenderer;

import com.csvreader.CsvReader;

public class DiffGUI {

	public static void main(String[] args) {
		JFrame jFrame = new JFrame("JDK Diff");
		jFrame.setBounds(100, 100, 1200, 500);
		jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JPanel panel = new JPanel(new BorderLayout());

		try {
			
			String fileName = "G:\\jdkCompare\\0900_Compare1208.csv";
			CsvReader csvReader = new CsvReader(fileName , ',', Charset.forName("GBK"));

			csvReader.readHeaders();
			Object[] columnNames = csvReader.getHeaders();
			int columnLength = columnNames.length + 1;
			int rowCount = getLineNumber(fileName) - 1;
			Object[][] data = new String[rowCount][columnLength];
			
			Object[] columnNamesButton = new String[columnLength];
			for (int i = 0; i < columnNames.length; i++) {
				columnNamesButton[i] = columnNames[i];
			}
			columnNamesButton[columnLength-1] = "Button";

			int i = 0;
			while (csvReader.readRecord()) {
				if (i < rowCount) {
					String[] values = csvReader.getValues();
					for (int j = 0; j < columnLength; j++) {
						if (j < values.length) {
							data[i][j] = values[j];
						} else {
							data[i][j] = ""; 
						}
					}
					i++;
				}
			}
			csvReader.close();

			JTable table = new JTable(data, columnNamesButton); // 生成一个JTable
			table.setRowSelectionAllowed(false);
			table.getColumnModel().getColumn(columnLength-1).setCellRenderer(new JTableButtonRenderer());
			table.getColumnModel().getColumn(columnLength-1).setCellEditor(new MyButtonEditor());
			
			for (i = 0; i < columnLength; i++) {
				int prefWidth = 1200 * 2/5 * 1/7;
				if(i==3) {
					prefWidth = 1200 * 3/5;
				}
				table.getColumnModel().getColumn(i).setPreferredWidth(prefWidth);
			}
			
			JScrollPane scrollPane = new JScrollPane(table); // 支持滚动
			panel.add(scrollPane, BorderLayout.CENTER);
			jFrame.setContentPane(panel);

			jFrame.setVisible(true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static class JTableButtonRenderer implements TableCellRenderer {        
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JButton button = new JButton();
            button.setText("Open");
            return button;  
        }
    }
	
	static int getLineNumber(String fileName) {

		 int i = 0;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if(!"".equals(line)) {
					i++;
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return i;
	}

}
