import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class LibrarySystem {
    private List<Book> books = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private User loggedInUser;

    // Load data from files
    public void loadUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    User user = new User(parts[0], parts[1], parts[2], parts[3]);
                    users.add(user);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("users.txt not found. Starting with empty user list.");
        } catch (IOException e) {
            System.out.println("Error reading users.txt: " + e.getMessage());
        }
    }

    public void loadBooks() {
        try (BufferedReader br = new BufferedReader(new FileReader("books.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    boolean available = Boolean.parseBoolean(parts[3]);
                    Book book = new Book(parts[0], parts[1], parts[2], available);
                    books.add(book);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("books.txt not found. Starting with empty book list.");
        } catch (IOException e) {
            System.out.println("Error reading books.txt: " + e.getMessage());
        }
    }

    public void loadTransactions() {
        try (BufferedReader br = new BufferedReader(new FileReader("transactions.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    LocalDate borrowed = LocalDate.parse(parts[3]);
                    LocalDate returned = parts[4].equals("null") ? null : LocalDate.parse(parts[4]);
                    Transaction t = new Transaction(parts[0], parts[1], parts[2], borrowed, returned);
                    transactions.add(t);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("transactions.txt not found. Starting with empty transaction list.");
        } catch (IOException e) {
            System.out.println("Error reading transactions.txt: " + e.getMessage());
        }
    }

    // Save data to files
    public void saveUsers() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("users.txt"))) {
            for (User u : users) {
                bw.write(u.getId() + "," + u.getName() + "," + u.getPassword() + "," + u.getRole());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing users.txt: " + e.getMessage());
        }
    }

    public void saveBooks() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("books.txt"))) {
            for (Book b : books) {
                bw.write(b.getBookId() + "," + b.getTitle() + "," + b.getAuthor() + "," + b.isAvailable());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing books.txt: " + e.getMessage());
        }
    }

    public void saveTransactions() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("transactions.txt"))) {
            for (Transaction t : transactions) {
                String returned = (t.getDateReturned() == null) ? "null" : t.getDateReturned().toString();
                bw.write(t.getTransactionId() + "," + t.getUserId() + "," + t.getBookId() + "," + t.getDateBorrowed() + "," + returned);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing transactions.txt: " + e.getMessage());
        }
    }

    // Simple login implementation (throws custom exception on failure)
    private boolean login(String username, String password) throws InvalidOperationException {
        for (User u : users) {
            if (u.getName().equals(username) && u.getPassword().equals(password)) {
                loggedInUser = u;
                return true;
            }
        }
        throw new InvalidOperationException("Invalid username or password.");
    }

    public void displayMenu() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to the Library Management System");
        System.out.println("----------------------------------------");
        System.out.print("Please log in to continue.\n\nUsername: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();
        int attempts = 3;
        boolean loggedIn = false;
        while (attempts > 0) {
            try {
                if (login(username, password)) {
                    loggedIn = true;
                    break;
                }
            } catch (InvalidOperationException e) {
                attempts--;
                if (attempts > 0) {
                    System.out.println(e.getMessage() + " Try again. (Attempts left: " + attempts + ")");
                    System.out.print("Username: ");
                    username = sc.nextLine();
                    System.out.print("Password: ");
                    password = sc.nextLine();
                } else {
                    System.out.println("Login failed after 3 attempts. Exiting.");
                    return;
                }
            }
        }
        if (!loggedIn || loggedInUser == null) {
            return;
        }
        System.out.println("Login successful! Welcome, " + loggedInUser.getName() + ".");
        // Load borrowed books for user
        for (Transaction t : transactions) {
            if (t.getUserId().equals(loggedInUser.getId()) && t.getDateReturned() == null) {
                loggedInUser.getBorrowedBooks().add(t.getBookId());
            }
        }
        boolean running = true;
        while (running) {
            System.out.println("\n1. View All Books");
            System.out.println("2. Borrow Book");
            System.out.println("3. Return Book");
            if (loggedInUser.getRole().equals("admin")) {
                System.out.println("4. Manage Users");
                System.out.println("5. Manage Catalogue");
                System.out.println("6. View Transactions");
                System.out.println("7. Exit");
            } else {
                System.out.println("4. Exit");
            }
            System.out.print("Enter choice: ");
            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid input.");
                continue;
            }
            try {
                switch (choice) {
                    case 1:
                        viewAllBooks(sc);
                        break;
                    case 2:
                        borrowBook(sc);
                        break;
                    case 3:
                        returnBook(sc);
                        break;
                    case 4:
                        if (loggedInUser.getRole().equals("admin")) {
                            manageUsers(sc);
                        } else {
                            running = false;
                        }
                        break;
                    case 5:
                        if (loggedInUser.getRole().equals("admin")) {
                            manageCatalogue(sc);
                        }
                        break;
                    case 6:
                        if (loggedInUser.getRole().equals("admin")) {
                            viewTransactions(sc);
                        }
                        break;
                    case 7:
                        if (loggedInUser.getRole().equals("admin")) {
                            running = false;
                        }
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (InvalidOperationException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        // Save data on exit
        saveUsers();
        saveBooks();
        saveTransactions();
        System.out.println("Data saved. Exiting.");
    }

    // View all books with search option (bonus)
    private void viewAllBooks(Scanner sc) {
        System.out.println("1. View All");
        System.out.println("2. Search by Title");
        System.out.println("3. Search by Author");
        System.out.print("Enter choice: ");
        int choice;
        try {
            choice = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid input.");
            return;
        }
        List<Book> toDisplay = new ArrayList<>();
        if (choice == 1) {
            toDisplay = books;
        } else if (choice == 2) {
            System.out.print("Enter title: ");
            String title = sc.nextLine().toLowerCase();
            for (Book b : books) {
                if (b.getTitle().toLowerCase().contains(title)) {
                    toDisplay.add(b);
                }
            }
        } else if (choice == 3) {
            System.out.print("Enter author: ");
            String author = sc.nextLine().toLowerCase();
            for (Book b : books) {
                if (b.getAuthor().toLowerCase().contains(author)) {
                    toDisplay.add(b);
                }
            }
        }
        for (Book b : toDisplay) {
            b.displayBookDetails();
        }
    }

    // Borrow book
    private void borrowBook(Scanner sc) throws InvalidOperationException {
        if (loggedInUser.getBorrowedBooks().size() >= 3) {
            throw new InvalidOperationException("You cannot borrow more than 3 books.");
        }
        System.out.print("Enter Book ID: ");
        String bookId = sc.nextLine();
        Book book = findBook(bookId);
        if (book == null) {
            throw new InvalidOperationException("Book not found.");
        }
        if (!book.isAvailable()) {
            throw new InvalidOperationException("Book is not available.");
        }
        book.setAvailable(false);
        loggedInUser.getBorrowedBooks().add(bookId);
        // Generate transaction ID
        String transId = "T" + String.format("%03d", transactions.size() + 1);
        Transaction t = new Transaction(transId, loggedInUser.getId(), bookId, LocalDate.now(), null);
        transactions.add(t);
        System.out.println("Book borrowed successfully!");
    }

    // Return book
    private void returnBook(Scanner sc) throws InvalidOperationException {
        System.out.print("Enter Book ID: ");
        String bookId = sc.nextLine();
        if (!loggedInUser.getBorrowedBooks().contains(bookId)) {
            throw new InvalidOperationException("You have not borrowed this book.");
        }
        Book book = findBook(bookId);
        if (book != null) {
            book.setAvailable(true);
        }
        loggedInUser.getBorrowedBooks().remove(bookId);
        // Update transaction
        for (Transaction t : transactions) {
            if (t.getUserId().equals(loggedInUser.getId()) && t.getBookId().equals(bookId) && t.getDateReturned() == null) {
                t.setDateReturned(LocalDate.now());
                break;
            }
        }
        System.out.println("Book returned successfully!");
    }

    // Helper methods
    private Book findBook(String bookId) {
        for (Book b : books) {
            if (b.getBookId().equals(bookId)) {
                return b;
            }
        }
        return null;
    }

    private User findUser(String id) {
        for (User u : users) {
            if (u.getId().equals(id)) {
                return u;
            }
        }
        return null;
    }

    // Admin: Manage Users
    private void manageUsers(Scanner sc) {
        System.out.println("1. Add User");
        System.out.println("2. Update User");
        System.out.println("3. Delete User");
        System.out.println("4. Display Users");
        System.out.println("5. Exit");
        System.out.print("Enter choice: ");
        int choice;
        try {
            choice = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid input.");
            return;
        }
        switch (choice) {
            case 1:
                addUser(sc);
                break;
            case 2:
                updateUser(sc);
                break;
            case 3:
                deleteUser(sc);
                break;
            case 4:
                displayUsers();
                break;
            case 5:
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void addUser(Scanner sc) {
        System.out.print("Enter Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();
        System.out.print("Enter Role (user/admin): ");
        String role = sc.nextLine();
        // Generate ID
        String id = "U" + String.format("%03d", users.size() + 1);
        User newUser = new User(id, name, password, role);
        users.add(newUser);
        System.out.println("User added successfully!");
    }

    private void updateUser(Scanner sc) {
        System.out.print("Enter User ID: ");
        String id = sc.nextLine();
        User user = findUser(id);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        System.out.print("Enter new Name: ");
        String name = sc.nextLine();
        System.out.print("Enter new Password: ");
        String password = sc.nextLine();
        System.out.print("Enter new Role: ");
        String role = sc.nextLine();
        user.setName(name);
        user.setPassword(password);
        user.setRole(role);
        System.out.println("User updated successfully!");
    }

    private void deleteUser(Scanner sc) {
        System.out.print("Enter User ID: ");
        String id = sc.nextLine();
        User user = findUser(id);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        users.remove(user);
        System.out.println("User deleted successfully!");
    }

    private void displayUsers() {
        for (User u : users) {
            // Polymorphism: Using Person reference for User
            Person p = u;
            p.displayInfo();
        }
    }

    // Admin: Manage Catalogue
    private void manageCatalogue(Scanner sc) {
        System.out.println("1. Add Book");
        System.out.println("2. Update Book");
        System.out.println("3. Delete Book");
        System.out.println("4. Display Books");
        System.out.println("5. Exit");
        System.out.print("Enter choice: ");
        int choice;
        try {
            choice = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid input.");
            return;
        }
        switch (choice) {
            case 1:
                addBook(sc);
                break;
            case 2:
                updateBook(sc);
                break;
            case 3:
                deleteBook(sc);
                break;
            case 4:
                displayBooks();
                break;
            case 5:
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void addBook(Scanner sc) {
        System.out.print("Enter Title: ");
        String title = sc.nextLine();
        System.out.print("Enter Author: ");
        String author = sc.nextLine();
        // Generate ID
        String id = "B" + String.format("%03d", books.size() + 1);
        Book newBook = new Book(id, title, author, true);
        books.add(newBook);
        System.out.println("Book added successfully!");
    }

    private void updateBook(Scanner sc) {
        System.out.print("Enter Book ID: ");
        String id = sc.nextLine();
        Book book = findBook(id);
        if (book == null) {
            System.out.println("Book not found.");
            return;
        }
        System.out.print("Enter new Title: ");
        String title = sc.nextLine();
        System.out.print("Enter new Author: ");
        String author = sc.nextLine();
        book.setTitle(title);
        book.setAuthor(author);
        System.out.println("Book updated successfully!");
        
    }

    private void deleteBook(Scanner sc) {
        System.out.print("Enter Book ID: ");
        String id = sc.nextLine();
        Book book = findBook(id);
        if (book == null) {
            System.out.println("Book not found.");
            return;
        }
        books.remove(book);
        System.out.println("Book deleted successfully!");
    }

    private void displayBooks() {
        for (Book b : books) {
            b.displayBookDetails();
        }
    }

    // Admin: View Transactions
    private void viewTransactions(Scanner sc) {
        System.out.println("1. View All Transactions");
        System.out.println("2. View By User");
        System.out.println("3. View By Book");
        System.out.println("4. Exit");
        System.out.print("Enter choice: ");
        int choice;
        try {
            choice = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid input.");
            return;
        }
        switch (choice) {
            case 1:
                for (Transaction t : transactions) {
                    t.displayTransaction();
                }
                break;
            case 2:
                System.out.print("Enter User ID: ");
                String userId = sc.nextLine();
                for (Transaction t : transactions) {
                    if (t.getUserId().equals(userId)) {
                        t.displayTransaction();
                    }
                }
                break;
            case 3:
                System.out.print("Enter Book ID: ");
                String bookId = sc.nextLine();
                for (Transaction t : transactions) {
                    if (t.getBookId().equals(bookId)) {
                        t.displayTransaction();
                    }
                }
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    // Main method to run the system
    public static void main(String[] args) {
        LibrarySystem system = new LibrarySystem();
        system.loadUsers();
        system.loadBooks();
        system.loadTransactions();
        system.displayMenu();
    }

    // Custom exception used by this class
    private static class InvalidOperationException extends Exception {
        public InvalidOperationException(String message) {
            super(message);
        }
    }
}

