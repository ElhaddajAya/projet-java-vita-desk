-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Dec 26, 2025 at 12:55 AM
-- Server version: 9.1.0
-- PHP Version: 8.3.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `vitadesk`
--

-- --------------------------------------------------------

--
-- Table structure for table `consultation`
--

DROP TABLE IF EXISTS `consultation`;
CREATE TABLE IF NOT EXISTS `consultation` (
  `idConsultation` int NOT NULL AUTO_INCREMENT,
  `dateConsultation` date NOT NULL,
  `diagnostic` text COLLATE utf8mb4_general_ci,
  `traitement` text COLLATE utf8mb4_general_ci,
  `observations` text COLLATE utf8mb4_general_ci,
  `prixConsultation` double DEFAULT '300',
  `idPatient` int NOT NULL,
  `idMedecin` int NOT NULL,
  `idRDV` int DEFAULT NULL,
  PRIMARY KEY (`idConsultation`),
  KEY `idPatient` (`idPatient`),
  KEY `idMedecin` (`idMedecin`),
  KEY `idRDV` (`idRDV`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `medecin`
--

DROP TABLE IF EXISTS `medecin`;
CREATE TABLE IF NOT EXISTS `medecin` (
  `idMedecin` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `prenom` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `specialite` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `telephone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `idUtilisateur` int DEFAULT NULL,
  PRIMARY KEY (`idMedecin`),
  UNIQUE KEY `idUtilisateur` (`idUtilisateur`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `medecin`
--

INSERT INTO `medecin` (`idMedecin`, `nom`, `prenom`, `specialite`, `telephone`, `email`, `idUtilisateur`) VALUES
(1, 'ESSADI', 'Alae', 'Généraliste', '0600000000', 'alae@example.com', 4);

-- --------------------------------------------------------

--
-- Table structure for table `patient`
--

DROP TABLE IF EXISTS `patient`;
CREATE TABLE IF NOT EXISTS `patient` (
  `idPatient` int NOT NULL AUTO_INCREMENT,
  `numSecuSociale` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `nom` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `prenom` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `dateNaissance` date DEFAULT NULL,
  `telephone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cin` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sexe` enum('M','F') COLLATE utf8mb4_general_ci DEFAULT NULL,
  `adresse` text COLLATE utf8mb4_general_ci,
  PRIMARY KEY (`idPatient`),
  UNIQUE KEY `numSecuSociale` (`numSecuSociale`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `patient`
--

INSERT INTO `patient` (`idPatient`, `numSecuSociale`, `nom`, `prenom`, `dateNaissance`, `telephone`, `cin`, `sexe`, `adresse`) VALUES
(1, '123456789', 'BENALI', 'Karim', '1980-01-01', '0600000000', 'CIN123', 'M', 'Casablanca'),
(2, '987654321', 'ZOUHAIR', 'Sara', '1995-05-15', '0611111111', 'CIN456', 'F', 'Rabat'),
(3, '555666777', 'LAHLOU', 'Ahmed', '1988-10-10', '0622222222', 'CIN789', 'M', 'Marrakech');

-- --------------------------------------------------------

--
-- Table structure for table `rendez_vous`
--

DROP TABLE IF EXISTS `rendez_vous`;
CREATE TABLE IF NOT EXISTS `rendez_vous` (
  `idRDV` int NOT NULL AUTO_INCREMENT,
  `dateRDV` date NOT NULL,
  `heureRDV` time NOT NULL,
  `motif` text COLLATE utf8mb4_general_ci,
  `statut` enum('PREVU','EFFECTUE','ANNULE','REPORTE','ABSENT') COLLATE utf8mb4_general_ci DEFAULT 'PREVU',
  `idPatient` int NOT NULL,
  `idMedecin` int NOT NULL,
  PRIMARY KEY (`idRDV`),
  KEY `idPatient` (`idPatient`),
  KEY `idMedecin` (`idMedecin`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `secretaire`
--

DROP TABLE IF EXISTS `secretaire`;
CREATE TABLE IF NOT EXISTS `secretaire` (
  `idSecretaire` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `prenom` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `telephone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `idUtilisateur` int DEFAULT NULL,
  PRIMARY KEY (`idSecretaire`),
  UNIQUE KEY `idUtilisateur` (`idUtilisateur`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `secretaire`
--

INSERT INTO `secretaire` (`idSecretaire`, `nom`, `prenom`, `telephone`, `email`, `idUtilisateur`) VALUES
(1, 'ADMIN', 'Secretaire', '0611111111', 'sec@example.com', 1);

-- --------------------------------------------------------

--
-- Table structure for table `utilisateur`
--

DROP TABLE IF EXISTS `utilisateur`;
CREATE TABLE IF NOT EXISTS `utilisateur` (
  `idUtilisateur` int NOT NULL AUTO_INCREMENT,
  `login` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `motDePasse` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `role` enum('SECRETAIRE','MEDECIN') COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`idUtilisateur`),
  UNIQUE KEY `login` (`login`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `utilisateur`
--

INSERT INTO `utilisateur` (`idUtilisateur`, `login`, `motDePasse`, `role`) VALUES
(1, 'secretaire', '$2a$10$QO1J0mPrR0rWGeQhwCOWdO1VRSsHMoa3DJbwqayTsbzQ7Qd7G6tF.', 'SECRETAIRE'),
(2, 'dr.ahmadi', '$2a$10$QO1J0mPrR0rWGeQhwCOWdO1VRSsHMoa3DJbwqayTsbzQ7Qd7G6tF.', 'MEDECIN'),
(3, 'dr.fatima', '$2a$10$QO1J0mPrR0rWGeQhwCOWdO1VRSsHMoa3DJbwqayTsbzQ7Qd7G6tF.', 'MEDECIN'),
(4, 'medecin', '$2a$10$QO1J0mPrR0rWGeQhwCOWdO1VRSsHMoa3DJbwqayTsbzQ7Qd7G6tF.', 'MEDECIN');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
