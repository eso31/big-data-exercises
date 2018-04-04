package nearsoft.academy.bigdata.recommendation;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


public class MovieRecommender{
	public static void main(String args[]){

	}

	public Map<String,Integer> mapProducts = new HashMap<String,Integer>();
	public Map<String,Integer> mapUsers = new HashMap<String,Integer>();
	public int totalReviews = 0;
	public int totalProducts = 0;
	public int totalUsers = 0;

	public MovieRecommender(String path){
		toCSV(path);
	}

	public void toCSV(String path){
		String encoding = "UTF-8";
		InputStream fileStream;
		InputStream gzipStream;
		Reader decoder;
		BufferedReader buffered = null;
		ArrayList<String> products = new ArrayList<String>();
		ArrayList<String> users = new ArrayList<String>();
		ArrayList<Double> scores = new ArrayList<Double>();

		try{
			fileStream = new FileInputStream("/home/dcross/Documents/github/big-data-exercises/src/test/java/nearsoft/academy/bigdata/recommendation/movies.txt.gz");
			gzipStream = new GZIPInputStream(fileStream);
			decoder = new InputStreamReader(gzipStream, encoding);
			buffered = new BufferedReader(decoder);
		}catch(IOException e){}


		String s="";
		try{
			while((s=buffered.readLine())!=null){
				//System.out.println(s);
				if(s.contains("product/productId: ")){
					String[] arr = s.split("product/productId: ");
					//System.out.println(arr[1]);
					products.add(arr[1]);
				}else if(s.contains("review/userId: ")){
					String[] arr = s.split("review/userId: ");
					//System.out.println(arr[1]);
					users.add(arr[1]);
				}else if(s.contains("review/score: ")){
					String[] arr = s.split("review/score: ");
					//System.out.println(arr[1]);
					scores.add(Double.parseDouble(arr[1]));
					this.totalReviews++;
				}
			}
		}catch(Exception e){}

		this.mapProducts = getMap(products);
		this.mapUsers = getMap(users);
		this.totalProducts = mapProducts.size();
		this.totalUsers = mapUsers.size();


		writeFile("movies.csv",products,users,scores);
	}

	public Map<String,Integer> getMap(ArrayList<String> list){
		Map<String,Integer> map = new HashMap<String,Integer>();
		int id = 1;
		for(String s:list){
			if(!map.containsKey(s)){
				map.put(s,id);
				id++;
			}
		}

		return map;
	}

	public int getTotalReviews(){
		return this.totalReviews;
	}

	public int getTotalProducts(){
		return this.totalProducts;
	}

	public int getTotalUsers(){
		return this.totalUsers;
	}

	public void writeFile(String name, ArrayList<String> p, ArrayList<String> u,ArrayList<Double> s){
		try{
			PrintWriter out = new PrintWriter(new File(name));

			for(int i=0; i<p.size(); i++){
				//System.out.println(i);
				out.write(mapUsers.get(u.get(i))+","+mapProducts.get(p.get(i))+","+s.get(i));
				out.println();
			}
			out.close();
		}catch(FileNotFoundException e){}
	}

	public List<String> getRecommendationsForUser(String userId){
		List<String> rec = new ArrayList<String>();
		DataModel model = null;
		UserSimilarity similarity = null;
		List<RecommendedItem> recommendations = null;

		try{
			model = new FileDataModel(new File("movies.csv"));
		}catch(IOException e){}

		try{
			similarity = new PearsonCorrelationSimilarity(model);
		}catch(TasteException e){}

		UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
		
		UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

		try{
			recommendations = recommender.recommend(mapUsers.get(userId), 3);
		}catch(TasteException e){}

		
		for (RecommendedItem recommendation : recommendations) {
		  	String r = getKeyFromProductMap((int) recommendation.getItemID());
			rec.add(r);
		}

		return rec;
	}

	public String getKeyFromProductMap(int value){
		for(String key: mapProducts.keySet()){
			if(mapProducts.get(key)==value)
				return key;
		}
		return null;
	}
}