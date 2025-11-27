CREATE DATABASE  IF NOT EXISTS `nenavredi` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `nenavredi`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: nenavredi
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `okaz_uslyga`
--

DROP TABLE IF EXISTS `okaz_uslyga`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `okaz_uslyga` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `users_service_id` int NOT NULL,
  `utilizator_id` int DEFAULT NULL,
  `status` enum('Назначена','В работе','Завершена') DEFAULT 'Назначена',
  `started_at` datetime DEFAULT NULL,
  `finished_at` datetime DEFAULT NULL,
  `archived` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `order_id` (`order_id`),
  KEY `users_service_id` (`users_service_id`),
  KEY `utilizator_id` (`utilizator_id`),
  CONSTRAINT `okaz_users_service_fk` FOREIGN KEY (`users_service_id`) REFERENCES `users_services` (`id`),
  CONSTRAINT `okaz_uslyga_fk1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `okaz_uslyga_fk3` FOREIGN KEY (`utilizator_id`) REFERENCES `utilizator` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `okaz_uslyga`
--

LOCK TABLES `okaz_uslyga` WRITE;
/*!40000 ALTER TABLE `okaz_uslyga` DISABLE KEYS */;
INSERT INTO `okaz_uslyga` VALUES (1,1,1,1,'В работе','2024-01-15 11:00:00','2024-01-20 16:00:00',0),(2,2,4,2,'Назначена','2024-01-17 10:00:00',NULL,0),(3,3,9,3,'Завершена','2024-01-17 10:00:00','2024-01-24 16:00:00',0);
/*!40000 ALTER TABLE `okaz_uslyga` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-27 12:40:27
