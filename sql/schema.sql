CREATE DATABASE IF NOT EXISTS bank_demo DEFAULT CHARACTER SET utf8mb4;
USE bank_demo;

DROP TABLE IF EXISTS buchung;
DROP TABLE IF EXISTS konto;

CREATE TABLE konto (
  ktonr   INT PRIMARY KEY,
  inhaber VARCHAR(50) NOT NULL,
  saldo   DECIMAL(10,2) NOT NULL CHECK (saldo >= 0)
) ENGINE=InnoDB;

CREATE TABLE buchung (
  buchung_id INT AUTO_INCREMENT PRIMARY KEY,
  von_kto    INT NOT NULL,
  nach_kto   INT NOT NULL,
  betrag     DECIMAL(10,2) NOT NULL,
  zeitpunkt  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (von_kto)  REFERENCES konto (ktonr),
  FOREIGN KEY (nach_kto) REFERENCES konto (ktonr)
) ENGINE=InnoDB;

INSERT INTO konto (ktonr, inhaber, saldo) VALUES
  (1, 'Max Muster', 1000.00),
  (2, 'Anna Kunde',  500.00);