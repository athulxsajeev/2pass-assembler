import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class Pass2AssemblerGUI {
    private JFrame frame;

   
    private JTextArea optabField;
    private JTextArea inputField;

    
    private JTextArea outputFileField;
    private JTextArea intermediateFileField;
    private JTextArea symtabFileField;
    private JTextArea finalOutputField;

    private JButton assembleButton;
    private JButton clearButton;

    public Pass2AssemblerGUI() {
        frame = new JFrame("Pass 2 Assembler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout(10, 10)); 

        
        JPanel inputPanel = new JPanel(new GridLayout(4, 1, 10, 10)); 
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));

        
        inputPanel.add(new JLabel("OPTAB:"));
        optabField = new JTextArea(5, 20);
        inputPanel.add(new JScrollPane(optabField));

        inputPanel.add(new JLabel("Input File:"));
        inputField = new JTextArea(5, 20);
        inputPanel.add(new JScrollPane(inputField));

       
        JPanel outputPanel = new JPanel(new GridLayout(2, 2, 10, 10)); 
        outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));

        
        JPanel outputFilePanel = createLabeledOutputPanel("Output File:", outputFileField = new JTextArea(5, 20));
        JPanel intermediateFilePanel = createLabeledOutputPanel("Intermediate File:",
                intermediateFileField = new JTextArea(5, 20));
        JPanel symtabFilePanel = createLabeledOutputPanel("SYMTAB:", symtabFileField = new JTextArea(5, 20));
        JPanel finalOutputPanel = createLabeledOutputPanel("Final Output:", finalOutputField = new JTextArea(5, 20));

       
        outputPanel.add(outputFilePanel);
        outputPanel.add(intermediateFilePanel);
        outputPanel.add(symtabFilePanel);
        outputPanel.add(finalOutputPanel);

        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        assembleButton = new JButton("Assemble");
        clearButton = new JButton("Clear");
        buttonPanel.add(assembleButton);
        buttonPanel.add(clearButton);

        
        frame.add(inputPanel, BorderLayout.WEST); 
        frame.add(outputPanel, BorderLayout.CENTER); 
        frame.add(buttonPanel, BorderLayout.SOUTH); 

        
        assembleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               
                String optab = optabField.getText();
                String input = inputField.getText();

                
                String[] pass1Result = pass1(input, optab);

                String[] output = pass2(pass1Result[0], pass1Result[1], optab);

                intermediateFileField.setText(pass1Result[0]);
                symtabFileField.setText(pass1Result[1]);
                outputFileField.setText(output[0]); 
                finalOutputField.setText(output[1]); 
            }
        });

       
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                optabField.setText("");
                inputField.setText("");
                outputFileField.setText("");
                intermediateFileField.setText("");
                symtabFileField.setText("");
                finalOutputField.setText("");
            }
        });

        
        frame.setVisible(true);
    }

    
    private JPanel createLabeledOutputPanel(String labelText, JTextArea textArea) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(labelText);
        textArea.setEditable(false);
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }

   
    private String[] pass1(String input, String optab) {
        StringBuilder intermediate = new StringBuilder();
        StringBuilder symtab = new StringBuilder();
    
        String[] inputLines = input.split("\n");
        String[] optabLines = optab.split("\n");
    
        int locctr = 0;
        boolean started = false;
    
        for (int i = 0; i < inputLines.length; i++) {
            String line = inputLines[i].trim(); // Trim whitespace
            if (line.isEmpty()) continue; // Skip empty lines
    
            String[] tokens = line.split("\\s+");
            if (tokens.length == 0) continue; // Skip empty lines
    
            if (i == 0 && line.contains("START")) {
                locctr = Integer.parseInt(tokens[2], 16);
                started = true; // Mark that we have started
                intermediate.append(Integer.toHexString(locctr).toUpperCase()).append("\t")
                            .append(String.join("\t", tokens)).append("\n");
                continue;
            }
    
            if (!started) {
                locctr = 0; // If we have not encountered a START, initialize locctr to 0
            }
    
            String label = tokens.length > 0 ? tokens[0] : "-";
            String opcode = tokens.length > 1 ? tokens[1] : "-";
            
            // Append current locctr to intermediate output
            intermediate.append(Integer.toHexString(locctr).toUpperCase()).append("\t")
                        .append(String.join("\t", tokens)).append("\n");
    
            boolean found = false;
            for (String optabEntry : optabLines) {
                String[] optabTokens = optabEntry.split("\\s+");
                if (optabTokens[0].equals(opcode)) {
                    locctr += 3; 
                    found = true;
                    break;
                }
            }
    
            if (!found) {
                if (opcode.equals("WORD")) {
                    locctr += 3;
                } else if (opcode.equals("RESW")) {
                    locctr += 3 * Integer.parseInt(tokens[2]);
                } else if (opcode.equals("RESB")) {
                    locctr += Integer.parseInt(tokens[2]);
                } else if (opcode.equals("BYTE")) {
                    locctr += tokens[2].length() - 3;
                }
            }
    
            if (!label.equals("-")) {
                symtab.append(label).append("\t").append(Integer.toHexString(locctr).toUpperCase()).append("\n");
            }
        }
    
        return new String[] { intermediate.toString(), symtab.toString() };
    }
    

   
    private String[] pass2(String intermediate, String symtab, String optab) {
        StringBuilder output = new StringBuilder();
        String[] intermediateLines = intermediate.split("\n");
        String[] optabLines = optab.split("\n");
        String[] symtabLines = symtab.split("\n");

        List<String[]> objectCodeArr = new ArrayList<>();
        List<String[]> intermediateArr = new ArrayList<>();
        List<String[]> symtabArr = new ArrayList<>();

       
        for (String line : intermediateLines) {
            
            String[] tokens = line.split("\\s+");
            intermediateArr.add(tokens);
        }

        for (String line : symtabLines) {
            String[] tokens = line.split("\\s+");
            symtabArr.add(tokens);
        }

        for (String line : optabLines) {
            String[] tokens = line.split("\\s+");
            objectCodeArr.add(tokens);
        }

        String[] x = pass2Local(objectCodeArr, intermediateArr, symtabArr);

        for (String line : intermediateLines) {
            String[] tokens = line.split("\\s+");
            if (tokens.length < 3)
                continue;

            String address = tokens[0];
            String opcode = tokens[2];
            String operand = tokens.length > 3 ? tokens[3] : null;

            String objectCode = "";
            for (String optabEntry : optabLines) {
                String[] optabTokens = optabEntry.split("\\s+");
                if (optabTokens[0].equals(opcode)) {
                    objectCode = optabTokens[1]; 
                    break;
                }
            }

            if (operand != null && !operand.equals("-")) {
                for (String symtabEntry : symtabLines) {
                    String[] symtabTokens = symtabEntry.split("\\s+");
                    if (symtabTokens[0].equals(operand)) {
                        objectCode += symtabTokens[1];
                        break;
                    }
                }
            }

            output.append(address).append("\t").append(String.join("\t", tokens)).append("\t").append(objectCode)
                    .append("\n");
        }

        return new String[] { output.toString(), x[1] };
    }

    public static String[] pass2Local(List<String[]> optabArr, List<String[]> intermediateArr,
        List<String[]> symtabArr) {
        int i = 1;
        String objectCode = "";
        List<String> objectCodeArr = new ArrayList<>();

        for (String[] symLine : symtabArr) {
            System.out.println(symLine[0] + " " + symLine[1]);
        }

        for (String[] opLine : optabArr) {
            System.out.println(opLine[0] + " " + opLine[1]);
        }

        for (String[] interLine : intermediateArr) {
            System.out.println(interLine[0] + " " + interLine[1] + " " + interLine[2] + " " + interLine[3]);
        }

        while (i < intermediateArr.size() && !intermediateArr.get(i)[2].equals("END")) {
            boolean found = false;

            for (String[] opLine : optabArr) {
                if (opLine[0].equals(intermediateArr.get(i)[2])) {
                    found = true;
                    objectCode = opLine[1];

                    for (String[] symLine : symtabArr) {
                        if (symLine[0].equals(intermediateArr.get(i)[3])) {
                            objectCode += symLine[1];
                            objectCodeArr.add(objectCode);
                        }
                    }
                }
            }

            if (!found) {
                switch (intermediateArr.get(i)[2]) {
                    case "WORD":
                        int val = Integer.parseInt(intermediateArr.get(i)[3]);
                        objectCode = String.format("%06X", val); 
                        objectCodeArr.add(objectCode);
                        break;

                    case "BYTE":
                        String byteVal = intermediateArr.get(i)[3].substring(2, intermediateArr.get(i)[3].length() - 1);
                        objectCode = "";
                        for (char c : byteVal.toCharArray()) {
                            objectCode += String.format("%02X", (int) c);
                        }
                        objectCodeArr.add(objectCode);
                        break;

                    case "RESW":
                    case "RESB":
                        objectCode = "\t";
                        objectCodeArr.add(objectCode);
                        break;
                }
            }
            i++;
        }
        objectCodeArr.add("\t");

       
        StringBuilder output = new StringBuilder(intermediateArr.get(0)[0] + "\t" + intermediateArr.get(0)[1] + "\t"
                + intermediateArr.get(0)[2] + "\t" + intermediateArr.get(0)[3] + "\n");
        for (int j = 1; j < intermediateArr.size(); j++) {
            output.append(intermediateArr.get(j)[0])
                    .append("\t")
                    .append(intermediateArr.get(j)[1])
                    .append("\t")
                    .append(intermediateArr.get(j)[2])
                    .append("\t")
                    .append(intermediateArr.get(j)[3])
                    .append("\t")
                    .append(objectCodeArr.get(j - 1))
                    .append("\n");
        }

       
        int lower = Integer.parseInt(intermediateArr.get(1)[0], 16);
        int upper = Integer.parseInt(intermediateArr.get(intermediateArr.size() - 1)[0], 16);
        int length = upper - lower;

        StringBuilder output2 = new StringBuilder(
                "H^" + String.format("%-6s", intermediateArr.get(0)[1]).replace(' ', '_') + "^"
                        + intermediateArr.get(1)[0] + "^" + String.format("%06X", length) + "\n\n");

        int lines = intermediateArr.size() - 1, x = 1, size = 0;
        boolean keri = false;
        String text = "", start = intermediateArr.get(x)[0];

        while (x < intermediateArr.size()) {
            keri = false;
            if (objectCodeArr.get(x - 1).equals("\t")) {
                x++;
                continue;
            }
            text += "^" + objectCodeArr.get(x - 1);
            size += objectCodeArr.get(x - 1).length() / 2;

            if (size > 21) {
                keri = true;
                size -= objectCodeArr.get(x - 1).length() / 2;
                text = text.substring(0, text.length() - objectCodeArr.get(x - 1).length() - 1);
                output2.append("T^").append(start).append("^").append(String.format("%02X", size)).append(text)
                        .append("\n");
                start = intermediateArr.get(x)[0];
                text = "";
                size = 0;
                continue;
            }
            x++;
        }

        if (!keri) {
            output2.append("T^").append(start).append("^").append(String.format("%02X", size)).append(text)
                    .append("\n\n");
        }

        output2.append("E^").append(intermediateArr.get(1)[0]);

        for (String[] symLine : symtabArr) {
            if (symLine.length > 2 && symLine[2].equals("1")) {
                output = new StringBuilder("AUGEYSTOOOO");
                output2 = new StringBuilder("AUGEYSTOOOO");
            }
        }

        return new String[] { output.toString(), output2.toString() };
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Pass2AssemblerGUI());
    }
}