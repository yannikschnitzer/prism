mdp

// Time horizon
const int T = 10;

// Parametric probabilities
// Base probability that a stock rises when 0 previous stocks are rising
const double BASE;
// Scaling factor: additional probability mass per rising stock in the sector
const double SCALE;

// Derived probabilities for exactly k rising stocks in a 
// 3-stock sector:
//   P[k] = BASE + SCALE * (k / {float(stocks_per_sector)})

const double P0 = BASE + SCALE * (0/3.0);   // k = 0

const double P1 = BASE + SCALE * (1/3.0);   // k = 1

const double P2 = BASE + SCALE * (2/3.0);   // k = 2

const double P3 = BASE + SCALE;   // k = 3



formula goal = (t1 = T);


///////////////////////////////////////////////////////////////////////////
// SECTOR 1 MODULE
module sector1
    owns1    : bool init false;
    rising11 : bool init false;
    rising12 : bool init false;
    rising13 : bool init false;
    t1       : [0..T] init 0;

    [buy1] t1 < T & rising11 = false & rising12 = false & rising13 = false ->
        (1 - P0) * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P0 * P0 * P0 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = true & rising12 = false & rising13 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = false & rising12 = true & rising13 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = true & rising12 = true & rising13 = false ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = false & rising12 = false & rising13 = true ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = true & rising12 = false & rising13 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = false & rising12 = true & rising13 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = true & rising12 = true & rising13 = true ->
        (1 - P3) * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P3 * P3 * P3 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = false & rising12 = false & rising13 = false ->
        (1 - P0) * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 * P0 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = true & rising12 = false & rising13 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = false & rising12 = true & rising13 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = true & rising12 = true & rising13 = false ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = false & rising12 = false & rising13 = true ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = true & rising12 = false & rising13 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = false & rising12 = true & rising13 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = true & rising12 = true & rising13 = true ->
        (1 - P3) * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * P3 * P3 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = false & rising12 = false & rising13 = false ->
        (1 - P0) * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P0 * P0 * P0 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = true & rising12 = false & rising13 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = false & rising12 = true & rising13 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = true & rising12 = true & rising13 = false ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = false & rising12 = false & rising13 = true ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = true & rising12 = false & rising13 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = false & rising12 = true & rising13 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = true & rising12 = true & rising13 = true ->
        (1 - P3) * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P3 * P3 * P3 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = false & rising12 = false & rising13 = false ->
        (1 - P0) * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 * P0 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = true & rising12 = false & rising13 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = false & rising12 = true & rising13 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = true & rising12 = true & rising13 = false ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = false & rising12 = false & rising13 = true ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = true & rising12 = false & rising13 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = false & rising12 = true & rising13 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = true & rising12 = true & rising13 = true ->
        (1 - P3) * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * P3 * P3 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = false & rising12 = false & rising13 = false ->
        (1 - P0) * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 * (1 - P0) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 * P0 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = true & rising12 = false & rising13 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = false & rising12 = true & rising13 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = true & rising12 = true & rising13 = false ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = false & rising12 = false & rising13 = true ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 * P1 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = true & rising12 = false & rising13 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = false & rising12 = true & rising13 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 * P2 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = true & rising12 = true & rising13 = true ->
        (1 - P3) * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = true) & (rising12' = false) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = true) & (rising12' = true) & (rising13' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * (1 - P3) * (1 - P3) : (rising11' = false) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = true) & (rising12' = false) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * P3 * (1 - P3) : (rising11' = false) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P3 * P3 * P3 : (rising11' = true) & (rising12' = true) & (rising13' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 = T -> (rising11' = rising11) & (rising12' = rising12) & (rising13' = rising13) & (owns1' = owns1) & (t1' = t1);

endmodule


///////////////////////////////////////////////////////////////////////////
// SECTOR 2 MODULE
module sector2
    owns2    : bool init false;
    rising21 : bool init false;
    rising22 : bool init false;
    rising23 : bool init false;

    [buy1] t1 < T & rising21 = false & rising22 = false & rising23 = false ->
        (1 - P0) * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P0 * P0 * (1 - P0) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P0 * P0 * (1 - P0) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P0 * P0 * (1 - P0) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P0 * P0 * P0 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = true & rising22 = false & rising23 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = false & rising22 = true & rising23 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = true & rising22 = true & rising23 = false ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = false & rising22 = false & rising23 = true ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = true & rising22 = false & rising23 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = false & rising22 = true & rising23 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = true & rising22 = true & rising23 = true ->
        (1 - P3) * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P3 * P3 * (1 - P3) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P3 * P3 * (1 - P3) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P3 * P3 * (1 - P3) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P3 * P3 * P3 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [buy2] t1 < T & rising21 = false & rising22 = false & rising23 = false ->
        (1 - P0) * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P0 * P0 * (1 - P0) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P0 * P0 * (1 - P0) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P0 * P0 * (1 - P0) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = true) +
        P0 * P0 * P0 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = true & rising22 = false & rising23 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = true) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = false & rising22 = true & rising23 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = true) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = true & rising22 = true & rising23 = false ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = true) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = false & rising22 = false & rising23 = true ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = true) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = true & rising22 = false & rising23 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = true) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = false & rising22 = true & rising23 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = true) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = true & rising22 = true & rising23 = true ->
        (1 - P3) * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = true) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P3 * P3 * (1 - P3) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = true) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P3 * P3 * (1 - P3) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = true) +
        P3 * P3 * (1 - P3) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = true) +
        P3 * P3 * P3 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = true);

    [sell1] t1 < T & rising21 = false & rising22 = false & rising23 = false ->
        (1 - P0) * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P0 * P0 * (1 - P0) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P0 * P0 * (1 - P0) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P0 * P0 * (1 - P0) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P0 * P0 * P0 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = true & rising22 = false & rising23 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = false & rising22 = true & rising23 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = true & rising22 = true & rising23 = false ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = false & rising22 = false & rising23 = true ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = true & rising22 = false & rising23 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = false & rising22 = true & rising23 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = true & rising22 = true & rising23 = true ->
        (1 - P3) * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P3 * P3 * (1 - P3) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P3 * P3 * (1 - P3) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P3 * P3 * (1 - P3) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P3 * P3 * P3 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [sell2] t1 < T & rising21 = false & rising22 = false & rising23 = false ->
        (1 - P0) * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P0 * P0 * (1 - P0) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P0 * P0 * (1 - P0) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P0 * P0 * (1 - P0) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = false) +
        P0 * P0 * P0 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = true & rising22 = false & rising23 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = false) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = false & rising22 = true & rising23 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = false) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = true & rising22 = true & rising23 = false ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = false) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = false & rising22 = false & rising23 = true ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = false) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = true & rising22 = false & rising23 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = false) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = false & rising22 = true & rising23 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = false) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = true & rising22 = true & rising23 = true ->
        (1 - P3) * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = false) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P3 * P3 * (1 - P3) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = false) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P3 * P3 * (1 - P3) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = false) +
        P3 * P3 * (1 - P3) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = false) +
        P3 * P3 * P3 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = false);

    [nothing] t1 < T & rising21 = false & rising22 = false & rising23 = false ->
        (1 - P0) * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P0 * P0 * (1 - P0) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P0 * (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P0 * P0 * (1 - P0) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P0 * P0 * (1 - P0) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P0 * P0 * P0 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = true & rising22 = false & rising23 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = false & rising22 = true & rising23 = false ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = true & rising22 = true & rising23 = false ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = false & rising22 = false & rising23 = true ->
        (1 - P1) * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P1 * (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P1 * P1 * P1 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = true & rising22 = false & rising23 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = false & rising22 = true & rising23 = true ->
        (1 - P2) * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P2 * (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P2 * P2 * P2 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = true & rising22 = true & rising23 = true ->
        (1 - P3) * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = true) & (rising22' = false) & (rising23' = false) & (owns2' = owns2) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P3 * P3 * (1 - P3) : (rising21' = true) & (rising22' = true) & (rising23' = false) & (owns2' = owns2) +
        P3 * (1 - P3) * (1 - P3) : (rising21' = false) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P3 * P3 * (1 - P3) : (rising21' = true) & (rising22' = false) & (rising23' = true) & (owns2' = owns2) +
        P3 * P3 * (1 - P3) : (rising21' = false) & (rising22' = true) & (rising23' = true) & (owns2' = owns2) +
        P3 * P3 * P3 : (rising21' = true) & (rising22' = true) & (rising23' = true) & (owns2' = owns2);

    [nothing] t1 = T -> (rising21' = rising21) & (rising22' = rising22) & (rising23' = rising23) & (owns2' = owns2);

endmodule


///////////////////////////////////////////////////////////////////////////
// REWARDS
// +1 for each rising stock in an owned sector, –1 for each non‐rising stock in that owned sector.
// In a 3-stock sector: payoff_i = owns_i ? (2*(sum_j rising_i3) - 3) : 0.
// Total reward = sum_i payoff_i, only when t1 < T.

rewards

    [buy1] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0) + (rising13?1:0)) - 3) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0) + (rising23?1:0)) - 3) : 0));

    [buy2] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0) + (rising13?1:0)) - 3) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0) + (rising23?1:0)) - 3) : 0));

    [sell1] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0) + (rising13?1:0)) - 3) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0) + (rising23?1:0)) - 3) : 0));

    [sell2] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0) + (rising13?1:0)) - 3) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0) + (rising23?1:0)) - 3) : 0));

    [nothing] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0) + (rising13?1:0)) - 3) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0) + (rising23?1:0)) - 3) : 0));

endrewards
