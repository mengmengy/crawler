package org.gwu.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.gwu.db.DataBaseManager;
import org.gwu.model.Job;

public class ParserDAO extends AbstractDAO {
	private String maxID;
	
	public ParserDAO()
	{
		super();
		maxID = this.getMaxJobID();
	}
	
	public ArrayList<String> getKeywords(){
		ArrayList<String> keyword = new ArrayList<String>();
		PreparedStatement st=null;
		ResultSet rs=null;
		Connection conn=getConnection();
		try{
			String selectSql="select req from requirement ";		
			st=conn.prepareStatement(selectSql);
			rs=st.executeQuery();
			while(rs.next()){
				keyword.add(rs.getString(1));
			}
			log.info(selectSql);
		}catch(SQLException e){
			log.error("get keywords error:",e);
			e.printStackTrace();
		}finally{
			DataBaseManager.closeStatement(st,null);
		}		
		return keyword;
	}
	//id就是当前job的id
	public void saveReq(Job r, String id){
		PreparedStatement st=null;
		Connection conn=getConnection();
		//拿到刚才放进去的每一个skill，比较requirement表，讲这个job的id和对应的requirement的id添加到job_req表里面
		ArrayList<String> req= r.getReq();		
		try{	
			for(String requirement : req)
			{
				String insertSql="insert into Job_Req(jobID,reqID) "
						+ "values(?, (select reqID from requirement where req = ?))";
				st=conn.prepareStatement(insertSql);
				st.setString(1,id);
				st.setString(2,requirement);
				st.execute();
				log.info(insertSql);
			}
		}catch(SQLException e){
			log.error("insert req error:",e);
			e.printStackTrace();
		}finally{
			DataBaseManager.closeStatement(st,null);
		}		
	}
	
	public void save2DB(List<Job> l){
		PreparedStatement st=null;
		Connection conn=getConnection();
		try{
			for(Job r : l)
			{
				String insertSql="insert into job(jobID,jobName,employer,postDate,applicationDeadline,"
						+ "location,positionType,description,opening,documentsReq,"
						+ "compensation,salary,desiredStartTime,educationLevel,yearOfExp,"
						+ "clearanceReq,others) "
						+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				st=conn.prepareStatement(insertSql);
				//String s = this.getMaxJobID();
				String s = maxID;
				s = s.substring(1);			
				s = String.valueOf(Integer.parseInt(s, 10) + 1);
				String ss = "J0000";
				ss = ss.substring(0, 5 - s.length()).concat(s);
				st.setString(1,ss);
				maxID = ss;
				st.setString(2,r.getJobName());	
				st.setString(3,r.getEmployer());
				if(r.getPostDate()!=null)
					st.setDate(4,Date.valueOf(r.getPostDate()));
				else
					st.setDate(4,null);
				if(r.getDeadline()!=null){
					if(r.getDeadline().compareToIgnoreCase("Available Year-round") == 0)
						st.setDate(5,Date.valueOf("2014-12-31"));
					else
						st.setDate(5,Date.valueOf(r.getDeadline()));
				}
				else
					st.setDate(5,null);
				st.setString(6,r.getLocation());	
				st.setString(7,r.getType());
				st.setString(8,r.getDes());
				st.setInt(9,r.getOpening());
				st.setString(10,r.getDocReq());
				st.setString(11,r.getCompensation());
				st.setInt(12,r.getSalary());
				if(r.getStartTime()!=null)
					st.setDate(13,Date.valueOf(r.getStartTime()));
				else
					st.setDate(13,null);
				st.setString(14,r.getEduLevel());
				st.setInt(15,r.getYearOfExp());
				st.setString(16,r.getClearanceReq());
				st.setString(17,r.getOther());
				st.execute();
				log.info(insertSql);
				this.saveReq(r, ss);
			}
		}catch(SQLException e){
			log.error("insert job error:",e);
			e.printStackTrace();
		}finally{
			DataBaseManager.closeStatement(st,null);
		}		
	}
	
	private String getMaxJobID(){
		String jobID = "";
		PreparedStatement st=null;
		ResultSet rs=null;
		Connection conn=getConnection();
		try{
			String selectSql="select Max(jobID) from job ";
			st=conn.prepareStatement(selectSql);
			rs=st.executeQuery();
			if(rs.next())
				jobID = rs.getString(1);
			log.info(selectSql);
		}catch(SQLException e){
			log.error("get max jobID error:",e);
			e.printStackTrace();
		}finally{
			DataBaseManager.closeStatement(st,null);
		}				
		return jobID;
	}
}
