-- MySQL dump 10.13  Distrib 5.7.22, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: mybatis
-- ------------------------------------------------------
-- Server version	5.7.22-0ubuntu0.16.04.1

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
-- Table structure for table `department`
--

DROP TABLE IF EXISTS `department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `department` (
  `department_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '院系id',
  `name` varchar(64) NOT NULL COMMENT '院系名称',
  PRIMARY KEY (`department_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `department`
--

LOCK TABLES `department` WRITE;
/*!40000 ALTER TABLE `department` DISABLE KEYS */;
INSERT INTO `department` VALUES (1,'计算机科学学院'),(2,'石油工程学院'),(3,'机械电子学院'),(4,'理学院'),(5,'化学工程学院');
/*!40000 ALTER TABLE `department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `female_attr`
--

DROP TABLE IF EXISTS `female_attr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `female_attr` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `cosmetics` varchar(50) DEFAULT '神仙水' COMMENT '化妆品',
  `student_id` bigint(20) NOT NULL COMMENT '学生id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='女生的特性';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `female_attr`
--

LOCK TABLES `female_attr` WRITE;
/*!40000 ALTER TABLE `female_attr` DISABLE KEYS */;
INSERT INTO `female_attr` VALUES (1,'神仙水',2);
/*!40000 ALTER TABLE `female_attr` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hobby`
--

DROP TABLE IF EXISTS `hobby`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hobby` (
  `hobby_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '爱好id',
  `name` varchar(20) NOT NULL COMMENT '爱好名称',
  PRIMARY KEY (`hobby_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='爱好';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hobby`
--

LOCK TABLES `hobby` WRITE;
/*!40000 ALTER TABLE `hobby` DISABLE KEYS */;
INSERT INTO `hobby` VALUES (1,'打篮球'),(2,'打羽毛球'),(3,'打乒乓球'),(4,'唱歌'),(5,'跳舞');
/*!40000 ALTER TABLE `hobby` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `male_attr`
--

DROP TABLE IF EXISTS `male_attr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `male_attr` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `game` varchar(50) DEFAULT '英雄联盟' COMMENT '游戏',
  `student_id` bigint(20) NOT NULL COMMENT '学生id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8 COMMENT='男生的特性';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `male_attr`
--

LOCK TABLES `male_attr` WRITE;
/*!40000 ALTER TABLE `male_attr` DISABLE KEYS */;
INSERT INTO `male_attr` VALUES (1,'英雄联盟',1);
/*!40000 ALTER TABLE `male_attr` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `student` (
  `student_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `department_id` int(11) NOT NULL,
  `number` varchar(20) NOT NULL COMMENT '学号',
  `name` varchar(20) NOT NULL COMMENT '姓名',
  `birthday` date NOT NULL COMMENT '生日',
  `sex` int(11) NOT NULL DEFAULT '1' COMMENT '性别：1-男;2-女',
  PRIMARY KEY (`student_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8 COMMENT='学生表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (1,1,'201801001','张一','1996-01-01',1),(2,1,'201801002','张二','1996-01-02',2),(3,1,'201801003','张三','1996-01-03',1),(4,1,'201801004','张四','1996-01-04',1),(5,2,'201802001','李一','1996-02-01',1),(6,2,'201802002','李二','1996-02-02',1),(7,2,'201802003','李三','1996-02-03',1),(8,2,'201802004','李四','1996-02-04',1),(9,3,'201803001','王一','1996-03-01',1),(10,3,'201803002','王二','1996-03-02',1),(11,3,'201803003','王三','1996-03-03',1),(12,3,'201803004','王四','1996-03-04',1),(13,4,'201804001','吴一','1996-04-01',1),(14,4,'201804002','吴二','1996-04-02',1),(15,4,'201804003','吴三','1996-04-03',1),(16,4,'201804004','吴四','1996-04-04',1),(17,5,'201805001','孙一','1996-05-01',1),(18,5,'201805002','孙二','1996-05-02',1),(19,5,'201805003','孙三','1996-05-03',1),(20,5,'201805004','孙四','1996-05-04',1);
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_hobby`
--

DROP TABLE IF EXISTS `student_hobby`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `student_hobby` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `student_id` int(11) NOT NULL COMMENT '学生id',
  `hobby_id` int(11) NOT NULL COMMENT '爱好id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8 COMMENT='学生与爱好的映射表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_hobby`
--

LOCK TABLES `student_hobby` WRITE;
/*!40000 ALTER TABLE `student_hobby` DISABLE KEYS */;
INSERT INTO `student_hobby` VALUES (1,1,1),(2,1,2),(3,1,3),(4,1,4),(5,1,5),(6,2,1),(7,2,2),(8,2,3),(9,2,4),(10,2,5),(11,3,1),(12,3,2),(13,3,3),(14,3,4),(15,3,5),(16,4,1),(17,4,2),(18,4,3),(19,4,4),(20,4,5),(21,5,1),(22,5,2),(23,5,3),(24,5,4),(25,5,5),(26,6,1),(27,6,2),(28,6,3),(29,6,4),(30,6,5),(31,7,1),(32,7,2),(33,7,3),(34,7,4),(35,7,5),(36,8,1),(37,8,2),(38,8,3),(39,8,4),(40,8,5),(41,9,1),(42,9,2),(43,9,3),(44,9,4),(45,9,5),(46,10,1),(47,10,2),(48,10,3),(49,10,4),(50,10,5),(51,11,1),(52,11,2),(53,11,3),(54,11,4),(55,11,5),(56,12,1),(57,12,2),(58,12,3),(59,12,4),(60,12,5),(61,13,1),(62,13,2),(63,13,3),(64,13,4),(65,13,5),(66,14,1),(67,14,2),(68,14,3),(69,14,4),(70,14,5),(71,15,1),(72,15,2),(73,15,3),(74,15,4),(75,15,5),(76,16,1),(77,16,2),(78,16,3),(79,16,4),(80,16,5),(81,17,1),(82,17,2),(83,17,3),(84,17,4),(85,17,5),(86,18,1),(87,18,2),(88,18,3),(89,18,4),(90,18,5),(91,19,1),(92,19,2),(93,19,3),(94,19,4),(95,19,5),(96,20,1),(97,20,2),(98,20,3),(99,20,4),(100,20,5);
/*!40000 ALTER TABLE `student_hobby` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-07-26 11:38:18