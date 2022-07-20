package edu.uoc.nertia.model.leaderboard;

import java.io.Serial;
import java.io.Serializable;

public record Score(String name, int points) implements Comparable<Score>, Serializable {
    //Attributes--------------------------------------------------------------------------------------------------------
    @Serial
    private static final long serialVersionUID = 13L;

    //Methods-----------------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return name().toUpperCase().concat(" : ").concat(Integer.toString(points())).concat(" pts");
    }

    @Override
    public int compareTo(Score o) {
        return Integer.compare(o.points(), points());
    }
}
