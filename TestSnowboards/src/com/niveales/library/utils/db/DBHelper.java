package com.niveales.library.utils.db;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper{
    
    private static final String TAG = "DBHelper";

//    private static final String DATABASE_NAME = "skieur_tests2012.db";
    @SuppressWarnings("unused")
	private static final int DATABASE_VERSION = 1;


    // Variable to hold the database instance
    protected SQLiteDatabase mDb;
    // Context of the application using the database.
    private final Context mContext;
    // Database open/upgrade helper
    private MyDbHelper mDbHelper;

    
    public DBHelper(Context context, String dbName) {
        mContext = context;
        mDbHelper = new MyDbHelper(mContext, dbName);
    }
    
    public DBHelper open() throws SQLException { 
        mDb = mDbHelper.getWritableDatabase();
        if(mDb != null) {
        	createUserSearchInputsTable();
        	mDb.rawQuery("CREATE TABLE IF NOT EXISTS \""+FAVORITES_TABLE+"\" (\"id\"  UNIQUE );", null).moveToFirst();
        }
        return this;
    }
    
    /**
	 * 
	 */
	public void createUserSearchInputsTable() {
		mDb.rawQuery("CREATE TABLE IF NOT EXISTS \"UserSearchInputs\" (\"ColName\" TEXT NOT NULL , \"UserInput\" TEXT, \"Title\" TEXT,\"QueryString\" TEXT);", null).moveToFirst();
		mDb.rawQuery("CREATE TABLE IF NOT EXISTS \"UserSearchInputsOld\" (\"ColName\" TEXT NOT NULL , \"UserInput\" TEXT, \"Title\" TEXT,\"QueryString\" TEXT);", null).moveToFirst();
	}


                                                     
    public void close() {
        mDb.close();
    }
    
    
    public Cursor getAllFromTable(String table) {
    	Cursor res = this.mDb.query(table, null, null, null, null, null, null);
    	if(res != null) {
    		res.moveToFirst();
    	}
    	return res;
    }
    
    public Cursor getAllFromTableWithOrder(String table, String order) {
    	Cursor res = this.mDb.query(table, null, null, null, null, null, order);
    	if(res != null) {
    		res.moveToFirst();
    	}
    	return res;
    }
    
    public Cursor getAllFromTableWithWhereAndOrder(String table, String where, String order) {
    	Cursor res = this.mDb.query(table, null, where, null, null, null, order);
    	if(res != null) {
    		res.moveToFirst();
    	}
    	return res;
    }
    
    public Cursor rawQuery(String sql, String [] args) {
    	Cursor res = this.mDb.rawQuery(sql, args);
    	if(res != null) {
    		res.moveToFirst();
    	}
    	return res;
    }
    // -------------- Advanced criteteria
    public static final String ADVANCED_CRITERIA_TABLE = "AdvancedCriteria";
    public static final String ADVANCED_CRITERIA_TITLE = "Title";
    public static final String ADVANCED_CRITERIA_COLNAME = "ColName";
    public static final String ADVANCED_CRITERIA_TYPE = "Type";
    public static final String ADVANCED_CRITERIA_HEADERTEXT = "HeaderText";
    
    public Cursor getAllAdvancedCriteria() {
    	Cursor res = mDb.query(ADVANCED_CRITERIA_TABLE, null, null, null, null, null, null);
    	if( res!=null ) {
    		res.moveToFirst();
    	}
    	return res;
	}

    
    // -------------- UserSearchInputs
    public static final String USER_SEARCH_INPUTS_TABLE ="UserSearchInputs";
    public static final String USER_SEARCH_INPUTS_COLNAME = "ColName";
    public static final String USER_SEARCH_INPUTS_USERINPUT = "UserInput";
    public static final String USER_SEARCH_INPUTS_TITLE = "Title";
    public static final String USER_SEARCH_INPUTS_QUERYSTRING = "QueryString";
    
    public Cursor getAllUserSearchInput() {
    	Cursor res = mDb.query(USER_SEARCH_INPUTS_TABLE, null, null, null, null, null, null);
    	if( res != null ) {
    		res.moveToFirst();
    	}
    	return res;
    }
    
    public Cursor getUserSearchInputTitleByColumn(String column) {
    	Cursor res = mDb.query(USER_SEARCH_INPUTS_TABLE, new String [] {
    			USER_SEARCH_INPUTS_TITLE
    	}, USER_SEARCH_INPUTS_COLNAME + " = '"+column + "'", null, null, null, null);
    	if( res != null ) {
    		res.moveToFirst();
    	}
    	return res;
    }
    
    public String getUserSearchInputStringByColumn (String column) {
    	Cursor cursor = getUserSearchInputTitleByColumn(column);
    	String res = "";
    	boolean isFirst = true;
    	if(cursor.getCount() > 0) {
			while (!cursor.isAfterLast()) {
				if (isFirst) {
					res = cursor.getString(cursor
							.getColumnIndexOrThrow(USER_SEARCH_INPUTS_TITLE));
					isFirst = false;
				} else {
					res += ", "
							+ cursor.getString(cursor
									.getColumnIndexOrThrow(USER_SEARCH_INPUTS_TITLE));
				}
				cursor.moveToNext();
			}
    	}
    	return res;
    }
	// -------------- LEXIQUE DEFINITIONS ------------

	public static final String LEXIQUE_TABLE = "Lexique";
	public static final String LEXIQUE_COL_1_KEY = "col_1";
	protected static final int LEXIQUE_COL_1_COLUMN = 1;
	public static final String LEXIQUE_GAMME_KEY = "Gamme";
	protected static final int LEXIQUE_GAMME_COLUMN = 2;
	public static final String LEXIQUE_DESCRIPTION_KEY = "Description";
	protected static final int LEXIQUE_DESCRIPTION_COLUMN = 3;
	public static final String LEXIQUE_COL_4_KEY = "col_4";
	protected static final int LEXIQUE_COL_4_COLUMN = 4;
//	public static final String LEXIQUE_ROW_ID = "_id";

	// -------------- MODELE DEFINITIONS ------------
	
	public static final String MODELE_GENRE_KEY = "Genre";
	public static final String TEST_TESTER_CHOICE_KEY = "test_tester_choice";

	public static final String MODELE_TABLE = "Modele";
	public static final String MODELE_ANNEE_KEY = "Année";
	protected static final int MODELE_ANNEE_COLUMN = 1;
	public static final String MODELE_ID_MODELE_KEY = "id_modele";
	protected static final int MODELE_ID_MODELE_COLUMN = 2;
	public static final String MODELE_ID_MARQUE_KEY = "Id_marque";
	protected static final int MODELE_ID_MARQUE_COLUMN = 3;
	public static final String MODELE_ID_GAMME_KEY = "Id_gamme";
	protected static final int MODELE_ID_GAMME_COLUMN = 4;
	public static final String MODELE_MARQUE_KEY = "Marque";
	protected static final int MODELE_MARQUE_COLUMN = 5;
	public static final String MODELE_GAMME_KEY = "Gamme";
	protected static final int MODELE_GAMME_COLUMN = 6;
	public static final String MODELE_MODELE_KEY = "Modele";
	protected static final int MODELE_MODELE_COLUMN = 7;
	public static final String MODELE_IMG_KEY = "img";
	protected static final int MODELE_IMG_COLUMN = 8;
	public static final String MODELE_ID_GENRE_KEY = "Id_genre";
	protected static final int MODELE_ID_GENRE_COLUMN = 9;
	public static final String MODELE_CARACTERE_KEY = "caractere";
	protected static final int MODELE_CARACTERE_COLUMN = 10;
	public static final String MODELE_NIVEAU_KEY = "niveau";
	protected static final int MODELE_NIVEAU_COLUMN = 11;
	public static final String MODELE_TAILLES_KEY = "Tailles";
	protected static final int MODELE_TAILLES_COLUMN = 12;
	public static final String MODELE_TAILLE_DE_REFERENCE_KEY = "Taille_de_reference";
	protected static final int MODELE_TAILLE_DE_REFERENCE_COLUMN = 13;
	public static final String MODELE_PRIX_DE_REFERENCE_KEY = "Prix_de_reference";
	protected static final int MODELE_PRIX_DE_REFERENCE_COLUMN = 14;
	public static final String MODELE_CARACTERISTIQUES_KEY = "Caractéristiques";
	protected static final int MODELE_CARACTéRISTIQUES_COLUMN = 15;
	public static final String MODELE_TEST_KEY = "Test";
	protected static final int MODELE_TEST_COLUMN = 16;
	public static final String MODELE_TEST_TESTERS_CHOICE_KEY = "test_testers_choice";
	protected static final int MODELE_TEST_TESTERS_CHOICE_COLUMN = 17;
	public static final String MODELE_TEST_TAILLE_TESTEE_KEY = "test_Taille_testee";
	protected static final int MODELE_TEST_TAILLE_TESTEE_COLUMN = 18;
	public static final String MODELE_TEST_BASELINE_KEY = "Test_baseline";
	protected static final int MODELE_TEST_BASELINE_COLUMN = 19;
	public static final String MODELE_DESCRIPTION_TEST_KEY = "Description_Test";
	protected static final int MODELE_DESCRIPTION_TEST_COLUMN = 20;
	public static final String MODELE_TEST_AVANTAGES_KEY = "Test_avantages";
	protected static final int MODELE_TEST_AVANTAGES_COLUMN = 21;
	public static final String MODELE_TEST_INCONVENIENTS_KEY = "test_inconvenients";
	protected static final int MODELE_TEST_INCONVENIENTS_COLUMN = 22;
//	public static final String MODELE_ROW_ID = "_id";



	public static final String FAVORITES_TABLE = "UserFavorites";
	public static final String FAVORITES_MODEL_ID_KEY = "id";
	protected static final int FAVORITES_MODEL_ID_COLUMN = 1;
	// -------- TABLES CREATION ----------

	public static final String ADVANCED_SELECT_TITLE_KEY = "Title";
	public static final String ADVANCED_SELECT_HEADER_KEY = "HeaderText";
	public static final String ADVANCED_SELECT_ICON_KEY = "Icon";
	public static final String ADVANCED_SELECT_DETAILLINK_KEY = "DetailLink";
	public static final String ADVANCED_SELECT_INPUT_KEY = "Input";
	public static final String USER_SEARCH_INPUTS_ID = "Id";

	public static final String USER_SEARCH_INPUTS_HEADERTEXT = "HeaderText";

	private static final String DETAIL_TABLE = "Detail";





	// -------------- LEXIQUE HELPERS ------------------


	public Cursor getAllLexique()
	{
		return mDb.query(LEXIQUE_TABLE, new String[] {
//					LEXIQUE_ROW_ID,
//					LEXIQUE_COL_1_KEY,
					LEXIQUE_GAMME_KEY,
					LEXIQUE_DESCRIPTION_KEY,
//					LEXIQUE_COL_4_KEY
					}, null, null, null, null, LEXIQUE_GAMME_KEY);
	}

	public Cursor getLexique(long rowIndex)
	{
		Cursor res = mDb.query(LEXIQUE_TABLE, new String[] {
//					LEXIQUE_ROW_ID,
					LEXIQUE_COL_1_KEY,
					LEXIQUE_GAMME_KEY,
					LEXIQUE_DESCRIPTION_KEY,
					LEXIQUE_COL_4_KEY}, 
//					LEXIQUE_ROW_ID + " = " + rowIndex,
					LEXIQUE_COL_1_KEY + " = " + rowIndex,
					null, null, null, LEXIQUE_GAMME_KEY);
		if(res != null){
			res.moveToFirst();
		}
		return res;
	}

	// -------------- MODELE HELPERS ------------------

	public Cursor getAllModele()
	{
		return mDb.query(MODELE_TABLE, new String[] {
//					MODELE_ROW_ID,
					MODELE_ANNEE_KEY,
					MODELE_ID_MODELE_KEY,
					MODELE_ID_MARQUE_KEY,
					MODELE_ID_GAMME_KEY,
					MODELE_MARQUE_KEY,
					MODELE_GAMME_KEY,
					MODELE_MODELE_KEY,
					MODELE_IMG_KEY,
					MODELE_ID_GENRE_KEY,
					MODELE_CARACTERE_KEY,
					MODELE_NIVEAU_KEY,
					MODELE_TAILLES_KEY,
					MODELE_TAILLE_DE_REFERENCE_KEY,
					MODELE_PRIX_DE_REFERENCE_KEY,
					MODELE_CARACTERISTIQUES_KEY,
					MODELE_TEST_KEY,
					MODELE_TEST_TESTERS_CHOICE_KEY,
					MODELE_TEST_TAILLE_TESTEE_KEY,
					MODELE_TEST_BASELINE_KEY,
					MODELE_DESCRIPTION_TEST_KEY,
					MODELE_TEST_AVANTAGES_KEY,
					MODELE_TEST_INCONVENIENTS_KEY}, null, null, null, null, null);
	}
	
	public Cursor getAllFavoriteModele()
	{
		return mDb.query(MODELE_TABLE + ", " +FAVORITES_TABLE, new String[] {
//					MODELE_ROW_ID,
					MODELE_ANNEE_KEY,
					MODELE_ID_MODELE_KEY,
					MODELE_ID_MARQUE_KEY,
					MODELE_ID_GAMME_KEY,
					MODELE_MARQUE_KEY,
					MODELE_GAMME_KEY,
					MODELE_MODELE_KEY,
					MODELE_IMG_KEY,
//					MODELE_ID_GENRE_KEY,
//					"case when id_genre='1' then '"+mContext.getResources().getStringArray(R.array.genre)[1]+"' when id_genre='2' then '"+mContext.getResources().getStringArray(R.array.genre)[0]+"' end as '"+MODELE_GENRE_KEY+"'",

					MODELE_CARACTERE_KEY,
					MODELE_NIVEAU_KEY,
					MODELE_TAILLES_KEY,
					MODELE_TAILLE_DE_REFERENCE_KEY,
					MODELE_PRIX_DE_REFERENCE_KEY,
					MODELE_CARACTERISTIQUES_KEY,
					MODELE_TEST_KEY,
					MODELE_TEST_TESTERS_CHOICE_KEY,
					"case when test_testers_choice='1' then 'Oui' end as "+TEST_TESTER_CHOICE_KEY,

					MODELE_TEST_TAILLE_TESTEE_KEY,
					MODELE_TEST_BASELINE_KEY,
					MODELE_DESCRIPTION_TEST_KEY,
					MODELE_TEST_AVANTAGES_KEY,
					MODELE_TEST_INCONVENIENTS_KEY}, MODELE_ID_MODELE_KEY + " = " +
					FAVORITES_TABLE + "." +FAVORITES_MODEL_ID_KEY, null, null, null, null);
	}

	public Cursor getModele(long rowIndex)
	{
		Cursor res = mDb.query(DETAIL_TABLE, null, MODELE_ID_MODELE_KEY + " = '" + rowIndex + "'", null, null, null, null);
		if(res != null){
			res.moveToFirst();
		}
		return res;
	}

	
	
	


	public Cursor getAllGenre() {
		// Here we do some SQL magic to translate genreIDs with actual strings
		Cursor res = mDb.query(true, MODELE_TABLE, new String[] {
				MODELE_ID_GENRE_KEY,
//				"case when id_genre='1' then '"+mContext.getResources().getStringArray(R.array.genre)[1]+"' when id_genre='2' then '"+mContext.getResources().getStringArray(R.array.genre)[0]+"' end as 'id_genre'"
		}, null, null, null, null, null, null);
		if(res != null) {
			res.moveToFirst();
		}
		return res;
	}
	
	public Cursor getTesterChoice() {
		// Here we do some SQL magic to translate genreIDs with actual strings
		Cursor res = mDb.query(true, MODELE_TABLE, new String[] {
				MODELE_TEST_TESTERS_CHOICE_KEY,
//				"case when test_testers_choice='0' then '"+mContext.getResources().getStringArray(R.array.YesNo)[0]+"' when test_testers_choice='1' then '"+mContext.getResources().getStringArray(R.array.YesNo)[1]+"' end as 'tester_choice'"
		}, null, null, null, null, null, null);
		if(res != null) {
			res.moveToFirst();
		}
		return res;
	}
	
	public Cursor getColumnWithId(String idRow, String columnRow) {
		// Here we do some SQL magic to translate genreIDs with actual strings
		Cursor res = mDb.query(true, MODELE_TABLE, new String[] {
				idRow,
				columnRow
		}, null, null, null, null, null, null);
		if(res != null) {
			res.moveToFirst();
		}
		return res;
	}
	
	public Cursor getColumnFromDetails(String columnRow) {
		
		// Here we do some SQL magic to translate genreIDs with actual strings
		
		Cursor res = mDb.query(true, DETAIL_TABLE, new String[] {
				columnRow
		}, columnRow +" != ''", null, null, null, columnRow, null);
		if(res != null) {
			res.moveToFirst();
		}
		return res;
	}
	
	public ArrayList<String> getColumnAsStringArray(String columnRow) {
		ArrayList<String> possibleValues = new ArrayList<String>();
		Cursor cursor = getColumnFromDetails(columnRow);
		// read all possible values from cursor
		while(!cursor.isAfterLast()) {
			possibleValues.add(cursor.getString(0));
			cursor.moveToNext();
		}
		return possibleValues;
	}

	
	public long addFavorite(String id) {

			ContentValues contentValues = new ContentValues();
			contentValues.put(FAVORITES_MODEL_ID_KEY, String.valueOf(id));
			return mDb.insert(FAVORITES_TABLE, null, contentValues);
	}
	
	public long deleteFavorite(String id) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(FAVORITES_MODEL_ID_KEY, id);

		return mDb.delete(FAVORITES_TABLE, "id = '"+id+"'", null);
	}
	
	public boolean isFavorite(String id) {
		Cursor cursor = mDb.query(FAVORITES_TABLE, new String [] {
				FAVORITES_MODEL_ID_KEY
		}, FAVORITES_MODEL_ID_KEY + " = '"+id+"'", null, null, null, null);
		if(cursor != null) {
			cursor.moveToFirst();
		}
		return cursor.getCount() > 0;
	}
	
	
    private static class MyDbHelper extends SQLiteOpenHelper {

		// The Android's default system path of your application database.
		private static String DB_PATH;

		private static final String ASSETS_DB_NAME = "skieur_tests2012_.sqlite";

		private SQLiteDatabase myDataBase;

		private final Context myContext;

		private String dbName;

		/**
		 * Constructor Takes and keeps a reference of the passed context in
		 * order to access to the application assets and resources.
		 * 
		 * @param context
		 */
		public MyDbHelper(Context context, String dbName) {
			
			super(context, dbName, null, 1);
			this.myContext = context;
			this.dbName  = dbName;
			DB_PATH = "/data/data/"+myContext.getPackageName()+"/databases/";
			try {
				createDataBase();
			} catch (IOException e) {
				e.printStackTrace();
				throw new IllegalStateException("Cannot init database, please free some space on the device");
			}
		}

		/**
		 * Creates a empty database on the system and rewrites it with your own
		 * database.
		 * */
		public void createDataBase() throws IOException {

			boolean dbExist = checkDataBase();

			if (dbExist) {
				// do nothing - database already exist
			} else {

				// By calling this method and empty database will be created
				// into the default system path
				// of your application so we are gonna be able to overwrite that
				// database with our database.
				this.getReadableDatabase();

				try {

					copyDataBase();

				} catch (IOException e) {

					throw new IOException("Error copying database");

				}
			}

		}

		/**
		 * Check if the database already exist to avoid re-copying the file each
		 * time you open the application.
		 * 
		 * @return true if it exists, false if it doesn't
		 */
		private boolean checkDataBase() {

			SQLiteDatabase checkDB = null;

			try {
				String myPath = DB_PATH + dbName;
				checkDB = SQLiteDatabase.openDatabase(myPath, null,
						SQLiteDatabase.OPEN_READONLY);

			} catch (SQLiteException e) {

				// database does't exist yet.

			}

			if (checkDB != null) {

				checkDB.close();

			}

			return checkDB != null ? true : false;
		}

		/**
		 * Copies your database from your local assets-folder to the just
		 * created empty database in the system folder, from where it can be
		 * accessed and handled. This is done by transfering bytestream.
		 * */
		private void copyDataBase() throws IOException {

			// Open your local db as the input stream
			InputStream myInput = myContext.getAssets().open(dbName+".jpg");

			// Path to the just created empty db
			String outFileName = DB_PATH + dbName;

			// Open the empty db as the output stream
			OutputStream myOutput = new FileOutputStream(outFileName);

			// transfer bytes from the inputfile to the outputfile
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}

			// Close the streams
			myOutput.flush();
			myOutput.close();
			myInput.close();

		}


		@Override
		public synchronized void close() {

			if (myDataBase != null)
				myDataBase.close();

			super.close();

		}

		@Override
		public void onCreate(SQLiteDatabase db) {

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}


	}


	
	


     
    /** Dummy object to allow class to compile */
}