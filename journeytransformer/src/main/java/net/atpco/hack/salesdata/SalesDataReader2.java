package net.atpco.hack.salesdata;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import net.atpco.hack.journeytransformer.vo.SalesData;                                                             

public class SalesDataReader2 {
	private BlockingQueue<SalesData> blockingQueue ;
	private static final int querySpan = 1 ; // date range of database query in days
	private static final List<String> inputColumns = new ArrayList<String>(Arrays.asList(
			"PRIM_TKT_NO"
			,"COUP_NO"
			,"FLT_DPRT_TS"
			,"FLT_ARRVL_TS"
			,"ORIG_MKT_PNT_CD"
			,"DEST_MKT_PNT_CD"
			,"MRKTG_CXR_CD"
			,"MRKTG_FLT_NO_TXT"
			,"CREATE_TS"
			));

	private static final List<String> outputColumns = new ArrayList<String>(Arrays.asList(
			""
			,"DPTR_TM"
			,"ARRV_TM"
			,"FLT_DATE"
			,"ORAC"
			,"DSTC"
			,"MCAR"
			,"MFTN"
			,"FILE_DATE"
			,"FLT_PATH"
			));

	private static final String timeMask = "HH:mm" ;
	private static final String dateMask = "yyyy-MM-dd" ;
	private static final String urlPrefix = "jdbc:db2:";
	private String url;
	private String user;
	private String password;
	private static SalesDataReader2 brokerInstance;

	public static SalesDataReader2 getInstance(BlockingQueue<SalesData> blockingQueue){
		if(null== brokerInstance){
			String url = urlPrefix + "//atpdb2prod.atpplex1.atpco.net:44615/ATPDB2PROD:retrieveMessagesFromServerOnGetMessage=true;" ;
			String user= "atp1ksb";
			String password  = "P2Mj9gm8";
			brokerInstance = new SalesDataReader2(url, user, password , blockingQueue);
		}
		return brokerInstance;
	}

	private SalesDataReader2(String url, String user, String password ,BlockingQueue<SalesData> blockingQueue) {
		super();
		this.url = url;
		this.user = user;
		this.password = password;
		this.blockingQueue = blockingQueue;
	}
	
	public void readSalesData() throws Exception {
		// TODO figure out round-trip issue

		Connection con;
		Statement stmt;
		ResultSet rs;

		System.out.println ("**** Enter class EzJava");

		// Check the that first argument has the correct form for the portion
		// of the URL that follows jdbc:db2:,
		// as described
		// in the Connecting to a data source using the DriverManager 
		// interface with the IBM Data Server Driver for JDBC and SQLJ topic.
		// For example, for IBM Data Server Driver for 
		// JDBC and SQLJ type 2 connectivity, 
		// args[0] might be MVS1DB2M. For 
		// type 4 connectivity, args[0] might
		// be //stlmvs1:10110/MVS1DB2M.


		try 
		{                                                                        
			// Load the driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");                             
			System.out.println("**** Loaded the JDBC driver");

			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
			con = DriverManager.getConnection (url, user, password);                
			// Commit changes manually
			con.setAutoCommit(false);
			System.out.println("**** Created a JDBC connection to the data source");

			// Create the Statement
			stmt = con.createStatement();                                           
			System.out.println("**** Created JDBC Statement object");

			// Build query string
			StringBuilder querySb = new StringBuilder("SELECT ") ;
			querySb.append(inputColumns.stream().collect(Collectors.joining(", "))) ;
			querySb.append(" FROM REVGRP.TAX_WRPR_COUP_RQST_HIST where CREATE_TS >=") ;
			querySb.append(" cast (DATE(CURRENT TIMESTAMP) as timestamp) - " + querySpan + " DAYS") ;
			querySb.append(" and CREATE_TS <") ;
			querySb.append(" cast (DATE(CURRENT TIMESTAMP) as timestamp)  AND FLT_DPRT_TS is NOT NULL AND FLT_ARRVL_TS is NOT NULL order by PRIM_TKT_NO,COUP_NO") ;
			querySb.append(" FETCH FIRST 1000 ROWS ONLY") ; // TODO remove when tested
			querySb.append(" WITH UR;") ;
			String query = querySb.toString() ;
			System.out.println("**** Created query: " + query) ;

			// Execute a query and generate a ResultSet instance
			rs = stmt.executeQuery(query) ;
			System.out.println("**** Created JDBC ResultSet object") ;

			// Print all of the employee numbers to standard output device
			String currentTktNo = null ;
			int currentCoupNo = -1 ;
			//	      if(rs.next()) {
			//	    	  currentTktNo = rs.getString("PRIM_TKT_NO") ;
			//	    	  currentCoupNo = rs.getInt("COUP_NO") ;
			//	    	  rs.beforeFirst() ;
			//	      }
			// ticketNo -> [ columnName -> columnValue(s) ]
			Map<String, Map<String, StringBuilder>> outputString = new HashMap<String, Map<String, StringBuilder>>() ;
			boolean isFirst = false;
			int tripNumber =0;
			while (rs.next()) {
				String ticketNo = rs.getString("PRIM_TKT_NO") ;
				int couponNo = rs.getInt("COUP_NO") ;
				Map<String, StringBuilder> outputBuilder = outputString.get(ticketNo + ":"+ tripNumber) ;

				if(outputBuilder == null) {
					isFirst = true;
					tripNumber =0;
					outputBuilder = new HashMap<String, StringBuilder>() ;
					outputString.put(ticketNo + ":"+ ++tripNumber , outputBuilder) ;
					currentTktNo = ticketNo ;
					currentCoupNo = couponNo ;
				}
				else if (!ticketNo.contentEquals(currentTktNo)) {
					System.out.println("FALSE ASSUMPTION: RESULT SET IS NOT SORTED AS ASSUMED: ticketNo is: " + ticketNo + " and currentTktNo is: " + currentTktNo) ;
				}
				else if ((couponNo != currentCoupNo + 1 ) || (outputBuilder.get("FLT_PATH").toString().contains(rs.getString("ORIG_MKT_PNT_CD").trim()))) {
					System.out.println("FALSE ASSUMPTION: RESULT SET IS NOT SORTED AS ASSUMED: couponNo is: " + couponNo + " and currentCoupNo is: " + currentCoupNo) ;
					isFirst = true;
					outputBuilder = new HashMap<String, StringBuilder>() ;
					outputString.put(ticketNo + ":"+ ++tripNumber , outputBuilder) ;
					currentTktNo = ticketNo ;
					currentCoupNo = couponNo ;
				}

				if(isFirst) {
					// Build the outputBuilder from scratch

					// ""
					isFirst = false;
					outputBuilder.put("", new StringBuilder("0")) ;

					// DPTR_TM
					StringBuilder sb = new StringBuilder() ;
					if(timestampToString(rs.getTimestamp("FLT_DPRT_TS"), timeMask) == null) {
					System.out.println("************** NULL "+sb);
					}
					sb.append(timestampToString(rs.getTimestamp("FLT_DPRT_TS"), timeMask)) ;
					outputBuilder.put("DPTR_TM", sb) ;

					// ARRV_TM
					sb = new StringBuilder() ;
					sb.append(timestampToString(rs.getTimestamp("FLT_ARRVL_TS"), timeMask)) ;
					outputBuilder.put("ARRV_TM", sb) ;

					// FLT_DATE
					sb = new StringBuilder() ;
					sb.append(timestampToString(rs.getTimestamp("FLT_DPRT_TS"), dateMask)) ;
					outputBuilder.put("FLT_DATE", sb) ;

					// ORAC
					sb = new StringBuilder() ;
					sb.append(rs.getString("ORIG_MKT_PNT_CD").trim()) ;
					outputBuilder.put("ORAC", sb) ;

					// DSTC
					sb = new StringBuilder() ;
					sb.append(rs.getString("DEST_MKT_PNT_CD").trim()) ;
					outputBuilder.put("DSTC", sb) ;

					// MCAR
					sb = new StringBuilder() ;
					sb.append(rs.getString("MRKTG_CXR_CD").trim()) ;
					outputBuilder.put("MCAR",  sb) ;

					// MFTN
					sb = new StringBuilder() ;
					sb.append(rs.getString("MRKTG_FLT_NO_TXT").trim()) ;
					outputBuilder.put("MFTN", sb) ;

					// FILE_DATE - this one will only be set for the first coupon number
					sb = new StringBuilder() ;
					sb.append(timestampToString(rs.getTimestamp("CREATE_TS"), "ddMMMyy").toUpperCase()) ;
					outputBuilder.put("FILE_DATE", sb) ;

					// FLT_PATH
					sb = new StringBuilder() ;
					sb.append(rs.getString("ORIG_MKT_PNT_CD").trim()) ;
					sb.append(";") ;
					sb.append(rs.getString("DEST_MKT_PNT_CD").trim()) ;
					outputBuilder.put("FLT_PATH", sb) ;
				}
				else {
					// This coupon number has already been started

					// DPTR_TM		11:50;14:40
					
					StringBuilder sb = outputBuilder.get("DPTR_TM") ;
					if(outputBuilder.get("DPTR_TM") == null) {
						sb = new StringBuilder("");
						outputBuilder.put("DPTR_TM", sb);
					}
					sb.append(";") ;
					sb.append(timestampToString(rs.getTimestamp("FLT_DPRT_TS"), timeMask)) ;

					if(outputBuilder.get("FLT_DPRT_TS") == null) {
						sb = new StringBuilder("");
						outputBuilder.put("FLT_DPRT_TS", sb);
					}
					
					// ARRV_TM		13:15;19:04
					sb = outputBuilder.get("ARRV_TM") ;
					if(outputBuilder.get("ARRV_TM") == null) {
						sb = new StringBuilder("");
						outputBuilder.put("ARRV_TM", sb);
					}
					sb.append(";") ;
					sb.append(timestampToString(rs.getTimestamp("FLT_ARRVL_TS"), timeMask)) ;
					
					// FLT_DATE		2019-04-14;2019-04-14
					sb = outputBuilder.get("FLT_DATE") ;
					if(outputBuilder.get("FLT_DATE") == null) {
						sb = new StringBuilder("");
						outputBuilder.put("FLT_DATE", sb);
					}
					sb.append(";") ;
					sb.append(timestampToString(rs.getTimestamp("FLT_DPRT_TS"), dateMask)) ;

					// ORAC			CPH;AMS
					sb = outputBuilder.get("ORAC") ;
					if(sb == null) {
						System.out.println();
					}
					sb.append(";") ;
					sb.append(rs.getString("ORIG_MKT_PNT_CD").trim()) ;

					// DSTC			AMS;MCO
					if(outputBuilder.get("DSTC") == null) {
						System.out.println("");
					}
					sb = outputBuilder.get("DSTC") ;
					String soFar = sb.toString();
					if(couponNo > 1 && !soFar.endsWith(rs.getString("ORIG_MKT_PNT_CD"))) {
						System.out.println("FALSE ASSUMPTION: NEXT ORIGIN CITY DOES NOT MATCH PREVIOUS DESTINATION: origin city is: " + rs.getString("ORIG_MKT_PNT_CD") + " and previous destination city is: " + soFar.substring(soFar.length() - 3)) ;
					}
					sb.append(";") ;
					sb.append(rs.getString("DEST_MKT_PNT_CD").trim()) ;

					// MCAR			DL;DL
					sb = outputBuilder.get("MCAR") ;
					sb.append(";") ;
					sb.append(rs.getString("MRKTG_CXR_CD").trim()) ;

					// MFTN			9435;127
					sb = outputBuilder.get("MFTN") ;
					sb.append(";") ;
					sb.append(rs.getString("MRKTG_FLT_NO_TXT").trim()) ;

					// FLT_PATH		CPH;AMS;MCO
					sb = outputBuilder.get("FLT_PATH") ;
					sb.append(";") ;
					sb.append(rs.getString("DEST_MKT_PNT_CD").trim()) ;
				}

				currentCoupNo = couponNo ;
				currentTktNo = ticketNo ;
			}
			System.out.println("**** Fetched all rows from JDBC ResultSet");
			// Close the ResultSet
			rs.close();
			System.out.println("**** Closed JDBC ResultSet");

			// Close the Statement
			stmt.close();
			System.out.println("**** Closed JDBC Statement");

			// Connection must be on a unit-of-work boundary to allow close
			con.commit();
			System.out.println ( "**** Transaction committed" );

			// Close the connection
			con.close();                                                            
			System.out.println("**** Disconnected from data source");

			// Now, we need to write the output
			System.out.println(outputColumns.stream().collect(Collectors.joining(","))) ;
			// Map<String, Map<String, StringBuilder>> outputString = new HashMap<String, Map<String, StringBuilder>>() ;
			Iterator<Map.Entry<String, Map<String, StringBuilder>>> it = outputString.entrySet().iterator() ;

			while(it.hasNext()) {
				Map.Entry<String, Map<String, StringBuilder>> entry = it.next();
				Map<String, StringBuilder> outputBuilder = entry.getValue() ;
				SalesData sd = new SalesData(outputBuilder.get(outputColumns.get(1)).toString().split(";"), outputBuilder.get(outputColumns.get(2)).toString().split(";"), 
						outputBuilder.get(outputColumns.get(3)).toString().split(";"), outputBuilder.get(outputColumns.get(6)).toString().split(";"),
						outputBuilder.get(outputColumns.get(7)).toString().split(";"), outputBuilder.get(outputColumns.get(8)).toString(), 
						outputBuilder.get(outputColumns.get(9)).toString().split(";"));
				blockingQueue.put(sd);
				for(int i = 0; i < outputColumns.size() ; i++) {
					String columnName = outputColumns.get(i) ;
					StringBuilder sb = outputBuilder.get(columnName) ;
					if(sb != null) System.out.print(sb.toString()) ;
//					sd.setDepartureTimes(departureTimes);
					if(i+1 < outputColumns.size()) System.out.print(","); 
				}

				System.out.println();
			}

			System.out.println("**** JDBC Exit from class EzJava - no errors");

		}

		catch (ClassNotFoundException e)
		{
			System.err.println("Could not load JDBC driver");
			System.out.println("Exception: " + e);
			e.printStackTrace();
		}

		catch(SQLException ex)                                                    
		{
			System.err.println("SQLException information");
			while(ex!=null) {
				System.err.println ("Error msg: " + ex.getMessage());
				System.err.println ("SQLSTATE: " + ex.getSQLState());
				System.err.println ("Error code: " + ex.getErrorCode());
				ex.printStackTrace();
				ex = ex.getNextException(); // For drivers that support chained exceptions
			}
		}

	}

	public static void main(String[] args) 
	{
		String url;
		String user;
		String password;
		BlockingQueue<SalesData> blockingQueue = new LinkedBlockingQueue<>();

		if (args.length!=3)
		{
			System.err.println ("Invalid value. First argument appended to "+
					"jdbc:db2: must specify a valid URL.");
			System.err.println ("Second argument must be a valid user ID.");
			System.err.println ("Third argument must be the password for the user ID.");
			System.exit(1);
		}
		url = urlPrefix + args[0];
		user = args[1];
		password = args[2];

		SalesDataReader2 reader = SalesDataReader2.getInstance(blockingQueue);
		try {
			reader.readSalesData();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}  // End main

	private static String timestampToString(Timestamp ts, String mask) {
		if(ts == null ) return "";
		DateFormat formatter = new SimpleDateFormat(mask) ;
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		return formatter.format(ts) ;
	}

	

}    // End EzJava