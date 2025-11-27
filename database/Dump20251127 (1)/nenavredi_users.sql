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
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `login` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `fio` varchar(150) NOT NULL,
  `last_login` datetime DEFAULT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `type_user_id` int DEFAULT NULL,
  `archived` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `login` (`login`),
  KEY `type_user_id` (`type_user_id`),
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`type_user_id`) REFERENCES `type_users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'chacking0','4tzqHdkqzo4','Clareta Hacking','2020-10-02 00:00:00','147.231.50.234',1,0),(2,'nmably1','ukM0e6','Northrop Mably','2020-06-20 00:00:00','22.32.15.211',2,0),(3,'frolf2','7QpCwac0yi','Fabian Rolf','2020-05-19 00:00:00','113.92.142.29',1,0),(4,'lraden3','5Ydp2mz','Lauree Raden','2020-06-22 00:00:00','39.24.146.52',1,0),(5,'bfollos4','ckmAJPQV','Barby Follos','2020-03-18 00:00:00','87.232.97.3',4,0),(6,'menterle5','0PRom6i','Mile Enterle','2020-07-04 00:00:00','85.121.209.6',2,0),(7,'mpeaker6','0Tc5oRc','Midge Peaker','2020-09-03 00:00:00','196.39.132.128',2,0),(8,'mrobichon7','LEwEjMlmE5X','Manon Robichon','2020-08-31 00:00:00','143.159.207.105',2,0),(9,'srobken8','Cbmj3Yi','Stavro Robken','2020-05-22 00:00:00','12.154.73.196',3,0),(10,'btidmas9','dYDHbBQfK','Bryan Tidmas','2020-06-06 00:00:00','24.42.134.21',4,0),(11,'jfussiea','EGxXuLQ9','Jeannette Fussie','2020-08-21 00:00:00','98.194.112.137',2,0),(12,'santonaccib','YcXAhY3Pja','Stephen Antonacci','2019-10-03 00:00:00','198.146.255.15',4,0),(13,'nbountiffc','5xfyjS9ZULGA','Niccolo Bountiff','2020-01-22 00:00:00','231.78.246.229',1,0),(14,'cbenjefieldd','tQOsP0ei9TuD','Clemente Benjefield','2020-07-09 00:00:00','88.126.93.246',4,0),(15,'ocorbyne','bG1ZIzwIoU','Orlan Corbyn','2020-04-24 00:00:00','232.175.48.179',3,0),(16,'cstickinsf','QRYADbgNj','Coreen Stickins','2020-04-20 00:00:00','64.30.175.158',1,0),(17,'dclarageg','Yp59ZIDnWe','Daveta Clarage','2020-06-09 00:00:00','139.88.229.111',3,0),(18,'jmccawleyh','g58zLcmCYON','Javier McCawley','2020-04-20 00:00:00','14.199.67.32',4,0),(19,'dbandi','yFAaYuVW','Daile Band','2019-12-02 00:00:00','206.105.148.56',3,0),(20,'abutteryj','ttOFbWWGtD','Angil Buttery','2020-06-21 00:00:00','192.158.7.138',2,0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
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
