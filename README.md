# Ticketing System for seat reservations in high-demand performance venue

## Assumptions

1. This is a simplistic implementation as far as the seat arrangement goes in that it assumes a rectangular seat layout with the same number of seats in every row.

2. Seat reservations are made automatically based on a "best available" algorithm.
TODO: Describe this algorithm

3. Volume is expected to be less than 1,000 seat reservation attempts per second. If higher volume than that is expected, additional performance optimizations may be necessary.

4. Customer emails are not validated to be real emails by this implementation, it is assumed that only valid emails are passed in (although not necessary that the email is in fact valid). 

## Building

This project uses `maven`, so you can run tests with:

    mvn clean verify

and the package can be built with:

    mvn clean package
