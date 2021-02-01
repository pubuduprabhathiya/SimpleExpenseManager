package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database.dbHandler;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database.tables.AccountTable;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

public class PersistentAccountDAO implements AccountDAO {
    private dbHandler dbhandler;
    @Override
    public List<String> getAccountNumbersList() {
        SQLiteDatabase db = dbhandler.getReadableDatabase();

        //query to get all account numbers from account table
        Cursor cursor = db.rawQuery(
                "SELECT " + AccountTable.getAccountAccountNo() + " FROM " + AccountTable.getTableName(),
                null
        );

        ArrayList<String> accountNumbersList = new ArrayList<>();

        //loop through the results and add to the list
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            accountNumbersList.add(cursor.getString(0));
        }

        cursor.close();
        return accountNumbersList;
    }

    @Override
    public List<Account> getAccountsList() {
        SQLiteDatabase db = dbhandler.getReadableDatabase();

        //query to select all rows of account table
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + AccountTable.getTableName(),
                null
        );

        ArrayList<Account> accountsList = new ArrayList<>();

        //loop through the results and create account objects and add to list
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Account account = new Account(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3)
            );
            accountsList.add(account);
        }

        cursor.close();
        return accountsList;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = dbhandler.getReadableDatabase();

        //query to get the row from the account table with relevant account number
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + AccountTable.getTableName() + " WHERE " + AccountTable.getAccountAccountNo() + "=?;"
                , new String[]{accountNo});

        //if a result exist create an account object else throw error
        Account account;
        if (cursor != null && cursor.moveToFirst()) {
            account = new Account(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3)
            );
        } else {
            throw new InvalidAccountException("The Account "+accountNo+" is Invalid");
        }
        cursor.close();
        return account;
    }

    public PersistentAccountDAO(dbHandler dbhandler) {
        this.dbhandler=dbhandler;
    }

    @Override
    public void addAccount(Account account) {
        //check if an already an account with this no exist
        Account ExistingAccount = null;
        try {
            ExistingAccount = getAccount(account.getAccountNo());
        } catch (InvalidAccountException e) {
            e.printStackTrace();
        }
        if (ExistingAccount!=null){

            return;
        }

        SQLiteDatabase db = dbhandler.getWritableDatabase();

        ContentValues contentvalues = new ContentValues();
        contentvalues.put(AccountTable.getAccountAccountNo(), account.getAccountNo());
        contentvalues.put(AccountTable.getAccountBankName(), account.getBankName());
        contentvalues.put(AccountTable.getAccountHolderName(), account.getAccountHolderName());
        contentvalues.put(AccountTable.getAccountBalance(), account.getBalance());

        //insert new row to account table
        db.insert(AccountTable.getTableName(), null, contentvalues);
        db.close();
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = dbhandler.getWritableDatabase();


         db.delete(
                 AccountTable.getTableName(),
                 AccountTable.getAccountAccountNo() + " = ?",
                 new String[]{accountNo}
                 );
         db.close();

    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        SQLiteDatabase db = dbhandler.getWritableDatabase();

        //if no account is specified to update
        if (accountNo==null) {
            throw new InvalidAccountException("Account is not specified.");
        }

        //get the account related using getAccount method in this call
        Account account = this.getAccount(accountNo);

        //if such account exist
        if (account != null) {
            double updatedBalance;

            //update the balance of account according to transaction type
            if (expenseType == ExpenseType.INCOME) {
                updatedBalance = account.getBalance() + amount;
            } else if (expenseType == ExpenseType.EXPENSE) {
                updatedBalance = account.getBalance() - amount;
            } else {
                throw new InvalidAccountException("Invalid Expense Type");
            }

            //if the account does not have enough balance throw error
            if (updatedBalance < 0){
                throw  new InvalidAccountException("Balance of " + account.getBalance() + " is insufficient for the transaction.");
            }

            // if ok query to update the balance in the account table
            db.execSQL(
                    "UPDATE " + AccountTable.getTableName() +
                            " SET " + AccountTable.getAccountBalance() + " = ?" +
                            " WHERE " + AccountTable.getAccountAccountNo() + " = ?",
                    new String[]{Double.toString(updatedBalance), accountNo});

        } else { //if such account does not exist throw error
            throw new InvalidAccountException("The Account "+accountNo+" is Invalid");
        }

        db.close();
    }
}
