DROP DATABASE IF EXISTS MTA;
CREATE DATABASE MTA;
USE MTA;

DROP TABLE IF EXISTS MTA_data;

CREATE TABLE MTA_data (
   RecordedAtTime            VARCHAR(200) 
  ,DirectionRef              VARCHAR(100)
  ,PublishedLineName         VARCHAR(100) 
  ,OriginName                VARCHAR(100)
  ,OriginLat                 VARCHAR(100)
  ,OriginLong                VARCHAR(100)
  ,DestinationName           VARCHAR(100) 
  ,DestinationLat            VARCHAR(100)
  ,DestinationLong           VARCHAR(100) 
  ,VehicleRef                VARCHAR(100) 
  ,VehicleLocationLatitude   VARCHAR(100)
  ,VehicleLocationLongitude  VARCHAR(100) 
  ,NextStopPointName         VARCHAR(100) 
  ,ArrivalProximityText      VARCHAR(100) 
  ,DistanceFromStop          VARCHAR(100)
  ,ExpectedArrivalTime       VARCHAR(100)
  ,ScheduledArrivalTime      VARCHAR(100)
);

LOAD DATA INFILE 'C:\\wamp64\\tmp\\mta_1706b.csv'
INTO TABLE  MTA_data 
FIELDS TERMINATED by ','
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(RecordedAtTime, DirectionRef, PublishedLineName, OriginName, OriginLat, OriginLong, 
DestinationName, DestinationLat, DestinationLong, VehicleRef, VehicleLocationLatitude, VehicleLocationLongitude, 
NextStopPointName, ArrivalProximityText, DistanceFromStop, ExpectedArrivalTime, ScheduledArrivalTime, @dummy);
-- @dummy accounts for the mistaken extra column

-- dataset fixes:

-- deletes where DistanceFromStop is not numeric
DELETE FROM MTA_data WHERE DistanceFromStop REGEXP '[^0-9].+';

DELETE FROM MTA_data WHERE ExpectedArrivalTime = 'NA';

DELETE FROM MTA_data WHERE ScheduledArrivalTime = 'NA';

-- some NAs were not being deleted
DELETE FROM MTA_data WHERE ScheduledArrivalTime > 'N';

-- creates a reference table with all the possible bus routes and destinations
DROP TABLE IF EXISTS routeInfo;

CREATE TABLE routeInfo (
lineName VARCHAR(10),
busID INT NOT NULL AUTO_INCREMENT,
destination VARCHAR(100),
PRIMARY KEY(lineName, busID, destination)
);

INSERT INTO routeInfo(lineName, destination)
(SELECT DISTINCT PublishedLineName, DestinationName
FROM MTA_data);

-- table representing all the station information
DROP TABLE IF EXISTS nextStop;
CREATE TABLE nextStop (
lineName VARCHAR(10),
destination VARCHAR(100),
dateAndTime DATETIME NOT NULL,
station VARCHAR(100) NOT NULL,
proximity VARCHAR(50) NOT NULL,
distance INT NOT NULL,
estArrival DATETIME DEFAULT NULL,
schedArrival TIME DEFAULT NULL
);

INSERT INTO nextStop
(SELECT PublishedLineName, DestinationName, STR_TO_DATE(RecordedAtTime, '%Y-%m-%d %T'), 
NextStopPointName, ArrivalProximityText, DistanceFromStop, 
STR_TO_DATE(ExpectedArrivalTime, '%Y-%m-%d %T'), ScheduledArrivalTime
FROM MTA_data);

-- loads all the buses on the grid within a 10-minute range of the user's inputted time and date

DROP PROCEDURE IF EXISTS load_buses;

delimiter //

CREATE PROCEDURE load_buses
(IN route_input VARCHAR(10), IN busID_input INT, IN day_input INT, IN time_input TIME)
BEGIN
	SELECT busID, n.lineName lineName, n.destination dest, dateAndTime, station, proximity, distance, TIME(estArrival) arrival
    FROM routeInfo r RIGHT JOIN nextStop n ON r.lineName = n.lineName AND r.destination = n.destination
    WHERE DAY(dateAndTime) = day_input AND TIME(dateAndTime) >= time_input AND 
    TIME(dateAndTime) <= DATE_ADD(time_input, INTERVAL 10 MINUTE) 
    AND busID = busID_input AND r.lineName = route_input
    ORDER BY estArrival;

END//

delimiter ;
