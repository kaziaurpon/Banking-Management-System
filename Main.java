import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// User Class
class User {
    private String username;
    private String password;
    private double balance;
    private List<String> transactions;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public double getBalance() { return balance; }
    public List<String> getTransactions() { return transactions; }

    public void deposit(double amount) {
        balance += amount;
        transactions.add("Deposit: +" + amount);
    }

    public boolean withdraw(double amount) {
        if (amount > balance) return false;
        balance -= amount;
        transactions.add("Withdrawal: -" + amount);
        return true;
    }

    public boolean transfer(User recipient, double amount) {
        if (amount > balance) return false;
        balance -= amount;
        recipient.balance += amount;
        transactions.add("Transfer to " + recipient.getUsername() + ": -" + amount);
        recipient.transactions.add("Transfer from " + username + ": +" + amount);
        return true;
    }
}

// BankSystem Class
class BankSystem {
    private Map<String, User> users;

    public BankSystem() {
        users = new HashMap<>();
        // default admin
        User admin = new User("admin", "admin");
        admin.deposit(1000);
        users.put("admin", admin);
    }

    public boolean register(String username, String password) {
        if (users.containsKey(username)) return false;
        users.put(username, new User(username, password));
        return true;
    }

    public User login(String username, String password) {
        if (users.containsKey(username) && users.get(username).getPassword().equals(password)) {
            return users.get(username);
        }
        return null;
    }

    public Map<String, User> getUsers() {
        return users;
    }
}

// GUI Main Class
public class Main extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    private BankSystem bankSystem = new BankSystem();
    private User currentUser;

    // Login Panel
    private JTextField usernameField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);
    private JButton loginButton = new JButton("Login");
    private JButton registerButton = new JButton("Register");

    // Dashboard
    private JLabel balanceLabel = new JLabel("Balance: $0.00");
    private JButton depositButton = new JButton("Deposit");
    private JButton withdrawButton = new JButton("Withdraw");
    private JButton transferButton = new JButton("Transfer");
    private JButton historyButton = new JButton("Transaction History");
    private JButton viewAllBalancesButton = new JButton("View All Balances");
    private JButton logoutButton = new JButton("Logout");

    public Main() {
        setTitle("Banking Management System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Login Panel
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridy++;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        loginPanel.add(usernameField, gbc);
        gbc.gridy++;
        loginPanel.add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        loginPanel.add(loginButton, gbc);
        gbc.gridy++;
        loginPanel.add(registerButton, gbc);

        // Dashboard Panel
        JPanel dashboardPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        dashboardPanel.add(balanceLabel, gbc);
        gbc.gridy++; gbc.gridwidth = 1;
        dashboardPanel.add(depositButton, gbc);
        gbc.gridx = 1; dashboardPanel.add(withdrawButton, gbc);
        gbc.gridx = 0; gbc.gridy++;
        dashboardPanel.add(transferButton, gbc);
        gbc.gridx = 1; dashboardPanel.add(historyButton, gbc);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        dashboardPanel.add(viewAllBalancesButton, gbc);
        gbc.gridy++;
        dashboardPanel.add(logoutButton, gbc);

        mainPanel.add(loginPanel, "login");
        mainPanel.add(dashboardPanel, "dashboard");
        add(mainPanel);

        // Listeners
        loginButton.addActionListener(new LoginAction());
        registerButton.addActionListener(new RegisterAction());
        depositButton.addActionListener(new DepositAction());
        withdrawButton.addActionListener(new WithdrawAction());
        transferButton.addActionListener(new TransferAction());
        historyButton.addActionListener(new HistoryAction());
        viewAllBalancesButton.addActionListener(new ViewAllBalancesAction());
        logoutButton.addActionListener(e -> {
            currentUser = null;
            viewAllBalancesButton.setVisible(false);
            cardLayout.show(mainPanel, "login");
            usernameField.setText("");
            passwordField.setText("");
        });

        cardLayout.show(mainPanel, "login");
        setVisible(true);
    }

    private void updateBalanceLabel() {
        balanceLabel.setText("Balance: $" + String.format("%.2f", currentUser.getBalance()));
    }

    // Inner Classes for Actions
    private class LoginAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            currentUser = bankSystem.login(username, password);
            if (currentUser != null) {
                updateBalanceLabel();
                viewAllBalancesButton.setVisible("admin".equals(currentUser.getUsername()));
                cardLayout.show(mainPanel, "dashboard");
            } else {
                JOptionPane.showMessageDialog(null, "Invalid login", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class RegisterAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Fields cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (bankSystem.register(username, password)) {
                JOptionPane.showMessageDialog(null, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class DepositAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String amountStr = JOptionPane.showInputDialog("Enter deposit amount:");
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) throw new NumberFormatException();
                currentUser.deposit(amount);
                updateBalanceLabel();
                JOptionPane.showMessageDialog(null, "Deposit successful");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class WithdrawAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String amountStr = JOptionPane.showInputDialog("Enter withdrawal amount:");
            try {
                double amount = Double.parseDouble(amountStr);
                if (!currentUser.withdraw(amount)) throw new NumberFormatException();
                updateBalanceLabel();
                JOptionPane.showMessageDialog(null, "Withdrawal successful");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Invalid or insufficient funds", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class TransferAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String toUser = JOptionPane.showInputDialog("Recipient username:");
            User recipient = bankSystem.getUsers().get(toUser);
            if (recipient == null || recipient == currentUser) {
                JOptionPane.showMessageDialog(null, "Invalid recipient", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String amountStr = JOptionPane.showInputDialog("Enter transfer amount:");
            try {
                double amount = Double.parseDouble(amountStr);
                if (!currentUser.transfer(recipient, amount)) throw new NumberFormatException();
                updateBalanceLabel();
                JOptionPane.showMessageDialog(null, "Transfer successful");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Invalid or insufficient funds", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class HistoryAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            StringBuilder sb = new StringBuilder("Transaction History:\n");
            for (String t : currentUser.getTransactions()) {
                sb.append(t).append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        }
    }

    private class ViewAllBalancesAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            StringBuilder sb = new StringBuilder("All User Balances:\n");
            for (User u : bankSystem.getUsers().values()) {
                sb.append(u.getUsername()).append(": $").append(String.format("%.2f", u.getBalance())).append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        }
    }

    // Main
    public static void main(String[] args) {
        new Main();
    }
}