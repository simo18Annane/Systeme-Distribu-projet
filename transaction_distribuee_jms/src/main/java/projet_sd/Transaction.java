package projet_sd;

import java.io.Serializable;

public class Transaction implements Serializable {
    private int id;
    private double amount;
    private String sourceBank;
    private int sourceAccount;
    private int destinationAccount;
    private String destinationBank;

    public Transaction(int id, int sourceId, String sourceBank, int destinationId, String destinationBank, double amount){
        this.id = id;
        this.sourceAccount = sourceId;
        this.sourceBank = sourceBank;
        this.destinationAccount = destinationId;
        this.destinationBank = destinationBank;
        this.amount = amount;
    }

    public int getId(){
        return this.id;
    }

    public String getSourceBank(){
        return this.sourceBank;
    }

    public int getSourceAccount(){
        return this.sourceAccount;
    }

    public int getDestinationAccount(){
        return this.destinationAccount;
    }

    public String getDestinationBank(){
        return this.destinationBank;
    }

    public double getAmount(){
        return this.amount;
    }



    public String descSource(){
        return "Virement emis vers le compte " +getDestinationAccount() + "/" + getDestinationBank() + " : -" + getAmount(); 
    }

    public String descDestination(){
        return "Virement recu de la part du compte " + getSourceAccount() + "/" + getSourceBank() + " : +" + getAmount();
    }

}
