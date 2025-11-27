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
-- Table structure for table `services`
--

DROP TABLE IF EXISTS `services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `services` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `cost` decimal(10,2) NOT NULL,
  `code` varchar(50) DEFAULT NULL,
  `duration_days` int DEFAULT NULL,
  `deviation_avg` decimal(5,2) DEFAULT NULL,
  `archived` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `services`
--

LOCK TABLES `services` WRITE;
/*!40000 ALTER TABLE `services` DISABLE KEYS */;
INSERT INTO `services` VALUES (1,'Утилизация батареек',262.71,'619',3,2.50,0),(2,'Утилизация аккумуляторов',361.88,'311',5,3.20,0),(3,'Утилизация ртутных градусников',234.09,'548',2,1.80,0),(4,'Утилизация лекарственных препаратов',143.22,'258',1,1.00,0),(5,'Утилизация сильно действующих химических препаратов',102.85,'176',7,4.10,0),(6,'Утилизация медицинских отходов',176.83,'501',2,1.50,0),(7,'Утилизация промышленных и производственных отходов',289.99,'543',10,5.20,0),(8,'Утилизация отходов химической промышленности',490.77,'557',14,6.80,0),(9,'Выезд курьера',341.78,'229',1,0.50,0),(10,'Хранение отходов',419.90,'415',30,2.10,0),(11,'Эко-паспорт',447.65,'323',7,3.50,0),(12,'Формирование карточки предприятия',209.78,'855',3,1.20,0),(13,'Формирование карточки частного лица',396.03,'346',2,0.80,0),(14,'Независимая экспертиза опасных отходов',105.32,'836',5,2.30,0),(15,'Исследования воздушной среды',443.66,'659',8,3.80,0),(16,'Исследования твердых отходов',370.62,'797',6,2.90,0),(17,'Исследования жидких отходов',290.11,'287',4,2.00,0);
/*!40000 ALTER TABLE `services` ENABLE KEYS */;
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
