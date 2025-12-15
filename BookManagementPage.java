import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class BookManagementPage extends JFrame {

    static class Book { String title, author, isbn; boolean available; Book(String t,String a,String i,boolean av){title=t;author=a;isbn=i;available=av;} }

    private java.util.List<Book> books = new ArrayList<>();
    private JTable table;
    private DefaultTableModel model;
    private JTextField titleField, authorField, isbnField, searchField;
    private JCheckBox availableCheck;

    public BookManagementPage() {
        setTitle("Book Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        ImageIcon bgIcon = new ImageIcon("00.jpg");
        Image bgImg = bgIcon.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
        JLabel background = new JLabel(new ImageIcon(bgImg));
        background.setLayout(new GridBagLayout());
        add(background);

        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(780, 500));
        card.setBackground(new Color(255,255,255,210));
        card.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        background.add(card);

        JPanel form = new JPanel(new GridLayout(3,4,10,10));
        form.setOpaque(false);
        form.add(new JLabel("Title:")); titleField = new JTextField(); form.add(titleField);
        form.add(new JLabel("Author:")); authorField = new JTextField(); form.add(authorField);
        form.add(new JLabel("ISBN:")); isbnField = new JTextField(); form.add(isbnField);
        form.add(new JLabel("Available:")); availableCheck = new JCheckBox(); availableCheck.setOpaque(false); form.add(availableCheck);

        JButton addBtn = new JButton("Add Book"), updateBtn = new JButton("Update Book"), deleteBtn = new JButton("Delete Book");
        form.add(addBtn); form.add(updateBtn); form.add(deleteBtn);
        card.add(form, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Title","Author","ISBN","Available"},0);
        table = new JTable(model);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new FlowLayout()); searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search:")); searchField = new JTextField(20); searchPanel.add(searchField);
        JButton searchBtn = new JButton("Search"), showAllBtn = new JButton("Show All"); searchPanel.add(searchBtn); searchPanel.add(showAllBtn);
        card.add(searchPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            String t = titleField.getText().trim(), a = authorField.getText().trim(), i = isbnField.getText().trim();
            if (t.isEmpty()||a.isEmpty()||i.isEmpty()){ JOptionPane.showMessageDialog(this,"Fill all fields!"); return; }
            books.add(new Book(t,a,i,availableCheck.isSelected())); refreshTable(); clearFields();
        });

        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow(); if (row==-1){ JOptionPane.showMessageDialog(this,"Select book!"); return; }
            Book b = books.get(row); b.title = titleField.getText(); b.author = authorField.getText(); b.isbn = isbnField.getText(); b.available = availableCheck.isSelected(); refreshTable(); clearFields();
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow(); if (row==-1){ JOptionPane.showMessageDialog(this,"Select book to delete!"); return; }
            books.remove(row); refreshTable(); clearFields();
        });

        table.addMouseListener(new MouseAdapter(){ public void mouseClicked(MouseEvent e){ int row = table.getSelectedRow(); titleField.setText(model.getValueAt(row,0).toString()); authorField.setText(model.getValueAt(row,1).toString()); isbnField.setText(model.getValueAt(row,2).toString()); availableCheck.setSelected(model.getValueAt(row,3).equals("Yes")); }});

        searchBtn.addActionListener(e -> {
            String text = searchField.getText().trim().toLowerCase(); model.setRowCount(0);
            for (Book b: books) if (b.title.toLowerCase().contains(text)||b.author.toLowerCase().contains(text)||b.isbn.contains(text)) model.addRow(new Object[]{b.title,b.author,b.isbn,b.available?"Yes":"No"});
        });

        showAllBtn.addActionListener(e -> refreshTable());

        // Back button to Dashboard
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT)); topRight.setOpaque(false);
        JButton backBtn = new JButton("Back"); backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.addActionListener(e -> { new DashboardPage("admin", false); dispose(); });
        topRight.add(backBtn);
        card.add(topRight, BorderLayout.NORTH);

        setVisible(true);
    }

    private void refreshTable(){ model.setRowCount(0); for (Book b: books) model.addRow(new Object[]{b.title,b.author,b.isbn,b.available?"Yes":"No"}); }
    private void clearFields(){ titleField.setText(""); authorField.setText(""); isbnField.setText(""); availableCheck.setSelected(false); }
}
