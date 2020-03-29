/*
 * Joshua Li
 * CS 3265 final project
 * Program for tracking all NYC buses only in June 2017
 */

package database;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
    	drawBus();
    	Scanner scnr = new Scanner(System.in);
    	
    	//create connection
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/MTA?autoReconnect=true&useSSL=false", "root", "");
            do {
            String route = chooseRoute(scnr, con);
            int terminal = chooseTerminal(route, con, scnr);
            int day = chooseDay(scnr);
            String time = chooseTime(scnr);
            displayBuses(route, terminal, day, time, con);
            } while (yesNo(scnr) == true);
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        exit();
    }

    /** 
     * draws a bus and welcomes the user
     */
    public static void drawBus() {
    	System.out.println("	   _________________________________");
    	System.out.println("	 _/_|[][][][][][][][][][][][][][][] | ~ ~ ~");
    	System.out.println("	(    VROOM VROOM I'M A BUSSSSSSSSS  | ~ ~ ~");
    	System.out.println("	=----OO-------------------OO--------=dwb");
    	System.out.println("");
    	System.out.println("Welcome to BusTime June 2017 edition!");
    	System.out.println("");
    }
   
    /**
     * prompts user to choose a route
     * @param scnr - scanner 
     * @param con - connection to the database
     * @return String of the user's inputed route
     * @throws SQLException
     */
    
	public static String chooseRoute(Scanner scnr, Connection con) throws SQLException {
		System.out.println("Pick a route:");
        String route = scnr.next().toUpperCase();
        scnr.nextLine();
        
        // deals with the anomaly of 'Bx' having a lowercase letter
        if (route.length() > 2 && route.substring(0, 2).equals("BX")) {
        	route = route.replace('X', 'x');
        }
        Statement destStmt = con.createStatement();
        ResultSet routeCheck = destStmt.executeQuery("SELECT lineName FROM routeInfo");
        boolean validRoute = false;
        while (routeCheck.next()) {
        	if (route.equals(routeCheck.getString(1))) {
        		validRoute = true;
        	}
        }
        if (validRoute == false) {
        	System.out.println("Invalid route. Please try again. \n");
        	return chooseRoute(scnr, con);
        }
        else {
        return route;
        }
	}
	
	/**
	 * provides a list of all the possible terminals and allows the user to choose which terminal
	 * based on the corresponding number
	 * @param route - the route user chose in chooseRoute
	 * @param con - connection to the database
	 * @param scnr - scanner
	 * @return terminal number
	 * @throws SQLException
	 */
	public static int chooseTerminal(String route, Connection con, Scanner scnr) throws SQLException {
	System.out.println("Please choose a terminal number:");
	Statement destStmt = con.createStatement();
	ResultSet rs1 = destStmt.executeQuery
			("SELECT busID, destination FROM routeInfo WHERE lineName =" + "'" + route + "'");
	int maxID = 0;
	while(rs1.next()) {
		String ID = rs1.getString(1);
		maxID = Integer.valueOf(ID);
		String terminal = rs1.getString(2);
		System.out.println(ID + " - " + terminal);
	}
	System.out.println();
	int userID = scnr.nextInt();
	scnr.nextLine();
	if (userID < 1 || userID > maxID) {
		System.out.println("Invalid terminal number. Please try again.");
		return chooseTerminal(route, con, scnr);
	} 
		return userID;
	}
	
	/**
	 * prompts and takes a user input for choosing the day in June
	 * @param scnr - scanner
	 * @return day
	 */
	public static int chooseDay(Scanner scnr) {
		System.out.println("Day? [1-30]");
		int day = scnr.nextInt();
		scnr.nextLine();
		if (day < 1 || day > 30) {
			System.out.println("Please just pick a number between 1 and 30");
			return chooseDay(scnr);
		}
		return day;
	}
	
	/**
	 * prompts user to put in military time
	 * @param scnr - scanner
	 * @return time with :00 tacked onto end
	 */
	public static String chooseTime(Scanner scnr) {
		System.out.println("Time? Use military notation. [xx:xx]");
		String time = scnr.next();
		scnr.nextLine();
		if(time.length() != 5 || time.charAt(2) != ':' || Integer.valueOf(time.substring(0, 2)) > 23
				|| Integer.valueOf(time.substring(0, 2)) < 0 || Integer.valueOf(time.substring(3, 5)) > 59
				|| Integer.valueOf(time.substring(3, 5)) < 0) {
			System.out.println("Please format your time properly! "
					+ "Make sure to use leading zeroes when necessary");
			return chooseTime(scnr);
		}
		return time + ":00";
	}
	
	/**
	 * displays all the buses on the grid using the stored procedure in mySQL
	 * @param route - from chooseRoute
	 * @param terminal - from chooseTerminal
	 * @param day - from chooseDay
	 * @param time - from chooseTime
	 * @param con - database connector
	 * @throws SQLException
	 */
	public static void displayBuses(String route, int terminal, int day, String time, Connection con) 
			throws SQLException {
		String query = "{CALL load_buses(?, ?, ?, ?)}";
    	CallableStatement loadStmt = con.prepareCall(query);
    	loadStmt.setString(1, route);
    	loadStmt.setLong(2, terminal);
    	loadStmt.setLong(3, day);
    	loadStmt.setString(4, time);
    	ResultSet buses = loadStmt.executeQuery();
    	if (!(buses.next())) {
    		System.out.println("No buses! Go get a baconegg&cheese at the local bodega.");
    	}
    	else {
    	System.out.println("These are the available buses:");
    	while (buses.next()) { 
    		System.out.println(String.format("%s", buses.getString("lineName") + " estimated to arrive at " +
    				buses.getString("station") + " at "
    				+ buses.getString("arrival") + ". "));
    	}
    	}
	}
	
	/**
	 * asks the user if they would like to continue
	 * @param scnr - scanner
	 * @return - yes = true; no = false
	 */
	public static boolean yesNo(Scanner scnr) {
		System.out.println("\nWould you like to make a new search? [yes/no]");
		String yes = scnr.next().toLowerCase();
		scnr.nextLine();
		while (!(yes.equals("yes")) && !(yes.equals("no"))) {
			System.out.println("Please say yes or no.");
			return yesNo(scnr);
		}
		return (yes.equals("yes"));
	}
	
	/**
	 * exits the program
	 */
	public static void exit() {
		System.out.println("");
		System.out.println("Thank you for riding with the MTA New York City Transit.");
		System.out.println("If you see something, say something.");
	}
	
}