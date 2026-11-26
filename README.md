# Bank Transaction Lab

A small Java + JDBC project that shows what database transactions do and what goes wrong without them. Everything is built on a simple bank with accounts and transfers (MariaDB, InnoDB).

## What it demonstrates

| Topic | Demo class | What you see |
|---|---|---|
| Atomicity | `Main` | A crash between two UPDATEs loses money without a transaction. With `commit()` / `rollback()` the transfer happens completely or not at all. |
| Consistency | `Main` | A `CHECK (saldo >= 0)` constraint rejects a transfer with too little money, and the transaction is rolled back. |
| Isolation | `LostUpdateDemo` | Two parallel deposits overwrite each other (lost update). `SELECT ... FOR UPDATE` fixes it with a row lock. |
| Deadlocks | `DeadlockDemo` | Two opposite transfers block each other. InnoDB aborts one with error 1213, and the application retries. |

## Tech stack

- Java 21, Maven
- JDBC with the MariaDB driver
- MariaDB / MySQL (InnoDB)

## Setup

1. Start MariaDB (for example with XAMPP).
2. Run `sql/schema.sql` to create the database `bank_demo` with two accounts.
3. Create a database user:
   ```sql
   CREATE USER 'bank'@'localhost' IDENTIFIED BY 'bank123';
   GRANT ALL PRIVILEGES ON bank_demo.* TO 'bank'@'localhost';
   ```
4. Open the Maven project in the folder `bank-transaction-lab` and run one of the classes `Main`, `LostUpdateDemo` or `DeadlockDemo`.

The connection settings can be changed with the environment variables `DB_URL`, `DB_USER` and `DB_PASSWORD`. The defaults (`bank` / `bank123`) are only meant for a local practice database.

## Key ideas

- `setAutoCommit(false)` starts a transaction, `commit()` confirms it, `rollback()` undoes it.
- `PreparedStatement` is used for all queries with parameters.
- Rolling back in the `catch` block keeps the connection clean after errors.

## Author

Mariam Barakzai, student of computer science (HTL).