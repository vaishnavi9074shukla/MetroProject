package com.group9.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.group9.bean.Transaction;

public class TransactionDAOImpl implements TransactionDAO {

	MetroStationDAO metroStationDAOImpl=new MetroStationDAOImpl();
	private static long transactionCount=0;
	
	static {
		try(Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/MetroDataBase", "root", "wiley");
				PreparedStatement preparedStatement=conn.prepareStatement("SELECT transactionId FROM TRANSACTION ORDER BY transactionId DESC LIMIT 1");){
			ResultSet resultSet=preparedStatement.executeQuery();
			if(resultSet.next())
				transactionCount=resultSet.getLong(1);
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	@Override
	public boolean swipeIn(long cardId, int sourceStationId) {
		try(Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/MetroDataBase", "root", "wiley")){
			PreparedStatement preparedStatement=conn.prepareStatement("INSERT INTO TRANSACTION(transactionId,cardId,sourceStationId,dateAndTimeOfBoarding) VALUES(?,?,?,?);");
			transactionCount++;
			preparedStatement.setLong(1,transactionCount);
			preparedStatement.setLong(2, cardId);
			preparedStatement.setInt(3, sourceStationId);
			preparedStatement.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
			int rows = preparedStatement.executeUpdate();
			if(rows==1) {
				return true;
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean swipeOut(long cardId, int destinationStationId, double fare) {
		try(Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/MetroDataBase", "root", "wiley")){
			PreparedStatement preparedStatement=conn.prepareStatement("UPDATE TRANSACTION "
					+ "SET destinationStationId=?,dateAndTimeOfExit=?,fare=? "
					+ "WHERE cardId=? AND dateAndTimeofExit is null;");
			preparedStatement.setInt(1, destinationStationId);
			preparedStatement.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
			preparedStatement.setDouble(3, fare);
			preparedStatement.setLong(4, cardId);
			int result=preparedStatement.executeUpdate();
			if(result>=1) {
				return true;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public List<Transaction> transactionHistory(long cardId) {
		List<Transaction> transactionList=new ArrayList<>();
		try(Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/MetroDataBase", "root", "wiley")){
			PreparedStatement preparedStatement=conn.prepareStatement("SELECT * FROM TRANSACTION WHERE CARDID=? ORDER BY dateAndTimeOfBoarding DESC;");
			preparedStatement.setLong(1, cardId);
			
			ResultSet resultSet=preparedStatement.executeQuery();
			
			while(resultSet.next()) {
				long transactionId=resultSet.getLong(1);
				long cardID=resultSet.getLong(2);
				int sourceStationId=resultSet.getInt(3);
				LocalDateTime dateAndTimeOfBoarding=resultSet.getTimestamp(4).toLocalDateTime();
				int destinationStationId=resultSet.getInt(5);
				LocalDateTime dateAndTimeOfExit=null;
				if(resultSet.getTimestamp(6)!=null)
					dateAndTimeOfExit=resultSet.getTimestamp(6).toLocalDateTime();
			    double fare=resultSet.getDouble(7);
			    
			    transactionList.add(new Transaction(transactionId,cardID,sourceStationId,dateAndTimeOfBoarding,
			    		destinationStationId,dateAndTimeOfExit,fare));
				
			}

			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return transactionList;
	}

	@Override
	public Transaction lastTransaction(long cardId) {
		
		Transaction lastTransac=null;
		try(Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/MetroDataBase", "root", "wiley")){
			
			PreparedStatement preparedStatement=conn.prepareStatement("SELECT * FROM TRANSACTION WHERE CARDID=? ORDER BY dateAndTimeOfBoarding DESC LIMIT 1;");
			preparedStatement.setLong(1, cardId);
			ResultSet resultSet=preparedStatement.executeQuery();
			
			if(resultSet.next()) {
				long transactionId=resultSet.getLong(1);
				long cardID=resultSet.getLong(2);
				int sourceStationId=resultSet.getInt(3);
				LocalDateTime dateAndTimeOfBoarding=resultSet.getTimestamp(4).toLocalDateTime();
//				LocalDateTime dateAndTimeOfBoarding=resultSet.getDate(3).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				int destinationStationId=resultSet.getInt(5);
				LocalDateTime dateAndTimeOfExit=null;
				if(resultSet.getTimestamp(6)!=null)
					dateAndTimeOfExit=resultSet.getTimestamp(6).toLocalDateTime();
//				LocalDateTime dateAndTimeOfExit=resultSet.getDate(5).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			    double fare=resultSet.getDouble(7);
			    
			    lastTransac=new Transaction(transactionId,cardID,sourceStationId,dateAndTimeOfBoarding,
			    		destinationStationId,dateAndTimeOfExit,fare);
			}
				
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return lastTransac;
	}

}
