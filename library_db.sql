CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

CREATE TABLE books (
    book_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    quantity INT DEFAULT 0
);

CREATE TABLE students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    grade VARCHAR(10),
    email VARCHAR(100)
);

CREATE TABLE transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    book_id INT,
    student_id INT,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    FOREIGN KEY (book_id) REFERENCES books(book_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id)
);

-- Test Data
INSERT INTO books (title, author, isbn, quantity) 
VALUES 
('Java Programming', 'John Doe', '123-456', 5),
('Python Basics', 'Jane Smith', '789-012', 3);

INSERT INTO students (name, grade, email)
VALUES
('Alice Johnson', '10A', 'alice@school.edu'),
('Bob Williams', '11B', 'bob@school.edu');