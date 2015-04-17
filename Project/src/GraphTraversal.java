import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class GraphTraversal {
	
	//private HashMap<String,ArrayList> nodeValues= new HashMap<String,ArrayList>();
	
	static MongoClient mongoClient;
	
	public static MongoClient getConnection(){
		MongoClientURI clientURI=new MongoClientURI("mongodb://deliver:admin@ds053937.mongolab.com:53937/deliveronthego");
		try
		{
			mongoClient =new MongoClient(clientURI);
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}
		return mongoClient;
	}
	
	public static HashMap<String, HashMap> initializeDriverSourceValues()
	{
		HashMap<String,HashMap> driverSourceValues= new HashMap<>();
		
		Double srcLat, srcLon;
		String driverId;
		mongoClient=getConnection();
		DB db=mongoClient.getDB("deliveronthego");
		DBCollection driverSourceDetails=db.getCollection("driverDetails");
		DBCursor cursor =driverSourceDetails.find();
		System.out.println("cursor=="+cursor.count());
		while(cursor.hasNext())
		{
			HashMap temp=new HashMap<String,Double>();
			BasicDBObject obj=(BasicDBObject)cursor.next();
			srcLat=Double.parseDouble((String)obj.get("latitude"));
			srcLon=Double.parseDouble((String)obj.get("longitude"));
			System.out.println("latitude==="+srcLat);
			driverId=(String)obj.get("userID");
			
			temp.put("latitude",srcLat);
			temp.put("longitude",srcLon);
			temp.put("visited",0); //visited or not
			temp.put("pheromone",0.0); //pheromone
			driverSourceValues.put(driverId,temp);
			
		}
				
		
		System.out.println("SourceDriver=="+driverSourceValues.toString());
		
		return driverSourceValues;
	}
	
	public static HashMap<String, HashMap> initializeDriverDestValues()
	{
		HashMap<String,HashMap> driverDestValues= new HashMap<>();
		
		Double destLat, destLon;
		String driverId;
		mongoClient=getConnection();
		DB db=mongoClient.getDB("deliveronthego");
		DBCollection driverSourceDetails=db.getCollection("driverDestDetails");
		DBCursor cursor =driverSourceDetails.find();
		System.out.println("cursor=="+cursor.count());
		while(cursor.hasNext())
		{
			HashMap temp=new HashMap<String,Double>();
			BasicDBObject obj=(BasicDBObject)cursor.next();
			destLat=Double.parseDouble((String)obj.get("latitude"));
			destLon=Double.parseDouble((String)obj.get("longitude"));
			driverId=(String)obj.get("userID");
			temp.put("latitude",destLat);
			temp.put("longitude",destLat);
			temp.put("visited",0); //visited or not
			temp.put("pheromone",0.0); //pheromone
			driverDestValues.put(driverId,temp);
		}
		
		System.out.println("SourceDestDriver=="+driverDestValues.toString());
		
		return driverDestValues;
	}
	
	public static HashMap<String, HashMap> initializeUserSourceValues()
	{
		HashMap<String,HashMap> UserSourceValues= new HashMap<>();
		HashMap temp=new HashMap<String,Double>();
		temp.put("latitude",137.43);
		temp.put("longitude",144.46);
		temp.put("visited",0); //visited or not
		temp.put("pheromone",0.0); //pheromone
		UserSourceValues.put("1",temp);
		
		return UserSourceValues;
	}
	
	public static HashMap<String, HashMap> initializeUserDestValues()
	{
		HashMap<String,HashMap> userDestValues= new HashMap<>();
		HashMap temp=new HashMap<String,Double>();
		temp.put("latitude",137.98);
		temp.put("longitude",144.12);
		temp.put("visited",0); //visited or not
		temp.put("pheromone",0.0); //pheromone
		
		userDestValues.put("1",temp);		
		return userDestValues;
	}
	
	
	public void traversal(HashMap sourceDriver,HashMap destDriver,HashMap sourceUser,HashMap destUser, int userId)
	{
		Iterator itSrcDriver = sourceDriver.entrySet().iterator();
		Iterator itDestDriver = destDriver.entrySet().iterator();
		//Iterator itSrcUser = sourceUser.entrySet().iterator();
		//Iterator itDestUser = destUser.entrySet().iterator();
		
		String key;
		GraphTraversal g=new GraphTraversal();
		HashMap markVisited;
		double driverSourceLat,driverSourceLon,userSourceLat,userSourceLon,distance, driverDistance;
		double driverDestLat, driverDestLon, userDestLat, userDestLon;
		
		while(itSrcDriver.hasNext())
		{
			
			Map.Entry pairSrcDriver=(Map.Entry)itSrcDriver.next();
			
			markVisited= (HashMap) pairSrcDriver.getValue();
			markVisited.replace("visited", 1);   //marking the node as visited
			driverSourceLat=(double) markVisited.get("latitude");
			driverSourceLon=(double) markVisited.get("longitude");
			sourceDriver.put(pairSrcDriver.getKey(), markVisited);	
			
									
			System.out.println("SourceDriver=="+sourceDriver.get(pairSrcDriver.getKey()).toString());
			
			markVisited= (HashMap) sourceUser.get("1");
			markVisited.replace("visited", 1);  //marking the node as visited
			userSourceLat=(double) markVisited.get("latitude");
			userSourceLon=(double) markVisited.get("longitude");
			sourceUser.put("1", markVisited);
			
			System.out.println("SourceUser=="+sourceUser.get("1").toString());
			
			//calculate the distance from driver source to user pickup source
			
			distance=g.calcDistance(driverSourceLat,driverSourceLon,userSourceLat,userSourceLon);
			
			markVisited= (HashMap) destUser.get("1");
			markVisited.replace("visited", 1);  //marking the node as visited
			userDestLat=(double) markVisited.get("latitude");
			userDestLon=(double) markVisited.get("longitude");
			 destUser.put("1", markVisited);
			 
			 distance=distance + g.calcDistance(userSourceLat, userSourceLon, userDestLat, userDestLon);
			
			System.out.println("Dest User=="+destUser.get("1").toString());
			
			Map.Entry pairDestDriver=(Map.Entry)itDestDriver.next();
			
			markVisited= (HashMap) pairDestDriver.getValue();
			markVisited.replace("visited", 1);  //marking the node as visited
			driverDestLat=(double) markVisited.get("latitude");
			driverDestLon=(double) markVisited.get("longitude");
			 destDriver.put(pairDestDriver.getKey(), markVisited);
			
			System.out.println("Dest Driver=="+destDriver.get(pairDestDriver.getKey()).toString());
			
			distance=distance + g.calcDistance(userDestLat, userDestLon, driverDestLat, driverDestLon);
			driverDistance=g.calcDistance(driverSourceLat,driverSourceLon,driverDestLat,driverDestLon);
			System.out.println("the total distance is=="+distance);
			placePhero(sourceDriver,(String)pairSrcDriver.getKey(),distance,driverDistance);
			
		}	
		
	}
	
	
	//good factors
	 private void placePhero (HashMap sourceDriver, String driverId, double distance, double driverDistance)
	  {                  
		 System.out.println("driverID======"+driverId);
	                       
	    HashMap placePhero;
	    double existingPheroAmount,weightage;
	    if((distance-driverDistance) <5)
	    {
	    	weightage=5.0;
	    }
	    if((distance-driverDistance) > 5 && (distance-driverDistance) < 10)
	    {
	    	weightage=3.0;
	    }
	    else 
	    {
	    	weightage=0.0;
	    }
	    	placePhero= (HashMap) sourceDriver.get(driverId);  //driver id
	    	existingPheroAmount= (double) placePhero.get("pheromone");
	    	placePhero.replace("pheromone", weightage);   //adding pheromone
			sourceDriver.put(driverId, placePhero);		
			
			System.out.println("SourceDriver after pheromone=="+sourceDriver.get(driverId).toString());
			System.out.println("Source Driver hashmap=="+sourceDriver.toString());
			getBestDriver(sourceDriver);
	                            
	  }  
	 
	 public void addEvaporation()
	 {
		 
	 }
	 
	 private int allocateAnts(int i)
	 {
		 
		 return i;
	 }
	
	 
	 
	 
	 
	 //Distance
	 private static String readAll(Reader rd) throws IOException 
		{
		   StringBuilder sb = new StringBuilder();
		   int cp;
		   while ((cp = rd.read()) != -1) 
		   {
		     sb.append((char) cp);
		   }
		   return sb.toString();
		}
	 
	 public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException 
		{
			
		   InputStream is = new URL(url).openStream();
		   try 
		   {
		     BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
		     String jsonText = readAll(rd);
		     JSONObject json = new JSONObject(jsonText);
		     return json;
		   }
		   finally 
		   {
		     is.close();
		   }
		}
	 
	 public float calcDistance(double srcLatitude,double srcLongitude,double destLatitude,double destLongitude) 
		{				
			JSONObject json=null;
			float dist = 0;
			try 
			{
		
			//json = readJsonFromUrl("https://maps.googleapis.com/maps/api/distancematrix/json?origins="+string+"&destinations="+string2+"&mode=driving&sensor=false&key=AIzaSyDnT3pFyPjKeyKWTMmlXdfBs-ZCuqf6zMg");
			json = readJsonFromUrl("https://maps.googleapis.com/maps/api/distancematrix/json?origins="+srcLatitude+","+srcLongitude+"&destinations="+destLatitude+","+destLongitude+"&mode=driving&sensor=false&key=AIzaSyDnT3pFyPjKeyKWTMmlXdfBs-ZCuqf6zMg");
			json.get("rows");
			JSONArray arr=null;
			arr = json.getJSONArray("rows");
			Integer tem = (Integer)arr.getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("distance").getInt("value");
			dist=(float)tem/1000;
			}
			catch (JSONException e) 
			{
			e.printStackTrace();
			} 
			catch (IOException e)
			{
			    e.printStackTrace();
			}
			return dist;
		}
	 
	 public void calcTime()
	 {
		 
	 }
	 
	 public void calcDimensions()
	 {
		 
	 }
	 
	 public void getBestDriver(HashMap drivers)
	 {
		 HashMap driver;
		 TreeMap bestDriverTreeMap=new TreeMap();
		 TreeMap sortedBestDriver=new TreeMap();
		 String driverId;
		 Iterator itDriver = drivers.entrySet().iterator();
		 //ArrayList driverWeightage=new ArrayList();
		 
		 System.out.println("drivers in getting best driver==="+drivers.toString());
		
		 double weightage, bestDriverWeightage;
		 while(itDriver.hasNext())
		 {
			 Map.Entry driverMap=(Map.Entry)itDriver.next();
			 driver=(HashMap) driverMap.getValue();
			 weightage=(double) driver.get("pheromone");
			 driverId=(String) driverMap.getKey();
			 
			 bestDriverTreeMap.put(driverId,weightage);
		 }
		 
		 weightage=5.0;
		 driverId="0";
		 
		 bestDriverTreeMap.put(driverId,weightage);
		// bestDriver.descendingMap();		 
		Map sortedBestDriverMap=sortByValues(bestDriverTreeMap);
		
		
		/* Set set = sortedBestDriverMap.entrySet();    //used to display the values
		 
		    // Get an iterator
		    Iterator i = set.iterator();
		 
		    // Display elements
		    while(i.hasNext()) {
		      Map.Entry me = (Map.Entry)i.next();
		      System.out.print(me.getKey() + ": ");
		      System.out.println(me.getValue());
		    }*/
		
		System.out.println("Best Driver after sorting=="+sortedBestDriverMap.toString());
		
	 }
	 
	 public static <K, V extends Comparable<V>> Map<K, V> 
	    sortByValues(final Map<K, V> map) {
	    Comparator<K> valueComparator = new Comparator<K>() {
		      public int compare(K k1, K k2) {
		        int compare = 
		              map.get(k1).compareTo(map.get(k2));
		        if (compare == 0) 
		          return 1;
		        else 
		          return compare;
		      }
	    };
	 
	    Map<K, V> sortedByValues = 
	      new TreeMap<K, V>(valueComparator.reversed());
	    sortedByValues.putAll(map);
	    return sortedByValues;
	  }
	 
	 //end
	public static void main(String args[])
	{		
		StringBuffer start =new StringBuffer();
		StringBuffer stop = new StringBuffer();
		float distance;
		start.append("San Francisco,CA");
		stop.append("Santa Cruz,CA");
		
		HashMap sourceDriver = initializeDriverSourceValues();
		HashMap destDriver = initializeDriverDestValues();
		HashMap sourceUser = initializeUserSourceValues();
		HashMap destUser = initializeUserDestValues();
		
		GraphTraversal g= new GraphTraversal();
		//distance= g.calcDistance(137.43,144.46,138.23,145.67);
		//System.out.println("Distance=="+distance);
		g.traversal(sourceDriver,destDriver,sourceUser,destUser,1); //parameter is userId
		g.allocateAnts(sourceDriver.size());				
	}
	

}
;