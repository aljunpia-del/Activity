import java.util.ArrayList;

public class User extends Person {
    private String password;
    private String role;
    private ArrayList<String> borrowedBooks;

    public User(String id, String name, String password, String role) {
        super(id, name);
        this.password = password;
        this.role = role;
        this.borrowedBooks = new ArrayList<>();
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Role: " + role);
        System.out.println("Borrowed Books: " + borrowedBooks);
    }

    // Getters and Setters
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public ArrayList<String> getBorrowedBooks() { return borrowedBooks; }
}
