package cashflowminimizergui;

import javax.swing.*;
import java.util.*;

class Person {
    public String name;
    public int netAmount;
    public Set<String> paymentModes;

    public Person(String name) {
        this.name = name;
        this.netAmount = 0;
        this.paymentModes = new HashSet<>();
    }
}

class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }
}

public class CashFlowMinimizerGUI {

    private static JFrame frame;
    private static JTextField numPersonsField;
    private static JTextField numTransactionsField;
    private static JTextArea outputTextArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Cash Flow Minimizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        numPersonsField = new JTextField(10);
        numPersonsField.setAlignmentX(JTextField.LEFT_ALIGNMENT);
        panel.add(new JLabel("Enter the number of persons:"));
        panel.add(numPersonsField);

        numTransactionsField = new JTextField(10);
        numTransactionsField.setAlignmentX(JTextField.LEFT_ALIGNMENT);
        panel.add(new JLabel("Enter the number of transactions:"));
        panel.add(numTransactionsField);

        JButton calculateButton = new JButton("Calculate Cash Flow");
        calculateButton.setAlignmentX(JButton.LEFT_ALIGNMENT);
        calculateButton.addActionListener(e -> performCashFlowCalculation());
        panel.add(calculateButton);

        outputTextArea = new JTextArea(20, 40);
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        scrollPane.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
        panel.add(scrollPane);

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void performCashFlowCalculation() {
        try {
            int numPersons = Integer.parseInt(numPersonsField.getText());
            int numTransactions = Integer.parseInt(numTransactionsField.getText());

            Person[] persons = new Person[numPersons];

            for (int i = 0; i < numPersons; i++) {
                String name = JOptionPane.showInputDialog("Enter name for Person " + (i + 1) + ":");
                persons[i] = new Person(name);

                int numModes = Integer.parseInt(JOptionPane.showInputDialog("Enter number of payment modes for " +
                        persons[i].name + ":"));

                for (int j = 0; j < numModes; j++) {
                    String mode = JOptionPane.showInputDialog("Enter Payment mode " + (j + 1) + " for " +
                            persons[i].name + ":");
                    persons[i].paymentModes.add(mode);
                }
            }

            for (int i = 0; i < numTransactions; i++) {
                String debtor = JOptionPane.showInputDialog("Enter debtor for Transaction " + (i + 1) + ":");
                String creditor = JOptionPane.showInputDialog("Enter creditor for Transaction " + (i + 1) + ":");
                int amount = Integer.parseInt(JOptionPane.showInputDialog("Enter amount for Transaction " + (i + 1) + ":"));

                int debtorIndex = Arrays.asList(persons).indexOf(Arrays.stream(persons)
                        .filter(person -> person.name.equals(debtor))
                        .findFirst()
                        .orElse(null));

                int creditorIndex = Arrays.asList(persons).indexOf(Arrays.stream(persons)
                        .filter(person -> person.name.equals(creditor))
                        .findFirst()
                        .orElse(null));

                persons[debtorIndex].netAmount -= amount;
                persons[creditorIndex].netAmount += amount;
            }

            settleCashFlow(persons);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid input. Please enter valid numbers.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void settleCashFlow(Person[] persons) {
        int numPersons = persons.length;

        while (true) {
            Pair<Integer, Integer> indices = getMinAndMaxIndices(persons);
            int minIndex = indices.getKey();
            int maxIndex = indices.getValue();

            if (persons[minIndex].netAmount == 0) {
                outputTextArea.append("\nCash Flow settled successfully.");
                break;
            }

            Pair<Integer, String> maxIndexWithCommonMode = getMaxIndexWithCommonPaymentMode(persons, minIndex);
            int creditorIndex = maxIndexWithCommonMode.getKey();
            String commonMode = maxIndexWithCommonMode.getValue();

            int transactionAmount = Math.min(Math.abs(persons[minIndex].netAmount), persons[creditorIndex].netAmount);

            persons[minIndex].netAmount += transactionAmount;
            persons[creditorIndex].netAmount -= transactionAmount;

            outputTextArea.append("\n" + persons[minIndex].name + " pays Rs " + transactionAmount + " to " +
                    persons[creditorIndex].name + " via " + commonMode);
        }
    }

    private static Pair<Integer, Integer> getMinAndMaxIndices(Person[] persons) {
        int minIndex = getMinIndex(persons);
        int maxIndex = getMaxIndex(persons, minIndex);
        return new Pair<>(minIndex, maxIndex);
    }

    private static int getMinIndex(Person[] persons) {
        int min = Integer.MAX_VALUE, minIndex = -1;
        for (int i = 0; i < persons.length; i++) {
            if (persons[i].netAmount < min) {
                min = persons[i].netAmount;
                minIndex = i;
            }
        }
        return minIndex;
    }

    private static int getMaxIndex(Person[] persons, int minIndex) {
        int max = Integer.MIN_VALUE, maxIndex = -1;
        for (int i = 0; i < persons.length; i++) {
            if (i != minIndex && persons[i].netAmount > max) {
                max = persons[i].netAmount;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private static Pair<Integer, String> getMaxIndexWithCommonPaymentMode(Person[] persons, int minIndex) {
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        String matchingPaymentMode = "";

        for (int i = 0; i < persons.length; i++) {
            if (i != minIndex && persons[i].netAmount > 0) {
                List<String> commonModes = new ArrayList<>(persons[minIndex].paymentModes);
                commonModes.retainAll(persons[i].paymentModes);

                if (!commonModes.isEmpty() && persons[i].netAmount > max) {
                    max = persons[i].netAmount;
                    maxIndex = i;
                    matchingPaymentMode = commonModes.get(0);
                }
            }
        }

        return new Pair<>(maxIndex, matchingPaymentMode);
    }
}