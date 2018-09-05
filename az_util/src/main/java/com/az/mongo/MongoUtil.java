package com.az.mongo;

import java.util.ArrayList;
import java.util.List;

import org.bson.BsonDocument;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class MongoUtil {
	public static MongoClient init(){
		MongoClient client = null;
		return client;
	}
	public static void main(String[] args) {
		long begin = System.currentTimeMillis();
		MongoClient mongoClient = null;
		try {
			ServerAddress serverAddress = new ServerAddress("192.168.31.138", 27017);
			List<ServerAddress> addrs = new ArrayList<ServerAddress>();
			addrs.add(serverAddress);
			MongoCredential credential = MongoCredential.createScramSha1Credential("root", "test",
					"root".toCharArray());
			List<MongoCredential> credentials = new ArrayList<MongoCredential>();
			credentials.add(credential);
			mongoClient = new MongoClient(addrs, credentials);
			MongoDatabase mongoDatabase = mongoClient.getDatabase("test");
			MongoCollection<?> coll = mongoDatabase.getCollection("a_col");
			FindIterable<?> findIterable = coll.find().limit(2).skip(2).sort(BsonDocument.parse("{age:1,name:1}"));
			MongoCursor<?> cursor = findIterable.iterator();
			while (cursor.hasNext()) {
				Document document = (Document) cursor.next();
				System.out.println(document.toJson());
			}
			System.out.println("耗时:"+(System.currentTimeMillis()-begin));
			begin = System.currentTimeMillis();
			findIterable = coll.find().limit(2).skip(2).sort(BsonDocument.parse("{age:1,name:1}"));
			cursor = findIterable.iterator();
			while (cursor.hasNext()) {
				Document document = (Document) cursor.next();
				System.out.println(document.toJson());
			}
			System.out.println("耗时:"+(System.currentTimeMillis()-begin));
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}finally {
			mongoClient.close();
		}
	}
}
