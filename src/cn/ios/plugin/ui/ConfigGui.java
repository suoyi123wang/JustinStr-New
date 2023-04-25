package cn.ios.plugin.ui;

import cn.ios.API;
import cn.ios.casegen.config.Config;
import cn.ios.casegen.config.GlobalCons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigGui extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField outputField = null;

    private JTextField minSizeOfSetText = null;
    private JTextField maxSizeOfSetText = null;
    private JTextField maxlengthOfStringText = null;
    private JTextField maxSizeOfCaseText = null;
    private JTextField maxTimeOfClassText = null;

    private JFileChooser fileChooser = null;

    private String classUnderTestPath = "";
    private String outputPath = "";
    private List<String> dependencies = null;

    /**
     * Create the frame.
     */
    public ConfigGui() {
        setTitle("JustinStr: String Test Case Generation Based On Regular Expressions");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 550, 520);

        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        fileChooser = newJFileChooser();

        int x = 60, y = 30, width = 300, width_s = 90, height = 30, dx = 300, dy = 50;

        Font fontSmall = new Font("Times New Roman", Font.PLAIN, 16);
        Font fontSmallBold = new Font("Times New Roman", Font.BOLD, 16);
        Font fontBold = new Font("Times New Roman", Font.BOLD, 18);

        addLable(x, y, width, height, "Required Settings: ", fontBold);

        y = y + dy;
        addLable(x, y, width, height, "JAVA_HOME: ", fontSmall);
        outputField = addTextField(x + 120, y, width, height, "", fontSmall);
        addButton(x + dx +120+ 5, y, height, height, "...", fontSmall, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> list = openFiles("Output dictionary", false, JFileChooser.DIRECTORIES_ONLY);
                if(!list.isEmpty()) {
                    outputPath = list.get(0);
                    outputField.setText(outputPath);
                }
            }
        });

        y = y + height + height;
        addLable(x, y, width, height, "Optional Settings: ", fontBold);

        // MaxLengthOfString
        y = y + dy;
        addLable(x, y, width, height, "Maximum length of string: ", fontSmall);
        maxlengthOfStringText = addTextField(x + dx, y, width_s, height, "50", fontSmall);

        // MinSizeOfSet
        y = y + dy;
        addLable(x, y, width, height, "Minimum size of Array/Collection/Map: ", fontSmall);
        minSizeOfSetText = addTextField(x + dx, y, width_s, height, "0", fontSmall);

        // MaxSizeOfSet
        y = y + dy;
        addLable(x, y, width, height, "Maximum size of Array/Collection/Map: ", fontSmall);
        maxSizeOfSetText = addTextField(x + dx, y, width_s, height, "5", fontSmall);

        // MaxSizeOfCase
        y = y + dy;
        addLable(x, y, width, height, "Maximum number of test cases per method:", fontSmall);
        maxSizeOfCaseText = addTextField(x + dx, y, width_s, height, "10", fontSmall);

        // TimeCostOfClass
        y = y + dy;
        addLable(x, y, width, height, "Maximum generation time per class(seconds):", fontSmall);
        maxTimeOfClassText = addTextField(x + dx, y, width_s, height, "60", fontSmall);

        JButton generate = addButton(310, 455, 100, 40, "Generate", fontSmallBold, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // todo other
                // 额外操作，可能比较耗时
                GlobalCons.START_TIME = System.currentTimeMillis();
                try {
                    String jrePath = fileChooser.getSelectedFile().getAbsolutePath();
                    Config.onceConfig(jrePath,null,null,Integer.parseInt(maxlengthOfStringText.getText()),
                            Integer.parseInt(minSizeOfSetText.getText()), Integer.parseInt(maxSizeOfSetText.getText()),
                            Integer.parseInt(maxSizeOfCaseText.getText()), Integer.parseInt(maxTimeOfClassText.getText()));
                    API.generateTestCaseAfterConfig();
                } catch (Exception exception){
                    showErrorDialog();
                }
                dispose();
            }
        });
        generate.setOpaque(true);
        generate.setBackground(Color.BLUE);


        // Exit
        addButton(420, 455, 90, 40, "Cancel", fontSmallBold, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    private List<String> openFiles(String text, boolean multi, int selectionMode) {
        fileChooser.setDialogTitle(text);
        fileChooser.setMultiSelectionEnabled(multi);
        fileChooser.setFileSelectionMode(selectionMode);
        List<String> files = new ArrayList<>();
        int result = fileChooser.showDialog(getParent(), "OK");
        if (result == JFileChooser.APPROVE_OPTION) {
            if (multi) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                if (selectedFiles != null && selectedFiles.length > 0) {
                    for (File file : selectedFiles) {
                        files.add(file.getAbsolutePath());
                    }
                }
            } else {
                files.add(fileChooser.getSelectedFile().getAbsolutePath());
            }
        } else if (result == JFileChooser.ERROR) {
        }
        return files;
    }

    private JFileChooser newJFileChooser() {
        JFileChooser fileChooser = new JFileChooser("");
        return fileChooser;
    }

    private JLabel addLable(int x, int y, int width, int height, String text, Font font) {
        JLabel jLabel = new JLabel(text);
        jLabel.setFont(font);
        jLabel.setBounds(x, y, width, height);
        contentPane.add(jLabel);
        return jLabel;
    }

    private JTextField addTextField(int x, int y, int width, int height, String text, Font font) {
        JTextField textField = new JTextField(text);
        textField.setBounds(x, y, width, height);
        textField.setFont(font);
        contentPane.add(textField);
        textField.setColumns(10);
        return textField;
    }

    private JButton addButton(int x, int y, int width, int height, String text, Font font, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.addActionListener(listener);
        btn.setBounds(x, y, width, height);
        btn.setFont(font);
        contentPane.add(btn);
        return btn;
    }

    public void showErrorDialog() {
        JOptionPane.showMessageDialog(this, "Invalid Configuration", "Warning", JOptionPane.WARNING_MESSAGE);
    }
}
