-- Joshua Li
-- Database Insertion part (same as in MTA file)

USE MTA;

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