# Ticketing System for seat reservations in high-demand performance venue

## Assumptions

1. This is a simplistic implementation as far as the seat arrangement goes in that it assumes a rectangular seat layout with the same number of seats in every row. However provisions have been made to supply other seat arrangements and seat selection algorithms.

2. Seat reservations are made automatically based on a "best available" algorithm. It selects seats based on distance from front, center seat - the closer to this center the seat is, the better it is considered to be.

3. Volume is expected to be less than 1,000 seat reservation attempts per second. If higher volume than that is expected, additional performance optimizations may be necessary.

4. Customer emails are not validated to be real emails by this implementation, it is assumed that only valid emails are passed in (although not necessary that the email is in fact valid).

5. Multiple SeatHolds are allowed per email - no limits are enforced.

## Building

This project uses `maven`, so you can run tests with:

    mvn clean verify

and the package can be built with:

    mvn clean package

## Notes

You may see something like the following in the SpotBugs phase of the build:

     [java] The following classes needed for analysis were missing:
     [java]   test
     [java]   run
     [java] Warnings generated: 2
     [java] Missing classes: 2

There could be more "classes" reported in the current output. I believe this is due to [issue #527 in SpotBugs](https://github.com/spotbugs/spotbugs/issues/527), and interaction with the `findsecbugs` plugin. These appear to be lambdas that are getting incorrectly reported as missing classes. The issue was addressed in version 3.1.2 of SpotBugs and we're on 3.1.3 as of the time of this writing, however there may still be a problem with the interaction with the `findsecbugs` plugin - when disabling that, most of the output except for the `Warnings generated: 2` goes away. 

Regardless, this should be inconsequential for the purposes of this project.
