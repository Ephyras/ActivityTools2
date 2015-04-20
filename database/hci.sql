-- MySQL dump 10.13  Distrib 5.6.21, for Win64 (x86_64)
--
-- Host: localhost    Database: hci
-- ------------------------------------------------------
-- Server version	5.6.21-log

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
-- Table structure for table `tbl_gobal_var`
--

DROP TABLE IF EXISTS `tbl_gobal_var`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_gobal_var` (
  `param_name` varchar(45) NOT NULL,
  `param_value` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`param_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_group_detail`
--

DROP TABLE IF EXISTS `tbl_group_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_group_detail` (
  `group_id` int(11) NOT NULL,
  `interaction_time` varchar(45) NOT NULL,
  `duration` double DEFAULT NULL,
  `screen_status` int(11) DEFAULT '0',
  `screen` mediumblob,
  PRIMARY KEY (`group_id`,`interaction_time`),
  KEY `time_index` (`interaction_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_group_interactions`
--

DROP TABLE IF EXISTS `tbl_group_interactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_group_interactions` (
  `group_id` int(11) NOT NULL AUTO_INCREMENT,
  `group_title` mediumtext,
  `group_app` varchar(45) DEFAULT NULL,
  `user_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`group_id`),
  KEY `group_index` (`group_title`(100),`group_app`),
  KEY `user_index` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=25213 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_interactions`
--

DROP TABLE IF EXISTS `tbl_interactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_interactions` (
  `user_name` varchar(50) NOT NULL,
  `timestamp` varchar(45) NOT NULL,
  `window` varchar(200) DEFAULT NULL,
  `parent_window` varchar(200) DEFAULT NULL,
  `application` varchar(100) DEFAULT NULL,
  `point_x` int(11) DEFAULT NULL,
  `point_y` int(11) DEFAULT NULL,
  `win_rect_left` int(11) DEFAULT NULL,
  `win_rect_top` int(11) DEFAULT NULL,
  `win_rect_right` int(11) DEFAULT NULL,
  `win_rect_bottom` int(11) DEFAULT NULL,
  `ui_name` mediumtext,
  `ui_type` varchar(45) DEFAULT NULL,
  `ui_value` mediumtext,
  `parent_ui_name` longtext,
  `parent_ui_type` varchar(45) DEFAULT NULL,
  `ui_bound_left` int(11) DEFAULT NULL,
  `ui_bound_top` int(11) DEFAULT NULL,
  `ui_bound_right` int(11) DEFAULT NULL,
  `ui_bound_bottom` int(11) DEFAULT NULL,
  `has_screen` int(11) DEFAULT NULL,
  `screen` mediumblob,
  PRIMARY KEY (`user_name`,`timestamp`),
  KEY `idx_application` (`application`),
  KEY `idx_window` (`window`),
  KEY `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_overview`
--

DROP TABLE IF EXISTS `tbl_overview`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_overview` (
  `resource` varchar(200) NOT NULL,
  `application` varchar(100) NOT NULL,
  `type` varchar(45) DEFAULT NULL,
  `duration` decimal(10,2) DEFAULT NULL,
  `lasttime` varchar(45) DEFAULT NULL,
  `scope` varchar(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-04-20 19:15:38
