package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database.tables.AccountTable;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database.tables.TransactionTable;


public class dbHandler extends SQLiteOpenHelper {
    private final static String DB_NAME = "180240J";
    private final static int DB_VERSION = 1;

    public dbHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + AccountTable.getTableName() + "(" +
                        AccountTable.getAccountAccountNo() + " TEXT PRIMARY KEY," +
                        AccountTable.getAccountBankName() + " TEXT NOT NULL," +
                        AccountTable.getAccountHolderName() + " TEXT NOT NULL," +
                        AccountTable.getAccountBalance() + " REAL" +
                        ");"
        );

        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TransactionTable.getTableName() + "(" +
                        TransactionTable.getTransactionId() + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        TransactionTable.getTransactionDate() + " TEXT NOT NULL," +
                        TransactionTable.getTransactionAccountNo() + " TEXT NOT NULL," +
                        TransactionTable.getTransactionType() + " TEXT NOT NULL," +
                        TransactionTable.getTransactionAmount() + " REAL NOT NULL," +
                        "FOREIGN KEY (" + TransactionTable.getTransactionAccountNo() + ") REFERENCES "
                        + AccountTable.getTableName() + "(" + AccountTable.getAccountAccountNo() + ")," +
                        "CHECK ("+TransactionTable.getTransactionType()+"==\""+TransactionTable.getTypeExpense()+"\" OR "+TransactionTable.getTransactionType()+"==\""+TransactionTable.getTypeIncome()+"\")"+
                        ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
