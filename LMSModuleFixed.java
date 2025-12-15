// Save as LMSModuleFixed.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.UUID;

/**
 * Single-file LMS module (fixed)
 * - Admin & Librarian: Add / Edit / Delete
 * - User: view-only
 * - Compile: javac LMSModuleFixed.java
 * - Run:     java LMSModuleFixed
 *
 * Persistence file: books.dat
 */

public class LMSModuleFixed {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LMSLoginPage());
    }
}

/* ---------- Model ---------- */
class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id, title, author, isbn, category, publisher;
    private int copies;
    private boolean available;

    public Book(String id, String title, String author, String isbn, String category, String publisher, int copies) {
        this.id = id; this.title = title; this.author = author; this.isbn = isbn;
        this.category = category; this.publisher = publisher; this.copies = copies;
        this.available = copies > 0;
    }
    public String getId(){return id;}
    public String getTitle(){return title;} public void setTitle(String t){title=t;}
    public String getAuthor(){return author;} public void setAuthor(String a){author=a;}
    public String getIsbn(){return isbn;} public void setIsbn(String i){isbn=i;}
    public String getCategory(){return category;} public void setCategory(String c){category=c;}
    public String getPublisher(){return publisher;} public void setPublisher(String p){publisher=p;}
    public int getCopies(){return copies;} public void setCopies(int c){copies=c; available = copies>0;}
    public boolean isAvailable(){return available;} public void setAvailable(boolean a){available=a;}
    @Override public String toString(){ return title + " by " + author + " (" + isbn + ")"; }
}

/* ---------- Simple persistent store ---------- */
class BookStore {
    private static BookStore inst;
    private List<Book> books = new ArrayList<>();
    private final File file = new File("books.dat");

    private BookStore(){ load(); }
    public static synchronized BookStore getInstance(){ if(inst==null) inst=new BookStore(); return inst; }

    public synchronized List<Book> getAll(){ return new ArrayList<>(books); }
    public synchronized void add(Book b){ books.add(b); save(); }
    public synchronized void update(Book up){
        for(int i=0;i<books.size();i++) if(books.get(i).getId().equals(up.getId())) { books.set(i, up); save(); return; }
    }
    public synchronized void delete(String id){ books.removeIf(b->b.getId().equals(id)); save(); }
    public synchronized void save(){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))){ oos.writeObject(books); }
        catch(Exception e){ e.printStackTrace(); }
    }
    @SuppressWarnings("unchecked")
    private void load(){
        if(!file.exists()) return;
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
            Object o = ois.readObject();
            if(o instanceof List) books = (List<Book>) o;
        } catch(Exception e){ e.printStackTrace(); }
    }
}

/* ---------- Login: choose role ---------- */
class LMSLoginPage extends JFrame {
    public LMSLoginPage(){
        setTitle("LMS - Login (module)");
        setSize(380,200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8,8));

        JLabel h = new JLabel("LMS Module — Add / Edit / Delete", SwingConstants.CENTER);
        h.setFont(new Font("Segoe UI", Font.BOLD, 16));
        add(h, BorderLayout.NORTH);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(6,6,6,6);
        c.gridx=0; c.gridy=0; p.add(new JLabel("Name:"), c);
        c.gridx=1; JTextField tfName = new JTextField("guest",14); p.add(tfName,c);
        c.gridx=0; c.gridy=1; p.add(new JLabel("Role:"), c);
        c.gridx=1; JComboBox<String> cb = new JComboBox<>(new String[]{"user","librarian","admin"}); p.add(cb,c);
        add(p, BorderLayout.CENTER);

        JPanel bot = new JPanel();
        JButton login = new JButton("Login");
        bot.add(login); add(bot, BorderLayout.SOUTH);

        login.addActionListener(e -> {
            String role = ((String)cb.getSelectedItem()).toLowerCase();
            dispose();
            SwingUtilities.invokeLater(() -> new Dashboard(role));
        });

        setVisible(true);
    }
}

/* ---------- Dashboard: simplified layout so clicks open windows reliably ---------- */
class Dashboard extends JFrame {
    private DefaultTableModel rightModel;

    public Dashboard(String role){
        setTitle("Dashboard - " + role.toUpperCase());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900,500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8,8));

        JLabel h = new JLabel("Library — Module (Add / Edit / Delete)", SwingConstants.CENTER);
        h.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(h, BorderLayout.NORTH);

        // Left panel with buttons (vertical)
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JLabel info = new JLabel("Logged in as: " + role.toUpperCase());
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        left.add(info);
        left.add(Box.createVerticalStrut(12));

        JButton btnAdd = new JButton("Add Book");
        JButton btnEdit = new JButton("Edit Books");
        JButton btnDelete = new JButton("Delete Books");
        JButton btnRefresh = new JButton("Refresh View");
        JButton btnLogout = new JButton("Logout");

        // Make buttons same width
        Dimension btnSize = new Dimension(180, 36);
        for (JButton b : new JButton[]{btnAdd, btnEdit, btnDelete, btnRefresh, btnLogout}) {
            b.setMaximumSize(btnSize);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            left.add(b);
            left.add(Box.createVerticalStrut(8));
        }

        add(left, BorderLayout.WEST);

        // Right: table viewer to show books
        rightModel = new DefaultTableModel(new String[]{"Title","Author","ISBN","Category","Publisher","Copies","Available"}, 0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable table = new JTable(rightModel);
        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);
        loadIntoModel(rightModel);

        // Button behavior
        btnAdd.addActionListener(e -> {
            // Only admin & librarian allowed
            if (!role.equals("admin") && !role.equals("librarian")) {
                JOptionPane.showMessageDialog(this, "Access denied.");
                return;
            }
            // open add dialog
            SwingUtilities.invokeLater(() -> new AddDialog(this, rightModel));
        });

        btnEdit.addActionListener(e -> {
            if (!role.equals("admin") && !role.equals("librarian")) {
                JOptionPane.showMessageDialog(this, "Access denied.");
                return;
            }
            SwingUtilities.invokeLater(() -> new EditWindow(this, rightModel));
        });

        btnDelete.addActionListener(e -> {
            if (!role.equals("admin") && !role.equals("librarian")) {
                JOptionPane.showMessageDialog(this, "Access denied.");
                return;
            }
            SwingUtilities.invokeLater(() -> new DeleteWindow(this, rightModel));
        });

        btnRefresh.addActionListener(e -> loadIntoModel(rightModel));

        btnLogout.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LMSLoginPage());
        });

        setVisible(true);
    }

    static void loadIntoModel(DefaultTableModel tm){
        tm.setRowCount(0);
        for(Book b: BookStore.getInstance().getAll()){
            tm.addRow(new Object[]{b.getTitle(), b.getAuthor(), b.getIsbn(), b.getCategory(), b.getPublisher(), b.getCopies(), b.isAvailable()});
        }
    }
}

/* ---------- Add Dialog ---------- */
class AddDialog extends JDialog {
    public AddDialog(Frame owner, DefaultTableModel model){
        super(owner, "Add Book", true);
        setSize(420,380); setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(6,6,6,6); c.fill = GridBagConstraints.HORIZONTAL;
        JTextField tTitle=new JTextField(), tAuthor=new JTextField(), tIsbn=new JTextField(), tCat=new JTextField(), tPub=new JTextField(), tCopies=new JTextField("1");

        c.gridx=0; c.gridy=0; form.add(new JLabel("Title:"),c); c.gridx=1; form.add(tTitle,c);
        c.gridx=0; c.gridy=1; form.add(new JLabel("Author:"),c); c.gridx=1; form.add(tAuthor,c);
        c.gridx=0; c.gridy=2; form.add(new JLabel("ISBN:"),c); c.gridx=1; form.add(tIsbn,c);
        c.gridx=0; c.gridy=3; form.add(new JLabel("Category:"),c); c.gridx=1; form.add(tCat,c);
        c.gridx=0; c.gridy=4; form.add(new JLabel("Publisher:"),c); c.gridx=1; form.add(tPub,c);
        c.gridx=0; c.gridy=5; form.add(new JLabel("Copies:"),c); c.gridx=1; form.add(tCopies,c);

        add(form, BorderLayout.CENTER);

        JPanel bot = new JPanel();
        JButton add = new JButton("Add"); JButton cancel = new JButton("Cancel");
        bot.add(add); bot.add(cancel); add(bot, BorderLayout.SOUTH);

        add.addActionListener(e -> {
            String title=tTitle.getText().trim(), author=tAuthor.getText().trim();
            if(title.isEmpty() || author.isEmpty()){ JOptionPane.showMessageDialog(this,"Enter title & author"); return; }
            int copies=1;
            try{ copies = Integer.parseInt(tCopies.getText().trim()); } catch(NumberFormatException ex){ JOptionPane.showMessageDialog(this,"Copies must be number"); return; }
            Book b = new Book(UUID.randomUUID().toString(), title, author, tIsbn.getText().trim(), tCat.getText().trim(), tPub.getText().trim(), copies);
            BookStore.getInstance().add(b);
            Dashboard.loadIntoModel(model);
            JOptionPane.showMessageDialog(this,"Added.");
            dispose();
        });

        cancel.addActionListener(e -> dispose());

        setVisible(true);
    }
}

/* ---------- Edit Window ---------- */
class EditWindow extends JFrame {
    private DefaultTableModel model;
    private JTable table;

    public EditWindow(Frame owner, DefaultTableModel rightModel){
        super("Edit Books");
        setSize(880,420); setLocationRelativeTo(owner); setLayout(new BorderLayout(8,8));
        model = new DefaultTableModel(new String[]{"ID","Title","Author","ISBN","Category","Publisher","Copies","Available"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        table = new JTable(model);
        table.removeColumn(table.getColumnModel().getColumn(0)); // hide id
        loadAll();
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bot = new JPanel();
        JButton btnEdit = new JButton("Edit Selected");
        bot.add(btnEdit); add(bot, BorderLayout.SOUTH);

        btnEdit.addActionListener(e -> doEdit(rightModel));
        setVisible(true);
    }

    private void loadAll(){
        model.setRowCount(0);
        for(Book b: BookStore.getInstance().getAll()){
            model.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(), b.getCategory(), b.getPublisher(), b.getCopies(), b.isAvailable()});
        }
    }

    private void doEdit(DefaultTableModel rightModel){
        int sel = table.getSelectedRow();
        if(sel==-1){ JOptionPane.showMessageDialog(this,"Select a row"); return; }
        String id = (String) model.getValueAt(sel, 0);
        Book target = null;
        for(Book b: BookStore.getInstance().getAll()) if(b.getId().equals(id)){ target=b; break;}
        if(target==null){ JOptionPane.showMessageDialog(this,"Not found"); return; }

        JTextField tTitle=new JTextField(target.getTitle());
        JTextField tAuthor=new JTextField(target.getAuthor());
        JTextField tIsbn=new JTextField(target.getIsbn());
        JTextField tCat=new JTextField(target.getCategory());
        JTextField tPub=new JTextField(target.getPublisher());
        JTextField tCopies=new JTextField(String.valueOf(target.getCopies()));

        JPanel p = new JPanel(new GridLayout(0,1,6,6));
        p.add(new JLabel("Title")); p.add(tTitle);
        p.add(new JLabel("Author")); p.add(tAuthor);
        p.add(new JLabel("ISBN")); p.add(tIsbn);
        p.add(new JLabel("Category")); p.add(tCat);
        p.add(new JLabel("Publisher")); p.add(tPub);
        p.add(new JLabel("Copies")); p.add(tCopies);

        int ok = JOptionPane.showConfirmDialog(this, p, "Edit Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(ok==JOptionPane.OK_OPTION){
            try{
                int copies = Integer.parseInt(tCopies.getText().trim());
                target.setTitle(tTitle.getText().trim());
                target.setAuthor(tAuthor.getText().trim());
                target.setIsbn(tIsbn.getText().trim());
                target.setCategory(tCat.getText().trim());
                target.setPublisher(tPub.getText().trim());
                target.setCopies(copies);
                BookStore.getInstance().update(target);
                loadAll();
                Dashboard.loadIntoModel(rightModel);
                JOptionPane.showMessageDialog(this,"Updated.");
            } catch(NumberFormatException ex){ JOptionPane.showMessageDialog(this,"Copies must be number"); }
        }
    }
}

/* ---------- Delete Window ---------- */
class DeleteWindow extends JFrame {
    private DefaultTableModel model;
    private JTable table;

    public DeleteWindow(Frame owner, DefaultTableModel rightModel){
        super("Delete Books");
        setSize(880,420); setLocationRelativeTo(owner); setLayout(new BorderLayout(8,8));
        model = new DefaultTableModel(new String[]{"ID","Title","Author","ISBN","Category","Publisher","Copies","Available"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        table = new JTable(model);
        table.removeColumn(table.getColumnModel().getColumn(0)); // hide id
        loadAll();
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bot = new JPanel();
        JButton btnDel = new JButton("Delete Selected");
        bot.add(btnDel); add(bot, BorderLayout.SOUTH);

        btnDel.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if(sel==-1){ JOptionPane.showMessageDialog(this,"Select a row"); return; }
            // get hidden id from model (column 0)
            String id = (String) model.getValueAt(sel, 0);
            int ok = JOptionPane.showConfirmDialog(this, "Delete selected book?", "Confirm", JOptionPane.YES_NO_OPTION);
            if(ok==JOptionPane.YES_OPTION){
                BookStore.getInstance().delete(id);
                loadAll();
                Dashboard.loadIntoModel(rightModel);
                JOptionPane.showMessageDialog(this,"Deleted.");
            }
        });

        setVisible(true);
    }

    private void loadAll(){
        model.setRowCount(0);
        for(Book b: BookStore.getInstance().getAll()){
            model.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(), b.getCategory(), b.getPublisher(), b.getCopies(), b.isAvailable()});
        }
    }
}
