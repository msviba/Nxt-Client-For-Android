package org.nextcoin.transactions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.json.JSONObject;
import org.nextcoin.node.NodeContext;
import org.nextcoin.node.NodesManager;

public class Transaction {
    public boolean mLoaded = false;
    public String mId;
    public int mType;
    public int mConfirmations;
    public int mTimestamp = 0;
    public String mRecipient;
    public String mSender;
    public float mAmount;
    public float mFee;
    
    static public boolean loadTransaction(Transaction transaction){
        NodeContext nodeContext = NodesManager.sharedInstance().getCurrentNode();
        if ( !nodeContext.isActive()){
            return false;
        }
        
        String ip = nodeContext.getIP();
        String base_url = "http://" + ip + ":7874";
        String httpUrl = String.format(
                "%s/nxt?requestType=getTransaction&&transaction=%s", 
                base_url, transaction.mId);

        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(httpUrl).openConnection();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while((line = br.readLine()) != null)
                sb.append(line);

            String strResult = sb.toString();
            JSONObject jsonObj;
            try {
                jsonObj = new JSONObject(strResult);
                transaction.mLoaded = true; 
                transaction.mType = jsonObj.getInt("type");
                if ( 0 == transaction.mType ){
                    transaction.mConfirmations = jsonObj.getInt("confirmations");
                    transaction.mTimestamp = jsonObj.getInt("timestamp");
                    transaction.mRecipient = jsonObj.getString("recipient");
                    transaction.mSender = jsonObj.getString("sender");
                    double amount = jsonObj.getDouble("amount");
                    transaction.mAmount = (float)amount;
                    double fee = jsonObj.getDouble("fee");
                    transaction.mFee = (float)fee;
                }

                return true;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
        return false;
    }
    
    static public void sortByTimestamp(LinkedList<Transaction> List){
        Collections.sort(List, new Comparator<Transaction>(){
            @Override
            public int compare(Transaction lhs, Transaction rhs) {
                return rhs.mTimestamp - lhs.mTimestamp;
            }});
    }
}
