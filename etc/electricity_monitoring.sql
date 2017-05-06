-- MySQL dump 10.15  Distrib 10.0.29-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: localhost
-- ------------------------------------------------------
-- Server version	10.0.29-MariaDB-0ubuntu0.16.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `pwd` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `powerstations`
--

DROP TABLE IF EXISTS `powerstations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `powerstations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ptype` varchar(255) NOT NULL,
  `capacity` double NOT NULL,
  `stock` double NOT NULL,
  `user_id` int(11) NOT NULL,
  `created` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_users` (`user_id`),
  CONSTRAINT `fk_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `powerstationevents`
--

DROP TABLE IF EXISTS `powerstationevents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `powerstationevents` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `amount` double NOT NULL,
  `ts` datetime NOT NULL,
  `current_amount` double NOT NULL,
  `power_station_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_power_stations` (`power_station_id`),
  CONSTRAINT `fk_power_stations` FOREIGN KEY (`power_station_id`) REFERENCES `powerstations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
