import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.*;
import com.sun.xml.internal.ws.util.StringUtils;


public class Person {
    private String name;
    private Double balance;
    private Integer streetNum;
    private String hashCalculated;

    // Constructor
    public Person(String name, Double balance, Integer streetNum)
    {
        this.name = name;
        this.balance = balance;
        this.streetNum = streetNum;
        this.hashCalculated = null;
    }

    // Calculate hash n times
    public void calculateHash(int i)
    {
        String strPerson = name + balance.toString() + streetNum.toString();

        try
        {
            // Calculate initial hash
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(strPerson.getBytes());
            byte[] digest = md.digest();
            hashCalculated = DatatypeConverter.printHexBinary(digest).toUpperCase();

            // Calculate hash of hash
            for(int n = 1; n < i; n++)
            {
                md.update(hashCalculated.getBytes());
                digest = md.digest();
                hashCalculated = DatatypeConverter.printHexBinary(digest).toUpperCase();
            }

        }// Exit if machine does not support MD5
        catch (NoSuchAlgorithmException e)
        {
            System.out.println("This computer does not support MD5 hashing!");
            System.exit(-1);
        }
    }

    // Returns true if street number is less than 3 digits
    public boolean streetNumLessThan3Digits()
    {
        return this.streetNum / 100.0 < 1.0;
    }


    // toString override for table printing
    @Override
    public String toString()
    {
        return String.format("| %-20s | %-13d | %-10.2f | %-32s |",
                this.name,
                this.streetNum,
                this.balance,
                this.hashCalculated != null ? this.hashCalculated : "--------------------------------");
    }

    // ASCII table header
    public static String tableHeader()
    {
        return String.format("%s\n| %-20s | %-13s | %-10s | %-32s |\n%s",
                String.join("", Collections.nCopies(88,"-")),
                "Name",
                "Street Number",
                "Balance",
                "MD5 hash",
                String.join("", Collections.nCopies(88,"-")));
    }

    public String toStringShort(){
        return String.format("%s - %d - %.2f - %s ",
                this.name,
                this.streetNum,
                this.balance,
                this.hashCalculated != null ? this.hashCalculated : "NaN");
    }
}
