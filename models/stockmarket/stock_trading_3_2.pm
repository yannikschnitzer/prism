mdp

// Time horizon
const int T = 10;

// Parametric probabilities
// Base probability that a stock rises when 0 previous stocks are rising
const double BASE;
// Scaling factor: additional probability mass per rising stock in the sector
const double SCALE;

// Derived probabilities for exactly k rising stocks in a 
// 2-stock sector:
//   P[k] = BASE + SCALE * (k / {float(stocks_per_sector)})

const double P0 = BASE + SCALE * (0/2.0);   // k = 0

const double P1 = BASE + SCALE * (1/2.0);   // k = 1

const double P2 = BASE + SCALE;   // k = 2



formula goal = (t1 = T);


///////////////////////////////////////////////////////////////////////////
// SECTOR 1 MODULE
module sector1
    owns1    : bool init false;
    rising11 : bool init false;
    rising12 : bool init false;
    t1       : [0..T] init 0;

    [buy1] t1 < T & rising11 = false & rising12 = false ->
        (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P0 * (1 - P0) : (rising11' = true) & (rising12' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P0 * (1 - P0) : (rising11' = false) & (rising12' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P0 * P0 : (rising11' = true) & (rising12' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = true & rising12 = false ->
        (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * P1 : (rising11' = true) & (rising12' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = false & rising12 = true ->
        (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P1 * P1 : (rising11' = true) & (rising12' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = true & rising12 = true ->
        (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (owns1' = true) & (t1' = t1 + 1) +
        P2 * P2 : (rising11' = true) & (rising12' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = false & rising12 = false ->
        (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = true & rising12 = false ->
        (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = false & rising12 = true ->
        (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = true & rising12 = true ->
        (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy3] t1 < T & rising11 = false & rising12 = false ->
        (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy3] t1 < T & rising11 = true & rising12 = false ->
        (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy3] t1 < T & rising11 = false & rising12 = true ->
        (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy3] t1 < T & rising11 = true & rising12 = true ->
        (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = false & rising12 = false ->
        (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P0 * (1 - P0) : (rising11' = true) & (rising12' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P0 * (1 - P0) : (rising11' = false) & (rising12' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P0 * P0 : (rising11' = true) & (rising12' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = true & rising12 = false ->
        (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * P1 : (rising11' = true) & (rising12' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = false & rising12 = true ->
        (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P1 * P1 : (rising11' = true) & (rising12' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = true & rising12 = true ->
        (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (owns1' = false) & (t1' = t1 + 1) +
        P2 * P2 : (rising11' = true) & (rising12' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = false & rising12 = false ->
        (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = true & rising12 = false ->
        (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = false & rising12 = true ->
        (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = true & rising12 = true ->
        (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell3] t1 < T & rising11 = false & rising12 = false ->
        (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell3] t1 < T & rising11 = true & rising12 = false ->
        (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell3] t1 < T & rising11 = false & rising12 = true ->
        (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell3] t1 < T & rising11 = true & rising12 = true ->
        (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = false & rising12 = false ->
        (1 - P0) * (1 - P0) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * (1 - P0) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P0 * P0 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = true & rising12 = false ->
        (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = false & rising12 = true ->
        (1 - P1) * (1 - P1) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * (1 - P1) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P1 * P1 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = true & rising12 = true ->
        (1 - P2) * (1 - P2) : (rising11' = false) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) : (rising11' = true) & (rising12' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * (1 - P2) : (rising11' = false) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        P2 * P2 : (rising11' = true) & (rising12' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 = T -> (rising11' = rising11) & (rising12' = rising12) & (owns1' = owns1) & (t1' = t1);

endmodule


///////////////////////////////////////////////////////////////////////////
// SECTOR 2 MODULE
module sector2
    owns2    : bool init false;
    rising21 : bool init false;
    rising22 : bool init false;

    [buy1] t1 < T & rising21 = false & rising22 = false ->
        (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P0 * (1 - P0) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P0 * (1 - P0) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P0 * P0 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = true & rising22 = false ->
        (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P1 * P1 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = false & rising22 = true ->
        (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P1 * P1 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = true & rising22 = true ->
        (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P2 * P2 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [buy2] t1 < T & rising21 = false & rising22 = false ->
        (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (owns2' = true) +
        P0 * (1 - P0) : (rising21' = true) & (rising22' = false) & (owns2' = true) +
        P0 * (1 - P0) : (rising21' = false) & (rising22' = true) & (owns2' = true) +
        P0 * P0 : (rising21' = true) & (rising22' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = true & rising22 = false ->
        (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (owns2' = true) +
        P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (owns2' = true) +
        P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (owns2' = true) +
        P1 * P1 : (rising21' = true) & (rising22' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = false & rising22 = true ->
        (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (owns2' = true) +
        P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (owns2' = true) +
        P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (owns2' = true) +
        P1 * P1 : (rising21' = true) & (rising22' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = true & rising22 = true ->
        (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (owns2' = true) +
        P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (owns2' = true) +
        P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (owns2' = true) +
        P2 * P2 : (rising21' = true) & (rising22' = true) & (owns2' = true);

    [buy3] t1 < T & rising21 = false & rising22 = false ->
        (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P0 * (1 - P0) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P0 * (1 - P0) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P0 * P0 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [buy3] t1 < T & rising21 = true & rising22 = false ->
        (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P1 * P1 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [buy3] t1 < T & rising21 = false & rising22 = true ->
        (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P1 * P1 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [buy3] t1 < T & rising21 = true & rising22 = true ->
        (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P2 * P2 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = false & rising22 = false ->
        (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P0 * (1 - P0) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P0 * (1 - P0) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P0 * P0 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = true & rising22 = false ->
        (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P1 * P1 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = false & rising22 = true ->
        (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P1 * P1 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = true & rising22 = true ->
        (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P2 * P2 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [sell2] t1 < T & rising21 = false & rising22 = false ->
        (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (owns2' = false) +
        P0 * (1 - P0) : (rising21' = true) & (rising22' = false) & (owns2' = false) +
        P0 * (1 - P0) : (rising21' = false) & (rising22' = true) & (owns2' = false) +
        P0 * P0 : (rising21' = true) & (rising22' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = true & rising22 = false ->
        (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (owns2' = false) +
        P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (owns2' = false) +
        P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (owns2' = false) +
        P1 * P1 : (rising21' = true) & (rising22' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = false & rising22 = true ->
        (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (owns2' = false) +
        P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (owns2' = false) +
        P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (owns2' = false) +
        P1 * P1 : (rising21' = true) & (rising22' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = true & rising22 = true ->
        (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (owns2' = false) +
        P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (owns2' = false) +
        P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (owns2' = false) +
        P2 * P2 : (rising21' = true) & (rising22' = true) & (owns2' = false);

    [sell3] t1 < T & rising21 = false & rising22 = false ->
        (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P0 * (1 - P0) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P0 * (1 - P0) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P0 * P0 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [sell3] t1 < T & rising21 = true & rising22 = false ->
        (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P1 * P1 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [sell3] t1 < T & rising21 = false & rising22 = true ->
        (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P1 * P1 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [sell3] t1 < T & rising21 = true & rising22 = true ->
        (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P2 * P2 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = false & rising22 = false ->
        (1 - P0) * (1 - P0) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P0 * (1 - P0) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P0 * (1 - P0) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P0 * P0 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = true & rising22 = false ->
        (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P1 * P1 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = false & rising22 = true ->
        (1 - P1) * (1 - P1) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P1 * (1 - P1) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P1 * P1 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = true & rising22 = true ->
        (1 - P2) * (1 - P2) : (rising21' = false) & (rising22' = false) & (owns2' = owns2) +
        P2 * (1 - P2) : (rising21' = true) & (rising22' = false) & (owns2' = owns2) +
        P2 * (1 - P2) : (rising21' = false) & (rising22' = true) & (owns2' = owns2) +
        P2 * P2 : (rising21' = true) & (rising22' = true) & (owns2' = owns2);

    [nothing] t1 = T -> (rising21' = rising21) & (rising22' = rising22) & (owns2' = owns2);

endmodule


///////////////////////////////////////////////////////////////////////////
// SECTOR 3 MODULE
module sector3
    owns3    : bool init false;
    rising31 : bool init false;
    rising32 : bool init false;

    [buy1] t1 < T & rising31 = false & rising32 = false ->
        (1 - P0) * (1 - P0) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P0 * (1 - P0) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P0 * (1 - P0) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P0 * P0 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [buy1] t1 < T & rising31 = true & rising32 = false ->
        (1 - P1) * (1 - P1) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P1 * P1 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [buy1] t1 < T & rising31 = false & rising32 = true ->
        (1 - P1) * (1 - P1) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P1 * P1 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [buy1] t1 < T & rising31 = true & rising32 = true ->
        (1 - P2) * (1 - P2) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P2 * (1 - P2) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P2 * (1 - P2) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P2 * P2 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [buy2] t1 < T & rising31 = false & rising32 = false ->
        (1 - P0) * (1 - P0) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P0 * (1 - P0) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P0 * (1 - P0) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P0 * P0 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [buy2] t1 < T & rising31 = true & rising32 = false ->
        (1 - P1) * (1 - P1) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P1 * P1 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [buy2] t1 < T & rising31 = false & rising32 = true ->
        (1 - P1) * (1 - P1) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P1 * P1 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [buy2] t1 < T & rising31 = true & rising32 = true ->
        (1 - P2) * (1 - P2) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P2 * (1 - P2) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P2 * (1 - P2) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P2 * P2 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [buy3] t1 < T & rising31 = false & rising32 = false ->
        (1 - P0) * (1 - P0) : (rising31' = false) & (rising32' = false) & (owns3' = true) +
        P0 * (1 - P0) : (rising31' = true) & (rising32' = false) & (owns3' = true) +
        P0 * (1 - P0) : (rising31' = false) & (rising32' = true) & (owns3' = true) +
        P0 * P0 : (rising31' = true) & (rising32' = true) & (owns3' = true);

    [buy3] t1 < T & rising31 = true & rising32 = false ->
        (1 - P1) * (1 - P1) : (rising31' = false) & (rising32' = false) & (owns3' = true) +
        P1 * (1 - P1) : (rising31' = true) & (rising32' = false) & (owns3' = true) +
        P1 * (1 - P1) : (rising31' = false) & (rising32' = true) & (owns3' = true) +
        P1 * P1 : (rising31' = true) & (rising32' = true) & (owns3' = true);

    [buy3] t1 < T & rising31 = false & rising32 = true ->
        (1 - P1) * (1 - P1) : (rising31' = false) & (rising32' = false) & (owns3' = true) +
        P1 * (1 - P1) : (rising31' = true) & (rising32' = false) & (owns3' = true) +
        P1 * (1 - P1) : (rising31' = false) & (rising32' = true) & (owns3' = true) +
        P1 * P1 : (rising31' = true) & (rising32' = true) & (owns3' = true);

    [buy3] t1 < T & rising31 = true & rising32 = true ->
        (1 - P2) * (1 - P2) : (rising31' = false) & (rising32' = false) & (owns3' = true) +
        P2 * (1 - P2) : (rising31' = true) & (rising32' = false) & (owns3' = true) +
        P2 * (1 - P2) : (rising31' = false) & (rising32' = true) & (owns3' = true) +
        P2 * P2 : (rising31' = true) & (rising32' = true) & (owns3' = true);

    [sell1] t1 < T & rising31 = false & rising32 = false ->
        (1 - P0) * (1 - P0) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P0 * (1 - P0) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P0 * (1 - P0) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P0 * P0 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [sell1] t1 < T & rising31 = true & rising32 = false ->
        (1 - P1) * (1 - P1) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P1 * P1 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [sell1] t1 < T & rising31 = false & rising32 = true ->
        (1 - P1) * (1 - P1) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P1 * P1 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [sell1] t1 < T & rising31 = true & rising32 = true ->
        (1 - P2) * (1 - P2) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P2 * (1 - P2) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P2 * (1 - P2) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P2 * P2 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [sell2] t1 < T & rising31 = false & rising32 = false ->
        (1 - P0) * (1 - P0) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P0 * (1 - P0) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P0 * (1 - P0) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P0 * P0 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [sell2] t1 < T & rising31 = true & rising32 = false ->
        (1 - P1) * (1 - P1) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P1 * P1 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [sell2] t1 < T & rising31 = false & rising32 = true ->
        (1 - P1) * (1 - P1) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P1 * P1 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [sell2] t1 < T & rising31 = true & rising32 = true ->
        (1 - P2) * (1 - P2) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P2 * (1 - P2) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P2 * (1 - P2) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P2 * P2 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [sell3] t1 < T & rising31 = false & rising32 = false ->
        (1 - P0) * (1 - P0) : (rising31' = false) & (rising32' = false) & (owns3' = false) +
        P0 * (1 - P0) : (rising31' = true) & (rising32' = false) & (owns3' = false) +
        P0 * (1 - P0) : (rising31' = false) & (rising32' = true) & (owns3' = false) +
        P0 * P0 : (rising31' = true) & (rising32' = true) & (owns3' = false);

    [sell3] t1 < T & rising31 = true & rising32 = false ->
        (1 - P1) * (1 - P1) : (rising31' = false) & (rising32' = false) & (owns3' = false) +
        P1 * (1 - P1) : (rising31' = true) & (rising32' = false) & (owns3' = false) +
        P1 * (1 - P1) : (rising31' = false) & (rising32' = true) & (owns3' = false) +
        P1 * P1 : (rising31' = true) & (rising32' = true) & (owns3' = false);

    [sell3] t1 < T & rising31 = false & rising32 = true ->
        (1 - P1) * (1 - P1) : (rising31' = false) & (rising32' = false) & (owns3' = false) +
        P1 * (1 - P1) : (rising31' = true) & (rising32' = false) & (owns3' = false) +
        P1 * (1 - P1) : (rising31' = false) & (rising32' = true) & (owns3' = false) +
        P1 * P1 : (rising31' = true) & (rising32' = true) & (owns3' = false);

    [sell3] t1 < T & rising31 = true & rising32 = true ->
        (1 - P2) * (1 - P2) : (rising31' = false) & (rising32' = false) & (owns3' = false) +
        P2 * (1 - P2) : (rising31' = true) & (rising32' = false) & (owns3' = false) +
        P2 * (1 - P2) : (rising31' = false) & (rising32' = true) & (owns3' = false) +
        P2 * P2 : (rising31' = true) & (rising32' = true) & (owns3' = false);

    [nothing] t1 < T & rising31 = false & rising32 = false ->
        (1 - P0) * (1 - P0) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P0 * (1 - P0) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P0 * (1 - P0) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P0 * P0 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [nothing] t1 < T & rising31 = true & rising32 = false ->
        (1 - P1) * (1 - P1) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P1 * P1 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [nothing] t1 < T & rising31 = false & rising32 = true ->
        (1 - P1) * (1 - P1) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P1 * (1 - P1) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P1 * P1 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [nothing] t1 < T & rising31 = true & rising32 = true ->
        (1 - P2) * (1 - P2) : (rising31' = false) & (rising32' = false) & (owns3' = owns3) +
        P2 * (1 - P2) : (rising31' = true) & (rising32' = false) & (owns3' = owns3) +
        P2 * (1 - P2) : (rising31' = false) & (rising32' = true) & (owns3' = owns3) +
        P2 * P2 : (rising31' = true) & (rising32' = true) & (owns3' = owns3);

    [nothing] t1 = T -> (rising31' = rising31) & (rising32' = rising32) & (owns3' = owns3);

endmodule


///////////////////////////////////////////////////////////////////////////
// REWARDS
// +1 for each rising stock in an owned sector, –1 for each non‐rising stock in that owned sector.
// In a 2-stock sector: payoff_i = owns_i ? (2*(sum_j rising_i2) - 2) : 0.
// Total reward = sum_i payoff_i, only when t1 < T.

rewards

    [buy1] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0)) - 2) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0)) - 2) : 0) + (owns3? (2*((rising31?1:0) + (rising32?1:0)) - 2) : 0));

    [buy2] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0)) - 2) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0)) - 2) : 0) + (owns3? (2*((rising31?1:0) + (rising32?1:0)) - 2) : 0));

    [buy3] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0)) - 2) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0)) - 2) : 0) + (owns3? (2*((rising31?1:0) + (rising32?1:0)) - 2) : 0));

    [sell1] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0)) - 2) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0)) - 2) : 0) + (owns3? (2*((rising31?1:0) + (rising32?1:0)) - 2) : 0));

    [sell2] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0)) - 2) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0)) - 2) : 0) + (owns3? (2*((rising31?1:0) + (rising32?1:0)) - 2) : 0));

    [sell3] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0)) - 2) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0)) - 2) : 0) + (owns3? (2*((rising31?1:0) + (rising32?1:0)) - 2) : 0));

    [nothing] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0)) - 2) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0)) - 2) : 0) + (owns3? (2*((rising31?1:0) + (rising32?1:0)) - 2) : 0));

endrewards
