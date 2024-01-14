package projet_sd;

import java.util.ArrayList;
import java.util.List;

public class BankAccount {
    private int id;
    private double balance;
    private List<String> transactionHistory;

    public BankAccount(int id, double balance){
        this.id = id;
        this.transactionHistory = new ArrayList<>();
        if(balance >= 0)
            this.balance = balance;
        else
            this.balance = -balance;
    }

    public int getId(){
        return this.id;
    }

    public double getBalance(){
        return this.balance;
    }

    public List<String> getTransactionHistory(){
        return this.transactionHistory;
    }

    //le depot
    public void deposit(double amount){
        balance += amount;
    }

    //le retrait
    public void withdraw(double amount){
        balance -= amount;
    }

    //ajouter transaction dans l'historique
    public void addTransactionHistory(String transaction){
        transactionHistory.add(transaction);
    }

    //affichier les infos du compte
    public void displayAccount(){
        System.out.println("--------------><------------");
        System.out.println("le solde du compte numero: " + getId() + " est: " + getBalance());
        System.out.println("historique des opération:--------> ");
        if(getTransactionHistory().size() == 0){
            System.out.println("Aucune transaction effectuée.");
        }
        else{
            for(String transaction : transactionHistory){
                System.out.println(transaction);
            }
        }
        System.out.println("--------------><------------");
    }
    
}
