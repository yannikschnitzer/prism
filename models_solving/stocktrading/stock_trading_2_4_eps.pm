mdp

// Time horizon
const int T = 5;

// Parametric probabilities
// Base probability that a stock rises when 0 previous stocks are rising
const double BASE;
// Scaling factor: additional probability mass per rising stock in the sector
const double SCALE;

const double eps;

// Derived probabilities for exactly k rising stocks in a 
// 4-stock sector:
//   P[k] = BASE + SCALE * (k / {float(stocks_per_sector)})

const double P0 = BASE + SCALE * (0/4.0);   // k = 0

const double P1 = BASE + SCALE * (1/4.0);   // k = 1

const double P2 = BASE + SCALE * (2/4.0);   // k = 2

const double P3 = BASE + SCALE * (3/4.0);   // k = 3

const double P4 = BASE + SCALE;   // k = 4



formula goal = (t1 = T);


///////////////////////////////////////////////////////////////////////////
// SECTOR 1 MODULE
module sector1
    owns1    : bool init false;
    rising11 : bool init false;
    rising12 : bool init false;
    rising13 : bool init false;
    rising14 : bool init false;
    t1       : [0..T] init 0;

    [buy1] t1 < T & rising11 = false & rising12 = false & rising13 = false & rising14 = false ->
        [max(0, (1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) - eps), min((1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * P0 - eps), min(P0 * P0 * P0 * P0 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = true & rising12 = false & rising13 = false & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = false & rising12 = true & rising13 = false & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = true & rising12 = true & rising13 = false & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = false & rising12 = false & rising13 = true & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = true & rising12 = false & rising13 = true & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = false & rising12 = true & rising13 = true & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = true & rising12 = true & rising13 = true & rising14 = false ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = false & rising12 = false & rising13 = false & rising14 = true ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = true & rising12 = false & rising13 = false & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = false & rising12 = true & rising13 = false & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = true & rising12 = true & rising13 = false & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = false & rising12 = false & rising13 = true & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = true & rising12 = false & rising13 = true & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = false & rising12 = true & rising13 = true & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy1] t1 < T & rising11 = true & rising12 = true & rising13 = true & rising14 = true ->
        [max(0, (1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) - eps), min((1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * P4 - eps), min(P4 * P4 * P4 * P4 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = true) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = false & rising12 = false & rising13 = false & rising14 = false ->
        [max(0, (1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) - eps), min((1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * P0 - eps), min(P0 * P0 * P0 * P0 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = true & rising12 = false & rising13 = false & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = false & rising12 = true & rising13 = false & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = true & rising12 = true & rising13 = false & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = false & rising12 = false & rising13 = true & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = true & rising12 = false & rising13 = true & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = false & rising12 = true & rising13 = true & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = true & rising12 = true & rising13 = true & rising14 = false ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = false & rising12 = false & rising13 = false & rising14 = true ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = true & rising12 = false & rising13 = false & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = false & rising12 = true & rising13 = false & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = true & rising12 = true & rising13 = false & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = false & rising12 = false & rising13 = true & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = true & rising12 = false & rising13 = true & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = false & rising12 = true & rising13 = true & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [buy2] t1 < T & rising11 = true & rising12 = true & rising13 = true & rising14 = true ->
        [max(0, (1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) - eps), min((1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * P4 - eps), min(P4 * P4 * P4 * P4 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = false & rising12 = false & rising13 = false & rising14 = false ->
        [max(0, (1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) - eps), min((1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * P0 - eps), min(P0 * P0 * P0 * P0 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = true & rising12 = false & rising13 = false & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = false & rising12 = true & rising13 = false & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = true & rising12 = true & rising13 = false & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = false & rising12 = false & rising13 = true & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = true & rising12 = false & rising13 = true & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = false & rising12 = true & rising13 = true & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = true & rising12 = true & rising13 = true & rising14 = false ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = false & rising12 = false & rising13 = false & rising14 = true ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = true & rising12 = false & rising13 = false & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = false & rising12 = true & rising13 = false & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = true & rising12 = true & rising13 = false & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = false & rising12 = false & rising13 = true & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = true & rising12 = false & rising13 = true & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = false & rising12 = true & rising13 = true & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell1] t1 < T & rising11 = true & rising12 = true & rising13 = true & rising14 = true ->
        [max(0, (1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) - eps), min((1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * P4 - eps), min(P4 * P4 * P4 * P4 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = false) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = false & rising12 = false & rising13 = false & rising14 = false ->
        [max(0, (1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) - eps), min((1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * P0 - eps), min(P0 * P0 * P0 * P0 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = true & rising12 = false & rising13 = false & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = false & rising12 = true & rising13 = false & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = true & rising12 = true & rising13 = false & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = false & rising12 = false & rising13 = true & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = true & rising12 = false & rising13 = true & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = false & rising12 = true & rising13 = true & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = true & rising12 = true & rising13 = true & rising14 = false ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = false & rising12 = false & rising13 = false & rising14 = true ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = true & rising12 = false & rising13 = false & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = false & rising12 = true & rising13 = false & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = true & rising12 = true & rising13 = false & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = false & rising12 = false & rising13 = true & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = true & rising12 = false & rising13 = true & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = false & rising12 = true & rising13 = true & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [sell2] t1 < T & rising11 = true & rising12 = true & rising13 = true & rising14 = true ->
        [max(0, (1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) - eps), min((1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * P4 - eps), min(P4 * P4 * P4 * P4 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = false & rising12 = false & rising13 = false & rising14 = false ->
        [max(0, (1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) - eps), min((1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P0 * P0 * P0 * P0 - eps), min(P0 * P0 * P0 * P0 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = true & rising12 = false & rising13 = false & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = false & rising12 = true & rising13 = false & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = true & rising12 = true & rising13 = false & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = false & rising12 = false & rising13 = true & rising14 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = true & rising12 = false & rising13 = true & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = false & rising12 = true & rising13 = true & rising14 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = true & rising12 = true & rising13 = true & rising14 = false ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = false & rising12 = false & rising13 = false & rising14 = true ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = true & rising12 = false & rising13 = false & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = false & rising12 = true & rising13 = false & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = true & rising12 = true & rising13 = false & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = false & rising12 = false & rising13 = true & rising14 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = true & rising12 = false & rising13 = true & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = false & rising12 = true & rising13 = true & rising14 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 < T & rising11 = true & rising12 = true & rising13 = true & rising14 = true ->
        [max(0, (1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) - eps), min((1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = false) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = false) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = true) & (rising12' = false) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising11' = false) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1) +
        [max(0, P4 * P4 * P4 * P4 - eps), min(P4 * P4 * P4 * P4 + eps, 1)] : (rising11' = true) & (rising12' = true) & (rising13' = true) & (rising14' = true) & (owns1' = owns1) & (t1' = t1 + 1);

    [nothing] t1 = T -> (rising11' = rising11) & (rising12' = rising12) & (rising13' = rising13) & (rising14' = rising14) & (owns1' = owns1) & (t1' = t1);

endmodule


///////////////////////////////////////////////////////////////////////////
// SECTOR 2 MODULE
module sector2
    owns2    : bool init false;
    rising21 : bool init false;
    rising22 : bool init false;
    rising23 : bool init false;
    rising24 : bool init false;

    [buy1] t1 < T & rising21 = false & rising22 = false & rising23 = false & rising24 = false ->
        [max(0, (1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) - eps), min((1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * P0 - eps), min(P0 * P0 * P0 * P0 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = true & rising22 = false & rising23 = false & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = false & rising22 = true & rising23 = false & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = true & rising22 = true & rising23 = false & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = false & rising22 = false & rising23 = true & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = true & rising22 = false & rising23 = true & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = false & rising22 = true & rising23 = true & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = true & rising22 = true & rising23 = true & rising24 = false ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = false & rising22 = false & rising23 = false & rising24 = true ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = true & rising22 = false & rising23 = false & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = false & rising22 = true & rising23 = false & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = true & rising22 = true & rising23 = false & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = false & rising22 = false & rising23 = true & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = true & rising22 = false & rising23 = true & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = false & rising22 = true & rising23 = true & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy1] t1 < T & rising21 = true & rising22 = true & rising23 = true & rising24 = true ->
        [max(0, (1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) - eps), min((1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * P4 - eps), min(P4 * P4 * P4 * P4 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [buy2] t1 < T & rising21 = false & rising22 = false & rising23 = false & rising24 = false ->
        [max(0, (1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) - eps), min((1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P0 * P0 * P0 * P0 - eps), min(P0 * P0 * P0 * P0 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = true & rising22 = false & rising23 = false & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = false & rising22 = true & rising23 = false & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = true & rising22 = true & rising23 = false & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = false & rising22 = false & rising23 = true & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = true & rising22 = false & rising23 = true & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = false & rising22 = true & rising23 = true & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = true & rising22 = true & rising23 = true & rising24 = false ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = false & rising22 = false & rising23 = false & rising24 = true ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = true & rising22 = false & rising23 = false & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = false & rising22 = true & rising23 = false & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = true & rising22 = true & rising23 = false & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = false & rising22 = false & rising23 = true & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = true & rising22 = false & rising23 = true & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = false & rising22 = true & rising23 = true & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [buy2] t1 < T & rising21 = true & rising22 = true & rising23 = true & rising24 = true ->
        [max(0, (1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) - eps), min((1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = true) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = true) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = true) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true) +
        [max(0, P4 * P4 * P4 * P4 - eps), min(P4 * P4 * P4 * P4 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = true);

    [sell1] t1 < T & rising21 = false & rising22 = false & rising23 = false & rising24 = false ->
        [max(0, (1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) - eps), min((1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * P0 - eps), min(P0 * P0 * P0 * P0 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = true & rising22 = false & rising23 = false & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = false & rising22 = true & rising23 = false & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = true & rising22 = true & rising23 = false & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = false & rising22 = false & rising23 = true & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = true & rising22 = false & rising23 = true & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = false & rising22 = true & rising23 = true & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = true & rising22 = true & rising23 = true & rising24 = false ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = false & rising22 = false & rising23 = false & rising24 = true ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = true & rising22 = false & rising23 = false & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = false & rising22 = true & rising23 = false & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = true & rising22 = true & rising23 = false & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = false & rising22 = false & rising23 = true & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = true & rising22 = false & rising23 = true & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = false & rising22 = true & rising23 = true & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell1] t1 < T & rising21 = true & rising22 = true & rising23 = true & rising24 = true ->
        [max(0, (1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) - eps), min((1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * P4 - eps), min(P4 * P4 * P4 * P4 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [sell2] t1 < T & rising21 = false & rising22 = false & rising23 = false & rising24 = false ->
        [max(0, (1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) - eps), min((1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P0 * P0 * P0 * P0 - eps), min(P0 * P0 * P0 * P0 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = true & rising22 = false & rising23 = false & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = false & rising22 = true & rising23 = false & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = true & rising22 = true & rising23 = false & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = false & rising22 = false & rising23 = true & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = true & rising22 = false & rising23 = true & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = false & rising22 = true & rising23 = true & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = true & rising22 = true & rising23 = true & rising24 = false ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = false & rising22 = false & rising23 = false & rising24 = true ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = true & rising22 = false & rising23 = false & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = false & rising22 = true & rising23 = false & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = true & rising22 = true & rising23 = false & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = false & rising22 = false & rising23 = true & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = true & rising22 = false & rising23 = true & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = false & rising22 = true & rising23 = true & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [sell2] t1 < T & rising21 = true & rising22 = true & rising23 = true & rising24 = true ->
        [max(0, (1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) - eps), min((1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = false) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = false) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = false) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false) +
        [max(0, P4 * P4 * P4 * P4 - eps), min(P4 * P4 * P4 * P4 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = false);

    [nothing] t1 < T & rising21 = false & rising22 = false & rising23 = false & rising24 = false ->
        [max(0, (1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) - eps), min((1 - P0) * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P0 * (1 - P0) * (1 - P0) * (1 - P0) - eps), min(P0 * (1 - P0) * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * (1 - P0) * (1 - P0) - eps), min(P0 * P0 * (1 - P0) * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * (1 - P0) - eps), min(P0 * P0 * P0 * (1 - P0) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P0 * P0 * P0 * P0 - eps), min(P0 * P0 * P0 * P0 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = true & rising22 = false & rising23 = false & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = false & rising22 = true & rising23 = false & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = true & rising22 = true & rising23 = false & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = false & rising22 = false & rising23 = true & rising24 = false ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = true & rising22 = false & rising23 = true & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = false & rising22 = true & rising23 = true & rising24 = false ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = true & rising22 = true & rising23 = true & rising24 = false ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = false & rising22 = false & rising23 = false & rising24 = true ->
        [max(0, (1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) - eps), min((1 - P1) * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P1 * (1 - P1) * (1 - P1) * (1 - P1) - eps), min(P1 * (1 - P1) * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * (1 - P1) * (1 - P1) - eps), min(P1 * P1 * (1 - P1) * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * (1 - P1) - eps), min(P1 * P1 * P1 * (1 - P1) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P1 * P1 * P1 * P1 - eps), min(P1 * P1 * P1 * P1 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = true & rising22 = false & rising23 = false & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = false & rising22 = true & rising23 = false & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = true & rising22 = true & rising23 = false & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = false & rising22 = false & rising23 = true & rising24 = true ->
        [max(0, (1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) - eps), min((1 - P2) * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P2 * (1 - P2) * (1 - P2) * (1 - P2) - eps), min(P2 * (1 - P2) * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * (1 - P2) * (1 - P2) - eps), min(P2 * P2 * (1 - P2) * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * (1 - P2) - eps), min(P2 * P2 * P2 * (1 - P2) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P2 * P2 * P2 * P2 - eps), min(P2 * P2 * P2 * P2 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = true & rising22 = false & rising23 = true & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = false & rising22 = true & rising23 = true & rising24 = true ->
        [max(0, (1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) - eps), min((1 - P3) * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P3 * (1 - P3) * (1 - P3) * (1 - P3) - eps), min(P3 * (1 - P3) * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * (1 - P3) * (1 - P3) - eps), min(P3 * P3 * (1 - P3) * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * (1 - P3) - eps), min(P3 * P3 * P3 * (1 - P3) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P3 * P3 * P3 * P3 - eps), min(P3 * P3 * P3 * P3 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 < T & rising21 = true & rising22 = true & rising23 = true & rising24 = true ->
        [max(0, (1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) - eps), min((1 - P4) * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = false) & (owns2' = owns2) +
        [max(0, P4 * (1 - P4) * (1 - P4) * (1 - P4) - eps), min(P4 * (1 - P4) * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = false) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * (1 - P4) * (1 - P4) - eps), min(P4 * P4 * (1 - P4) * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = true) & (rising22' = false) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * (1 - P4) - eps), min(P4 * P4 * P4 * (1 - P4) + eps, 1)] : (rising21' = false) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2) +
        [max(0, P4 * P4 * P4 * P4 - eps), min(P4 * P4 * P4 * P4 + eps, 1)] : (rising21' = true) & (rising22' = true) & (rising23' = true) & (rising24' = true) & (owns2' = owns2);

    [nothing] t1 = T -> (rising21' = rising21) & (rising22' = rising22) & (rising23' = rising23) & (rising24' = rising24) & (owns2' = owns2);

endmodule


///////////////////////////////////////////////////////////////////////////
// REWARDS
// +1 for each rising stock in an owned sector, –1 for each non‐rising stock in that owned sector.
// In a 4-stock sector: payoff_i = owns_i ? (2*(sum_j rising_i4) - 4) : 0.
// Total reward = sum_i payoff_i, only when t1 < T.

rewards

    [buy1] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0) + (rising13?1:0) + (rising14?1:0)) - 4) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0) + (rising23?1:0) + (rising24?1:0)) - 4) : 0));

    [buy2] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0) + (rising13?1:0) + (rising14?1:0)) - 4) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0) + (rising23?1:0) + (rising24?1:0)) - 4) : 0));

    [sell1] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0) + (rising13?1:0) + (rising14?1:0)) - 4) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0) + (rising23?1:0) + (rising24?1:0)) - 4) : 0));

    [sell2] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0) + (rising13?1:0) + (rising14?1:0)) - 4) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0) + (rising23?1:0) + (rising24?1:0)) - 4) : 0));

    [nothing] t1 < T : max(0, (owns1? (2*((rising11?1:0) + (rising12?1:0) + (rising13?1:0) + (rising14?1:0)) - 4) : 0) + (owns2? (2*((rising21?1:0) + (rising22?1:0) + (rising23?1:0) + (rising24?1:0)) - 4) : 0));

endrewards
