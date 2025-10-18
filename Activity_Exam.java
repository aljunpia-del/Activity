import java.io.*;
import java.util.*;
import java.time.LocalDate;

class Person {
    String id;
    String name;

    Person(String id, String name) {
        this.id = id;
        this.name = name;
    }

    void displayInfo() {
        System.out.println("ID: " + id + ", Name: " + name);
    }
}

class User extends Person {
    String password;
    String role;
    ArrayList<String> borrowedBooks = new ArrayList<>();

    User(String id, String name, String password, String role) {
        super(id, name);
        this.password = password;
        this.role = role;
    }

    @Override
    void displayInfo() {
        super.displayInfo();
        System.out.println("Role: " + role);
    }

    // Note: Add, Update, Delete, Display methods are implemented in LibrarySystem for users
}

class Book {
    String bookId;
    String title;
    String author;
    boolean available;

    Book(String bookId, String title, String author, boolean available) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.available = available;
    }

    void displayBookDetails() {
        System.out.println("ID: " + bookId + ", Title: " + title + ", Author: " + author + ", Available: " + available);
    }

    // Note: Add, Update, Delete methods are implemented in LibrarySystem for books
}

class Transaction {
    String transactionId;
    String userId;
    String bookId;
    String dateBorrowed;
    String dateReturned;

    Transaction(String transactionId, String userId, String bookId, String dateBorrowed, String dateReturned) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.bookId = bookId;
        this.dateBorrowed = dateBorrowed;
        this.dateReturned = dateReturned;
    }

    void displayTransaction() {
        System.out.println("TID: " + transactionId + ", UID: " + userId + ", BID: " + bookId + ", Borrowed: " + dateBorrowed + ", Returned: " + dateReturned);
    }

    // Note: displayBy methods are implemented in LibrarySystem
}

class LibrarySystem {
    List<Book> books = new ArrayList<>();
    List<User> users = new ArrayList<>();
    List<Transaction> transactions = new ArrayList<>();
    User loggedInUser = null;

    void loadUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    users.add(new User(parts[0], parts[1], parts[2], parts[3]));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("users.txt not found. Creating empty list.");
        } catch (IOException e) {
            System.out.println("Error reading users.txt: " + e.getMessage());
        }
    }

    void loadBooks() {
        try (BufferedReader br = new BufferedReader(new FileReader("books.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    books.add(new Book(parts[0], parts[1], parts[2], Boolean.parseBoolean(parts[3])));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("books.txt not found. Creating empty list.");
        } catch (IOException e) {
            System.out.println("Error reading books.txt: " + e.getMessage());
        }
    }

    void loadTransactions() {
        try (BufferedReader br = new BufferedReader(new FileReader("transactions.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    transactions.add(new Transaction(parts[0], parts[1], parts[2], parts[3], parts[4].equals("null") ? null : parts[4]));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("transactions.txt not found. Creating empty list.");
        } catch (IOException e) {
            System.out.println("Error reading transactions.txt: " + e.getMessage());
        }
    }

    void saveUsers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("users.txt"))) {
            for (User u : users) {
                pw.println(u.id + "," + u.name + "," + u.password + "," + u.role);
            }
        } catch (IOException e) {
            System.out.println("Error saving users.txt: " + e.getMessage());
        }
    }

    void saveBooks() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("books.txt"))) {
            for (Book b : books) {
                pw.println(b.bookId + "," + b.title + "," + b.author + "," + b.available);
            }
        } catch (IOException e) {
            System.out.println("Error saving books.txt: " + e.getMessage());
        }
    }

    void saveTransactions() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("transactions.txt"))) {
            for (Transaction t : transactions) {
                pw.println(t.transactionId + "," + t.userId + "," + t.bookId + "," + t.dateBorrowed + "," + (t.dateReturned == null ? "null" : t.dateReturned));
            }
        } catch (IOException e) {
            System.out.println("Error saving transactions.txt: " + e.getMessage());
        }
    }

    void saveAll() {
        saveUsers();
        saveBooks();
        saveTransactions();
    }

    boolean login(String username, String password) {
        for (User u : users) {
            if (u.name.equals(username) && u.password.equals(password)) {
                loggedInUser = u;
                return true;
            }
        }
        return false;
    }

    void displayMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n1. View All Books");
            System.out.println("2. Borrow Book");
            System.out.println("3. Return Book");
            if (loggedInUser != null && loggedInUser.role.equals("admin")) {
                System.out.println("4. Manage Users");
                System.out.println("5. Manage Catalogue");
                System.out.println("6. View Transactions");
            }
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline
            switch (choice) {
                case 1:
                    viewAllBooks();
                    break;
                case 2:
                    borrowBook(sc);
                    break;
                case 3:
                    returnBook(sc);
                    break;
                case 4:
                    if (loggedInUser != null && loggedInUser.role.equals("admin")) manageUsers(sc);
                    break;
                case 5:
                    if (loggedInUser != null && loggedInUser.role.equals("admin")) manageCatalogue(sc);
                    break;
                case 6:
                    if (loggedInUser != null && loggedInUser.role.equals("admin")) viewTransactions(sc);
                    break;
                case 0:
                    saveAll();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    void viewAllBooks() {
        if (books.isEmpty()) {
            System.out.println("No books available.");
            return;
        }
        for (Book b : books) {
            b.displayBookDetails();
        }
    }

    void borrowBook(Scanner sc) {
        if (loggedInUser.borrowedBooks.size() >= 3) {
            System.out.println("You cannot borrow more than 3 books at once.");
            return;
        }
        System.out.print("Enter Book ID: ");
        String bid = sc.nextLine();
        for (Book b : books) {
            if (b.bookId.equals(bid) && b.available) {
                b.available = false;
                loggedInUser.borrowedBooks.add(bid);
                String tid = "T" + String.format("%03d", transactions.size() + 1);
                String date = LocalDate.now().toString();
                transactions.add(new Transaction(tid, loggedInUser.id, bid, date, null));
                System.out.println("Book borrowed successfully!");
                return;
            }
        }
        System.out.println("Book not available or invalid ID.");
    }

    void returnBook(Scanner sc) {
        System.out.print("Enter Book ID: ");
        String bid = sc.nextLine();
        if (loggedInUser.borrowedBooks.contains(bid)) {
            loggedInUser.borrowedBooks.remove(bid);
            for (Book b : books) {
                if (b.bookId.equals(bid)) {
                    b.available = true;
                    break;
                }
            }
            for (Transaction t : transactions) {
                if (t.userId.equals(loggedInUser.id) && t.bookId.equals(bid) && t.dateReturned == null) {
                    t.dateReturned = LocalDate.now().toString();
                    break;
                }
            }
            System.out.println("Book returned successfully!");
        } else {
            System.out.println("You haven't borrowed this book.");
        }
    }

    void manageUsers(Scanner sc) {
        while (true) {
            System.out.println("\nManage Users:");
            System.out.println("1. Add User");
            System.out.println("2. Update User");
            System.out.println("3. Delete User");
            System.out.println("4. Display Users");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            int ch = sc.nextInt();
            sc.nextLine();
            switch (ch) {
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
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    void addUser(Scanner sc) {
        System.out.print("ID: ");
        String id = sc.nextLine();
        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Password: ");
        String pass = sc.nextLine();
        System.out.print("Role: ");
        String role = sc.nextLine();
        users.add(new User(id, name, pass, role));
        System.out.println("User added.");
    }

    void updateUser(Scanner sc) {
        System.out.print("Enter User ID to update: ");
        String id = sc.nextLine();
        for (User u : users) {
            if (u.id.equals(id)) {
                System.out.print("New Name: ");
                u.name = sc.nextLine();
                System.out.print("New Password: ");
                u.password = sc.nextLine();
                System.out.print("New Role: ");
                u.role = sc.nextLine();
                System.out.println("User updated.");
                return;
            }
        }
        System.out.println("User not found.");
    }

    void deleteUser(Scanner sc) {
        System.out.print("Enter User ID to delete: ");
        String id = sc.nextLine();
        users.removeIf(u -> u.id.equals(id));
        System.out.println("User deleted if existed.");
    }

    void displayUsers() {
        if (users.isEmpty()) {
            System.out.println("No users.");
            return;
        }
        for (User u : users) {
            // Polymorphism: Using Person reference
            Person p = u;
            p.displayInfo();
        }
    }

    void manageCatalogue(Scanner sc) {
        while (true) {
            System.out.println("\nManage Catalogue:");
            System.out.println("1. Add Book");
            System.out.println("2. Update Book");
            System.out.println("3. Delete Book");
            System.out.println("4. Display Books");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            int ch = sc.nextInt();
            sc.nextLine();
            switch (ch) {
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
                    viewAllBooks();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    void addBook(Scanner sc) {
        System.out.print("ID: ");
        String id = sc.nextLine();
        System.out.print("Title: ");
        String title = sc.nextLine();
        System.out.print("Author: ");
        String author = sc.nextLine();
        books.add(new Book(id, title, author, true));
        System.out.println("Book added.");
    }

    void updateBook(Scanner sc) {
        System.out.print("Enter Book ID to update: ");
        String id = sc.nextLine();
        for (Book b : books) {
            if (b.bookId.equals(id)) {
                System.out.print("New Title: ");
                b.title = sc.nextLine();
                System.out.print("New Author: ");
                b.author = sc.nextLine();
                System.out.println("Book updated.");
                return;
            }
        }
        System.out.println("Book not found.");
    }

    void deleteBook(Scanner sc) {
        System.out.print("Enter Book ID to delete: ");
        String id = sc.nextLine();
        books.removeIf(b -> b.bookId.equals(id));
        System.out.println("Book deleted if existed.");
    }

    void viewTransactions(Scanner sc) {
        while (true) {
            System.out.println("\nView Transactions:");
            System.out.println("1. View All Transactions");
            System.out.println("2. View By User");
            System.out.println("3. View By Book");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            int ch = sc.nextInt();
            sc.nextLine();
            switch (ch) {
                case 1:
                    for (Transaction t : transactions) {
                        t.displayTransaction();
                    }
                    break;
                case 2:
                    System.out.print("Enter User ID: ");
                    String uid = sc.nextLine();
                    for (Transaction t : transactions) {
                        if (t.userId.equals(uid)) {
                            t.displayTransaction();
                        }
                    }
                    break;
                case 3:
                    System.out.print("Enter Book ID: ");
                    String bid = sc.nextLine();
                    for (Transaction t : transactions) {
                        if (t.bookId.equals(bid)) {
                            t.displayTransaction();
                        }
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static void main(String[] args) {
        LibrarySystem ls = new LibrarySystem();
        ls.loadUsers();
        ls.loadBooks();
        ls.loadTransactions();
        Scanner sc = new Scanner(System.in);
        int attempts = 3;
        System.out.println("Welcome to the Library Management System");
        System.out.println("----------------------------------------");
        while (attempts > 0) {
            System.out.println("Please log in to continue.");
            System.out.print("Username: ");
            String user = sc.nextLine();
            System.out.print("Password: ");
            String pass = sc.nextLine();
            if (ls.login(user, pass)) {
                System.out.println("Login successful! Welcome, " + ls.loggedInUser.name);
                ls.displayMenu();
            } else {
                attempts--;
                System.out.println("Invalid username or password. Try again. (Attempts left: " + attempts + ")");
            }
        }
        System.out.println("Too many failed attempts. Exiting.");
    }
}
