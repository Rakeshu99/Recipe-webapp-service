package com.example.ead.be;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class Persistence {
  // a few pre-wired recipes we can insert into the database as examples.
  public static List<Recipe> recipes = Arrays.asList(
          new Recipe("elotes",
                  Arrays.asList("corn", "mayonnaise", "cotija cheese", "sour cream", "lime" ),
                  35),
          new Recipe("loco moco",
                  Arrays.asList("ground beef", "butter", "onion", "egg", "bread bun", "mushrooms" ),
                  54),
          new Recipe("patatas bravas",
                  Arrays.asList("potato", "tomato", "olive oil", "onion", "garlic", "paprika" ),
                  80),
          new Recipe("fried rice",
                  Arrays.asList("rice", "soy sauce", "egg", "onion", "pea", "carrot", "sesame oil" ),
                  40)
  );

  private MongoClient mongoClient = null;
  private MongoDatabase database = null;
  private MongoCollection<Recipe> collection = null;

  public Persistence()
  {
    // User/Password should NOT be embedded in the code. I just do it for simplicity while taking care to give this user the least privilege to avoid security issues!
    initMongoDBClient("mongodb+srv://ead2024:ead2024.@ead-2023-24.lpclwdo.mongodb.net/", "ead_ca2", "ead_2024");
  }

  public Persistence(String connString, String dbName, String colName)
  {
    initMongoDBClient(connString, dbName, colName);
  }

  public void initMongoDBClient(String connString, String dbName, String collectionName)
  {
    ConnectionString mongoUri = new ConnectionString(connString);

    if (mongoClient==null) {
      Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.WARNING);

      CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
              fromProviders(PojoCodecProvider.builder().automatic(true).build()));

      MongoClientSettings settings = MongoClientSettings.builder()
              .codecRegistry(pojoCodecRegistry)
              .applyConnectionString(mongoUri).build();

      mongoClient = null;
      try {
        mongoClient = MongoClients.create(settings);
      } catch (MongoException me) {
        System.err.println("Unable to connect to the MongoDB instance due to an error: " + me);
        System.exit(1);
      }
    }

    // MongoDatabase defines a connection to a specific MongoDB database
    database = mongoClient.getDatabase(dbName);
    // MongoCollection defines a connection to a specific collection of documents in a specific database
    collection = database.getCollection(collectionName, Recipe.class);
  }

  public List<Recipe> getAllRecipes()
  {
    MongoCursor<Recipe> cur = collection.find().iterator();
    List<Recipe> myRecipes = new ArrayList<Recipe>();
    while(cur.hasNext()) {
      myRecipes.add(cur.next());
    }
    return myRecipes;
  }

  public int addRecipes (List<Recipe> recipes)
  {
    try {
      //System.out.println("BEFORE:" + collection.countDocuments());
      InsertManyResult result = collection.insertMany(recipes);
      System.out.println("Inserted " + result.getInsertedIds().size() + " documents.\n");
      //System.out.println("AFTER:" + collection.countDocuments());
      return result.getInsertedIds().size();
    } catch (MongoException me) {
      System.err.println("Unable to insert any recipes into MongoDB due to an error: " + me);
      return -1;
    }
  }

  public int deleteRecipes (Bson deleteFilter)
  {
    try {
      DeleteResult deleteResult = collection.deleteMany(deleteFilter);
      System.out.printf("\nDeleted %d documents.\n", deleteResult.getDeletedCount());
      return (int) deleteResult.getDeletedCount();
    } catch (MongoException me) {
      System.err.println("Unable to delete any recipes due to an error: " + me);
      return -1;
    }
  }

  public int deleteRecipesByName (List<String> recipeNames)
  {
    Bson deleteFilter = Filters.in("name", recipeNames);
    return this.deleteRecipes(deleteFilter);
  }

  // The aim of this method is to mimic main to check if my refactoring (in prep for the webservices) is OK.
  public void main2() {
    addRecipes(recipes);

    List<Recipe> myRecipes = getAllRecipes();
    for (Recipe currentRecipe : myRecipes) {
      System.out.printf("%s has %d ingredients and takes %d minutes to make\n",
              currentRecipe.getName(), currentRecipe.getIngredients().size(), currentRecipe.getPrepTimeInMinutes());
    }

    // Purposely skipping to replicate the find and update, as they are not needed for illustrative purposes.
    // If needed, such methods can be created, based on the existing examples.

    deleteRecipesByName(Arrays.asList("elotes", "fried rice"));
  }

  public static void main(String[] args) {
    Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.WARNING);
    ConnectionString mongoUri = new ConnectionString("mongodb+srv://ead2024:ead2024.@ead-2023-24.lpclwdo.mongodb.net/");
    String dbName = "ead_ca2";
    String collectionName = "ead_2024";

    CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    MongoClientSettings settings = MongoClientSettings.builder()
            .codecRegistry(pojoCodecRegistry)
            .applyConnectionString(mongoUri).build();

    MongoClient mongoClient = null;
    try {
       mongoClient = MongoClients.create(settings);
    } catch (MongoException me) {
      System.err.println("Unable to connect to the MongoDB instance due to an error: " + me);
      System.exit(1);
    }

    // MongoDatabase defines a connection to a specific MongoDB database
    MongoDatabase database = mongoClient.getDatabase(dbName);
    // MongoCollection defines a connection to a specific collection of documents in a specific database
    MongoCollection<Recipe> collection = database.getCollection(collectionName, Recipe.class);

    /*      *** INSERT DOCUMENTS ***
     *
     * You can insert individual documents using collection.insert().
     * In this example, we're going to create 4 documents and then
     * insert them all in one call with insertMany().
     */

    try {
      // recipes is a static variable defined above
      InsertManyResult result = collection.insertMany(recipes);
      System.out.println("Inserted " + result.getInsertedIds().size() + " documents.\n");
    } catch (MongoException me) {
      System.err.println("Unable to insert any recipes into MongoDB due to an error: " + me);
      System.exit(1);
    }

    /*      *** FIND DOCUMENTS ***
     *
     * Now that we have data in Atlas, we can read it. To retrieve all of
     * the data in a collection, we call find() with an empty filter. We can
     * retrieve an iterator to return the results from our call to the find()
     * method. Here we use the try-with-resources pattern to automatically
     * close the cursor once we finish reading the recipes.
     */

    try (MongoCursor<Recipe> cursor = collection.find().iterator()) {
      while (cursor.hasNext()) {
        Recipe currentRecipe = cursor.next();
        System.out.printf("%s has %d ingredients and takes %d minutes to make\n",
                currentRecipe.getName(), currentRecipe.getIngredients().size(), currentRecipe.getPrepTimeInMinutes());
      }
    } catch (MongoException me) {
      System.err.println("Unable to find any recipes in MongoDB due to an error: " + me);
    }
//*
    // We can also find a single document. Let's find the first document
    // that has the string "potato" in the ingredients list. We
    // use the Filters.eq() method to search for any values in any
    // ingredients list that match the string "potato":

    Bson findPotato = Filters.eq("ingredients", "potato");
    try {
      Recipe firstPotato = collection.find(findPotato).first();
      if (firstPotato == null) {
        System.out.println("Couldn't find any recipes containing 'potato' as an ingredient in MongoDB.");
        System.exit(1);
      }
    } catch (MongoException me) {
      System.err.println("Unable to find a recipe to update in MongoDB due to an error: " + me);
      System.exit(1);
    }

    //      *** UPDATE A DOCUMENT ***
    //
    // You can update a single document or multiple documents in a single call.
    //
    // Here we update the PrepTimeInMinutes value on the document we
    // just found.

    Bson updateFilter = Updates.set("prepTimeInMinutes", 72);

    // The following FindOneAndUpdateOptions specify that we want it to return
    // the *updated* document to us. By default, we get the document as it was *before*
    // the update.
    FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);

    // The updatedDocument object is a Recipe object that reflects the
    // changes we just made.
    try {
      Recipe updatedDocument = collection.findOneAndUpdate(findPotato,
              updateFilter, options);
      if (updatedDocument == null) {
        System.out.println("Couldn't update the recipe. Did someone (or something) delete it?");
      } else {
        System.out.println("\nUpdated the recipe to: " + updatedDocument);
      }
    } catch (MongoException me) {
      System.err.println("Unable to update any recipes due to an error: " + me);
    }
//*/
    /*      *** DELETE DOCUMENTS ***
     *
     *      As with other CRUD methods, you can delete a single document
     *      or all documents that match a specified filter. To delete all
     *      of the documents in a collection, pass an empty filter to
     *      the deleteMany() method. In this example, we'll delete 2 of
     *      the recipes.
     */
    Bson deleteFilter = Filters.in("name", Arrays.asList("elotes", "fried rice"));
    try {
      DeleteResult deleteResult = collection
              .deleteMany(deleteFilter);
      System.out.printf("\nDeleted %d documents.\n", deleteResult.getDeletedCount());
    } catch (MongoException me) {
      System.err.println("Unable to delete any recipes due to an error: " + me);
    }

    // always close the connection when done working with the client
    mongoClient.close();
  }
}
