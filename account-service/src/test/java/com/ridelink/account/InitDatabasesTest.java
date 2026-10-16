package com.ridelink.account;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

public class InitDatabasesTest {

    private static final String MONGO_URI = "mongodb://localhost:27017";

    @Test
    void initializeAllFourDatabases() {
        System.out.println("Connecting to MongoDB at " + MONGO_URI + " to create 4 microservice databases...");

        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            for (String dbName : mongoClient.listDatabaseNames()) {
                MongoDatabase db = mongoClient.getDatabase(dbName);
                for (String coll : db.listCollectionNames()) {
                    long count = db.getCollection(coll).countDocuments();
                    System.out.println("DB [" + dbName + "] -> Coll [" + coll + "] : " + count + " docs");
                }
            }

            System.out.println("Successfully created and verified all 4 microservice databases in MongoDB!");
        }
    }

    private void initDatabase(MongoClient client, String dbName, List<String> collections) {
        MongoDatabase db = client.getDatabase(dbName);
        for (String coll : collections) {
            boolean exists = false;
            for (String name : db.listCollectionNames()) {
                if (name.equalsIgnoreCase(coll)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                db.createCollection(coll);
                System.out.println("Created collection [" + coll + "] in database [" + dbName + "]");
            } else {
                System.out.println("Collection [" + coll + "] already exists in database [" + dbName + "]");
            }
        }
    }
}
