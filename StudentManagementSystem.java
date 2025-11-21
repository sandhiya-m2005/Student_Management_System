import javax.swing.*;  
import javax.swing.table.DefaultTableModel;  
import java.awt.*;  
import java.awt.event.*;  
import java.sql.*;  

public class StudentManagementSystem extends JFrame {

    private JTextField idField, nameField, ageField, courseField, marksField;  
    private DefaultTableModel tableModel;  
    private JTable table;  

    private static final String DB_URL = "jdbc:mysql://localhost:3306/studentdb?useSSL=false&serverTimezone=UTC";  
    private static final String DB_USER = "root";  
    private static final String DB_PASS = "Msand@20";  

    public StudentManagementSystem() {  
        setTitle(" Student Management System (MySQL)");  
        setSize(900, 600);  
        setDefaultCloseOperation(EXIT_ON_CLOSE);  
        setLocationRelativeTo(null);  
        setLayout(new BorderLayout(10, 10));  

        // Input panel  
        JPanel inputPanel = new JPanel(new GridBagLayout());  
        GridBagConstraints gbc = new GridBagConstraints();  
        gbc.insets = new Insets(5, 10, 5, 10);  
        gbc.fill = GridBagConstraints.HORIZONTAL;  

        int y = 0;  
        gbc.gridx = 0; gbc.gridy = y;  
        inputPanel.add(new JLabel("Student ID:"), gbc);  
        gbc.gridx = 1;  
        idField = new JTextField(10);  
        inputPanel.add(idField, gbc);  

        gbc.gridx = 2;  
        inputPanel.add(new JLabel("Name:"), gbc);  
        gbc.gridx = 3;  
        nameField = new JTextField(15);  
        inputPanel.add(nameField, gbc);  

        y++;  
        gbc.gridx = 0; gbc.gridy = y;  
        inputPanel.add(new JLabel("Age:"), gbc);  
        gbc.gridx = 1;  
        ageField = new JTextField(10);  
        inputPanel.add(ageField, gbc);  

        gbc.gridx = 2;  
        inputPanel.add(new JLabel("Course:"), gbc);  
        gbc.gridx = 3;  
        courseField = new JTextField(15);  
        inputPanel.add(courseField, gbc);  

        y++;  
        gbc.gridx = 0; gbc.gridy = y;  
        inputPanel.add(new JLabel("Marks:"), gbc);  
        gbc.gridx = 1;  
        marksField = new JTextField(10);  
        inputPanel.add(marksField, gbc);  

        JPanel buttonPanel = new JPanel(new FlowLayout());  
        JButton addButton = new JButton("Add");  
        JButton updateButton = new JButton("Update");  
        JButton deleteButton = new JButton("Delete");  
        JButton clearButton = new JButton("Clear");  
        JButton reportButton = new JButton("Generate Report");  

        buttonPanel.add(addButton);  
        buttonPanel.add(updateButton);  
        buttonPanel.add(deleteButton);  
        buttonPanel.add(clearButton);  
        buttonPanel.add(reportButton);  

        gbc.gridx = 0; gbc.gridy = ++y; gbc.gridwidth = 4;  
        inputPanel.add(buttonPanel, gbc);  

        String[] columnNames = {"ID", "Student ID", "Name", "Age", "Course", "Marks"};  
        tableModel = new DefaultTableModel(columnNames, 0);  
        table = new JTable(tableModel);  
        JScrollPane scrollPane = new JScrollPane(table);  

        add(inputPanel, BorderLayout.NORTH);  
        add(scrollPane, BorderLayout.CENTER);  

        addButton.addActionListener(e -> addStudent());  
        updateButton.addActionListener(e -> updateStudent());  
        deleteButton.addActionListener(e -> deleteStudent());  
        clearButton.addActionListener(e -> clearFields());  
        reportButton.addActionListener(e -> showReport());  

        table.addMouseListener(new MouseAdapter() {  
            public void mouseClicked(MouseEvent e) {  
                int i = table.getSelectedRow();  
                if (i >= 0) {  
                    idField.setText(tableModel.getValueAt(i, 1).toString());  
                    nameField.setText(tableModel.getValueAt(i, 2).toString());  
                    ageField.setText(tableModel.getValueAt(i, 3).toString());  
                    courseField.setText(tableModel.getValueAt(i, 4).toString());  
                    marksField.setText(tableModel.getValueAt(i, 5).toString());  
                }  
            }  
        });  

        loadStudents();  
    }  

    private Connection getConnection() {  
        try {  
            Class.forName("com.mysql.cj.jdbc.Driver");  
        } catch (ClassNotFoundException e) {  
            JOptionPane.showMessageDialog(this, "MySQL Driver NOT FOUND! Please add the Connector JAR.");  
            return null;  
        }  

        try {  
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);  
        } catch (SQLException e) {  
            JOptionPane.showMessageDialog(this, "Cannot connect to DB: " + e.getMessage());  
            return null;  
        }  
    }  

    private void loadStudents() {  
        tableModel.setRowCount(0);  
        Connection conn = getConnection();  
        if (conn == null) {  
            return;  // stop if connection failed  
        }  
        try (Statement stmt = conn.createStatement();  
             ResultSet rs = stmt.executeQuery("SELECT * FROM students ORDER BY id ASC")) {  

            while (rs.next()) {  
                tableModel.addRow(new Object[]{  
                        rs.getInt("id"),  
                        rs.getString("student_id"),  
                        rs.getString("name"),  
                        rs.getInt("age"),  
                        rs.getString("course"),  
                        rs.getInt("marks")  
                });  
            }  
        } catch (SQLException e) {  
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());  
        } finally {  
            try { conn.close(); } catch (SQLException ignored) {}  
        }  
    }  

    private void addStudent() {  
        String studentID = idField.getText();  
        String name = nameField.getText();  
        String ageText = ageField.getText();  
        String course = courseField.getText();  
        String marksText = marksField.getText();  

        if (studentID.isEmpty() || name.isEmpty() || ageText.isEmpty() || course.isEmpty() || marksText.isEmpty()) {  
            JOptionPane.showMessageDialog(this, "Please fill all fields!");  
            return;  
        }  

        int age, marks;  
        try {  
            age = Integer.parseInt(ageText);  
            marks = Integer.parseInt(marksText);  
        } catch (NumberFormatException e) {  
            JOptionPane.showMessageDialog(this, "Age and Marks must be numbers.");  
            return;  
        }  

        Connection conn = getConnection();  
        if (conn == null) {  
            return;  
        }  

        try (PreparedStatement ps = conn.prepareStatement(  
                "INSERT INTO students (student_id, name, age, course, marks) VALUES (?, ?, ?, ?, ?)")) {  
            ps.setString(1, studentID);  
            ps.setString(2, name);  
            ps.setInt(3, age);  
            ps.setString(4, course);  
            ps.setInt(5, marks);  
            ps.executeUpdate();  

            JOptionPane.showMessageDialog(this, "Student added successfully!");  
            loadStudents();  
            clearFields();  
        } catch (SQLException ex) {  
            JOptionPane.showMessageDialog(this, "Error adding student: " + ex.getMessage());  
        } finally {  
            try { conn.close(); } catch (SQLException ignored) {}  
        }  
    }  

    private void updateStudent() {  
        int selectedRow = table.getSelectedRow();  
        if (selectedRow < 0) {  
            JOptionPane.showMessageDialog(this, "Please select a student to update.");  
            return;  
        }  

        String dbID = tableModel.getValueAt(selectedRow, 0).toString();  
        String studentID = idField.getText();  
        String name = nameField.getText();  
        String ageText = ageField.getText();  
        String course = courseField.getText();  
        String marksText = marksField.getText();  

        int age, marks;  
        try {  
            age = Integer.parseInt(ageText);  
            marks = Integer.parseInt(marksText);  
        } catch (NumberFormatException e) {  
            JOptionPane.showMessageDialog(this, "Age and Marks must be numbers.");  
            return;  
        }  

        Connection conn = getConnection();  
        if (conn == null) {  
            return;  
        }  

        try (PreparedStatement ps = conn.prepareStatement(  
                "UPDATE students SET student_id=?, name=?, age=?, course=?, marks=? WHERE id=?")) {  
            ps.setString(1, studentID);  
            ps.setString(2, name);  
            ps.setInt(3, age);  
            ps.setString(4, course);  
            ps.setInt(5, marks);  
            ps.setInt(6, Integer.parseInt(dbID));  
            ps.executeUpdate();  

            JOptionPane.showMessageDialog(this, "Student updated successfully!");  
            loadStudents();  
            clearFields();  
        } catch (SQLException ex) {  
            JOptionPane.showMessageDialog(this, "Error updating student: " + ex.getMessage());  
        } finally {  
            try { conn.close(); } catch (SQLException ignored) {}  
        }  
    }  

    private void deleteStudent() {  
        int selectedRow = table.getSelectedRow();  
        if (selectedRow < 0) {  
            JOptionPane.showMessageDialog(this, "Please select a student to delete.");  
            return;  
        }  

        String dbID = tableModel.getValueAt(selectedRow, 0).toString();  
        Connection conn = getConnection();  
        if (conn == null) {  
            return;  
        }  

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE id=?")) {  
            ps.setInt(1, Integer.parseInt(dbID));  
            ps.executeUpdate();  

            JOptionPane.showMessageDialog(this, "Student deleted successfully!");  
            loadStudents();  
            clearFields();  
        } catch (SQLException ex) {  
            JOptionPane.showMessageDialog(this, "Error deleting student: " + ex.getMessage());  
        } finally {  
            try { conn.close(); } catch (SQLException ignored) {}  
        }  
    }  

    private void showReport() {  
        JFrame reportFrame = new JFrame("Student Rank Report");  
        reportFrame.setSize(600, 400);  
        reportFrame.setLocationRelativeTo(null);  

        DefaultTableModel reportModel = new DefaultTableModel(  
                new String[]{"Rank", "Student ID", "Name", "Marks"}, 0);  
        JTable reportTable = new JTable(reportModel);  
        JScrollPane scrollPane = new JScrollPane(reportTable);  

        Connection conn = getConnection();  
        if (conn == null) {  
            return;  
        }  

        try (Statement stmt = conn.createStatement();  
             ResultSet rs = stmt.executeQuery("SELECT student_id, name, marks FROM students ORDER BY marks DESC")) {  

            int rank = 1;  
            while (rs.next()) {  
                reportModel.addRow(new Object[]{  
                        rank++,  
                        rs.getString("student_id"),  
                        rs.getString("name"),  
                        rs.getInt("marks")  
                });  
            }  
        } catch (SQLException e) {  
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage());  
        } finally {  
            try { conn.close(); } catch (SQLException ignored) {}  
        }  

        reportFrame.add(scrollPane);  
        reportFrame.setVisible(true);  
    }  

    private void clearFields() {  
        idField.setText("");  
        nameField.setText("");  
        ageField.setText("");  
        courseField.setText("");  
        marksField.setText("");  
    }  

    public static void main(String[] args) {  
        SwingUtilities.invokeLater(() -> new StudentManagementSystem().setVisible(true));  
    }  
}
