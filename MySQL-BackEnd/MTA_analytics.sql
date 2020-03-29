-- Joshua Li
-- Analytics

-- loads all the buses on the grid within a 10-minute range of the user's inputted time and date

USE MTA;

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
