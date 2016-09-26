package model.structure;

import java.util.Comparator;

/**
 * Tempo is a glorified byte wrapper,
 * which allows for a little more dress
 * and prevents problems down the line
 * with arguments to constructors.
 */
public class Tempo implements Comparator<Tempo>, Comparable<Tempo> {

    /* A bunch of preset dynamics for general use. */
    public static Tempo PRESTO      = new Tempo(160);
    public static Tempo VIVACE      = new Tempo(140);
    public static Tempo ALLEGRO     = new Tempo(120);
    public static Tempo MODERATO    = new Tempo(100);
    public static Tempo ANDANTE     = new Tempo(80);
    public static Tempo ADAGIO      = new Tempo(70);
    public static Tempo LENTO       = new Tempo(60);
    public static Tempo LARGO       = new Tempo(50);
    public static Tempo GRAVE       = new Tempo(30);

    /* Some basic bounds on Tempo. */
    private static int MIN_TEMPO = 0;
    private static int MAX_TEMPO = 200;

    /**
     * Stores a tempo value, in BPM
     */
    private final int value;

    /**
     * Constructor taking in a Tempo speed.
     * @param value The Tempo's speed.
     */
    public Tempo(int value) {
        if(value >= MIN_TEMPO && value <= MAX_TEMPO) {
            this.value = (byte)value;
        }
        else {
            throw new Error("Invalid tempo range!");
        }
    }

    /**
     * Copy constructor for Tempo.
     * @param other The other Tempo.
     */
    public Tempo(Tempo other) {
        this.value = other.value;
    }

    /**
     * Compares this Tempo to another Tempo.
     * @param other The other Tempo.
     * @return The comparison.
     */
    @Override
    public int compareTo(Tempo other) {
        return new Integer(value).compareTo(other.value);
    }

    /**
     * Compares two Tempi.
     * @param p1 The first Tempo.
     * @param p2 The second Tempo.
     * @return The comparison.
     */
    @Override
    public int compare(Tempo p1, Tempo p2) {
        return new Integer(p1.value).compareTo(p2.value);
    }

    /**
     * Checks if this Tempo equals another Object.
     * @param o The other Object.
     * @return If this Tempo is equal to the Object.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tempo tempo = (Tempo) o;
        return value == tempo.value;

    }

    /**
     * A simple hash code for storage of Tempi in special Collections.
     * @return The hash code for this Tempo.
     */
    @Override
    public int hashCode() {
        return (int) value;
    }
}