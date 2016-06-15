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

    public double rent (Film film, int days){
        assert(film != null && film.getStatus()== Status.RENTABLE);
        if (film instanceof NewFilm){
            this.bonusPoints += 2;
        }else{
            this.bonusPoints += 1;
        }
        rentedFilms.put(film.getId(), film);
        return film.rent(days);
    }

    public double returnFilm(BigInteger id){
        assert(id!=null);
        Film film = rentedFilms.get(id);
        if (film==null) throw new IllegalStateException("This customer doesn't have this film rented");
        return film.returnIt();
    }

    public double returnFilm(Film film){
        assert(film != null);
        return this.returnFilm(film.getId());
    }

    public double returnFilm(String id){
        assert(id != null);
        return this.returnFilm(idParser.fromString(id));
    }



}
