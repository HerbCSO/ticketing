package org.dreesbach.ticketing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A seat picking strategy for a retangular venue.
 */
public class RectangularVenueSimpleSeatPickingStrategy implements SeatPickingStrategy<RectangularVenue> {
    /**
     * This will be a priority queue sorted from best to worst seat so that we can pick off the head of the queue in order to
     * select the best seats.
     */
    private Queue<Seat> seatQueueBestToWorst;

    /**
     * Go through the available seats and return the best ones.
     *
     * @param numSeatsToPick number of seats to pick for reservation
     * @return an array of available {@link Seat}s in the best locations
     */
    @Override
    public final synchronized List<Seat> pickBestAvailableSeats(
            final RectangularVenue venue, final int numSeatsToPick
    ) {
        checkArgument(numSeatsToPick >= 0, "Number of seats to pick must be greater than 0");
        int seatsPicked = 0;
        if (seatQueueBestToWorst == null) {
            fillSeatQueue(checkNotNull(venue));
        }
        List<Seat> bestSeats = new ArrayList<>();
        Seat nextSeat;
        while (seatQueueBestToWorst.peek() != null && seatsPicked < numSeatsToPick) {
            nextSeat = seatQueueBestToWorst.poll();
            seatsPicked++;
            bestSeats.add(nextSeat);
        }
        return bestSeats;
    }

    /**
     * Fill the queue with the seats, ordered from best to worst.
     *
     * @param venue the venue that has the seats
     */
    private void fillSeatQueue(final RectangularVenue venue) {
        checkState(seatQueueBestToWorst == null, "Tried to re-initialize seat queue, this shouldn't happen");
        checkNotNull(venue, "venue should not be null");
        seatQueueBestToWorst =
                new PriorityBlockingQueue<>(venue.getTotalNumSeats(), Comparator.comparingDouble(Seat::seatGoodness));
        List<Seat> seats = venue.getSeats();
        for (Seat seat : seats) {
            seatQueueBestToWorst.add(seat);
        }
    }
}
