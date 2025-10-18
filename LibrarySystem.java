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
                bw.write(u.getId() + "," + u.getName() + "," + u.getPassword() + "," + u.getRole() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error writing users.txt: " + e.getMessage());
        }
    }

    public void saveBooks() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("books.txt"))) {
            for (Book b : books) {
                bw.write(b.getBookId() + "," + b.getTitle() + "," + b.getAuthor() + "," + b.isAvailable() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error writing books.txt: " + e.getMessage());
        }
    }

    public void saveTransactions() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("transactions.txt"))) {
            for (Transaction t : transactions) {
                String returned = t.getDateReturned() == null ? "null" : t.getDateReturned().toString();
                bw.write(t.getTransactionId() + "," + t.getUserId() + "," + t.getBookId() + "," + t.getDateBorrowed() + "," + returned + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error writing transactions.txt: " + e.getMessage());
        }
    }

    // Login method
    public boolean login(String username, String password) throws InvalidOperationException {
        for (User u : users) {
            if (u.getName().equals(username) && u.getPassword().equals(password)) {
                loggedInUser = u;
                return true;
            }
        }
        throw new InvalidOperationException("Invalid username or password.");
    }

    // Display menu
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
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

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
        int choice = sc.nextInt();
        sc.nextLine();
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
        int choice = sc.nextInt();
        sc.nextLine();
        switch (choice) {
            case 1:
                addUser(sc);
                break;
            case 2:
                updateUser(sc);
