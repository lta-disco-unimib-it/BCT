-- MySQL dump 10.10
--
-- Host: localhost    Database: bctSimple
-- ------------------------------------------------------
-- Server version	5.0.26-log

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
-- Table structure for table `beginenddeclaration`
--

DROP TABLE IF EXISTS `beginenddeclaration`;
CREATE TABLE `beginenddeclaration` (
  `idBeginEndDeclaration` int(10) unsigned NOT NULL auto_increment,
  `method_idMethod` int(10) unsigned NOT NULL,
  `beginDeclaration` text,
  `endDeclaration` text,
  PRIMARY KEY  (`idBeginEndDeclaration`),
  KEY `beginenddeclaration_FKIndex1` (`method_idMethod`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `beginenddeclaration`
--

LOCK TABLES `beginenddeclaration` WRITE;
/*!40000 ALTER TABLE `beginenddeclaration` DISABLE KEYS */;
/*!40000 ALTER TABLE `beginenddeclaration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `beginendexecmethod`
--

DROP TABLE IF EXISTS `beginendexecmethod`;
CREATE TABLE `beginendexecmethod` (
  `idBeginEndExecMethod` int(10) unsigned NOT NULL auto_increment,
  `thread_idThread` int(10) unsigned NOT NULL,
  `method_idMethod` int(10) unsigned NOT NULL,
  `occurrence` int(10) unsigned NOT NULL,
  `beginEnd` varchar(2) NOT NULL,
  `startMethod` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`idBeginEndExecMethod`),
  KEY `beginendexecmethod_FKIndex1` (`method_idMethod`),
  KEY `beginendexecmethod_FKIndex2` (`thread_idThread`)
) ENGINE=MyISAM AUTO_INCREMENT=401 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `beginendexecmethod`
--

LOCK TABLES `beginendexecmethod` WRITE;
/*!40000 ALTER TABLE `beginendexecmethod` DISABLE KEYS */;
INSERT INTO `beginendexecmethod` VALUES (1,1,1,1,'B#',0),(2,1,2,2,'B#',0),(3,1,2,3,'E#',2),(4,1,1,4,'E#',1),(5,1,1,5,'B#',0),(6,1,2,6,'B#',0),(7,1,2,7,'E#',6),(8,1,1,8,'E#',5),(9,1,1,9,'B#',0),(10,1,2,10,'B#',0),(11,1,2,11,'E#',10),(12,1,1,12,'E#',9),(13,1,1,13,'B#',0),(14,1,2,14,'B#',0),(15,1,2,15,'E#',14),(16,1,1,16,'E#',13),(17,1,1,17,'B#',0),(18,1,2,18,'B#',0),(19,1,2,19,'E#',18),(20,1,1,20,'E#',17),(21,1,1,21,'B#',0),(22,1,2,22,'B#',0),(23,1,2,23,'E#',22),(24,1,1,24,'E#',21),(25,1,1,25,'B#',0),(26,1,2,26,'B#',0),(27,1,2,27,'E#',26),(28,1,1,28,'E#',25),(29,1,1,29,'B#',0),(30,1,2,30,'B#',0),(31,1,2,31,'E#',30),(32,1,1,32,'E#',29),(33,1,1,33,'B#',0),(34,1,2,34,'B#',0),(35,1,2,35,'E#',34),(36,1,1,36,'E#',33),(37,1,1,37,'B#',0),(38,1,2,38,'B#',0),(39,1,2,39,'E#',38),(40,1,1,40,'E#',37),(41,1,1,41,'B#',0),(42,1,2,42,'B#',0),(43,1,2,43,'E#',42),(44,1,1,44,'E#',41),(45,1,1,45,'B#',0),(46,1,2,46,'B#',0),(47,1,2,47,'E#',46),(48,1,1,48,'E#',45),(49,1,1,49,'B#',0),(50,1,2,50,'B#',0),(51,1,2,51,'E#',50),(52,1,1,52,'E#',49),(53,1,1,53,'B#',0),(54,1,2,54,'B#',0),(55,1,2,55,'E#',54),(56,1,1,56,'E#',53),(57,1,1,57,'B#',0),(58,1,2,58,'B#',0),(59,1,2,59,'E#',58),(60,1,1,60,'E#',57),(61,1,1,61,'B#',0),(62,1,2,62,'B#',0),(63,1,2,63,'E#',62),(64,1,1,64,'E#',61),(65,1,1,65,'B#',0),(66,1,2,66,'B#',0),(67,1,2,67,'E#',66),(68,1,1,68,'E#',65),(69,1,1,69,'B#',0),(70,1,2,70,'B#',0),(71,1,2,71,'E#',70),(72,1,1,72,'E#',69),(73,1,1,73,'B#',0),(74,1,2,74,'B#',0),(75,1,2,75,'E#',74),(76,1,1,76,'E#',73),(77,1,1,77,'B#',0),(78,1,2,78,'B#',0),(79,1,2,79,'E#',78),(80,1,1,80,'E#',77),(81,1,1,81,'B#',0),(82,1,2,82,'B#',0),(83,1,2,83,'E#',82),(84,1,1,84,'E#',81),(85,1,1,85,'B#',0),(86,1,2,86,'B#',0),(87,1,2,87,'E#',86),(88,1,1,88,'E#',85),(89,1,1,89,'B#',0),(90,1,2,90,'B#',0),(91,1,2,91,'E#',90),(92,1,1,92,'E#',89),(93,1,1,93,'B#',0),(94,1,2,94,'B#',0),(95,1,2,95,'E#',94),(96,1,1,96,'E#',93),(97,1,1,97,'B#',0),(98,1,2,98,'B#',0),(99,1,2,99,'E#',98),(100,1,1,100,'E#',97),(101,1,1,101,'B#',0),(102,1,2,102,'B#',0),(103,1,2,103,'E#',102),(104,1,1,104,'E#',101),(105,1,1,105,'B#',0),(106,1,2,106,'B#',0),(107,1,2,107,'E#',106),(108,1,1,108,'E#',105),(109,1,1,109,'B#',0),(110,1,2,110,'B#',0),(111,1,2,111,'E#',110),(112,1,1,112,'E#',109),(113,1,1,113,'B#',0),(114,1,2,114,'B#',0),(115,1,2,115,'E#',114),(116,1,1,116,'E#',113),(117,1,1,117,'B#',0),(118,1,2,118,'B#',0),(119,1,2,119,'E#',118),(120,1,1,120,'E#',117),(121,1,1,121,'B#',0),(122,1,2,122,'B#',0),(123,1,2,123,'E#',122),(124,1,1,124,'E#',121),(125,1,1,125,'B#',0),(126,1,2,126,'B#',0),(127,1,2,127,'E#',126),(128,1,1,128,'E#',125),(129,1,1,129,'B#',0),(130,1,2,130,'B#',0),(131,1,2,131,'E#',130),(132,1,1,132,'E#',129),(133,1,1,133,'B#',0),(134,1,2,134,'B#',0),(135,1,2,135,'E#',134),(136,1,1,136,'E#',133),(137,1,1,137,'B#',0),(138,1,2,138,'B#',0),(139,1,2,139,'E#',138),(140,1,1,140,'E#',137),(141,1,1,141,'B#',0),(142,1,2,142,'B#',0),(143,1,2,143,'E#',142),(144,1,1,144,'E#',141),(145,1,1,145,'B#',0),(146,1,2,146,'B#',0),(147,1,2,147,'E#',146),(148,1,1,148,'E#',145),(149,1,1,149,'B#',0),(150,1,2,150,'B#',0),(151,1,2,151,'E#',150),(152,1,1,152,'E#',149),(153,1,1,153,'B#',0),(154,1,2,154,'B#',0),(155,1,2,155,'E#',154),(156,1,1,156,'E#',153),(157,1,1,157,'B#',0),(158,1,2,158,'B#',0),(159,1,2,159,'E#',158),(160,1,1,160,'E#',157),(161,1,1,161,'B#',0),(162,1,2,162,'B#',0),(163,1,2,163,'E#',162),(164,1,1,164,'E#',161),(165,1,1,165,'B#',0),(166,1,2,166,'B#',0),(167,1,2,167,'E#',166),(168,1,1,168,'E#',165),(169,1,1,169,'B#',0),(170,1,2,170,'B#',0),(171,1,2,171,'E#',170),(172,1,1,172,'E#',169),(173,1,1,173,'B#',0),(174,1,2,174,'B#',0),(175,1,2,175,'E#',174),(176,1,1,176,'E#',173),(177,1,1,177,'B#',0),(178,1,2,178,'B#',0),(179,1,2,179,'E#',178),(180,1,1,180,'E#',177),(181,1,1,181,'B#',0),(182,1,2,182,'B#',0),(183,1,2,183,'E#',182),(184,1,1,184,'E#',181),(185,1,1,185,'B#',0),(186,1,2,186,'B#',0),(187,1,2,187,'E#',186),(188,1,1,188,'E#',185),(189,1,1,189,'B#',0),(190,1,2,190,'B#',0),(191,1,2,191,'E#',190),(192,1,1,192,'E#',189),(193,1,1,193,'B#',0),(194,1,2,194,'B#',0),(195,1,2,195,'E#',194),(196,1,1,196,'E#',193),(197,1,1,197,'B#',0),(198,1,2,198,'B#',0),(199,1,2,199,'E#',198),(200,1,1,200,'E#',197),(201,1,1,201,'B#',0),(202,1,2,202,'B#',0),(203,1,2,203,'E#',202),(204,1,1,204,'E#',201),(205,1,1,205,'B#',0),(206,1,2,206,'B#',0),(207,1,2,207,'E#',206),(208,1,1,208,'E#',205),(209,1,1,209,'B#',0),(210,1,2,210,'B#',0),(211,1,2,211,'E#',210),(212,1,1,212,'E#',209),(213,1,1,213,'B#',0),(214,1,2,214,'B#',0),(215,1,2,215,'E#',214),(216,1,1,216,'E#',213),(217,1,1,217,'B#',0),(218,1,2,218,'B#',0),(219,1,2,219,'E#',218),(220,1,1,220,'E#',217),(221,1,1,221,'B#',0),(222,1,2,222,'B#',0),(223,1,2,223,'E#',222),(224,1,1,224,'E#',221),(225,1,1,225,'B#',0),(226,1,2,226,'B#',0),(227,1,2,227,'E#',226),(228,1,1,228,'E#',225),(229,1,1,229,'B#',0),(230,1,2,230,'B#',0),(231,1,2,231,'E#',230),(232,1,1,232,'E#',229),(233,1,1,233,'B#',0),(234,1,2,234,'B#',0),(235,1,2,235,'E#',234),(236,1,1,236,'E#',233),(237,1,1,237,'B#',0),(238,1,2,238,'B#',0),(239,1,2,239,'E#',238),(240,1,1,240,'E#',237),(241,1,1,241,'B#',0),(242,1,2,242,'B#',0),(243,1,2,243,'E#',242),(244,1,1,244,'E#',241),(245,1,1,245,'B#',0),(246,1,2,246,'B#',0),(247,1,2,247,'E#',246),(248,1,1,248,'E#',245),(249,1,1,249,'B#',0),(250,1,2,250,'B#',0),(251,1,2,251,'E#',250),(252,1,1,252,'E#',249),(253,1,1,253,'B#',0),(254,1,2,254,'B#',0),(255,1,2,255,'E#',254),(256,1,1,256,'E#',253),(257,1,1,257,'B#',0),(258,1,2,258,'B#',0),(259,1,2,259,'E#',258),(260,1,1,260,'E#',257),(261,1,1,261,'B#',0),(262,1,2,262,'B#',0),(263,1,2,263,'E#',262),(264,1,1,264,'E#',261),(265,1,1,265,'B#',0),(266,1,2,266,'B#',0),(267,1,2,267,'E#',266),(268,1,1,268,'E#',265),(269,1,1,269,'B#',0),(270,1,2,270,'B#',0),(271,1,2,271,'E#',270),(272,1,1,272,'E#',269),(273,1,1,273,'B#',0),(274,1,2,274,'B#',0),(275,1,2,275,'E#',274),(276,1,1,276,'E#',273),(277,1,1,277,'B#',0),(278,1,2,278,'B#',0),(279,1,2,279,'E#',278),(280,1,1,280,'E#',277),(281,1,1,281,'B#',0),(282,1,2,282,'B#',0),(283,1,2,283,'E#',282),(284,1,1,284,'E#',281),(285,1,1,285,'B#',0),(286,1,2,286,'B#',0),(287,1,2,287,'E#',286),(288,1,1,288,'E#',285),(289,1,1,289,'B#',0),(290,1,2,290,'B#',0),(291,1,2,291,'E#',290),(292,1,1,292,'E#',289),(293,1,1,293,'B#',0),(294,1,2,294,'B#',0),(295,1,2,295,'E#',294),(296,1,1,296,'E#',293),(297,1,1,297,'B#',0),(298,1,2,298,'B#',0),(299,1,2,299,'E#',298),(300,1,1,300,'E#',297),(301,1,1,301,'B#',0),(302,1,2,302,'B#',0),(303,1,2,303,'E#',302),(304,1,1,304,'E#',301),(305,1,1,305,'B#',0),(306,1,2,306,'B#',0),(307,1,2,307,'E#',306),(308,1,1,308,'E#',305),(309,1,1,309,'B#',0),(310,1,2,310,'B#',0),(311,1,2,311,'E#',310),(312,1,1,312,'E#',309),(313,1,1,313,'B#',0),(314,1,2,314,'B#',0),(315,1,2,315,'E#',314),(316,1,1,316,'E#',313),(317,1,1,317,'B#',0),(318,1,2,318,'B#',0),(319,1,2,319,'E#',318),(320,1,1,320,'E#',317),(321,1,1,321,'B#',0),(322,1,2,322,'B#',0),(323,1,2,323,'E#',322),(324,1,1,324,'E#',321),(325,1,1,325,'B#',0),(326,1,2,326,'B#',0),(327,1,2,327,'E#',326),(328,1,1,328,'E#',325),(329,1,1,329,'B#',0),(330,1,2,330,'B#',0),(331,1,2,331,'E#',330),(332,1,1,332,'E#',329),(333,1,1,333,'B#',0),(334,1,2,334,'B#',0),(335,1,2,335,'E#',334),(336,1,1,336,'E#',333),(337,1,1,337,'B#',0),(338,1,2,338,'B#',0),(339,1,2,339,'E#',338),(340,1,1,340,'E#',337),(341,1,1,341,'B#',0),(342,1,2,342,'B#',0),(343,1,2,343,'E#',342),(344,1,1,344,'E#',341),(345,1,1,345,'B#',0),(346,1,2,346,'B#',0),(347,1,2,347,'E#',346),(348,1,1,348,'E#',345),(349,1,1,349,'B#',0),(350,1,2,350,'B#',0),(351,1,2,351,'E#',350),(352,1,1,352,'E#',349),(353,1,1,353,'B#',0),(354,1,2,354,'B#',0),(355,1,2,355,'E#',354),(356,1,1,356,'E#',353),(357,1,1,357,'B#',0),(358,1,2,358,'B#',0),(359,1,2,359,'E#',358),(360,1,1,360,'E#',357),(361,1,1,361,'B#',0),(362,1,2,362,'B#',0),(363,1,2,363,'E#',362),(364,1,1,364,'E#',361),(365,1,1,365,'B#',0),(366,1,2,366,'B#',0),(367,1,2,367,'E#',366),(368,1,1,368,'E#',365),(369,1,1,369,'B#',0),(370,1,2,370,'B#',0),(371,1,2,371,'E#',370),(372,1,1,372,'E#',369),(373,1,1,373,'B#',0),(374,1,2,374,'B#',0),(375,1,2,375,'E#',374),(376,1,1,376,'E#',373),(377,1,1,377,'B#',0),(378,1,2,378,'B#',0),(379,1,2,379,'E#',378),(380,1,1,380,'E#',377),(381,1,1,381,'B#',0),(382,1,2,382,'B#',0),(383,1,2,383,'E#',382),(384,1,1,384,'E#',381),(385,1,1,385,'B#',0),(386,1,2,386,'B#',0),(387,1,2,387,'E#',386),(388,1,1,388,'E#',385),(389,1,1,389,'B#',0),(390,1,2,390,'B#',0),(391,1,2,391,'E#',390),(392,1,1,392,'E#',389),(393,1,1,393,'B#',0),(394,1,2,394,'B#',0),(395,1,2,395,'E#',394),(396,1,1,396,'E#',393),(397,1,1,397,'B#',0),(398,1,2,398,'B#',0),(399,1,2,399,'E#',398),(400,1,1,400,'E#',397);
/*!40000 ALTER TABLE `beginendexecmethod` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `datamodel`
--

DROP TABLE IF EXISTS `datamodel`;
CREATE TABLE `datamodel` (
  `idDataModel` int(10) unsigned NOT NULL auto_increment,
  `method_idMethod` int(10) unsigned NOT NULL,
  `modelIN` text,
  `modelOUT` text,
  PRIMARY KEY  (`idDataModel`),
  KEY `datamodel_FKIndex1` (`method_idMethod`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `datamodel`
--

LOCK TABLES `datamodel` WRITE;
/*!40000 ALTER TABLE `datamodel` DISABLE KEYS */;
/*!40000 ALTER TABLE `datamodel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `datum`
--

DROP TABLE IF EXISTS `datum`;
CREATE TABLE `datum` (
  `idDatum` int(10) unsigned NOT NULL auto_increment,
  `beginendexecmethod_idBeginEndExecMethod` int(10) unsigned NOT NULL,
  `dataDefinition` text,
  PRIMARY KEY  (`idDatum`),
  KEY `datum_FKIndex1` (`beginendexecmethod_idBeginEndExecMethod`)
) ENGINE=MyISAM AUTO_INCREMENT=401 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `datum`
--

LOCK TABLES `datum` WRITE;
/*!40000 ALTER TABLE `datum` DISABLE KEYS */;
INSERT INTO `datum` VALUES (1,1,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n0\n1\n'),(2,2,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n0\n1\n'),(3,3,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n0\n1\nreturnValue.intValue()\n0\n1\n'),(4,4,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n0\n1\n'),(5,5,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n1\n1\n'),(6,6,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n1\n1\n'),(7,7,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n1\n1\nreturnValue.intValue()\n1\n1\n'),(8,8,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n1\n1\n'),(9,9,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n2\n1\n'),(10,10,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n2\n1\n'),(11,11,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n2\n1\nreturnValue.intValue()\n2\n1\n'),(12,12,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n2\n1\n'),(13,13,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n3\n1\n'),(14,14,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n3\n1\n'),(15,15,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n3\n1\nreturnValue.intValue()\n3\n1\n'),(16,16,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n3\n1\n'),(17,17,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n4\n1\n'),(18,18,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n4\n1\n'),(19,19,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n4\n1\nreturnValue.intValue()\n4\n1\n'),(20,20,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n4\n1\n'),(21,21,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n5\n1\n'),(22,22,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n5\n1\n'),(23,23,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n5\n1\nreturnValue.intValue()\n5\n1\n'),(24,24,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n5\n1\n'),(25,25,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n6\n1\n'),(26,26,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n6\n1\n'),(27,27,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n6\n1\nreturnValue.intValue()\n6\n1\n'),(28,28,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n6\n1\n'),(29,29,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n7\n1\n'),(30,30,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n7\n1\n'),(31,31,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n7\n1\nreturnValue.intValue()\n7\n1\n'),(32,32,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n7\n1\n'),(33,33,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n8\n1\n'),(34,34,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n8\n1\n'),(35,35,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n8\n1\nreturnValue.intValue()\n8\n1\n'),(36,36,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n8\n1\n'),(37,37,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n9\n1\n'),(38,38,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n9\n1\n'),(39,39,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n9\n1\nreturnValue.intValue()\n9\n1\n'),(40,40,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n9\n1\n'),(41,41,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n10\n1\n'),(42,42,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n10\n1\n'),(43,43,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n10\n1\nreturnValue.intValue()\n10\n1\n'),(44,44,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n10\n1\n'),(45,45,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n11\n1\n'),(46,46,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n11\n1\n'),(47,47,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n11\n1\nreturnValue.intValue()\n11\n1\n'),(48,48,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n11\n1\n'),(49,49,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n12\n1\n'),(50,50,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n12\n1\n'),(51,51,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n12\n1\nreturnValue.intValue()\n12\n1\n'),(52,52,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n12\n1\n'),(53,53,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n13\n1\n'),(54,54,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n13\n1\n'),(55,55,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n13\n1\nreturnValue.intValue()\n13\n1\n'),(56,56,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n13\n1\n'),(57,57,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n14\n1\n'),(58,58,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n14\n1\n'),(59,59,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n14\n1\nreturnValue.intValue()\n14\n1\n'),(60,60,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n14\n1\n'),(61,61,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n15\n1\n'),(62,62,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n15\n1\n'),(63,63,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n15\n1\nreturnValue.intValue()\n15\n1\n'),(64,64,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n15\n1\n'),(65,65,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n16\n1\n'),(66,66,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n16\n1\n'),(67,67,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n16\n1\nreturnValue.intValue()\n16\n1\n'),(68,68,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n16\n1\n'),(69,69,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n17\n1\n'),(70,70,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n17\n1\n'),(71,71,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n17\n1\nreturnValue.intValue()\n17\n1\n'),(72,72,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n17\n1\n'),(73,73,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n18\n1\n'),(74,74,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n18\n1\n'),(75,75,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n18\n1\nreturnValue.intValue()\n18\n1\n'),(76,76,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n18\n1\n'),(77,77,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n19\n1\n'),(78,78,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n19\n1\n'),(79,79,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n19\n1\nreturnValue.intValue()\n19\n1\n'),(80,80,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n19\n1\n'),(81,81,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n20\n1\n'),(82,82,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n20\n1\n'),(83,83,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n20\n1\nreturnValue.intValue()\n20\n1\n'),(84,84,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n20\n1\n'),(85,85,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n21\n1\n'),(86,86,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n21\n1\n'),(87,87,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n21\n1\nreturnValue.intValue()\n21\n1\n'),(88,88,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n21\n1\n'),(89,89,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n22\n1\n'),(90,90,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n22\n1\n'),(91,91,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n22\n1\nreturnValue.intValue()\n22\n1\n'),(92,92,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n22\n1\n'),(93,93,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n23\n1\n'),(94,94,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n23\n1\n'),(95,95,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n23\n1\nreturnValue.intValue()\n23\n1\n'),(96,96,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n23\n1\n'),(97,97,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n24\n1\n'),(98,98,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n24\n1\n'),(99,99,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n24\n1\nreturnValue.intValue()\n24\n1\n'),(100,100,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n24\n1\n'),(101,101,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n25\n1\n'),(102,102,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n25\n1\n'),(103,103,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n25\n1\nreturnValue.intValue()\n25\n1\n'),(104,104,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n25\n1\n'),(105,105,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n26\n1\n'),(106,106,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n26\n1\n'),(107,107,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n26\n1\nreturnValue.intValue()\n26\n1\n'),(108,108,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n26\n1\n'),(109,109,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n27\n1\n'),(110,110,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n27\n1\n'),(111,111,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n27\n1\nreturnValue.intValue()\n27\n1\n'),(112,112,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n27\n1\n'),(113,113,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n28\n1\n'),(114,114,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n28\n1\n'),(115,115,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n28\n1\nreturnValue.intValue()\n28\n1\n'),(116,116,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n28\n1\n'),(117,117,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n29\n1\n'),(118,118,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n29\n1\n'),(119,119,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n29\n1\nreturnValue.intValue()\n29\n1\n'),(120,120,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n29\n1\n'),(121,121,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n30\n1\n'),(122,122,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n30\n1\n'),(123,123,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n30\n1\nreturnValue.intValue()\n30\n1\n'),(124,124,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n30\n1\n'),(125,125,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n31\n1\n'),(126,126,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n31\n1\n'),(127,127,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n31\n1\nreturnValue.intValue()\n31\n1\n'),(128,128,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n31\n1\n'),(129,129,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n32\n1\n'),(130,130,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n32\n1\n'),(131,131,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n32\n1\nreturnValue.intValue()\n32\n1\n'),(132,132,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n32\n1\n'),(133,133,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n33\n1\n'),(134,134,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n33\n1\n'),(135,135,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n33\n1\nreturnValue.intValue()\n33\n1\n'),(136,136,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n33\n1\n'),(137,137,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n34\n1\n'),(138,138,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n34\n1\n'),(139,139,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n34\n1\nreturnValue.intValue()\n34\n1\n'),(140,140,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n34\n1\n'),(141,141,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n35\n1\n'),(142,142,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n35\n1\n'),(143,143,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n35\n1\nreturnValue.intValue()\n35\n1\n'),(144,144,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n35\n1\n'),(145,145,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n36\n1\n'),(146,146,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n36\n1\n'),(147,147,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n36\n1\nreturnValue.intValue()\n36\n1\n'),(148,148,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n36\n1\n'),(149,149,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n37\n1\n'),(150,150,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n37\n1\n'),(151,151,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n37\n1\nreturnValue.intValue()\n37\n1\n'),(152,152,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n37\n1\n'),(153,153,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n38\n1\n'),(154,154,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n38\n1\n'),(155,155,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n38\n1\nreturnValue.intValue()\n38\n1\n'),(156,156,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n38\n1\n'),(157,157,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n39\n1\n'),(158,158,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n39\n1\n'),(159,159,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n39\n1\nreturnValue.intValue()\n39\n1\n'),(160,160,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n39\n1\n'),(161,161,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n40\n1\n'),(162,162,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n40\n1\n'),(163,163,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n40\n1\nreturnValue.intValue()\n40\n1\n'),(164,164,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n40\n1\n'),(165,165,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n41\n1\n'),(166,166,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n41\n1\n'),(167,167,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n41\n1\nreturnValue.intValue()\n41\n1\n'),(168,168,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n41\n1\n'),(169,169,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n42\n1\n'),(170,170,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n42\n1\n'),(171,171,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n42\n1\nreturnValue.intValue()\n42\n1\n'),(172,172,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n42\n1\n'),(173,173,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n43\n1\n'),(174,174,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n43\n1\n'),(175,175,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n43\n1\nreturnValue.intValue()\n43\n1\n'),(176,176,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n43\n1\n'),(177,177,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n44\n1\n'),(178,178,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n44\n1\n'),(179,179,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n44\n1\nreturnValue.intValue()\n44\n1\n'),(180,180,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n44\n1\n'),(181,181,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n45\n1\n'),(182,182,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n45\n1\n'),(183,183,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n45\n1\nreturnValue.intValue()\n45\n1\n'),(184,184,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n45\n1\n'),(185,185,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n46\n1\n'),(186,186,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n46\n1\n'),(187,187,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n46\n1\nreturnValue.intValue()\n46\n1\n'),(188,188,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n46\n1\n'),(189,189,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n47\n1\n'),(190,190,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n47\n1\n'),(191,191,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n47\n1\nreturnValue.intValue()\n47\n1\n'),(192,192,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n47\n1\n'),(193,193,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n48\n1\n'),(194,194,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n48\n1\n'),(195,195,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n48\n1\nreturnValue.intValue()\n48\n1\n'),(196,196,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n48\n1\n'),(197,197,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n49\n1\n'),(198,198,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n49\n1\n'),(199,199,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n49\n1\nreturnValue.intValue()\n49\n1\n'),(200,200,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n49\n1\n'),(201,201,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n0\n1\n'),(202,202,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n0\n1\n'),(203,203,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n0\n1\nreturnValue.intValue()\n0\n1\n'),(204,204,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n0\n1\n'),(205,205,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n1\n1\n'),(206,206,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n1\n1\n'),(207,207,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n1\n1\nreturnValue.intValue()\n1\n1\n'),(208,208,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n1\n1\n'),(209,209,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n2\n1\n'),(210,210,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n2\n1\n'),(211,211,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n2\n1\nreturnValue.intValue()\n2\n1\n'),(212,212,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n2\n1\n'),(213,213,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n3\n1\n'),(214,214,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n3\n1\n'),(215,215,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n3\n1\nreturnValue.intValue()\n3\n1\n'),(216,216,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n3\n1\n'),(217,217,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n4\n1\n'),(218,218,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n4\n1\n'),(219,219,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n4\n1\nreturnValue.intValue()\n4\n1\n'),(220,220,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n4\n1\n'),(221,221,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n5\n1\n'),(222,222,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n5\n1\n'),(223,223,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n5\n1\nreturnValue.intValue()\n5\n1\n'),(224,224,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n5\n1\n'),(225,225,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n6\n1\n'),(226,226,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n6\n1\n'),(227,227,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n6\n1\nreturnValue.intValue()\n6\n1\n'),(228,228,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n6\n1\n'),(229,229,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n7\n1\n'),(230,230,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n7\n1\n'),(231,231,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n7\n1\nreturnValue.intValue()\n7\n1\n'),(232,232,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n7\n1\n'),(233,233,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n8\n1\n'),(234,234,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n8\n1\n'),(235,235,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n8\n1\nreturnValue.intValue()\n8\n1\n'),(236,236,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n8\n1\n'),(237,237,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n9\n1\n'),(238,238,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n9\n1\n'),(239,239,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n9\n1\nreturnValue.intValue()\n9\n1\n'),(240,240,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n9\n1\n'),(241,241,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n10\n1\n'),(242,242,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n10\n1\n'),(243,243,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n10\n1\nreturnValue.intValue()\n10\n1\n'),(244,244,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n10\n1\n'),(245,245,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n11\n1\n'),(246,246,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n11\n1\n'),(247,247,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n11\n1\nreturnValue.intValue()\n11\n1\n'),(248,248,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n11\n1\n'),(249,249,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n12\n1\n'),(250,250,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n12\n1\n'),(251,251,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n12\n1\nreturnValue.intValue()\n12\n1\n'),(252,252,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n12\n1\n'),(253,253,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n13\n1\n'),(254,254,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n13\n1\n'),(255,255,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n13\n1\nreturnValue.intValue()\n13\n1\n'),(256,256,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n13\n1\n'),(257,257,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n14\n1\n'),(258,258,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n14\n1\n'),(259,259,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n14\n1\nreturnValue.intValue()\n14\n1\n'),(260,260,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n14\n1\n'),(261,261,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n15\n1\n'),(262,262,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n15\n1\n'),(263,263,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n15\n1\nreturnValue.intValue()\n15\n1\n'),(264,264,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n15\n1\n'),(265,265,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n16\n1\n'),(266,266,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n16\n1\n'),(267,267,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n16\n1\nreturnValue.intValue()\n16\n1\n'),(268,268,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n16\n1\n'),(269,269,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n17\n1\n'),(270,270,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n17\n1\n'),(271,271,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n17\n1\nreturnValue.intValue()\n17\n1\n'),(272,272,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n17\n1\n'),(273,273,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n18\n1\n'),(274,274,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n18\n1\n'),(275,275,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n18\n1\nreturnValue.intValue()\n18\n1\n'),(276,276,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n18\n1\n'),(277,277,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n19\n1\n'),(278,278,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n19\n1\n'),(279,279,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n19\n1\nreturnValue.intValue()\n19\n1\n'),(280,280,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n19\n1\n'),(281,281,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n20\n1\n'),(282,282,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n20\n1\n'),(283,283,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n20\n1\nreturnValue.intValue()\n20\n1\n'),(284,284,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n20\n1\n'),(285,285,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n21\n1\n'),(286,286,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n21\n1\n'),(287,287,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n21\n1\nreturnValue.intValue()\n21\n1\n'),(288,288,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n21\n1\n'),(289,289,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n22\n1\n'),(290,290,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n22\n1\n'),(291,291,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n22\n1\nreturnValue.intValue()\n22\n1\n'),(292,292,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n22\n1\n'),(293,293,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n23\n1\n'),(294,294,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n23\n1\n'),(295,295,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n23\n1\nreturnValue.intValue()\n23\n1\n'),(296,296,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n23\n1\n'),(297,297,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n24\n1\n'),(298,298,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n24\n1\n'),(299,299,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n24\n1\nreturnValue.intValue()\n24\n1\n'),(300,300,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n24\n1\n'),(301,301,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n25\n1\n'),(302,302,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n25\n1\n'),(303,303,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n25\n1\nreturnValue.intValue()\n25\n1\n'),(304,304,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n25\n1\n'),(305,305,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n26\n1\n'),(306,306,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n26\n1\n'),(307,307,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n26\n1\nreturnValue.intValue()\n26\n1\n'),(308,308,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n26\n1\n'),(309,309,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n27\n1\n'),(310,310,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n27\n1\n'),(311,311,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n27\n1\nreturnValue.intValue()\n27\n1\n'),(312,312,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n27\n1\n'),(313,313,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n28\n1\n'),(314,314,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n28\n1\n'),(315,315,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n28\n1\nreturnValue.intValue()\n28\n1\n'),(316,316,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n28\n1\n'),(317,317,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n29\n1\n'),(318,318,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n29\n1\n'),(319,319,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n29\n1\nreturnValue.intValue()\n29\n1\n'),(320,320,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n29\n1\n'),(321,321,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n30\n1\n'),(322,322,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n30\n1\n'),(323,323,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n30\n1\nreturnValue.intValue()\n30\n1\n'),(324,324,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n30\n1\n'),(325,325,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n31\n1\n'),(326,326,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n31\n1\n'),(327,327,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n31\n1\nreturnValue.intValue()\n31\n1\n'),(328,328,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n31\n1\n'),(329,329,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n32\n1\n'),(330,330,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n32\n1\n'),(331,331,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n32\n1\nreturnValue.intValue()\n32\n1\n'),(332,332,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n32\n1\n'),(333,333,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n33\n1\n'),(334,334,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n33\n1\n'),(335,335,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n33\n1\nreturnValue.intValue()\n33\n1\n'),(336,336,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n33\n1\n'),(337,337,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n34\n1\n'),(338,338,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n34\n1\n'),(339,339,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n34\n1\nreturnValue.intValue()\n34\n1\n'),(340,340,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n34\n1\n'),(341,341,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n35\n1\n'),(342,342,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n35\n1\n'),(343,343,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n35\n1\nreturnValue.intValue()\n35\n1\n'),(344,344,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n35\n1\n'),(345,345,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n36\n1\n'),(346,346,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n36\n1\n'),(347,347,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n36\n1\nreturnValue.intValue()\n36\n1\n'),(348,348,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n36\n1\n'),(349,349,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n37\n1\n'),(350,350,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n37\n1\n'),(351,351,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n37\n1\nreturnValue.intValue()\n37\n1\n'),(352,352,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n37\n1\n'),(353,353,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n38\n1\n'),(354,354,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n38\n1\n'),(355,355,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n38\n1\nreturnValue.intValue()\n38\n1\n'),(356,356,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n38\n1\n'),(357,357,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n39\n1\n'),(358,358,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n39\n1\n'),(359,359,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n39\n1\nreturnValue.intValue()\n39\n1\n'),(360,360,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n39\n1\n'),(361,361,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n40\n1\n'),(362,362,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n40\n1\n'),(363,363,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n40\n1\nreturnValue.intValue()\n40\n1\n'),(364,364,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n40\n1\n'),(365,365,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n41\n1\n'),(366,366,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n41\n1\n'),(367,367,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n41\n1\nreturnValue.intValue()\n41\n1\n'),(368,368,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n41\n1\n'),(369,369,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n42\n1\n'),(370,370,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n42\n1\n'),(371,371,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n42\n1\nreturnValue.intValue()\n42\n1\n'),(372,372,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n42\n1\n'),(373,373,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n43\n1\n'),(374,374,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n43\n1\n'),(375,375,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n43\n1\nreturnValue.intValue()\n43\n1\n'),(376,376,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n43\n1\n'),(377,377,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n44\n1\n'),(378,378,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n44\n1\n'),(379,379,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n44\n1\nreturnValue.intValue()\n44\n1\n'),(380,380,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n44\n1\n'),(381,381,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n45\n1\n'),(382,382,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n45\n1\n'),(383,383,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n45\n1\nreturnValue.intValue()\n45\n1\n'),(384,384,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n45\n1\n'),(385,385,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n46\n1\n'),(386,386,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n46\n1\n'),(387,387,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n46\n1\nreturnValue.intValue()\n46\n1\n'),(388,388,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n46\n1\n'),(389,389,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n47\n1\n'),(390,390,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n47\n1\n'),(391,391,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n47\n1\nreturnValue.intValue()\n47\n1\n'),(392,392,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n47\n1\n'),(393,393,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n48\n1\n'),(394,394,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n48\n1\n'),(395,395,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n48\n1\nreturnValue.intValue()\n48\n1\n'),(396,396,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n48\n1\n'),(397,397,'\npack.TestClass.doSome(int):::ENTER\nparameter[0].intValue()\n49\n1\n'),(398,398,'\npack.InternalClass.doSome(int):::ENTER\nparameter[0].intValue()\n49\n1\n'),(399,399,'\npack.InternalClass.doSome(int):::EXIT1\nparameter[0].intValue()\n49\n1\nreturnValue.intValue()\n49\n1\n'),(400,400,'\npack.TestClass.doSome(int):::EXIT1\nparameter[0].intValue()\n49\n1\n');
/*!40000 ALTER TABLE `datum` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `efsa`
--

DROP TABLE IF EXISTS `efsa`;
CREATE TABLE `efsa` (
  `idEFSA` int(10) unsigned NOT NULL auto_increment,
  `method_idMethod` int(10) unsigned NOT NULL,
  `efsa` blob,
  PRIMARY KEY  (`idEFSA`),
  KEY `efsa_FKIndex1` (`method_idMethod`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `efsa`
--

LOCK TABLES `efsa` WRITE;
/*!40000 ALTER TABLE `efsa` DISABLE KEYS */;
/*!40000 ALTER TABLE `efsa` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fsa`
--

DROP TABLE IF EXISTS `fsa`;
CREATE TABLE `fsa` (
  `idFSA` int(10) unsigned NOT NULL auto_increment,
  `method_idMethod` int(10) unsigned NOT NULL,
  `fsa` blob,
  PRIMARY KEY  (`idFSA`),
  KEY `fsa_FKIndex1` (`method_idMethod`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fsa`
--

LOCK TABLES `fsa` WRITE;
/*!40000 ALTER TABLE `fsa` DISABLE KEYS */;
/*!40000 ALTER TABLE `fsa` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gktaildatamodel`
--

DROP TABLE IF EXISTS `gktaildatamodel`;
CREATE TABLE `gktaildatamodel` (
  `idGKTailDataModel` int(10) unsigned NOT NULL auto_increment,
  `gktailmethodcall_idGKTailMethodCall` int(10) unsigned NOT NULL,
  `modelIN` text,
  `modelOUT` text,
  PRIMARY KEY  (`idGKTailDataModel`),
  KEY `gktaildatamodel_FKIndex1` (`gktailmethodcall_idGKTailMethodCall`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `gktaildatamodel`
--

LOCK TABLES `gktaildatamodel` WRITE;
/*!40000 ALTER TABLE `gktaildatamodel` DISABLE KEYS */;
/*!40000 ALTER TABLE `gktaildatamodel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gktailinteractiontrace`
--

DROP TABLE IF EXISTS `gktailinteractiontrace`;
CREATE TABLE `gktailinteractiontrace` (
  `idGKTailInteractionTrace` int(10) unsigned NOT NULL auto_increment,
  `thread_idThread` int(10) unsigned NOT NULL,
  `method_idMethod` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`idGKTailInteractionTrace`),
  KEY `gktailinteractiontrace_FKIndex1` (`method_idMethod`),
  KEY `gktailinteractiontrace_FKIndex2` (`thread_idThread`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `gktailinteractiontrace`
--

LOCK TABLES `gktailinteractiontrace` WRITE;
/*!40000 ALTER TABLE `gktailinteractiontrace` DISABLE KEYS */;
/*!40000 ALTER TABLE `gktailinteractiontrace` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gktailmethodcall`
--

DROP TABLE IF EXISTS `gktailmethodcall`;
CREATE TABLE `gktailmethodcall` (
  `idGKTailMethodCall` int(10) unsigned NOT NULL auto_increment,
  `method_idMethod` int(10) unsigned NOT NULL,
  `gktailinteractiontrace_idGKTailInteractionTrace` int(10) unsigned NOT NULL,
  `occurrence` int(10) unsigned NOT NULL,
  `marker` text,
  PRIMARY KEY  (`idGKTailMethodCall`),
  KEY `gktailmethodcall_FKIndex1` (`method_idMethod`),
  KEY `gktailmethodcall_FKIndex2` (`gktailinteractiontrace_idGKTailInteractionTrace`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `gktailmethodcall`
--

LOCK TABLES `gktailmethodcall` WRITE;
/*!40000 ALTER TABLE `gktailmethodcall` DISABLE KEYS */;
/*!40000 ALTER TABLE `gktailmethodcall` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gktailnormalizeddata`
--

DROP TABLE IF EXISTS `gktailnormalizeddata`;
CREATE TABLE `gktailnormalizeddata` (
  `idGKTailNormalizedData` int(10) unsigned NOT NULL auto_increment,
  `gktailmethodCall_idGKTailMethodCall` int(10) unsigned default NULL,
  `normalizedDataDefinition` text,
  PRIMARY KEY  (`idGKTailNormalizedData`),
  KEY `normalizeddata_FKIndex1` (`gktailmethodCall_idGKTailMethodCall`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `gktailnormalizeddata`
--

LOCK TABLES `gktailnormalizeddata` WRITE;
/*!40000 ALTER TABLE `gktailnormalizeddata` DISABLE KEYS */;
/*!40000 ALTER TABLE `gktailnormalizeddata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interactiontrace`
--

DROP TABLE IF EXISTS `interactiontrace`;
CREATE TABLE `interactiontrace` (
  `idInteractionTrace` int(10) unsigned NOT NULL auto_increment,
  `thread_idThread` int(10) unsigned NOT NULL,
  `method_idMethod` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`idInteractionTrace`),
  KEY `interactiontrace_FKIndex1` (`method_idMethod`),
  KEY `interactiontrace_FKIndex2` (`thread_idThread`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `interactiontrace`
--

LOCK TABLES `interactiontrace` WRITE;
/*!40000 ALTER TABLE `interactiontrace` DISABLE KEYS */;
/*!40000 ALTER TABLE `interactiontrace` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `method`
--

DROP TABLE IF EXISTS `method`;
CREATE TABLE `method` (
  `idMethod` int(10) unsigned NOT NULL auto_increment,
  `methodDeclaration` text,
  PRIMARY KEY  (`idMethod`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `method`
--

LOCK TABLES `method` WRITE;
/*!40000 ALTER TABLE `method` DISABLE KEYS */;
INSERT INTO `method` VALUES (1,'pack.TestClass.doSome(int)'),(2,'pack.InternalClass.doSome(int)');
/*!40000 ALTER TABLE `method` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `methodcall`
--

DROP TABLE IF EXISTS `methodcall`;
CREATE TABLE `methodcall` (
  `idMethodCall` int(10) unsigned NOT NULL auto_increment,
  `method_idMethod` int(10) unsigned NOT NULL,
  `interactiontrace_idInteractionTrace` int(10) unsigned NOT NULL,
  `beginendexecmethod_idBeginEndExecMethod` int(10) unsigned NOT NULL,
  `occurrence` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`idMethodCall`),
  KEY `methodcall_FKIndex1` (`interactiontrace_idInteractionTrace`),
  KEY `methodcall_FKIndex2` (`method_idMethod`),
  KEY `methodcall_FKIndex3` (`beginendexecmethod_idBeginEndExecMethod`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `methodcall`
--

LOCK TABLES `methodcall` WRITE;
/*!40000 ALTER TABLE `methodcall` DISABLE KEYS */;
/*!40000 ALTER TABLE `methodcall` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `normalizeddata`
--

DROP TABLE IF EXISTS `normalizeddata`;
CREATE TABLE `normalizeddata` (
  `idNormalizedData` int(10) unsigned NOT NULL auto_increment,
  `beginendexecmethod_idBeginEndExecMethod` int(10) unsigned NOT NULL,
  `methodCall_idMethodCall` int(10) unsigned default NULL,
  `normalizedDataDefinition` text,
  PRIMARY KEY  (`idNormalizedData`),
  KEY `normalizeddata_FKIndex1` (`methodCall_idMethodCall`),
  KEY `normalizeddata_FKIndex2` (`beginendexecmethod_idBeginEndExecMethod`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `normalizeddata`
--

LOCK TABLES `normalizeddata` WRITE;
/*!40000 ALTER TABLE `normalizeddata` DISABLE KEYS */;
/*!40000 ALTER TABLE `normalizeddata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `session`
--

DROP TABLE IF EXISTS `session`;
CREATE TABLE `session` (
  `idSession` int(10) unsigned NOT NULL auto_increment,
  `dataSession` text,
  PRIMARY KEY  (`idSession`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `session`
--

LOCK TABLES `session` WRITE;
/*!40000 ALTER TABLE `session` DISABLE KEYS */;
/*!40000 ALTER TABLE `session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thread`
--

DROP TABLE IF EXISTS `thread`;
CREATE TABLE `thread` (
  `idThread` int(10) unsigned NOT NULL auto_increment,
  `session_idSession` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`idThread`),
  KEY `thread_FKIndex1` (`session_idSession`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `thread`
--

LOCK TABLES `thread` WRITE;
/*!40000 ALTER TABLE `thread` DISABLE KEYS */;
INSERT INTO `thread` VALUES (1,0);
/*!40000 ALTER TABLE `thread` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2007-07-24 10:37:11
