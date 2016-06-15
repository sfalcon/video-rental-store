package me.sfalcon.film;

import javafx.util.converter.BigIntegerStringConverter;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.math.BigInteger;

public abstract class Film {
    //this will be helpful when parsing string id from server requests
    private final static BigIntegerStringConverter idParser = new BigIntegerStringConverter();
    private BigInteger id;
    private String title;
    protected double price;
    protected DateTime rentDeadline;

    public Film (){
        this.price = 30;
    }

    public BigInteger getId(){
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public void setId(String id){
        this.id = idParser.fromString(id);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Status getRentStatus() {
        Status status;
        //A film is rentable if it doesn't have a deadline assigned
        if(this.rentDeadline == null){
            status = Status.RENTABLE;
        }else{
            status = Status.RENTED;
        }
        return status;
    }

    protected boolean hasExceededRentPeriod(){
        return DateTime.now().isAfter(this.rentDeadline);
    }

    protected double exceededPrice(){
        Days difference = Days.daysBetween(this.rentDeadline, DateTime.now());
        return this.price * difference.getDays();
    }
    protected double exceededPrice(int days){
        return this.price * days;
    }

    protected Integer maxRentableDays;

    public double rent(int days){
        if (days<=0)
            throw new AssertionError("Must rent for at least 1 day");
        if (this.getRentStatus()==Status.RENTED)
            throw new IllegalStateException("This film is currently rented");
        this.rentDeadline = DateTime.now().plusDays(days);
        double price = this.price;
        if (days>this.maxRentableDays) {
            price += this.exceededPrice(days - maxRentableDays);
        }
        return price;
    }

    public double returnIt(){
        if (this.getRentStatus()==Status.RENTABLE)
            throw new IllegalStateException("This film is not rented");
        double extraCharge = 0;
        if (this.hasExceededRentPeriod())
          extraCharge = this.exceededPrice();
        this.rentDeadline = null;
        return extraCharge;
    }
}
