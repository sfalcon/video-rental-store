package me.sfalcon.customer;

import javafx.util.converter.BigIntegerStringConverter;
import me.sfalcon.film.Film;
import me.sfalcon.film.NewFilm;
import me.sfalcon.film.Status;

import java.math.BigInteger;
import java.util.HashMap;

public class Customer {
    //this will be helpful when parsing string id from server requests
    private final static BigIntegerStringConverter idParser = new BigIntegerStringConverter();
    private BigInteger id;

    private HashMap<BigInteger, Film> rentedFilms;
    private Integer bonusPoints = 0;

    public Customer(){
        this.rentedFilms = new HashMap<>();
    }

    public Integer getBonusPoints() {
        return bonusPoints;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = idParser.fromString(id);
    }

    public void rent (Film film){
        assert film != null;
        if (film.getRentStatus()==Status.RENTABLE){
            throw new IllegalArgumentException("Can only assign rented films to a customer");
        }
        if (film instanceof NewFilm){
            this.bonusPoints += 2;
        }else{
            this.bonusPoints += 1;
        }
        rentedFilms.put(film.getId(), film);
    }

    public void returnFilm(BigInteger id){
        assert(id!=null);
        Film film = rentedFilms.get(id);
        if (film == null)
            throw new IllegalStateException("The customer doesn't have that film rented");
        rentedFilms.remove(id);
    }

    public void returnFilm(Film film){
        assert(film != null);
        this.returnFilm(film.getId());
    }

    public void returnFilm(String id){
        assert(id != null);
        this.returnFilm(idParser.fromString(id));
    }



}
