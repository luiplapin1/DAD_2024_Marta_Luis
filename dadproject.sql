-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Versión del servidor:         11.3.2-MariaDB - mariadb.org binary distribution
-- SO del servidor:              Win64
-- HeidiSQL Versión:             12.5.0.6677
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Volcando estructura de base de datos para dadproject
CREATE DATABASE IF NOT EXISTS `dadproject` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;
USE `dadproject`;

-- Volcando estructura para tabla dadproject.actuador
CREATE TABLE IF NOT EXISTS `actuador` (
  `nPlaca` int(11) NOT NULL,
  `idActuador` int(11) NOT NULL,
  `timestamp` bigint(15) NOT NULL,
  `activo` tinyint(1) NOT NULL,
  `encendido` tinyint(1) NOT NULL,
  `idGroup` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Volcando datos para la tabla dadproject.actuador: ~4 rows (aproximadamente)
REPLACE INTO `actuador` (`nPlaca`, `idActuador`, `timestamp`, `activo`, `encendido`, `idGroup`) VALUES
	(1, 2, 15159615, 0, 1, 0),
	(1, 3, 156165, 1, 1, 0),
	(1, 0, 156165, 1, 1, 0),
	(1, 9, 156165, 1, 1, 1);

-- Volcando estructura para tabla dadproject.sensor
CREATE TABLE IF NOT EXISTS `sensor` (
  `idSensor` int(11) NOT NULL,
  `nPlaca` int(11) NOT NULL,
  `humedad` float NOT NULL,
  `timestamp` bigint(20) NOT NULL,
  `temperatura` float NOT NULL,
  `idGroup` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Volcando datos para la tabla dadproject.sensor: ~6 rows (aproximadamente)
REPLACE INTO `sensor` (`idSensor`, `nPlaca`, `humedad`, `timestamp`, `temperatura`, `idGroup`) VALUES
	(1, 0, 43.6, 165651615605, 25.35, 0),
	(1, 0, 43.6, 165651615605, 25.35, 0),
	(0, 0, 30.6, 51615605, 25.2, 1),
	(25, 0, 30.6, 51615605, 25.2, 0),
	(29, 0, 30.6, 51615605, 25.2, 0),
	(9, 0, 30.6, 51615605, 25.2, 1);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
